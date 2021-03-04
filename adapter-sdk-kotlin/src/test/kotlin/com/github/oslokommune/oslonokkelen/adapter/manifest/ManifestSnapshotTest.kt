package com.github.oslokommune.oslonokkelen.adapter.manifest

import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCode
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodeDescription
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ManifestSnapshotTest {

    @Nested
    inner class ErrorCodes {

        @Test
        fun `Adding the same error code twice returns the same manifest`() {
            val original = ManifestSnapshot()
            val errorCodeDescription = ErrorCodeDescription("code", "description")

            val updated = original + errorCodeDescription
            val same = updated + errorCodeDescription

            assertThat(same).isEqualTo(
                ManifestSnapshot(
                    version = 2,
                    errorCodes = ErrorCodes(errorCodeDescription)
                )
            )
        }

        @Test
        fun `Updating the error code description bumps the manifest`() {
            val original = ManifestSnapshot()
            val errorCodeDescription = ErrorCodeDescription("code", "description")
            val updatedErrorCodeDescription = ErrorCodeDescription("code", "better description")

            val updated = original + errorCodeDescription
            val updatedAgain = updated + updatedErrorCodeDescription

            assertThat(updatedAgain).isEqualTo(
                ManifestSnapshot(
                    version = 3,
                    errorCodes = ErrorCodes(updatedErrorCodeDescription)
                )
            )
        }

        @Test
        fun `Removing an error code that does not exist returns the same manifest instance`() {
            val original = ManifestSnapshot()
            val errorCodeDescription = ErrorCodeDescription("code", "description")
            val withErrorDescription = original + errorCodeDescription
            val same = withErrorDescription - ErrorCode("something-else")

            assertSame(withErrorDescription, same)
        }

        @Test
        fun `Removing an error code bumps the manifest`() {
            val original = ManifestSnapshot()
            val errorCodeDescription = ErrorCodeDescription("code", "description")
            val withErrorDescription = original + errorCodeDescription
            val updated = withErrorDescription - errorCodeDescription.code

            assertThat(updated).isEqualTo(ManifestSnapshot(version = 3))
        }

    }


}