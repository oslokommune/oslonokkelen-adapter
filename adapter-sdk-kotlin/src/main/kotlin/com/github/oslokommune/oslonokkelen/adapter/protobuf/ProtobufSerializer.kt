package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.action.ActionResponseMessage
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter

object ProtobufSerializer {

    fun serialize(response: ActionResponseMessage): Adapter.ActionResponse {
        return Adapter.ActionResponse.newBuilder()
            .addAllAttachments(response.attachments.map { serializeAttachment(it) })
            .setStatus(response.status)
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

                        if (attachment.expiresAt != null) {
                            expiresAt = attachment.expiresAt.toString()
                        }
                        build()
                    })
                    .build()
            }

            is AdapterAttachment.DeniedReason -> {
                Adapter.Attachment.newBuilder()
                    .setErrorDescription(
                        Adapter.Attachment.ErrorDescription.newBuilder()
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
                            link = attachment.link.toString()
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
                            .setCode(attachment.code)
                            .build()
                    )
                    .build()
            }

            is AdapterAttachment.PunchCard -> {
                Adapter.Attachment.newBuilder()
                    .setPunchCard(Adapter.Attachment.PunchCard.newBuilder().run {
                        remaining = attachment.total - attachment.used
                        used = attachment.used

                        if (attachment.expiresAt != null) {
                            expiresAt = attachment.expiresAt.toString()
                        }

                        build()
                    })
                    .build()
            }
        }
    }

}