package com.github.oslokommune.oslonokkelen.adapter.manifest

import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCode
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodeDescription
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodes
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingDescription
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingId
import kotlinx.collections.immutable.persistentMapOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
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

    @Nested
    inner class Things {

        private val frontDoor = ThingDescription(
            id = ThingId("front-door"),
            description = "The front door",
            adminRole = "master-of-doors"
        )

        @Test
        fun `Can add thing to manifest`() {
            val original = ManifestSnapshot()
            val withFrontDoor = original + frontDoor

            assertThat(withFrontDoor).isEqualTo(
                ManifestSnapshot(
                    version = 2,
                    things = persistentMapOf(frontDoor.id to frontDoor)
                )
            )
        }

        @Test
        fun `Adding exactly the same door twice returns the same manifest`() {
            val original = ManifestSnapshot()
            val withFrontDoor = original + frontDoor + frontDoor

            assertThat(withFrontDoor).isEqualTo(
                ManifestSnapshot(
                    version = 2,
                    things = persistentMapOf(frontDoor.id to frontDoor)
                )
            )
        }

        @Test
        fun `Updating the door description returns a new manifest`() {
            val original = ManifestSnapshot()
            val updatedFrontDoor = frontDoor.copy(description = "The big front door")
            val withFrontDoor = original + frontDoor + updatedFrontDoor

            assertThat(withFrontDoor).isEqualTo(
                ManifestSnapshot(
                    version = 3,
                    things = persistentMapOf(frontDoor.id to updatedFrontDoor)
                )
            )
        }

        @Test
        fun `Removing a thing that does not exist returns the same manifest instance`() {
            val original = ManifestSnapshot()
            val originalAgain = original - frontDoor.id

            assertSame(original, originalAgain)
        }

        @Test
        fun `Removing a thing will bump the manifest`() {
            val original = ManifestSnapshot()
            val withFrontDoor = original + frontDoor
            val withoutFrontDoor = withFrontDoor - frontDoor.id

            assertThat(withoutFrontDoor).isEqualTo(
                ManifestSnapshot(version = 3)
            )
        }

    }


}