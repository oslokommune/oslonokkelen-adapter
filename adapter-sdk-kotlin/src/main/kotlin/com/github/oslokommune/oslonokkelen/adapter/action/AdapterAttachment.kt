package com.github.oslokommune.oslonokkelen.adapter.action

import java.net.URI
import java.time.ZonedDateTime

sealed class AdapterAttachment {

    operator fun plus(other: AdapterAttachment): ActionResponseMessage {
        if (this.javaClass.isAssignableFrom(other.javaClass)) {
            throw IllegalStateException("Can't add more than one ${other.javaClass.simpleName}")
        }

        return ActionResponseMessage(
            attachments = listOf(this, other)
        )
    }

    data class Code(
        val id: String,
        val code: String,
        val expiresAt: ZonedDateTime? = null,
        val headerText: String? = null,
        val footerText: String? = null
    ) : AdapterAttachment() {
        init {
            if (id.isBlank()) {
                throw IllegalArgumentException("Id can't be blank")
            }
            if (code.isBlank()) {
                throw IllegalArgumentException("Code can't be blank")
            }
        }
    }

    data class EndUserMessage(
        val message: String,
        val link: Link? = null
    ) : AdapterAttachment() {
        init {
            if (message.isBlank()) {
                throw IllegalArgumentException("Message can't be blank")
            }
        }
    }

    data class Link(val link: URI, val name: String? = null) {
        init {
            if (name != null) {
                if (name.length > 20) {
                    throw IllegalArgumentException("Name is too long")
                }
                if (name.contains('\n')) {
                    throw IllegalArgumentException("Name must be single line")
                }
            }
        }
    }

    data class DeniedReason(val code: String, val debugMessage: String) : AdapterAttachment() {
        init {
            if (code.isBlank()) {
                throw IllegalArgumentException("Denied code can't be blank")
            }
        }
    }

    data class ErrorDescription(
        val code: String,
        val debugMessage: String,
        val permanent: Boolean = true
    ) : AdapterAttachment() {
        companion object {
            fun from(trouble: Throwable): ErrorDescription {
                return ErrorDescription(
                    code = "exception",
                    debugMessage = "${trouble.javaClass.name}: ${trouble.message ?: "No message"}",
                    permanent = true
                )
            }
        }
    }

    /**
     * http://www.fnrinfo.no/verktoy/finnlovlige_tilfeldig.aspx
     */
    data class NorwegianFodselsnummer(val number: String) : AdapterAttachment() {
        init {
            if (number.isBlank()) {
                throw IllegalArgumentException("Fødselsnummer can't be blank")
            }
        }
    }

    data class ErrorSource(val value: Value) : AdapterAttachment() {

        enum class Value {
            EXTERNAL_API, DOOR;
        }

        companion object {
            val ExternalApi = ErrorSource(Value.EXTERNAL_API)
            val Door = ErrorSource(Value.DOOR)
        }
    }
}
