package com.github.oslokommune.oslonokkelen.adapter.tokens

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * This implementation can be used only if you run a single
 * instance of your adapter. Adapters with more then one running
 * instance will need a distributed implementation of this.
 *
 * You will have to tune the capacity according to the expected
 * traffic. If your tokens are valid in 60 seconds and you expect
 * one request every 10th second your capacity should be (at least) 6.
 */
class InMemoryTokenReplayDetector(
    private val capacity: Int = 1000,
    private val timestamper: () -> Instant = { Instant.now() }
) : TokenReplayDetector {

    private val entries = ConcurrentHashMap<String, Instant>(capacity)

    override fun append(tokenId: String, expiresAt: Instant) {
        if (entries.size == capacity) {
            log.info("Capacity reached, will purge expired tokens")
            purgeExpiredTokens()

            if (entries.size == capacity) {
                throw TokenValidationException.TokenReplayDetectorCapacityExceeded(entries.size)
            }
        }

        val previousValue = entries.put(tokenId, expiresAt)

        if (previousValue != null) {
            throw TokenValidationException.TokenReplayDetected(tokenId)
        }
    }

    /**
     * When a token has expired it can no longer be used anyway
     * so it is safe to remove it from our list as re-using an
     * expired token is detected.
     */
    @Synchronized
    private fun purgeExpiredTokens() {
        val iterator = entries.iterator()
        val now = timestamper()
        var removed = 0

        while (iterator.hasNext()) {
            val (_, expiresAt) = iterator.next()

            if (expiresAt < now) {
                iterator.remove()
                removed++
            }
        }

        log.info("Purged {} expired tokens", removed)
    }


    companion object {
        private val log : Logger = LoggerFactory.getLogger(InMemoryTokenReplayDetector::class.java)
    }

}
