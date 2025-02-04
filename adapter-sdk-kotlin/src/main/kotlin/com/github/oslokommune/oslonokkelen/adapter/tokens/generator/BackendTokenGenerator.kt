package com.github.oslokommune.oslonokkelen.adapter.tokens.generator

import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID

/**
 * This class can be used to simulate tokens / requests generated by Oslonøkkelen.
 * Useful for testing your adapter implementation.
 */
class BackendTokenGenerator(
    private val tokenSigningKeySupplier: TokenSigningKeySupplier,
    private val oslonokkelenBackendUri: URI,
    private val tokenExpireTime: Duration = Duration.ofSeconds(60),
    private val timestamper: () -> Instant = { Instant.now() },
    private val jwtIdGenerator: () -> String = { UUID.randomUUID().toString() }
) {


    fun createManifestScrapeToken(remoteUri: URI): SignedJWT {
        return buildToken {
            audience("${remoteUri.scheme}://${remoteUri.host}")
            issuer("${oslonokkelenBackendUri.scheme}://${oslonokkelenBackendUri.host}")
            jwtID(jwtIdGenerator())
            claim("scope", listOf("manifest:scrape"))
        }
    }

    fun createActionRequestToken(remoteUri: URI, request: AdapterActionRequest): SignedJWT {
        val requestClaim = mapOf(
            "thingId" to request.actionId.thingId.value,
            "actionId" to request.actionId.value,
            "timeBudgetMillis" to request.timeBudget.toMillis(),
            "requestId" to request.requestId,
            "attachments" to request.attachments.mapNotNull { attachment ->
                when (attachment) {
                    is AdapterAttachment.NorwegianFodselsnummer -> {
                        mapOf(
                            "norwegianFodselsnummer" to mapOf(
                                "number" to attachment.number
                            )
                        )
                    }

                    // These are response attachments
                    is AdapterAttachment.Code,
                    is AdapterAttachment.DeniedReason,
                    is AdapterAttachment.EndUserMessage,
                    is AdapterAttachment.ErrorDescription,
                    is AdapterAttachment.ErrorCategory -> null
                }
            },
            "parameters" to request.parameters
        )

        return buildToken {
            audience("${remoteUri.scheme}://${remoteUri.host}")
            issuer("${oslonokkelenBackendUri.scheme}://${oslonokkelenBackendUri.host}")
            jwtID(jwtIdGenerator())
            claim("scope", listOf("action:execute"))
            claim("request", requestClaim)
        }
    }

    fun buildToken(builderBlock: JWTClaimsSet.Builder.() -> Unit): SignedJWT {
        val claims = createTokenClaims(builderBlock)
        val signingKey = tokenSigningKeySupplier.signingKeyFor()
        val header = createTokenHeader(signingKey)

        return signToken(header, claims, signingKey)
    }

    private fun createTokenClaims(claimsBuilderBlock: JWTClaimsSet.Builder.() -> Unit): JWTClaimsSet {
        val currentTimestamp = timestamper()
        val claimsBuilder = JWTClaimsSet.Builder()
        claimsBuilder.expirationTime(Date.from(currentTimestamp.plus(tokenExpireTime)))
        claimsBuilder.issueTime(Date.from(currentTimestamp))
        claimsBuilderBlock(claimsBuilder)

        return claimsBuilder.build()
    }

    private fun createTokenHeader(signingKey: ECKey): JWSHeader {
        val headerBuilder = JWSHeader.Builder(JWSAlgorithm.ES256)
        val publicKey = signingKey.toPublicJWK()
        headerBuilder.keyID(publicKey.keyID)
        return headerBuilder.build()
    }

    private fun signToken(header: JWSHeader?, claims: JWTClaimsSet, signingKey: ECKey): SignedJWT {
        val signedJWT = SignedJWT(header, claims)
        signedJWT.sign(ECDSASigner(signingKey))
        return signedJWT
    }


    companion object {

        private val generator = ECKeyGenerator(Curve.P_256)
            .keyOperations(setOf(KeyOperation.SIGN, KeyOperation.VERIFY))
            .keyUse(KeyUse.SIGNATURE)

        fun generateKeyPair(keyId: String? = null): ECKey {
            return if (keyId != null) {
                generator.keyID(keyId).generate()
            } else {
                generator.keyIDFromThumbprint(true).generate()
            }
        }

    }

}