package com.github.oslokommune.oslonokkelen.adapter.tokens.generator

import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.protobuf.ProtobufSerializer
import com.google.gson.JsonParser
import com.google.protobuf.util.JsonFormat
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
        val parsedRequest = serializeRequestAsJson(request)

        return buildToken {
            audience("${remoteUri.scheme}://${remoteUri.host}")
            issuer("${oslonokkelenBackendUri.scheme}://${oslonokkelenBackendUri.host}")
            jwtID(jwtIdGenerator())
            claim("scope", listOf("action:execute"))
            claim("request", parsedRequest)
        }
    }

    /**
     * This is a bit of a hack..
     *
     * We use protobuf to describe messages and it is possible to serialize the
     * Java classes generated from the .proto files to json, BUT the jwt library
     * can't work with these classes so we have to serialize the request to json
     * and then back to classes Nimbus JWT can work with in order to embed the
     * request in the token.
     */
    private fun serializeRequestAsJson(request: AdapterActionRequest): Any? {
        val protobufRequest = ProtobufSerializer.serialize(request)
        val jsonRequest = JsonFormat.printer().print(protobufRequest)
        return JsonParser.parseString(jsonRequest)
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