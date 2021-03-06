package com.github.oslokommune.oslonokkelen.adapter.tokens.generator

import com.nimbusds.jose.jwk.ECKey
import java.net.URI

fun interface TokenSigningKeySupplier {

    fun signingKeyFor(adapterRemoteUri: URI) : ECKey

}