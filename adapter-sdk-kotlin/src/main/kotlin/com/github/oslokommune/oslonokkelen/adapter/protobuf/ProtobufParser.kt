package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.action.ActionDescription
import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.action.ActionResponseMessage
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodeDescription
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodes
import com.github.oslokommune.oslonokkelen.adapter.manifest.ManifestSnapshot
import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingDescription
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingId
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingState
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingStateSnapshot
import com.google.protobuf.util.JsonFormat
import com.nimbusds.jose.util.JSONObjectUtils
import com.nimbusds.jwt.JWTClaimsSet
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentHashMap
import kotlinx.collections.immutable.toPersistentList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

object ProtobufParser {

    private val log: Logger = LoggerFactory.getLogger(ProtobufParser::class.java)

    @Suppress("UNCHECKED_CAST")
    fun parseActionRequestFromClaims(verifiedClaims: JWTClaimsSet) : Adapter.ActionRequest {
        val request = verifiedClaims.getClaim("request") ?: throw IllegalStateException("No 'request' in token")
        val json = JSONObjectUtils.toJSONString(request as MutableMap<String, *>?)

        val jsonParser = JsonFormat.parser()
        val requestBuilder = Adapter.ActionRequest.newBuilder()
        jsonParser.merge(json, requestBuilder)
        return requestBuilder.build()
    }

    fun parse(serializedRequest: Adapter.ActionRequest): AdapterActionRequest {
        return AdapterActionRequest(
            requestId = serializedRequest.requestId,
            actionId = ActionId(serializedRequest.thingId, serializedRequest.actionId),
            timeBudget = Duration.ofMillis(serializedRequest.timeBudgetMillis.toLong()),
            attachments = serializedRequest.attachmentsList.map { attachment ->
                parseAttachment(attachment)
            }
        )
    }

    fun parse(serializedResponse: Adapter.ActionResponse): ActionResponseMessage {
        return ActionResponseMessage(serializedResponse.attachmentsList.map { attachment ->
            parseAttachment(attachment)
        })
    }

    private fun parseAttachment(attachment: Adapter.Attachment): AdapterAttachment {
        return when (attachment.valueCase) {
            Adapter.Attachment.ValueCase.NORWEGIAN_FODSELSNUMMER -> {
                AdapterAttachment.NorwegianFodselsnummer(attachment.norwegianFodselsnummer.number)
            }

            Adapter.Attachment.ValueCase.CODE -> {
                AdapterAttachment.Code(
                    id = attachment.code.id,
                    code = attachment.code.code,
                    expiresAt = if (attachment.code.expiresAt.isNotBlank()) {
                        ZonedDateTime.parse(attachment.code.expiresAt)
                    } else {
                        null
                    },
                    headerText = if (attachment.code.headerText != "") {
                        attachment.code.headerText
                    } else {
                        null
                    },
                    footerText = if (attachment.code.footerText != "") {
                        attachment.code.footerText
                    } else {
                        null
                    }
                )
            }

            Adapter.Attachment.ValueCase.PUNCH_CARD -> {
                AdapterAttachment.PunchCard(
                    used = attachment.punchCard.used,
                    total = attachment.punchCard.used + attachment.punchCard.remaining,
                    expiresAt = if (attachment.punchCard.expiresAt.isNotBlank()) {
                        ZonedDateTime.parse(attachment.punchCard.expiresAt)
                    } else {
                        null
                    }
                )
            }

            Adapter.Attachment.ValueCase.ERROR_DESCRIPTION -> {
                AdapterAttachment.ErrorDescription(
                    code = attachment.errorDescription.code,
                    debugMessage = attachment.errorDescription.debugMessage,
                    permanent = attachment.errorDescription.permanent
                )
            }

            Adapter.Attachment.ValueCase.DENIED_REASON -> {
                AdapterAttachment.DeniedReason(
                    code = attachment.deniedReason.code,
                    debugMessage = attachment.deniedReason.debugMessage
                )
            }

            Adapter.Attachment.ValueCase.END_USER_MESSAGE -> {
                AdapterAttachment.EndUserMessage(
                    message = attachment.endUserMessage.message.message,
                    link = if (attachment.endUserMessage.link.isNotBlank()) {
                        URI.create(attachment.endUserMessage.link)
                    } else {
                        null
                    }
                )
            }

            Adapter.Attachment.ValueCase.VALUE_NOT_SET, null -> {
                throw UnsupportedOperationException("Unsupported attachment: ${attachment.valueCase}")
            }
        }
    }

