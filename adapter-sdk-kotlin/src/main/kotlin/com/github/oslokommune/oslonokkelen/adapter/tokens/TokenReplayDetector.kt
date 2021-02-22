package com.github.oslokommune.oslonokkelen.adapter.tokens

import java.time.Instant

/**
 * Used to prevent someone using re-using a valid token.
 */
fun interface TokenReplayDetector {

    /**
     * Make sure you validate the signature and check the
     * expire date before invoking this.
     */
    fun append(tokenId: String, expiresAt: Instant)

}

