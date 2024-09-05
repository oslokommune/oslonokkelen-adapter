package com.github.oslokommune.oslonokkelen.adapter.action

import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter

data class ActionResponseMessage(val attachments: List<AdapterAttachment> = emptyList()) {

    constructor(vararg attachments: AdapterAttachment) : this(attachments.toList())


    val status: Adapter.ActionResponse.Status
        get() {
            for (attachment in attachments) {
                if (attachment is AdapterAttachment.DeniedReason) {
                    return Adapter.ActionResponse.Status.DENIED
                } else if (attachment is AdapterAttachment.ErrorDescription) {
                    return if (attachment.permanent) {
                        Adapter.ActionResponse.Status.ERROR_PERMANENT
                    } else {
                        Adapter.ActionResponse.Status.ERROR_TEMPORARY
                    }
                }
            }

            return Adapter.ActionResponse.Status.SUCCESS
        }

    val isError: Boolean
        get() = status == Adapter.ActionResponse.Status.ERROR_PERMANENT || status == Adapter.ActionResponse.Status.ERROR_TEMPORARY

    operator fun plus(attachment: AdapterAttachment): ActionResponseMessage {
        if (attachments.any { it.javaClass.isAssignableFrom(attachment.javaClass) }) {
            throw IllegalStateException("Can't add more then one ${attachment.javaClass.simpleName}")
        }

        return copy(
            attachments = attachments + attachment
        )
    }

    operator fun plus(trouble: Throwable): ActionResponseMessage {
        return copy(
            attachments = attachments + AdapterAttachment.ErrorDescription.from(trouble, null)
        )
    }

    inline fun <reified T : AdapterAttachment> with(block: (T) -> Unit) {
        val match = attachments.filterIsInstance<T>().firstOrNull()

        if (match != null) {
            block(match)
        }
    }

    inline fun <reified T : AdapterAttachment> findInstanceOfOrNull(): T? {
        return attachments.filterIsInstance<T>().firstOrNull()
    }

    companion object {
        fun permanentError(code: String, debugMessage: String): ActionResponseMessage {
            return ActionResponseMessage(AdapterAttachment.ErrorDescription(
                code = code,
                debugMessage = debugMessage,
                errorSource = null
            ))
        }
    }
}
