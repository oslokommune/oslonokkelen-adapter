package com.github.oslokommune.oslonokkelen.adapter.tokens.generator

import com.nimbusds.jose.jwk.ECKey

fun interface TokenSigningKeySupplier {

    fun signingKeyFor(): ECKey
}
