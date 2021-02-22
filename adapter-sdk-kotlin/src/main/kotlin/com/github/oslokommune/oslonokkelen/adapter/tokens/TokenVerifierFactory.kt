package com.github.oslokommune.oslonokkelen.adapter.tokens

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.BadJOSEException
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import java.net.URI
import java.text.ParseException

/**
 * @param expectedAudience Our adapters public uri
 * @param expectedIssuer Oslonøkkelen backend uri
 * @param replayDetector Responsible for detecting token re-use
 * @param keySource Client responsible for fetching public keys
 */
class TokenVerifierFactory(
    private val expectedIssuer: URI,
    private val expectedAudience: URI,
    private val replayDetector: TokenReplayDetector,
    private val keySource: JWKSource<SecurityContext>
) {


    fun createVerifierForScopes(requiredScope: String): TokenVerifier {
        val requiredClaims = setOf("iat", "exp", "jti")

        val exactMatchClaims = JWTClaimsSet.Builder()
            .claim("scope", listOf(requiredScope))
            .issuer("${expectedIssuer.scheme}://${expectedIssuer.host}")
            .audience("${expectedAudience.scheme}://${expectedAudience.host}")
            .build()

        val processor = DefaultJWTProcessor<SecurityContext>()
        val claimVerifier = DefaultJWTClaimsVerifier<SecurityContext>(exactMatchClaims, requiredClaims)
        claimVerifier.maxClockSkew = 0

        processor.jweTypeVerifier = DefaultJOSEObjectTypeVerifier(JOSEObjectType("at+jwt"))
        processor.jwsKeySelector = JWSVerificationKeySelector(JWSAlgorithm.ES256, keySource)
        processor.jwtClaimsSetVerifier = claimVerifier

        return TokenVerifier { rawToken ->
            try {
                val parsedToken = SignedJWT.parse(rawToken)
                val validatedClaims = processor.process(parsedToken, null)

                replayDetector.append(
                    tokenId = validatedClaims.jwtid,
                    expiresAt = validatedClaims.expirationTime.toInstant()
                )

                validatedClaims
            } catch (ex: TokenValidationException) {
                throw ex
            } catch (ex: BadJOSEException) {
                throw TokenValidationException.Invalid(ex)
            } catch (ex: ParseException) {
                throw TokenValidationException.Parsing(ex)
            } catch (ex: Exception) {
                throw TokenValidationException.Unknown(ex)
            }
        }
    }

}

