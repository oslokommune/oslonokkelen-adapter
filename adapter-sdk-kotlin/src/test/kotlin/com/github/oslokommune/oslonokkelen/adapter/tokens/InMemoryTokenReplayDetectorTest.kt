package com.github.oslokommune.oslonokkelen.adapter.tokens

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal class InMemoryTokenReplayDetectorTest {

    private var now = Clock.System.now()
    private val detector = InMemoryTokenReplayDetector(capacity = 3, timestamper = { now })

    @Test
    fun `Limit exceeded`() {
        detector.append("t1", now.plus(100.seconds))
        detector.append("t2", now.plus(100.seconds))
        detector.append("t3", now.plus(100.seconds))

        assertThrows<TokenValidationException.TokenReplayDetectorCapacityExceeded> {
            detector.append("t4", now.plus(100.seconds))
        }
    }

    @Test
    fun `Limit not exceeded if a token can be purged`() {
        detector.append("t1", now.plus(10.seconds))
        detector.append("t2", now.plus(20.seconds))
        detector.append("t3", now.plus(30.seconds))

        now = now.plus(11.seconds)
        detector.append("t4", now.plus(10.seconds))
    }

    @Test
    fun `Will detect replay`() {
        detector.append("t1", now.plus(10.seconds))

        assertThrows<TokenValidationException.TokenReplayDetected> {
            detector.append("t1", now.plus(10.seconds))
        }
    }

    @RepeatedTest(20)
    fun `Capacity is respected under concurrent appends`() {
        val capacity = 10
        val concurrentDetector = InMemoryTokenReplayDetector(capacity = capacity, timestamper = { now })
        val expiresAt = now.plus(100.seconds)

        val results = appendConcurrently(concurrentDetector, (1..200).map { "t$it" }, expiresAt)

        assertThat(results.filter { it == null }).hasSize(capacity)
        assertThat(results.filterNotNull()).allMatch { it is TokenValidationException.TokenReplayDetectorCapacityExceeded }
    }

    @RepeatedTest(20)
    fun `Capacity is still enforced and expired tokens purged after concurrent appends filled the detector`() {
        val capacity = 10
        val concurrentDetector = InMemoryTokenReplayDetector(capacity = capacity, timestamper = { now })

        appendConcurrently(concurrentDetector, (1..200).map { "t$it" }, now.plus(10.seconds))

        assertThrows<TokenValidationException.TokenReplayDetectorCapacityExceeded> {
            concurrentDetector.append("extra", now.plus(10.seconds))
        }

        now = now.plus(11.seconds)
        concurrentDetector.append("fresh", now.plus(10.seconds))
    }

    @RepeatedTest(20)
    fun `Only one of many concurrent appends of the same token is accepted`() {
        val concurrentDetector = InMemoryTokenReplayDetector(capacity = 1000, timestamper = { now })

        val results = appendConcurrently(concurrentDetector, List(200) { "t1" }, now.plus(100.seconds))

        assertThat(results.filter { it == null }).hasSize(1)
        assertThat(results.filterNotNull()).allMatch { it is TokenValidationException.TokenReplayDetected }
    }

    /**
     * Appends all tokens from separate threads, released at the same time.
     * Returns the exception thrown by each append, or null if it was accepted.
     */
    private fun appendConcurrently(
        detector: InMemoryTokenReplayDetector,
        tokenIds: List<String>,
        expiresAt: Instant
    ): List<TokenValidationException?> {
        val executor = Executors.newFixedThreadPool(32)
        val start = CountDownLatch(1)

        try {
            val futures = tokenIds.map { tokenId ->
                executor.submit<TokenValidationException?> {
                    start.await()
                    try {
                        detector.append(tokenId, expiresAt)
                        null
                    } catch (ex: TokenValidationException) {
                        ex
                    }
                }
            }

            start.countDown()
            return futures.map { it.get(10, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
        }
    }
}
