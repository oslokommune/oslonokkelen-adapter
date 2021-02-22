package com.github.oslokommune.oslonokkelen.adapter.tokens

import com.nimbusds.jwt.JWTClaimsSet

fun interface TokenVerifier {

    fun verify(rawToken: String) : JWTClaimsSet

}

