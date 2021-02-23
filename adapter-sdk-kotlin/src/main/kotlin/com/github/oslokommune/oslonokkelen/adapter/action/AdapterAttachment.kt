package com.github.oslokommune.oslonokkelen.adapter.action

import kotlinx.collections.immutable.persistentListOf
import java.net.URI
import java.time.ZonedDateTime

sealed class AdapterAttachment {

    operator fun plus(other: AdapterAttachment): ActionResponseMessage {
        if (this.javaClass.isAssignableFrom(other.javaClass)) {
            throw IllegalStateException("Can't add more then one ${other.javaClass.simpleName}")
        }

        return ActionResponseMessage(
            attachments = persistentListOf(this, other)
        )
    }

    data class Code(
        val id: String,
        val code: String,
        val expiresAt: ZonedDateTime? = null
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
        val link: URI? = null
    ) : AdapterAttachment() {
        init {
            if (message.isBlank()) {
                throw IllegalArgumentException("Message can't be blank")
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

    data class PunchCard(
        val used: Int,
        val total: Int
    ) : AdapterAttachment() {
        init {
            if (used < 0) {
                throw IllegalArgumentException("Used must be positive")
            }
            if (total < 0) {
                throw IllegalArgumentException("Total must be positive")
            }
            if (used > total) {
                throw IllegalArgumentException("Used can't be greater then total")
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
}
