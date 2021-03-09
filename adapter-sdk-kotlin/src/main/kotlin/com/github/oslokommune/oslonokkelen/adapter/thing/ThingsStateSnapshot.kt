package com.github.oslokommune.oslonokkelen.adapter.thing

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentHashMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class ThingStateSnapshot(
    val thingId: ThingId,
    val data: PersistentMap<ThingState.Key, ThingState> = persistentMapOf()
) {

    constructor(first: ThingState, vararg state: ThingState) : this(
        thingId = first.thingId,
        data = (state.toList() + first).associateBy { it.key }.toPersistentHashMap()
    )

    operator fun get(key: ThingState.Key): ThingState? {
        return data[key]
    }

    operator fun minus(key: ThingState.Key): ThingStateSnapshot? {
        val updated = data.remove(key)

        return if (updated.isEmpty()) {
            null
        } else {
            copy(data = updated)
        }
    }

    operator fun plus(state: ThingState): ThingStateSnapshot {
        if (state.thingId != thingId) {
            throw IllegalArgumentException("Tried to add state belonging to ${state.thingId} to $thingId")
        }

        return when (val previous = data[state.key]) {
            state -> {
                log.debug("Tried to add exactly same state twice")
                this
            }
            is ComparableThingState -> {
                if (!previous.hasSameStateAs(state)) {
                    log.info("{} -> {}", previous, state)
                }

                copy(data = data.put(state.key, state))
            }
            null -> {
                log.info("Adding {}", state)
                copy(data = data.put(state.key, state))
            }
            else -> {
                log.info("Replacing {} -> {}", previous, state)
                copy(data = data.put(state.key, state))
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ThingStateSnapshot::class.java)
    }
}