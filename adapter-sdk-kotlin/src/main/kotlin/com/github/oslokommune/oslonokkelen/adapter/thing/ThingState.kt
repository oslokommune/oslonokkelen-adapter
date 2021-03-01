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
    ) : ThingState(), RelatedToAction {

        override val thingId = actionId.thingId
        override val key = Key("action.health.${actionId.thingId.value}.${actionId.value}")
    }

    data class Lock(
        override val timestamp: Instant,
        override val thingId: ThingId,
        val locked: Boolean
    ) : ThingState() {

        override val key = Key("thing.${thingId.value}.locked")
    }

    /**
     * Some "things" works as proxies in front of other systems.
     * This can be used to indicate the health of the connection between
     * your adapter and this other system.
     */
    data class RemoteSystemConnection(
        override val thingId: ThingId,
        override val timestamp: Instant,
        val debugMessage: String,
        val state: ConnectionState
    ) : ThingState() {

        override val key = Key("thing.${thingId.value}.remote")

        val connected: Boolean
            get() = when (state) {
                is ConnectionState.Connected -> true
                is ConnectionState.Disconnected -> false
            }

        sealed class ConnectionState {
            data class Connected(val connectedAt: Instant) : ConnectionState() {
                override fun toString(): String {
                    return "Connected at $connectedAt"
                }
            }

            data class Disconnected(val disconnectedAt: Instant, val reason: String) : ConnectionState() {
                override fun toString(): String {
                    return "Disconnected with reason: $reason"
                }
            }
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

        override val key: Key = Key("thing.${thingId.value}.debug-log")

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

    data class Key(val value: String)

    interface RelatedToAction {
        val actionId: ActionId
    }
}
