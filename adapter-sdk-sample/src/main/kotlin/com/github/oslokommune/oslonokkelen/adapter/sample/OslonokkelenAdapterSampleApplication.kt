package com.github.oslokommune.oslonokkelen.adapter.sample

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.protobuf.ProtobufParser
import com.github.oslokommune.oslonokkelen.adapter.protobuf.ProtobufSerializer
import com.github.oslokommune.oslonokkelen.adapter.tokens.InMemoryTokenReplayDetector
import com.github.oslokommune.oslonokkelen.adapter.tokens.TokenVerifierFactory
import com.github.oslokommune.oslonokkelen.adapter.tokens.client.OslonokkelenJWKSourceFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.net.URI
import java.time.Duration

fun main(args: Array<String>) {
    val command = RootCommand().subcommands(SampleAdapterCommand())
    command.main(args)
}

class RootCommand : CliktCommand(name = "oslonokkelen-sample-adapter") {
    override fun run() {

    }
}

class SampleAdapterCommand : CliktCommand(name = "server") {

    private val port by option("--port", help = "Server port")
        .int()
        .default(8080)

    private val backendUri by option("--oslonokkelen-backend-uri", help = "Oslonøkkelen backend uri")
        .default("https://oslonokkelen-backend-api.k8s.oslo.kommune.no")

    private val adapterUri by option(
        "--adapter-uri",
        help = "Where your adapter is deployed, example: https://example.com/oslonokkelen-adapter"
    )
        .default("https://oslonokkelen-backend-api.k8s.oslo.kommune.no")

    override fun run() {
        echo("Starting adapter on port $port")
        val backendRootUri = URI.create(backendUri)

        val httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
            expectSuccess = false
        }

        val jwkSourceFactory = OslonokkelenJWKSourceFactory(
            client = httpClient,
            backendRootUri = backendRootUri
        )

        val replayDetector = InMemoryTokenReplayDetector(
            capacity = 1000
        )

        val tokenVerifierFactory = TokenVerifierFactory(
            expectedAudience = URI.create(adapterUri),
            keySource = jwkSourceFactory.createSource(),
            expectedIssuer = backendRootUri,
            replayDetector = replayDetector
        )

        val server = embeddedServer(
            io.ktor.server.cio.CIO,
            applicationEngineEnvironment {

                connector {
                    port = port
                }

                module {
                    install(CORS) {
                        anyHost()
                    }
                    install(Compression)
                    install(DefaultHeaders)
                    install(CallLogging) {
                        level = Level.INFO
                    }

                    val manifestTokenVerifier = tokenVerifierFactory.createVerifierForScopes("manifest:scrape")
                    val executeTokenVerifier = tokenVerifierFactory.createVerifierForScopes("action:execute")

                    routing {
                        get("/") {
                            call.respondText("Oslonøkkelen sample adapter")
                        }
                        get("/api/oslonokkelen/manifest") {
                            try {
                                withVerifier(manifestTokenVerifier) {
                                    val manifest = manifestController.buildProtobuf()
                                    val contentType = ContentType.parse("application/protobuf;type=manifest")

                                    log.trace("Responding with manifest: {}", manifest)
                                    call.respondBytes(manifest.toByteArray(), contentType)
                                }
                            } catch (ex: Throwable) {
                                log.error("Failed to handle manifest request", ex)
                                call.respondText("Error", status = HttpStatusCode.InternalServerError)
                            }
                        }
                        post("/api/oslonokkelen/execute") {
                            val timestamp = timestamper()
                            try {
                                withVerifier(executeTokenVerifier) { claims ->
                                    val contentType = ContentType.parse("application/protobuf;type=action-response")
                                    val request = ProtobufParser.parseActionRequestFromClaims(claims)

                                    CitykeyTracer.suspendedSpan("action.execute") { span ->
                                        span.setTag("action-id", "${request.thingId}/${request.actionId}")
                                        span.setTag("request-id", request.requestId)
                                        span.setTag("time-budget-ms", request.timeBudgetMillis)

                                        this@InHouseAdapterServer.log.info(
                                            "Executing: {}/{}",
                                            request.thingId,
                                            request.actionId
                                        )

                                        val adapterRequest = ProtobufParser.parse(request)
                                        val response = this@InHouseAdapterServer.manifestController.execute(adapterRequest)

                                        span.setTag("status", response.status.name)
                                        span.setTag(HelpFormatter.Tags.ERROR, response.isError)

                                        response.with<AdapterAttachment.ErrorDescription> { description ->
                                            span.setTag("debug-message", description.debugMessage)
                                            span.setTag("error-code", description.code)
                                        }
                                        response.with<AdapterAttachment.DeniedReason> { reason ->
                                            span.setTag("debug-message", reason.debugMessage)
                                            span.setTag("error-code", reason.code)
                                        }

                                        val duration = Duration.between(timestamp, timestamper())
                                        this@InHouseAdapterServer.log.info(
                                            "Request: {}, action {}/{} executed in {}ms with status: {}",
                                            request.requestId,
                                            request.thingId,
                                            request.actionId,
                                            duration.toMillis(),
                                            response.status.name
                                        )

                                        val proto = ProtobufSerializer.serialize(response)
                                        this.call.respondBytes(proto.toByteArray(), contentType)
                                    }
                                }
                            } catch (ex: Throwable) {
                                this@InHouseAdapterServer.log.error("Failed to handle manifest request", ex)
                                call.respondText("Error", status = HttpStatusCode.InternalServerError)
                            }
                        }


                    }
                }
            }
        )


    }
}