package com.github.oslokommune.oslonokkelen.adapter.tokens.client

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache
import com.nimbusds.jose.jwk.source.JWKSetCache
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jose.util.Resource
import com.nimbusds.jose.util.ResourceRetriever
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

class OslonokkelenJWKSourceFactory(
    private val client: HttpClient,
    private val backendRootUri: URI,
    private val keyCache: JWKSetCache = DefaultJWKSetCache(24, -1, TimeUnit.HOURS)
) : ResourceRetriever {

    private val log: Logger = LoggerFactory.getLogger(OslonokkelenJWKSourceFactory::class.java)

    fun createSource(): JWKSource<SecurityContext> {
        val jwkUrl = URL("$backendRootUri/adapter/v2/keys")
        return RemoteJWKSet(jwkUrl, this, keyCache)
    }

    override fun retrieveResource(targetUrl: URL): Resource {
        return runBlocking {
            log.info("Downloading all the keys from {}", targetUrl)
            val response = client.get(targetUrl) {
                accept(ContentType.parse(JWKSet.MIME_TYPE))
            }

            if (response.status.isSuccess()) {
                val rawJson = response.bodyAsText()
                val contentType = response.contentType().toString()

                Resource(rawJson, contentType)
            } else {
                throw IOException("Failed to download keys: ${response.status}")
            }
        }
    }
}