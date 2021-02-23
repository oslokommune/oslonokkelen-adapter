package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

internal class ProtobufSerializerTest {

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


}