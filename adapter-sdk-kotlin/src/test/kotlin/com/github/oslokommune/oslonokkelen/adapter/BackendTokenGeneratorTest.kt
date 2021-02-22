package com.github.oslokommune.oslonokkelen.adapter

import com.nimbusds.jose.jwk.JWK
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Duration

class BackendTokenGeneratorTest {


    @Test
    fun `Manifest token`() {

        val generator = BackendTokenGenerator(
            key = key,
            oslonokkelenBackendUri = URI.create("https://oslonokkelen.oslo.kommune.no"),
            tokenExpireTime = Duration.ofSeconds(30)
        )

        val token = generator.createManifestScrapeToken(
            remoteUri = URI.create("https://third-party-system.com/oslonokkelen-adapter")
        )

        println(token.serialize())
    }


    companion object {

        private val key =
            JWK.parse("""{"kty":"EC","d":"_QxboB_I6TlKxJJl35-vEwPJyBvbnTKs4Bmk_lGgg2o","use":"sig","crv":"P-256","kid":"FcxpczK3IvWqvClvdTXWvE1Pi7zaU_hi_MHgTjX-0Ok","key_ops":["sign","verify"],"x":"ok7D8MXbeNd8SNra23LFL4jHK6IOMn2-3aqOHVLt-YA","y":"7_E19ml4XpO8l1VkSfRyv46nibsJ5jygSYwl-14CSVQ"}""")
                .toECKey()

    }

}