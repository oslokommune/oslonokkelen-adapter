package com.github.oslokommune.oslonokkelen.adapter.tokens

import com.github.oslokommune.oslonokkelen.adapter.tokens.generator.BackendTokenGenerator
import com.github.oslokommune.oslonokkelen.adapter.tokens.generator.TokenSigningKeySupplier
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.time.Duration
import java.time.Instant

internal class TokenVerifierFactoryTest {

    private val backendUri = URI.create("https://oslonokkelen.oslo.kommune.no")
    private val adapterUri = URI.create("https://example.com/my-adapter")

    @Test
    fun `Invalid token`() {
        val factor = createVerificationFactory()
        val verifier = factor.createVerifierForScopes(requiredScope = "manifest:scrape")

        assertThrows<TokenValidationException.Parsing> {
            verifier.verify("this-is-not-a-valid-jwt")
        }
    }

    @Test
    fun `Valid token with wrong audience`() {
        val key = BackendTokenGenerator.generateKeyPair()

        val tokenGenerator = BackendTokenGenerator(
            oslonokkelenBackendUri = backendUri,
            tokenSigningKeySupplier = { key }
        )

        val factor = createVerificationFactory(keySource = ImmutableJWKSet(JWKSet(key)))
        val verifier = factor.createVerifierForScopes(requiredScope = "manifest:scrape")
        val scrapeToken = tokenGenerator.createManifestScrapeToken(URI.create("https://other-adpater.com"))

        assertThrows<TokenValidationException.Invalid> {
            verifier.verify(scrapeToken.serialize())
        }
    }

    @Test
    fun `Valid token with wrong issuer`() {
        val key = BackendTokenGenerator.generateKeyPair()

        val tokenGenerator = BackendTokenGenerator(
            oslonokkelenBackendUri = URI.create("https://not-expected-backend.com"),
            tokenSigningKeySupplier = { key }
        )

        val factor = createVerificationFactory(keySource = ImmutableJWKSet(JWKSet(key)))
        val verifier = factor.createVerifierForScopes(requiredScope = "manifest:scrape")
        val scrapeToken = tokenGenerator.createManifestScrapeToken(adapterUri)

        assertThrows<TokenValidationException.Invalid> {
            verifier.verify(scrapeToken.serialize())
        }
    }

    @Test
    fun `Expired token`() {
        val key = BackendTokenGenerator.generateKeyPair()

        val tokenGenerator = BackendTokenGenerator(
            oslonokkelenBackendUri = backendUri,
            timestamper = { Instant.now().minusSeconds(61) },
            tokenExpireTime = Duration.ofSeconds(60),
            tokenSigningKeySupplier = { key }
        )

        val factor = createVerificationFactory(keySource = ImmutableJWKSet(JWKSet(key)))
        val verifier = factor.createVerifierForScopes(requiredScope = "manifest:scrape")
        val scrapeToken = tokenGenerator.createManifestScrapeToken(adapterUri)

        assertThrows<TokenValidationException.Invalid> {
            verifier.verify(scrapeToken.serialize())
        }
    }

    @Test
    fun `Token without required token id`() {
        val key = BackendTokenGenerator.generateKeyPair()

        val tokenGenerator = BackendTokenGenerator(
            oslonokkelenBackendUri = backendUri,
            tokenSigningKeySupplier = { key }
        )

        val factor = createVerificationFactory(keySource = ImmutableJWKSet(JWKSet(key)))
        val verifier = factor.createVerifierForScopes(requiredScope = "manifest:scrape")

        val scrapeToken = tokenGenerator.buildToken {
            audience(adapterUri.toString())
            issuer(backendUri.toString())
            claim("scope", listOf("manifest:scrape"))
            // Missing token id
        }

        assertThrows<TokenValidationException.Invalid> {
            verifier.verify(scrapeToken.serialize())
        }
    }

    @Test
    fun `Invalid key`() {
        val fakeKey = BackendTokenGenerator.generateKeyPair(keyId = "kid")
        val realKey = BackendTokenGenerator.generateKeyPair(keyId = "kid")

        val tokenGenerator = BackendTokenGenerator(
            oslonokkelenBackendUri = backendUri,
            tokenSigningKeySupplier = { fakeKey }
        )

        val factor = createVerificationFactory(keySource = ImmutableJWKSet(JWKSet(realKey)))
        val verifier = factor.createVerifierForScopes(requiredScope = "manifest:scrape")
        val scrapeToken = tokenGenerator.createManifestScrapeToken(adapterUri)

        assertThrows<TokenValidationException.Invalid> {
            verifier.verify(scrapeToken.serialize())
        }
    }

    @Test
    fun `Signed with unknown key`() {
        val key = BackendTokenGenerator.generateKeyPair()

        val tokenGenerator = BackendTokenGenerator(
            oslonokkelenBackendUri = backendUri,
            tokenSigningKeySupplier = { key }
        )

        val factor = createVerificationFactory(keySource = ImmutableJWKSet(JWKSet())) // Empty key set
        val verifier = factor.createVerifierForScopes(requiredScope = "manifest:scrape")
        val scrapeToken = tokenGenerator.createManifestScrapeToken(adapterUri)

        assertThrows<TokenValidationException.Invalid> {
            verifier.verify(scrapeToken.serialize())
        }
    }

    @Test
    fun `Token replay`() {
        val key = BackendTokenGenerator.generateKeyPair()

        val tokenGenerator = BackendTokenGenerator(
            oslonokkelenBackendUri = backendUri,
            tokenSigningKeySupplier = { key }
        )

        val factor = createVerificationFactory(keySource = ImmutableJWKSet(JWKSet(key)))
        val verifier = factor.createVerifierForScopes(requiredScope = "manifest:scrape")
        val scrapeToken = tokenGenerator.createManifestScrapeToken(adapterUri)

        // First time is fine
        verifier.verify(scrapeToken.serialize())

        assertThrows<TokenValidationException.TokenReplayDetected> {
            verifier.verify(scrapeToken.serialize())
        }
    }


    private fun createVerificationFactory(
        expectedIssuer: URI = backendUri,
        expectedAudience: URI = adapterUri,
        keySource: JWKSource<SecurityContext> = ImmutableJWKSet(JWKSet()),
        replayDetector: TokenReplayDetector = InMemoryTokenReplayDetector(
            capacity = 3,
            timestamper = { Instant.now() })
    ): TokenVerifierFactory {
        return TokenVerifierFactory(
            expectedIssuer = expectedIssuer,
            expectedAudience = expectedAudience,
            replayDetector = replayDetector,
            keySource = keySource
        )
    }

}