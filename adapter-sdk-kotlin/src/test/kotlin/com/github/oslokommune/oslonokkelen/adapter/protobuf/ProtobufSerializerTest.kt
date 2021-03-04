package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.action.ActionResponseMessage
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter
import kotlinx.collections.immutable.persistentListOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

internal class ProtobufSerializerTest {

    @Test
    fun `Serialize and parse a request`() {
        val receivedAt = Instant.now()

        val request = AdapterActionRequest(
            requestId = "request-1",
            actionId = ActionId("door", "open"),
            timeBudget = Duration.ofSeconds(3),
            receivedAt = receivedAt,
            attachments = listOf(
                AdapterAttachment.NorwegianFodselsnummer("30098602247")
            )
        )

        val serializedRequest = ProtobufSerializer.serialize(request)
        val parsedRequest = ProtobufParser.parse(serializedRequest, receivedAt)

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
                link = URI.create("https://example.com/opening-hours")
            )
        )

        val serializedResponse = ProtobufSerializer.serialize(response)
        val parsedResponse = ProtobufParser.parse(serializedResponse)

        assertEquals(response, parsedResponse)
        assertEquals(Adapter.ActionResponse.Status.DENIED, parsedResponse.status)
    }

}