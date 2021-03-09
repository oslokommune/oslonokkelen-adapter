package com.github.oslokommune.oslonokkelen.adapter.manifest

import com.github.oslokommune.oslonokkelen.adapter.action.ActionDescription
import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCode
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodeDescription
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodes
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingDescription
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingId
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingState
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingStateSnapshot
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

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


        @Nested
        inner class Actions {

            private val unlockFrontDoor = ActionDescription(
                id = ActionId("front-door", "unlock"),
                description = "Unlock the front door"
            )

            @Test
            fun `Can't add action to manifest before the thing`() {
                val ex = assertThrows<InvalidManifestException> {
                    val original = ManifestSnapshot()
                    original + unlockFrontDoor
                }

                assertThat(ex).hasMessage("Can't add action 'action: front-door/unlock' to a manifest that doesn't have a description of thing 'thing: front-door'")
            }

            @Test
            fun `Can add action to manifest`() {
                val original = ManifestSnapshot()
                val withAction = original + frontDoor + unlockFrontDoor

                assertThat(withAction).isEqualTo(
                    ManifestSnapshot(
                        version = 3,
                        things = persistentMapOf(frontDoor.id to frontDoor),
                        actions = persistentMapOf(frontDoor.id to persistentMapOf(unlockFrontDoor.id to unlockFrontDoor))
                    )
                )
            }

            @Test
            fun `Adding exactly the same thing twice does not bump manifest`() {
                val original = ManifestSnapshot()
                val withAction = original + frontDoor + unlockFrontDoor + unlockFrontDoor

                assertThat(withAction).isEqualTo(
                    ManifestSnapshot(
                        version = 3,
                        things = persistentMapOf(frontDoor.id to frontDoor),
                        actions = persistentMapOf(frontDoor.id to persistentMapOf(unlockFrontDoor.id to unlockFrontDoor))
                    )
                )
            }

            @Test
            fun `Adding existing action with new description bumps manifest`() {
                val original = ManifestSnapshot()
                val modifiedUnlockAction = unlockFrontDoor.copy(description = "Sesam sesam")
                val withAction = original + frontDoor + unlockFrontDoor + modifiedUnlockAction

                assertThat(withAction).isEqualTo(
                    ManifestSnapshot(
                        version = 4,
                        things = persistentMapOf(frontDoor.id to frontDoor),
                        actions = persistentMapOf(frontDoor.id to persistentMapOf(unlockFrontDoor.id to modifiedUnlockAction))
                    )
                )
            }

            @Test
            fun `Removing a thing also removes its actions`() {
                val original = ManifestSnapshot()
                val withAction = original + frontDoor + unlockFrontDoor
                val withoutAction = withAction - frontDoor.id

                assertThat(withoutAction).isEqualTo(
                    ManifestSnapshot(version = 4)
                )
            }

            @Test
            fun `Removing action that does not exist does not bump the manifest`() {
                val original = ManifestSnapshot()
                val same = original - frontDoor.id

                assertSame(original, same)
            }

        }

        @Nested
        inner class ThingStates {

            private val unlockFrontDoor = ActionDescription(
                id = ActionId("front-door", "unlock"),
                description = "Unlock the front door"
            )

            private val lockedFrontDoor = ThingState.Lock(
                timestamp = Instant.now(),
                thingId = frontDoor.id,
                locked = true
            )

            private val frontDoorLog = ThingState.DebugLog(
                thingId = frontDoor.id,
                lines = persistentListOf()
            )

            private val unlockHealthy = ThingState.ActionHealth(
                debugMessage = "Not doing so well..",
                actionId = unlockFrontDoor.id,
                timestamp = Instant.now(),
                healthy = false
            )

            @Test
            fun `Can't add thing state for unknown thing`() {
                val ex = assertThrows<InvalidManifestException> {
                    val original = ManifestSnapshot()
                    original + lockedFrontDoor
                }

                assertThat(ex).hasMessage("Can't add state for unknown thing 'thing: front-door' to manifest")
            }

            @Test
            fun `Can't add action health for unknown action`() {
                val ex = assertThrows<InvalidManifestException> {
                    val original = ManifestSnapshot()
                    original + unlockHealthy
                }

                assertThat(ex).hasMessage("Tried to add state related to unknown action: front-door/unlock")
            }

            @Test
            fun `Adding the same state twice does not bump the manifest`() {
                val original = ManifestSnapshot()
                val withState = original + frontDoor + lockedFrontDoor + lockedFrontDoor

                assertThat(withState).isEqualTo(
                    ManifestSnapshot(
                        version = 3,
                        things = persistentMapOf(frontDoor.id to frontDoor),
                        thingStates = persistentMapOf(frontDoor.id to ThingStateSnapshot(lockedFrontDoor))
                    )
                )
            }

            @Test
            fun `Confirming exactly the same state later will bump manifest`() {
                val original = ManifestSnapshot()
                val updatedLockedFrontDoor = lockedFrontDoor.copy(timestamp = lockedFrontDoor.timestamp.plusSeconds(20))
                val withState = original + frontDoor + lockedFrontDoor + updatedLockedFrontDoor

                assertThat(withState).isEqualTo(
                    ManifestSnapshot(
                        version = 4,
                        things = persistentMapOf(frontDoor.id to frontDoor),
                        thingStates = persistentMapOf(frontDoor.id to ThingStateSnapshot(updatedLockedFrontDoor))
                    )
                )
            }

            @Test
            fun `Removing the thing will also remove the associated state`() {
                val original = ManifestSnapshot()
                val withState = original + frontDoor + lockedFrontDoor - frontDoor.id

                assertThat(withState).isEqualTo(
                    ManifestSnapshot(
                        version = 4
                    )
                )
            }

            @Test
            fun `Can remove the last state by key for a thing`() {
                val original = ManifestSnapshot()
                val withState = original + frontDoor + lockedFrontDoor
                val withoutState = withState - lockedFrontDoor.key

                assertThat(withoutState).isEqualTo(
                    ManifestSnapshot(
                        version = 4,
                        things = persistentMapOf(frontDoor.id to frontDoor)
                    )
                )
            }

            @Test
            fun `Removing state for an unknown thing returns the same manifest instance`() {
                val original = ManifestSnapshot()
                val same = original - lockedFrontDoor.key

                assertSame(original, same)
            }

            @Test
            fun `Can remove one off two states for thing`() {
                val original = ManifestSnapshot()
                val withState = original + frontDoor + lockedFrontDoor + frontDoorLog
                val withoutState = withState - lockedFrontDoor.key

                assertThat(withoutState).isEqualTo(
                    ManifestSnapshot(
                        version = 5,
                        things = persistentMapOf(frontDoor.id to frontDoor),
                        thingStates = persistentMapOf(frontDoor.id to ThingStateSnapshot(frontDoorLog))
                    )
                )
            }

            @Test
            fun `Removing the same state twice does not bump the manifest`() {
                val original = ManifestSnapshot()
                val withLockedState = original + frontDoor + lockedFrontDoor
                val withoutLockedState = withLockedState - lockedFrontDoor.key

                assertThat(withoutLockedState - lockedFrontDoor.key).isSameAs(withoutLockedState)
            }

            @Test
            fun `Removing action will also remove state associated with that action`() {
                val original = ManifestSnapshot()
                val withState = original + frontDoor + unlockFrontDoor + unlockHealthy

                assertThat(withState - unlockFrontDoor.id).isEqualTo(
                    ManifestSnapshot(
                        version = 5,
                        things = persistentMapOf(frontDoor.id to frontDoor)
                    )
                )
            }

            @Test
            fun `Removing unknown action does not bump manifest`() {
                val original = ManifestSnapshot()
                val withThing = original + frontDoor
                val same = withThing - unlockFrontDoor.id

                assertSame(withThing, same)
            }

            @Test
            fun `Removing completely unknown action does not bump manifest`() {
                val original = ManifestSnapshot()
                val same = original - unlockFrontDoor.id

                assertSame(original, same)
            }

        }

    }


}