    fun parse(serializedManifest: Adapter.AdapterManifest): ManifestSnapshot {
        var things = persistentMapOf<ThingId, ThingDescription>()
        var allThingState = persistentMapOf<ThingId, ThingStateSnapshot>()
        var actions = persistentMapOf<ThingId, PersistentMap<ActionId, ActionDescription>>()

        for (serializedThing in serializedManifest.thingsList) {
            val thing = ThingDescription(
                id = ThingId(serializedThing.id),
                description = serializedThing.description,
                adminRole = serializedThing.adminRole,
                supportedStateTypes = serializedThing.supportedStateTypesList.toSet()
            )

            // Actions
            var thingActions = persistentMapOf<ActionId, ActionDescription>()

            for (serializedAction in serializedThing.actionsList) {
                val action = ActionDescription(
                    id = thing.id.createActionId(serializedAction.id),
                    description = serializedAction.description,
                    requiredAttachmentTypes = serializedAction.requiredInputAttachmentTypesList.toSet(),
                    possibleOutputAttachmentTypes = serializedAction.possibleOutputAttachmentTypesList.toSet()
                )

                thingActions = thingActions.put(action.id, action)
            }

            // State
            var thingStates = persistentMapOf<ThingState.Key, ThingState>()

            for (serializedState in serializedThing.stateList) {
                val lastUpdate = Instant.parse(serializedState.lastUpdate)

                val state = when (serializedState.valueCase) {
                    Adapter.ThingState.ValueCase.OPEN -> {
                        ThingState.OpenPosition(
                            timestamp = lastUpdate,
                            thingId = thing.id,
                            open = serializedState.open == Adapter.ThingState.Open.OPEN
                        )
                    }

                    Adapter.ThingState.ValueCase.LOCKED -> {
                        ThingState.Lock(
                            timestamp = lastUpdate,
                            locked = serializedState.locked == Adapter.ThingState.Locked.LOCKED,
                            thingId = thing.id
                        )
                    }

                    Adapter.ThingState.ValueCase.QUEUE_ESTIMATE -> {
                        log.warn("Support for queue estimate has not been implemented")
                        null
                    }

                    Adapter.ThingState.ValueCase.ACTION_HEALTH -> {
                        ThingState.ActionHealth(
                            timestamp = lastUpdate,
                            actionId = thing.id.createActionId(serializedState.actionHealth.actionId),
                            debugMessage = serializedState.actionHealth.debugMessage,
                            healthy = serializedState.actionHealth.healthy
                        )
                    }

                    Adapter.ThingState.ValueCase.REMOTE_CONNECTION -> {
                        ThingState.RemoteSystemConnection(
                            timestamp = lastUpdate,
                            thingId = thing.id,
                            debugMessage = serializedState.remoteConnection.debugMessage,
                            state = when (serializedState.remoteConnection.connected) {
                                true -> {
                                    ThingState.RemoteSystemConnection.ConnectionState.Connected(
                                        connectedAt = Instant.ofEpochSecond(serializedState.remoteConnection.timestampEpochSeconds)
                                    )
                                }
                                false -> {
                                    ThingState.RemoteSystemConnection.ConnectionState.Disconnected(
                                        disconnectedAt = Instant.ofEpochSecond(serializedState.remoteConnection.timestampEpochSeconds)
                                    )
                                }
                            }
                        )
                    }

                    Adapter.ThingState.ValueCase.DEBUG_LOG -> {
                        ThingState.DebugLog(
                            thingId = thing.id,
                            lines = serializedState.debugLog.linesList.map { line ->
                                ThingState.DebugLog.Line(
                                    timestamp = Instant.ofEpochMilli(line.timestampEpochMillis),
                                    message = line.message,
                                    level = line.level
                                )
                            }.toPersistentList()
                        )
                    }

                    Adapter.ThingState.ValueCase.VALUE_NOT_SET, null -> {
                        log.warn("Unsupported thing state detected: {}", serializedState.valueCase)
                        null
                    }
                }

                if (state != null) {
                    thingStates = thingStates.put(state.key, state)
                }
            }


            things = things.put(thing.id, thing)

            if (thingActions.isNotEmpty()) {
                actions = actions.put(thing.id, thingActions)
            }
            if (thingStates.isNotEmpty()) {
                allThingState = allThingState.put(thing.id, ThingStateSnapshot(thing.id, thingStates))
            }
        }

        val errorCodes = serializedManifest.errorCodeDescriptionsList.map { serializedErrorCode ->
            ErrorCodeDescription(
                code = serializedErrorCode.code,
                description = serializedErrorCode.description
            )
        }

        return ManifestSnapshot(
            version = serializedManifest.version,
            errorCodes = ErrorCodes(errorCodes.associateBy { it.code }.toPersistentHashMap()),
            thingStates = allThingState,
            actions = actions,
            things = things
        )
    }


}