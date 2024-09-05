package com.github.oslokommune.oslonokkelen.adapter.error

import com.github.oslokommune.oslonokkelen.adapter.action.ActionResponseMessage
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorType.DENIED
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorType.PERMANENT_ERROR
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorType.TEMPORARY_ERROR
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ErrorCodeDescriptionTest {

    @Test
    fun `Too long description`() {
        val ex = assertThrows<IllegalArgumentException> {
            ErrorCodeDescription(
                description = "x".repeat(ErrorCodeDescription.DESCRIPTION_MAX_LENGTH + 1),
                code = "too-long"
            )
        }

        assertThat(ex).hasMessage("Description of 'too-long' is too long: ${"x".repeat(ErrorCodeDescription.DESCRIPTION_MAX_LENGTH + 1)}")
    }

    @Test
    fun `Too long code`() {
        val ex = assertThrows<IllegalArgumentException> {
            ErrorCodeDescription(
                description = "Too long code",
                code = "x".repeat(ErrorCode.CODE_MAX_LENGTH + 1)
            )
        }

        assertThat(ex).hasMessage("Error code is too long: ${"x".repeat(ErrorCode.CODE_MAX_LENGTH + 1)}")
    }

    @Test
    fun `Invalid part`() {
        val ex = assertThrows<IllegalArgumentException> {
            ErrorCodeDescription(
                description = "Invalid code",
                code = "not-valid.ø"
            )
        }

        assertThat(ex).hasMessage("Invalid error code: not-valid.ø")
    }

    @Test
    fun `Valid code`() {
        ErrorCodeDescription(
            description = "Valid code",
            code = "this.is.valid"
        )
    }

    @Test
    fun `Another valid code`() {
        ErrorCodeDescription(
            description = "Another valid code",
            code = "this.is.also-valid"
        )
    }

    @Test
    fun `Use error description to create permanent action error with default debug message`() {
        val errorDescription = ErrorCodeDescription(
            description = "This is bad..",
            code = "bad.really.bad",
            type = PERMANENT_ERROR
        )

        val response = errorDescription.createActionResponse()

        assertThat(response).isEqualTo(ActionResponseMessage(
            AdapterAttachment.ErrorDescription(
                code = "bad.really.bad",
                debugMessage = "This is bad..",
                permanent = true,
                errorSource = null
            )
        ))
    }

    @Test
    fun `Use error description to create temporary action error with default debug message`() {
        val errorDescription = ErrorCodeDescription(
            description = "This is semi bad..",
            code = "semi.bad",
            type = TEMPORARY_ERROR
        )

        val response = errorDescription.createActionResponse()

        assertThat(response).isEqualTo(ActionResponseMessage(
            AdapterAttachment.ErrorDescription(
                code = "semi.bad",
                debugMessage = "This is semi bad..",
                permanent = false,
                errorSource = null
            )
        ))
    }

    @Test
    fun `Use error description to create temporary action error with overridden debug message`() {
        val errorDescription = ErrorCodeDescription(
            description = "This is semi bad..",
            code = "semi.bad",
            type = TEMPORARY_ERROR
        )

        val response = errorDescription.createActionResponse("Could be worse...")

        assertThat(response).isEqualTo(ActionResponseMessage(
            AdapterAttachment.ErrorDescription(
                code = "semi.bad",
                debugMessage = "Could be worse...",
                permanent = false,
                errorSource = null
            )
        ))
    }

    @Test
    fun `Use error description to create denied action error with overridden debug message`() {
        val errorDescription = ErrorCodeDescription(
            description = "No access",
            code = "no.access",
            type = DENIED
        )

        val response = errorDescription.createActionResponse("Members only")

        assertThat(response).isEqualTo(ActionResponseMessage(
            AdapterAttachment.DeniedReason(
                debugMessage = "Members only",
                code = "no.access"
            )
        ))
    }

    @Test
    fun `Create action error response from exception`() {
        val errorDescription = ErrorCodeDescription(
            description = "Unexpected exception",
            code = "unexpected"
        )

        val response = errorDescription.createActionResponse(IllegalStateException("You didn't see this coming!"))

        assertThat(response).isEqualTo(ActionResponseMessage(
            AdapterAttachment.ErrorDescription(
                code = "unexpected",
                debugMessage = "java.lang.IllegalStateException: You didn't see this coming!",
                permanent = true,
                errorSource = null
            )
        ))
    }

}