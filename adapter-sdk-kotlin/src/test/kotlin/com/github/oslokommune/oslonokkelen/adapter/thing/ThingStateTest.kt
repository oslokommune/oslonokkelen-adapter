package com.github.oslokommune.oslonokkelen.adapter.thing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

internal class ThingStateTest {


    @Nested
    inner class ThingLog {

        private val now = Instant.now()
        private val thingId = ThingId("front-door")
        private val emptyLog = ThingState.DebugLog(thingId, maxLength = 3)

        @Test
        fun `The first lines are dropped when capacity is exceeded`() {
            val log1 = emptyLog + ThingState.DebugLog.Line(timestamp = now, message = "Line 1")
            val log2 = log1 + ThingState.DebugLog.Line(timestamp = now, message = "Line 2")
            val log3 = log2 + ThingState.DebugLog.Line(timestamp = now, message = "Line 3")
            val log4 = log3 + ThingState.DebugLog.Line(timestamp = now, message = "Line 4")
            val log5 = log4 + ThingState.DebugLog.Line(timestamp = now, message = "Line 5")

            assertThat(log1.lines.map { it.message }).containsExactly("Line 1")
            assertThat(log2.lines.map { it.message }).containsExactly("Line 1", "Line 2")
            assertThat(log3.lines.map { it.message }).containsExactly("Line 1", "Line 2", "Line 3")
            assertThat(log4.lines.map { it.message }).containsExactly("Line 2", "Line 3", "Line 4")
            assertThat(log5.lines.map { it.message }).containsExactly("Line 3", "Line 4", "Line 5")
        }

    }


}