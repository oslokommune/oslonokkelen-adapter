package com.github.oslokommune.oslonokkelen.adapter.thing

import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import java.time.Instant

sealed class ThingState {

    abstract val thingId: ThingId
    abstract val timestamp: Instant
    abstract val key: Key

    data class ActionHealth(
        override val timestamp: Instant,
        override val actionId: ActionId,
        val healthy: Boolean,
        val debugMessage: String
    ) : ThingState(), RelatedToAction, ComparableThingState {

        override val thingId = actionId.thingId
        override val key = Key(thingId, "action.health.${actionId.thingId.value}.${actionId.value}")

        override fun hasSameStateAs(other: ThingState): Boolean {
            return other is ActionHealth && healthy == other.healthy
        }

        override fun toString(): String {
            return "${actionId.short}, healthy: $healthy, message: $debugMessage"
        }
    }

    data class Lock(
        override val timestamp: Instant,
        override val thingId: ThingId,
        val locked: Boolean
    ) : ThingState(), ComparableThingState {

        override val key = Key(thingId, "thing.${thingId.value}.locked")

        override fun hasSameStateAs(other: ThingState): Boolean {
            return other is Lock && locked == other.locked
        }

        override fun toString(): String {
            return "${thingId.value} locked: $locked"
        }

    }

    data class OpenPosition(
        override val timestamp: Instant,
        override val thingId: ThingId,
        val open: Boolean
    ) : ThingState(), ComparableThingState {
        override val key = Key(thingId, "thing.${thingId.value}.open")

        override fun hasSameStateAs(other: ThingState): Boolean {
            return other is OpenPosition && open == other.open
        }

        override fun toString(): String {
            return "${thingId.value} open: $open"
        }
    }

    data class DebugLog(
        override val thingId: ThingId,
        val maxLength: Int = DEFAULT_MAX_LENGTH,
        val lines: PersistentList<Line> = persistentListOf()
    ) : ThingState() {

        operator fun plus(line: Line): DebugLog {
            var updatedLines = lines.add(line)

            if (updatedLines.size > maxLength) {
                updatedLines = updatedLines.removeAt(0)
            }

            return copy(lines = updatedLines)
        }

        override val key: Key = Key(thingId, "thing.${thingId.value}.debug-log")

        override val timestamp: Instant
            get() = lines.last().timestamp

        data class Line(
            val timestamp: Instant,
            val message: String,
            val level: Adapter.ThingState.DebugLog.Level = Adapter.ThingState.DebugLog.Level.INFO
        ) : Comparable<Line> {
            override fun compareTo(other: Line): Int {
                return timestamp.compareTo(other.timestamp)
            }
        }

        companion object {

            const val DEFAULT_MAX_LENGTH = 10

        }

    }

    data class Key(
        val thingId: ThingId,
        val value: String
    )

    interface RelatedToAction {
        val actionId: ActionId
        val key: Key
    }
}
