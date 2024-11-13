package com.github.oslokommune.oslonokkelen.adapter.protobuf

import com.github.oslokommune.oslonokkelen.adapter.action.ActionId
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterActionRequest
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter
import com.github.oslokommune.oslonokkelen.adapter.tokens.generator.BackendTokenGeneratorTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Duration

internal class ProtobufParserTest {

    @Test
    fun `Parse execute token from jwt`() {
        val request = AdapterActionRequest(
            actionId = ActionId("door", "open"),
            requestId = "r-1",
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
        val restoredRequest = ProtobufParser.parse(protobufRequest)

        assertEquals(request, restoredRequest)
    }

    @Test
    fun `Ignores entire message if link is not valid`() {
        val parsed = ProtobufParser.parseAttachment(
            Adapter.Attachment.newBuilder()
                .setEndUserMessage(
                    Adapter.Attachment.EndUserMessage.newBuilder()
                        .setMessage(
                            Adapter.TextContent.newBuilder()
                                .setMessage("Halla")
                                .build()
                        )
                        .setLink("None")
                        .setLinkName("None")
                        .build()
                )
                .build()
        )

        assertNull(parsed)
    }

    @Test
    fun `Does not ignore link with http prefix`() {
        val parsed = ProtobufParser.parseAttachment(
            Adapter.Attachment.newBuilder()
                .setEndUserMessage(
                    Adapter.Attachment.EndUserMessage.newBuilder()
                        .setMessage(
                            Adapter.TextContent.newBuilder()
                                .setMessage("Halla")
                                .build()
                        )
                        .setLink("http://vg.no")
                        .setLinkName("vg")
                        .build()
                )
                .build()
        )

        val expected = AdapterAttachment.EndUserMessage(
            message = "Halla",
            link = AdapterAttachment.Link(
                link = URI.create("http://vg.no"),
                name = "vg"
            )
        )

        assertEquals(expected, parsed)
    }

    @Test
    fun `Does not ignore link with https prefix`() {
        val parsed = ProtobufParser.parseAttachment(
            Adapter.Attachment.newBuilder()
                .setEndUserMessage(
                    Adapter.Attachment.EndUserMessage.newBuilder()
                        .setMessage(
                            Adapter.TextContent.newBuilder()
                                .setMessage("Halla")
                                .build()
                        )
                        .setLink("https://vg.no")
                        .setLinkName("vg")
                        .build()
                )
                .build()
        )

        val expected = AdapterAttachment.EndUserMessage(
            message = "Halla",
            link = AdapterAttachment.Link(
                link = URI.create("https://vg.no"),
                name = "vg"
            )
        )

        assertEquals(expected, parsed)
    }

    @Test
    fun `Parse message without link`() {
        val parsed = ProtobufParser.parseAttachment(
            Adapter.Attachment.newBuilder()
                .setEndUserMessage(
                    Adapter.Attachment.EndUserMessage.newBuilder()
                        .setMessage(
                            Adapter.TextContent.newBuilder()
                                .setMessage("Halla")
                                .build()
                        )
                        .build()
                )
                .build()
        )

        val expected = AdapterAttachment.EndUserMessage(
            message = "Halla"
        )

        assertEquals(expected, parsed)
    }

}