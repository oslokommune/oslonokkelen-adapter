package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.BackendTokenGeneratorTest
import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Duration
import java.time.Instant

internal class ProtobufParserTest {

    @Test
    fun `Parse execute token from jwt`() {
        val timestamp = Instant.now()

        val request = AdapterActionRequest(
            actionId = ActionId("door", "open"),
            requestId = "r-1",
            receivedAt = timestamp,
            timeBudget = Duration.ofMillis(2400),
            attachments = listOf(
                AdapterAttachment.NorwegianFodselsnummer("12345678912")
            )
        )

        val token = BackendTokenGeneratorTest.testGenerator.createActionRequestToken(
            remoteUri = URI.create("https://adapter.example.com"),
            request = request
        )

        val protobufRequest = ProtobufParser.parseActionRequestFromClaims(token.jwtClaimsSet)
        val restoredRequest = ProtobufParser.parse(protobufRequest, timestamp)

        assertEquals(request, restoredRequest)
    }

}