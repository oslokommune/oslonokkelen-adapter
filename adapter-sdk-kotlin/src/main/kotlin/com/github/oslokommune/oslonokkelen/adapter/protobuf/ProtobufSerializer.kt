package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.action.ActionResponseMessage
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.manifest.ManifestSnapshot
import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingDescription
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingState

object ProtobufSerializer {

    fun serialize(response: ActionResponseMessage): Adapter.ActionResponse {
        return Adapter.ActionResponse.newBuilder()
            .setStatus(response.status)
            .addAllAttachments(response.attachments.map { serializeAttachment(it) })
            .build()
    }

    fun serialize(request: AdapterActionRequest): Adapter.ActionRequest {
        return Adapter.ActionRequest.newBuilder()
            .addAllAttachments(request.attachments.map { serializeAttachment(it) })
            .setTimeBudgetMillis(request.timeBudget.toMillis().toInt())
            .setRequestId(request.requestId)
            .setThingId(request.actionId.thingId.value)
            .setActionId(request.actionId.value)
            .build()
    }

    private fun serializeAttachment(attachment: AdapterAttachment): Adapter.Attachment {
        return when (attachment) {
            is AdapterAttachment.NorwegianFodselsnummer -> {
                Adapter.Attachment.newBuilder()
                    .setNorwegianFodselsnummer(
                        Adapter.Attachment.NorwegianFodselsnummer.newBuilder()
                            .setNumber(attachment.number)
                            .build()
                    )
                    .build()
            }

            is AdapterAttachment.Code -> {
                Adapter.Attachment.newBuilder()
                    .setCode(Adapter.Attachment.Code.newBuilder().run {
                        code = attachment.code
                        id = attachment.id

                        if (attachment.expiresAt != null) {
                            expiresAt = attachment.expiresAt.toString()
                        }
                        if (attachment.headerText != null) {
                            headerText = attachment.headerText
                        }
                        if (attachment.footerText != null) {
                            footerText = attachment.footerText
                        }
                        build()
                    })
                    .build()
            }

            is AdapterAttachment.DeniedReason -> {
                Adapter.Attachment.newBuilder()
                    .setDeniedReason(
                        Adapter.Attachment.DeniedReason.newBuilder()
                            .setDebugMessage(attachment.debugMessage)
                            .setCode(attachment.code)
                            .build()
                    )
                    .build()
            }

            is AdapterAttachment.EndUserMessage -> {
                Adapter.Attachment.newBuilder()
                    .setEndUserMessage(Adapter.Attachment.EndUserMessage.newBuilder().run {
                        message = Adapter.TextContent.newBuilder()
                            .setContentType(Adapter.TextContent.ContentType.PLAIN_TEXT)
                            .setMessage(attachment.message)
                            .build()

                        if (attachment.link != null) {
                            link = attachment.link.link.toString()

                            if (attachment.link.name != null) {
                                linkName = attachment.link.name
                            }
                        }
                        build()
                    })
                    .build()
            }

            is AdapterAttachment.ErrorDescription -> {
                Adapter.Attachment.newBuilder()
                    .setErrorDescription(
                        Adapter.Attachment.ErrorDescription.newBuilder()
                            .setDebugMessage(attachment.debugMessage)
                            .setPermanent(attachment.permanent)
                            .setCode(attachment.code)
                            .build()
                    )
                    .build()
            }
        }
    }

    fun serialize(manifest: ManifestSnapshot): Adapter.AdapterManifest {
        val thingList = manifest.things.values.map { thing ->
            val thingBuilder = Adapter.AdapterManifest.Thing.newBuilder()
                .setId(thing.id.value)
                .setDescription(thing.description)
                .addAllActions(serializeActions(manifest, thing))
                .addAllState(serializeThingState(manifest, thing))
                .setAdminRole(thing.adminRole)

            if (thing.link != null) {
                thingBuilder.setUri(thing.link.toString())
            }

            thingBuilder.build()
        }

        val errorList = manifest.errorCodes.codes.map { (code, description) ->
            Adapter.AdapterManifest.ErrorCodeDescription.newBuilder()
                .setCode(code.code)
                .setDescription(description.description)
                .build()
        }

        return Adapter.AdapterManifest.newBuilder()
            .setVersion(manifest.version)
            .addAllThings(thingList)
            .addAllErrorCodeDescriptions(errorList)
            .build()
    }

