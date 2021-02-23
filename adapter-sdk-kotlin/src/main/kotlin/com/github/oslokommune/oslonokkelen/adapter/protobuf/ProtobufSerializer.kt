package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter

object ProtobufSerializer {


    fun serialize(request: AdapterActionRequest): Adapter.ActionRequest {

        return Adapter.ActionRequest.newBuilder()
            .addAllAttachments(request.attachments.map { attachment ->
                when (attachment) {
                    is AdapterAttachment.NorwegianFodselsnummer -> {
                        Adapter.Attachment.newBuilder()
                            .setNorwegianFodselsnummer(
                                Adapter.Attachment.NorwegianFodselsnummer.newBuilder()
                                    .setNumber(attachment.number)
                                    .build()
                            )
                            .build()
                    }
                    else -> {
                        throw UnsupportedOperationException("Unsupported request attachment: $attachment")
                    }
                }
            })
            .setRequestId(request.requestId)
            .setThingId(request.actionId.thingId.value)
            .setActionId(request.actionId.value)
            .build()
    }

}