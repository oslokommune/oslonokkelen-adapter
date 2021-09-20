package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.action.ActionDescription
import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.action.ActionResponseMessage
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorCodeDescription
import com.github.oslokommune.oslonokkelen.adapter.manifest.ManifestSnapshot
import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingDescription
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingId
import com.github.oslokommune.oslonokkelen.adapter.thing.ThingState
import kotlinx.collections.immutable.persistentListOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

internal class ProtobufSerializerTest {


    @Nested
    inner class Action {

        @Test
        fun `Serialize and parse a request`() {
            val request = AdapterActionRequest(
                requestId = "request-1",
                actionId = ActionId("door", "open"),
                timeBudget = Duration.ofSeconds(3),
                attachments = listOf(
                    AdapterAttachment.NorwegianFodselsnummer("30098602247")
                )
            )

            val serializedRequest = ProtobufSerializer.serialize(request)
            val parsedRequest = ProtobufParser.parse(serializedRequest)

            assertEquals(request, parsedRequest)
        }

        @Test
        fun `Serialize and parse response with code and message`() {
            val code = AdapterAttachment.Code(
                expiresAt = ZonedDateTime.now(),
                id = "x",
                code = "y"
            )

            val punchCard = AdapterAttachment.PunchCard(
                expiresAt = ZonedDateTime.now(),
                used = 10,
                total = 20
            )

            val message = AdapterAttachment.EndUserMessage(
                message = "Here is your code"
            )

            val response = code + message + punchCard
            val serializedResponse = ProtobufSerializer.serialize(response)
            val parsedResponse = ProtobufParser.parse(serializedResponse)

            assertEquals(response, parsedResponse)
            assertEquals(Adapter.ActionResponse.Status.SUCCESS, parsedResponse.status)
        }

        @Test
        fun `Serialize and parse response with code without expiration date`() {
            val code = AdapterAttachment.Code(
                code = "y",
                id = "x"
            )

            val message = AdapterAttachment.PunchCard(
                used = 10,
                total = 20
            )

            val response = code + message
            val serializedResponse = ProtobufSerializer.serialize(response)
            val parsedResponse = ProtobufParser.parse(serializedResponse)

            assertEquals(response, parsedResponse)
            assertEquals(Adapter.ActionResponse.Status.SUCCESS, parsedResponse.status)
        }

        @Test
        fun `Serialize and parse response permanent error description`() {
            val response = ActionResponseMessage(
                AdapterAttachment.ErrorDescription(
                    debugMessage = "Some error occurred",
                    code = "some.error",
                    permanent = true
                )
            )
            val serializedResponse = ProtobufSerializer.serialize(response)
            val parsedResponse = ProtobufParser.parse(serializedResponse)

            assertEquals(response, parsedResponse)
            assertEquals(Adapter.ActionResponse.Status.ERROR_PERMANENT, parsedResponse.status)
        }

        @Test
        fun `Serialize and parse response temporary error description`() {
            val response = ActionResponseMessage(
                AdapterAttachment.ErrorDescription(
                    debugMessage = "Some error occurred",
                    code = "some.error",
                    permanent = false
                )
            )
            val serializedResponse = ProtobufSerializer.serialize(response)
            val parsedResponse = ProtobufParser.parse(serializedResponse)

            assertEquals(response, parsedResponse)
            assertEquals(Adapter.ActionResponse.Status.ERROR_TEMPORARY, parsedResponse.status)
        }

        @Test
        fun `Serialize and parse response denied description`() {
            val response = ActionResponseMessage(
                AdapterAttachment.DeniedReason(
                    debugMessage = "Not today",
                    code = "not.today"
                )
            )
            val serializedResponse = ProtobufSerializer.serialize(response)
            val parsedResponse = ProtobufParser.parse(serializedResponse)

            assertEquals(response, parsedResponse)
            assertEquals(Adapter.ActionResponse.Status.DENIED, parsedResponse.status)
        }

        @Test
        fun `Serialize and parse response denied description with message`() {
            val response = ActionResponseMessage(
                AdapterAttachment.DeniedReason(
                    debugMessage = "Not today",
                    code = "not.today"
                ),
                AdapterAttachment.EndUserMessage(
                    message = "Not open today",
                    link = AdapterAttachment.Link(
                        link = URI.create("https://example.com/opening-hours"),
                        name = "Opening hours"
                    )
                )
            )

            val serializedResponse = ProtobufSerializer.serialize(response)
            val parsedResponse = ProtobufParser.parse(serializedResponse)

            assertEquals(response, parsedResponse)
            assertEquals(Adapter.ActionResponse.Status.DENIED, parsedResponse.status)
        }

        @Test
        fun `Serialize and parse response with code`() {
            val response = ActionResponseMessage(
                AdapterAttachment.Code(
                    id = "some-code",
                    code = "x-123",
                    headerText = "Header",
                    footerText = "Footer"
                )
            )

            val serializedResponse = ProtobufSerializer.serialize(response)
            val parsedResponse = ProtobufParser.parse(serializedResponse)

            assertEquals(response, parsedResponse)
            assertEquals(Adapter.ActionResponse.Status.SUCCESS, parsedResponse.status)
        }

    }

