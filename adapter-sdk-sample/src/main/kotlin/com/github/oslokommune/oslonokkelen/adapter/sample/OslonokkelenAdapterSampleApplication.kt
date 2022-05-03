package com.github.oslokommune.oslonokkelen.adapter.sample

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.oslokommune.oslonokkelen.adapter.tokens.InMemoryTokenReplayDetector
import com.github.oslokommune.oslonokkelen.adapter.tokens.TokenVerifierFactory
import com.github.oslokommune.oslonokkelen.adapter.tokens.client.OslonokkelenJWKSourceFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.net.URI

fun main(args: Array<String>) {
    val command = RootCommand().subcommands(SampleAdapterCommand())
    command.main(args)
}

class RootCommand :CliktCommand(name = "oslonokkelen-sample-adapter") {
    override fun run() {

    }
}

class SampleAdapterCommand : CliktCommand(name = "server") {

    private val port by option("--port", help = "Server port")
        .int()
        .default(8080)

    private val backendUri by option("--oslonokkelen-backend-uri", help = "Oslonøkkelen backend uri")
        .default("https://oslonokkelen-backend-api.k8s.oslo.kommune.no")

    private val adapterUri by option("--adapter-uri", help = "Where your adapter is deployed, example: https://example.com/oslonokkelen-adapter")
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

        val tokenVerifierFactory =  TokenVerifierFactory(
            expectedAudience = URI.create(adapterUri),
            expectedIssuer = backendRootUri,
            replayDetector = InMemoryTokenReplayDetector(capacity = 1000),
            keySource = OslonokkelenJWKSourceFactory(httpClient, backendRootUri).createSource()
        )

    }
}