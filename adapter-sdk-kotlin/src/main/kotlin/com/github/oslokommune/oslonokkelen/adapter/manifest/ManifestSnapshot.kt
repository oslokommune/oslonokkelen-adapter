package com.github.oslokommune.oslonokkelen.adapter.manifest

import com.github.oslokommune.oslonokkelen.adapter.action.ActionDescription
import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCode
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodeDescription
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodes
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingDescription
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingId
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingState
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class ManifestSnapshot(
    val version: Long = 1,
    val things: PersistentMap<ThingId, ThingDescription> = persistentMapOf(),
    val actions: PersistentMap<ThingId, PersistentMap<ActionId, ActionDescription>> = persistentMapOf(),
    val thingStates: PersistentMap<ThingId, PersistentMap<ThingState.Key, ThingState>> = persistentMapOf(),
    val errorCodes : ErrorCodes = ErrorCodes()
) {

    operator fun plus(description: ErrorCodeDescription) : ManifestSnapshot {
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

    operator fun minus(code: ErrorCode) : ManifestSnapshot {
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
        return thingStates[thingId]?.values?.filterIsInstance<S>()?.firstOrNull()
    }

    fun withAction(description: ActionDescription): ManifestSnapshot {
        if (!things.containsKey(description.id.thingId)) {
            throw InvalidManifestException("Can't add action ${description.id} to a manifest without ${description.id.thingId}")
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

    fun withThingState(newState: ThingState): ManifestSnapshot {
        if (newState is ThingState.RelatedToAction) {
            if (!actions.containsKey(newState.actionId.thingId)) {
                throw InvalidManifestException("Tried to add state related to unknown ${newState.actionId}")
            }
        }

        return if (thingStates[newState.thingId]?.get(newState.key) == newState) {
            log.debug("State already up to date: {}", newState)
            this
        } else {
            val state = thingStates[newState.thingId] ?: persistentMapOf()
            val updatedThingState = state.put(newState.key, newState)

            copy(
                version = version + 1,
                thingStates = thingStates.put(newState.thingId, updatedThingState)
            )
        }
    }


    companion object {
        private val log: Logger = LoggerFactory.getLogger(ManifestSnapshot::class.java)
    }
}