    private fun serializeThingState(manifest: ManifestSnapshot, thing: ThingDescription): List<Adapter.ThingState> {
        return manifest.thingStates[thing.id]?.data?.map { (_, state) ->
            when (state) {
                is ThingState.ActionHealth -> {
                    Adapter.ThingState.newBuilder()
                        .setLastUpdate(state.timestamp.toString())
                        .setActionHealth(
                            Adapter.ThingState.ActionHealth.newBuilder()
                                .setDebugMessage(state.debugMessage)
                                .setActionId(state.actionId.value)
                                .setHealthy(state.healthy)
                                .build()
                        )
                        .build()
                }
                is ThingState.Lock -> {
                    Adapter.ThingState.newBuilder()
                        .setLastUpdate(state.timestamp.toString())
                        .setLocked(
                            if (state.locked) {
                                Adapter.ThingState.Locked.LOCKED
                            } else {
                                Adapter.ThingState.Locked.UNLOCKED
                            }
                        )
                        .build()
                }
                is ThingState.DebugLog -> {
                    Adapter.ThingState.newBuilder()
                        .setLastUpdate(state.timestamp.toString())
                        .setDebugLog(
                            Adapter.ThingState.DebugLog.newBuilder()
                                .addAllLines(
                                    state.lines.map { line ->
                                        Adapter.ThingState.DebugLog.Line.newBuilder()
                                            .setTimestampEpochMillis(line.timestamp.toEpochMilli())
                                            .setMessage(line.message)
                                            .setLevel(
                                                when (line.level) {
                                                    Adapter.ThingState.DebugLog.Level.DEBUG -> Adapter.ThingState.DebugLog.Level.DEBUG
                                                    Adapter.ThingState.DebugLog.Level.INFO -> Adapter.ThingState.DebugLog.Level.INFO
                                                    Adapter.ThingState.DebugLog.Level.WARNING -> Adapter.ThingState.DebugLog.Level.WARNING
                                                    Adapter.ThingState.DebugLog.Level.ERROR -> Adapter.ThingState.DebugLog.Level.ERROR
                                                    Adapter.ThingState.DebugLog.Level.UNRECOGNIZED -> Adapter.ThingState.DebugLog.Level.ERROR
                                                }
                                            )
                                            .build()
                                    }
                                )
                                .build()
                        )
                        .build()
                }
                is ThingState.OpenPosition -> {
                    Adapter.ThingState.newBuilder()
                        .setLastUpdate(state.timestamp.toString())
                        .setOpen(
                            when (state.open) {
                                true -> Adapter.ThingState.Open.OPEN
                                false -> Adapter.ThingState.Open.CLOSED
                            }
                        )
                        .build()
                }

                is ThingState.BatteryStatus -> {
                    Adapter.ThingState.newBuilder()
                        .setLastUpdate(state.timestamp.toString())
                        .setBatteryStatus(when (state.state) {
                            ThingState.BatteryStatus.State.EMPTY -> Adapter.ThingState.BatteryStatus.EMPTY
                            ThingState.BatteryStatus.State.GOOD -> Adapter.ThingState.BatteryStatus.GOOD
                            ThingState.BatteryStatus.State.POOR -> Adapter.ThingState.BatteryStatus.POOR
                        })
                        .build()
                }
                is ThingState.DeviceType -> {
                    val deviceInfoBuilder = Adapter.ThingState.DeviceInfo.newBuilder()

                    if (state.vendor != null) {
                        deviceInfoBuilder.setVendor(state.vendor)
                    }
                    if (state.model != null) {
                        deviceInfoBuilder.setModel(state.model)
                    }
                    if (state.firmwareVersion != null) {
                        deviceInfoBuilder.setFirmwareVersion(state.firmwareVersion)
                    }

                    Adapter.ThingState.newBuilder()
                        .setLastUpdate(state.timestamp.toString())
                        .setDeviceType(deviceInfoBuilder.build())
                        .build()
                }
                is ThingState.Online -> {
                    val onlineBuilder = Adapter.ThingState.Online.newBuilder()
                    onlineBuilder.setIsOnline(state.online)

                    if (state.lastSeen != null) {
                        onlineBuilder.setLastSeen(state.lastSeen.toString())
                    }

                    Adapter.ThingState.newBuilder()
                        .setLastUpdate(state.timestamp.toString())
                        .setOnline(onlineBuilder.build())
                        .build()
                }
            }
        } ?: emptyList()
    }

    private fun serializeActions(
        manifest: ManifestSnapshot,
        thing: ThingDescription
    ): List<Adapter.AdapterManifest.Action> {
        val actionList = manifest.actions[thing.id]?.map { (_, action) ->
            Adapter.AdapterManifest.Action.newBuilder()
                .setId(action.id.value)
                .setDescription(action.description)
                .addAllRequiredInputAttachmentTypes(action.requiredAttachmentTypes.toList())
                .addAllPossibleOutputAttachmentTypes(action.possibleOutputAttachmentTypes.toList())
                .build()
        } ?: emptyList()

        return actionList.sortedBy { it.id }
    }

}