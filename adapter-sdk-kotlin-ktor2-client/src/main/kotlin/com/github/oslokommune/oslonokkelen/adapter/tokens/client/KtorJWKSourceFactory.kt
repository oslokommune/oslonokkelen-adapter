package com.github.oslokommune.oslonokkelen.adapter.tokens.client

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.JWKSetCacheRefreshEvaluator
import com.nimbusds.jose.jwk.source.JWKSetSource
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.SecurityContext
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI

object KtorJWKSourceFactory {

    private val log: Logger = LoggerFactory.getLogger(KtorJWKSourceFactory::class.java)

    fun build(
        httpClient: HttpClient,
        backendUri: URI,
        cache: Boolean = true,
        retrying: Boolean = false,
        rateLimited: Boolean = false
    ): JWKSource<SecurityContext> {
        val url = "$backendUri/adapter/v2/keys"

        return JWKSourceBuilder.create(object : JWKSetSource<SecurityContext> {
            override fun getJWKSet(
                refreshEvaluator: JWKSetCacheRefreshEvaluator?,
                currentTime: Long,
                context: SecurityContext?
            ): JWKSet {
                return runBlocking {
                    log.info("Downloading all the keys from {}", url)
                    val response = httpClient.get(url) {
                        accept(ContentType.parse(JWKSet.MIME_TYPE))
                    }

                    if (response.status.isSuccess()) {
                        val rawJson = response.bodyAsText()
                        JWKSet.parse(rawJson)
                    } else {
                        throw IOException("Failed to download keys: ${response.status}")
                    }
                }
            }

            override fun close() {
            }
        })
            .cache(cache)
            .retrying(retrying)
            .rateLimited(rateLimited)
            .build()
    }

}