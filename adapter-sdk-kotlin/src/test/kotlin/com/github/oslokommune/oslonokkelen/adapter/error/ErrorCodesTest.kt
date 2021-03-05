package com.github.oslokommune.oslonokkelen.adapter.error

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class ErrorCodesTest {

    @Test
    fun `Can add one error code description`() {
        val someErrorCode = ErrorCodeDescription("some.error", "Some error code")

        val original = ErrorCodes()
        val withError = original + someErrorCode
        val expected = ErrorCodes(someErrorCode)

        assertEquals(expected, withError)
    }

    @Test
    fun `Can add two error code descriptions`() {
        val someErrorCode = ErrorCodeDescription("some.error", "Some error code")
        val anotherErrorCode = ErrorCodeDescription("other.error", "Some other error")

        val original = ErrorCodes()
        val withErrors = original + someErrorCode + anotherErrorCode
        val expected = ErrorCodes(someErrorCode, anotherErrorCode)

        assertEquals(expected, withErrors)
        assertNotEquals(original, withErrors)
    }

    @Test
    fun `Adding exactly the same code twice does nothing`() {
        val someErrorCode = ErrorCodeDescription("some.error", "Some error code")

        val original = ErrorCodes()
        val withError1 = original + someErrorCode
        val withError2 = withError1 + someErrorCode

        assertSame(withError1, withError2)
    }

    @Test
    fun `Adding the same code with a different description replaces the first`() {
        val firstRevision = ErrorCodeDescription("some.error", "Some error code")
        val secondRevision = ErrorCodeDescription("some.error", "Scary error code")

        val original = ErrorCodes()
        val rev1 = original + firstRevision
        val rev2 = rev1 + secondRevision
        val expected = ErrorCodes(secondRevision)

        assertEquals(expected, rev2)
    }

}