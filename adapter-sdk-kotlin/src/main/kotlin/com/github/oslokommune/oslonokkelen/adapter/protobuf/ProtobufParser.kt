package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter
import java.time.Duration

object ProtobufParser {

    fun parse(serializedRequest: Adapter.ActionRequest): AdapterActionRequest {
        return AdapterActionRequest(
            requestId = serializedRequest.requestId,
            actionId = ActionId(serializedRequest.thingId, serializedRequest.actionId),
            timeBudget = Duration.ofMillis(serializedRequest.timeBudgetMillis.toLong()),
            attachments = serializedRequest.attachmentsList.map { attachment ->
                when (attachment.valueCase) {
                    Adapter.Attachment.ValueCase.NORWEGIAN_FODSELSNUMMER -> {
                        AdapterAttachment.NorwegianFodselsnummer(attachment.norwegianFodselsnummer.number)
                    }
                    else -> {
                        throw UnsupportedOperationException("Unsupported request attachment: ${attachment.valueCase}")
                    }
                }
            }
        )
    }


}