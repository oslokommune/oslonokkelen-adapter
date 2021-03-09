package com.github.oslokommune.oslonokkelen.adapter.thing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

internal class ThingStateSnapshotTest {

    private val timestamp = Instant.now()

    @Test
    fun `Can add state to an empty snapshot`() {
        val frontDoor = ThingId("front-door")
        val snapshot = ThingStateSnapshot(frontDoor)
        val modifiedSnapshot = snapshot + ThingState.OpenPosition(timestamp, frontDoor, open = true)

        assertNotEquals(snapshot, modifiedSnapshot)
    }

    @Test
    fun `Adding exactly the same state twice returns the same snapshot`() {
        val frontDoor = ThingId("front-door")
        val snapshot = ThingStateSnapshot(frontDoor)
        val openFrontDoor = ThingState.OpenPosition(timestamp, frontDoor, open = true)
        val modifiedSnapshot = snapshot + openFrontDoor
        val sameModifiedSnapshot = modifiedSnapshot + openFrontDoor

        assertSame(modifiedSnapshot, sameModifiedSnapshot)
    }

    @Test
    fun `Adding the same state, but with a later timestamp returns a new snapshot`() {
        val frontDoor = ThingId("front-door")
        val snapshot = ThingStateSnapshot(frontDoor)
        val openFrontDoor = ThingState.OpenPosition(timestamp, frontDoor, open = true)
        val modifiedSnapshot = snapshot + openFrontDoor

        val stillOpenFrontDoor = openFrontDoor.copy(timestamp = openFrontDoor.timestamp.plusSeconds(30))
        val newModifiedSnapshot = modifiedSnapshot + stillOpenFrontDoor

        assertThat(newModifiedSnapshot).isEqualTo(
            ThingStateSnapshot(
                stillOpenFrontDoor
            )
        )
    }

    @Test
    fun `Adding the different state, but with a later timestamp returns a new snapshot`() {
        val frontDoor = ThingId("front-door")
        val snapshot = ThingStateSnapshot(frontDoor)
        val openFrontDoor = ThingState.OpenPosition(timestamp, frontDoor, open = true)
        val modifiedSnapshot = snapshot + openFrontDoor

        val closedFrontDoor = ThingState.OpenPosition(timestamp.plusSeconds(40), frontDoor, open = false)
        val newModifiedSnapshot = modifiedSnapshot + closedFrontDoor

        assertThat(newModifiedSnapshot).isEqualTo(
            ThingStateSnapshot(
                closedFrontDoor
            )
        )
    }

    @Test
    fun `Can add different kinds of state`() {
        val frontDoor = ThingId("front-door")
        val snapshot = ThingStateSnapshot(frontDoor)

        val closedFrontDoor = ThingState.OpenPosition(timestamp, frontDoor, open = false)
        val lockedFrontDoor = ThingState.Lock(timestamp, frontDoor, locked = true)
        val modifiedSnapshot = snapshot + closedFrontDoor + lockedFrontDoor

        assertThat(modifiedSnapshot).isEqualTo(
            ThingStateSnapshot(
                closedFrontDoor, lockedFrontDoor
            )
        )
    }

    @Test
    fun `Can't add state belonging to different thing`() {
        val frontDoor = ThingId("front-door")
        val backDoor = ThingId("back-door")
        val closedBackDoor = ThingState.OpenPosition(timestamp, backDoor, open = false)

        val ex = assertThrows<IllegalArgumentException> {
            ThingStateSnapshot(frontDoor) + closedBackDoor
        }

        assertThat(ex).hasMessage("Tried to add state belonging to thing: back-door to thing: front-door")
    }

}