    @Nested
    inner class Manifest {

        private val frontDoor = ThingDescription(
            id = ThingId("front-door"),
            description = "This is the front door",
            adminRole = "master-of-doors",
            supportedStateTypes = setOf(Adapter.ThingStateType.ACTION_HEALTH)
        )

        private val unlockFrontDoor = ActionDescription(
            id = ActionId("front-door", "unlock"),
            description = "Unlock the front door"
        )

        private val timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS)

        @Test
        fun `Serialize and parse manifest with thing`() {
            test(ManifestSnapshot() + frontDoor)
        }

        @Test
        fun `Serialize and parse manifest with action`() {
            test(ManifestSnapshot() + frontDoor + unlockFrontDoor)
        }

        @Test
        fun `Serialize and parse manifest with locked door`() {
            val lockedDoor = ThingState.Lock(
                timestamp = timestamp,
                thingId = frontDoor.id,
                locked = true
            )

            test(ManifestSnapshot() + frontDoor + lockedDoor)
        }

        @Test
        fun `Serialize and parse manifest with unlocked door`() {
            val unlockedDoor = ThingState.Lock(
                timestamp = timestamp,
                thingId = frontDoor.id,
                locked = false
            )

            test(ManifestSnapshot() + frontDoor + unlockedDoor)
        }

        @Test
        fun `Serialize and parse manifest with action health`() {
            val healthyAction = ThingState.ActionHealth(
                timestamp = timestamp,
                actionId = unlockFrontDoor.id,
                debugMessage = "All good",
                healthy = true
            )

            test(ManifestSnapshot() + frontDoor + unlockFrontDoor + healthyAction)
        }

        @Test
        fun `Serialize and parse manifest with remote connection - Connected`() {
            val remoteConnection = ThingState.RemoteSystemConnection(
                timestamp = timestamp,
                debugMessage = "All good",
                state = ThingState.RemoteSystemConnection.ConnectionState.Connected(timestamp),
                thingId = frontDoor.id
            )

            test(ManifestSnapshot() + frontDoor + remoteConnection)
        }

        @Test
        fun `Serialize and parse manifest with remote connection - Disconnected`() {
            val remoteConnection = ThingState.RemoteSystemConnection(
                timestamp = timestamp,
                debugMessage = "We are disconnected",
                state = ThingState.RemoteSystemConnection.ConnectionState.Disconnected(timestamp),
                thingId = frontDoor.id
            )

            test(ManifestSnapshot() + frontDoor + remoteConnection)
        }

        @Test
        fun `Serialize and parse manifest with open door`() {
            val openDoor = ThingState.OpenPosition(
                timestamp = timestamp,
                thingId = frontDoor.id,
                open = true
            )

            test(ManifestSnapshot() + frontDoor + openDoor)
        }

        @Test
        fun `Serialize and parse manifest with debug log`() {
            val debugLog = ThingState.DebugLog(
                thingId = frontDoor.id,
                lines = persistentListOf(
                    ThingState.DebugLog.Line(
                        timestamp = timestamp,
                        message = "Some info",
                        level = Adapter.ThingState.DebugLog.Level.INFO
                    ),
                    ThingState.DebugLog.Line(
                        timestamp = timestamp.plusSeconds(1),
                        message = "Some warning",
                        level = Adapter.ThingState.DebugLog.Level.WARNING
                    )
                )
            )

            test(ManifestSnapshot() + frontDoor + debugLog)
        }

        @Test
        fun `Serialize manifest with error codes`() {
            test(ManifestSnapshot() + ErrorCodeDescription("some.error", "Some error"))
        }


        private fun test(originalSnapshot: ManifestSnapshot) {
            val serialized = ProtobufSerializer.serialize(originalSnapshot)
            val restored = ProtobufParser.parse(serialized)

            assertEquals(originalSnapshot, restored)
        }

    }

}