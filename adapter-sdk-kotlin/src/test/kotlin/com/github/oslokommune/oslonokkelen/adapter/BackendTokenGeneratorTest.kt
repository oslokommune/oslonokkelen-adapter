package com.github.oslokommune.oslonokkelen.adapter

import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.nimbusds.jose.jwk.JWK
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Duration
import java.time.Instant

/**
 * No real test, just here so you can experiment and see
 * what different tokens look like.
 */
class BackendTokenGeneratorTest {

    private val rightNow = Instant.now()

    private val generator = BackendTokenGenerator(
        key = JWK.parse(
            """{
                  "kty": "EC",
                  "d": "_QxboB_I6TlKxJJl35-vEwPJyBvbnTKs4Bmk_lGgg2o",
                  "use": "sig",
                  "crv": "P-256",
                  "kid": "FcxpczK3IvWqvClvdTXWvE1Pi7zaU_hi_MHgTjX-0Ok",
                  "key_ops": [
                    "sign",
                    "verify"
                  ],
                  "x": "ok7D8MXbeNd8SNra23LFL4jHK6IOMn2-3aqOHVLt-YA",
                  "y": "7_E19ml4XpO8l1VkSfRyv46nibsJ5jygSYwl-14CSVQ"
                }"""
        )
            .toECKey(),
        oslonokkelenBackendUri = URI.create("https://oslonokkelen.oslo.kommune.no"),
        tokenExpireTime = Duration.ofSeconds(30),
        timestamper = { rightNow }
    )

    @Test
    fun `Manifest token`() {
        val token = generator.createManifestScrapeToken(
            remoteUri = URI.create("https://third-party-system.com/oslonokkelen-adapter")
        )

        println(token.serialize())
    }

    @Test
    fun `Action request token`() {
        val token = generator.createActionRequestToken(
            remoteUri = URI.create("https://third-party-system.com/oslonokkelen-adapter"),
            request = AdapterActionRequest(
                requestId = "r1",
                actionId = ActionId("door", "open"),
                timeBudget = Duration.ofSeconds(2),
                timestamp = rightNow,
                attachments = listOf(
                    AdapterAttachment.NorwegianFodselsnummer("30098602247")
                )
            )
        )

        println(token.serialize())
    }


}