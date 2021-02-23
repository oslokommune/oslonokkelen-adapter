package com.github.oslokommune.oslonokkelen.adapter.action

import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class ActionResponseMessage(val attachments: PersistentList<AdapterAttachment> = persistentListOf()) {

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
            attachments = attachments.add(attachment)
        )
    }

    operator fun plus(trouble: Throwable): ActionResponseMessage {
        return copy(
            attachments = attachments.add(AdapterAttachment.ErrorDescription.from(trouble))
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
            return ActionResponseMessage(persistentListOf(AdapterAttachment.ErrorDescription(code, debugMessage)))
        }
    }
}
