package com.github.oslokommune.oslonokkelen.adapter.manifest

import com.github.oslokommune.oslonokkelen.adapter.action.ActionDescription
import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCode
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodeDescription
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodes
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingDescription
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingId
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingState
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingStateSnapshot
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class ManifestSnapshot(
    val version: Long = 1,
    val things: PersistentMap<ThingId, ThingDescription> = persistentMapOf(),
    val actions: PersistentMap<ThingId, PersistentMap<ActionId, ActionDescription>> = persistentMapOf(),
    val thingStates: PersistentMap<ThingId, ThingStateSnapshot> = persistentMapOf(),
    val errorCodes: ErrorCodes = ErrorCodes()
) {

    operator fun plus(description: ErrorCodeDescription): ManifestSnapshot {
        val updatedErrorCodes = errorCodes + description

        return if (updatedErrorCodes == errorCodes) {
            this
        } else {
            copy(
                version = version + 1,
                errorCodes = updatedErrorCodes
            )
        }
    }

    operator fun minus(code: ErrorCode): ManifestSnapshot {
        val updatedCodes = errorCodes - code

        return if (errorCodes == updatedCodes) {
            this
        } else {
            copy(
                version = version + 1,
                errorCodes = errorCodes - code
            )
        }
    }

    operator fun plus(description: ThingDescription): ManifestSnapshot {
        return if (things[description.id] == description) {
            this
        } else {
            copy(
                version = version + 1,
                things = things.put(description.id, description)
            )
        }
    }

    operator fun minus(id: ThingId): ManifestSnapshot {
        return if (!things.containsKey(id)) {
            this
        } else {
            log.info("Removed {} from manifest", id)

            copy(
                version = version + 1,
                things = things.remove(id),
                thingStates = thingStates.remove(id),
                actions = actions.remove(id)
            )
        }
    }

    inline fun <reified S : ThingState> stateOfTypeOrNull(thingId: ThingId): S? {
        return thingStates[thingId]?.data?.values?.filterIsInstance<S>()?.firstOrNull()
    }

    operator fun plus(description: ActionDescription): ManifestSnapshot {
        if (!things.containsKey(description.id.thingId)) {
            throw InvalidManifestException("Can't add action '${description.id}' to a manifest that doesn't have a description of thing '${description.id.thingId}'")
        }

        return if (actions[description.id.thingId]?.get(description.id) == description) {
            this
        } else {
            val thingActions = actions[description.id.thingId] ?: persistentMapOf()
            val updatedThingActions = thingActions.put(description.id, description)

            copy(
                version = version + 1,
                actions = actions.put(description.id.thingId, updatedThingActions)
            )
        }
    }

    operator fun minus(actionId: ActionId): ManifestSnapshot {
        val thingId = actionId.thingId
        val thingActions = actions[thingId]

        return if (thingActions?.containsKey(actionId) == true) {
            val updatedThingActions = thingActions.remove(actionId)
            val stateToRemove = thingStates[thingId]
                ?.data
                ?.values
                ?.filterIsInstance<ThingState.RelatedToAction>()
                ?.filter { it.actionId == actionId }
                ?: emptyList()

            var tmp = this

            for (s: ThingState.RelatedToAction in stateToRemove) {
                tmp -= s.key
            }

            if (updatedThingActions.isEmpty()) {
                tmp.copy(
                    version = version + 1,
                    actions = actions.remove(thingId)
                )
            } else {
                tmp.copy(
                    version = version + 1,
                    actions = actions.put(thingId, updatedThingActions)
                )
            }
        } else {
            this
        }
    }

    operator fun plus(newState: ThingState): ManifestSnapshot {
        if (newState is ThingState.RelatedToAction) {
            if (!actions.containsKey(newState.actionId.thingId)) {
                throw InvalidManifestException("Tried to add state related to unknown ${newState.actionId}")
            }
        }
        if (!things.containsKey(newState.thingId)) {
            throw InvalidManifestException("Can't add state for unknown thing '${newState.thingId}' to manifest")
        }

        return if (thingStates[newState.thingId]?.data?.get(newState.key) == newState) {
            log.debug("State already up to date: {}", newState)
            this
        } else {
            val thingState = thingStates[newState.thingId] ?: ThingStateSnapshot(newState.thingId)
            val updatedThingState = thingState + newState

            copy(
                version = version + 1,
                thingStates = thingStates.put(newState.thingId, updatedThingState)
            )
        }
    }

    operator fun minus(stateKey: ThingState.Key): ManifestSnapshot {
        val thingId = stateKey.thingId
        val currentStateForThing = thingStates[thingId]

        return if (currentStateForThing == null) {
            this
        } else {
            val currentState = currentStateForThing[stateKey]

            if (currentState == null) {
                this
            } else {
                val updatedStateForThing = currentStateForThing - (stateKey)

                if (updatedStateForThing != null) {
                    copy(
                        version = version + 1,
                        thingStates = thingStates.put(thingId, updatedStateForThing)
                    )
                } else {
                    copy(
                        version = version + 1,
                        thingStates = thingStates.remove(thingId)
                    )
                }
            }
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ManifestSnapshot::class.java)
    }
}
