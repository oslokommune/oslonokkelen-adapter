package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.action.ActionResponseMessage
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

object ProtobufParser {

    fun parse(serializedRequest: Adapter.ActionRequest, timestamp: Instant): AdapterActionRequest {
        return AdapterActionRequest(
            requestId = serializedRequest.requestId,
            actionId = ActionId(serializedRequest.thingId, serializedRequest.actionId),
            timeBudget = Duration.ofMillis(serializedRequest.timeBudgetMillis.toLong()),
            receivedAt = timestamp,
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


}