package com.github.oslokommune.oslonokkelen.adapter.error

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

}