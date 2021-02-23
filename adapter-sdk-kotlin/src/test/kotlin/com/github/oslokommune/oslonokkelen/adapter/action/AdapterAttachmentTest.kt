package com.github.oslokommune.oslonokkelen.adapter.action

import kotlinx.collections.immutable.persistentListOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AdapterAttachmentTest {

    @Test
    fun `Can add human readable message to a code`() {
        val code = AdapterAttachment.Code("super-secret-code", "x-123")
        val response = code + AdapterAttachment.EndUserMessage("Here is your super secret code")

        assertThat(response).isEqualTo(
            ActionResponseMessage(
                persistentListOf(
                    AdapterAttachment.Code("super-secret-code", "x-123"),
                    AdapterAttachment.EndUserMessage("Here is your super secret code")
                )
            )
        )
    }

    @Test
    fun `Cant add a code to another code`() {
        val code = AdapterAttachment.Code("super-secret-code", "x-123")

        assertThrows<IllegalStateException> {
            code + AdapterAttachment.Code("other-secret-code", "x-321")
        }
    }

    @Test
    fun `Cant add a code to response already containing a code`() {
        val code = AdapterAttachment.Code("super-secret-code", "x-123")
        val response = code + AdapterAttachment.EndUserMessage("Here is your super secret code")

        assertThrows<IllegalStateException> {
            response + AdapterAttachment.Code("other-secret-code", "x-321")
        }
    }

}