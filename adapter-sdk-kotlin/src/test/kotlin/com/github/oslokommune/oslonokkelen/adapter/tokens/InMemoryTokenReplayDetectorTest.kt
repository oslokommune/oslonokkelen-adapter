package com.github.oslokommune.oslonokkelen.adapter.tokens

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

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

}