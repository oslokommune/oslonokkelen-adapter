package com.github.oslokommune.oslonokkelen.adapter.tokens

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

internal class InMemoryTokenReplayDetectorTest {

    private var now = Instant.now()
    private val detector = InMemoryTokenReplayDetector(capacity = 3, timestamper = { now })

    @Test
    fun `Limit exceeded`() {
        detector.append("t1", now.plusSeconds(100))
        detector.append("t2", now.plusSeconds(100))
        detector.append("t3", now.plusSeconds(100))

        assertThrows<TokenValidationException.TokenReplayDetectorCapacityExceeded> {
            detector.append("t4", now.plusSeconds(100))
        }
    }

    @Test
    fun `Limit not exceeded if a token can be purged`() {
        detector.append("t1", now.plusSeconds(10))
        detector.append("t2", now.plusSeconds(20))
        detector.append("t3", now.plusSeconds(30))

        now = now.plusSeconds(11)
        detector.append("t4", now.plusSeconds(10))
    }

    @Test
    fun `Will detect replay`() {
        detector.append("t1", now.plusSeconds(10))

        assertThrows<TokenValidationException.TokenReplayDetected> {
            detector.append("t1", now.plusSeconds(10))
        }
    }

}