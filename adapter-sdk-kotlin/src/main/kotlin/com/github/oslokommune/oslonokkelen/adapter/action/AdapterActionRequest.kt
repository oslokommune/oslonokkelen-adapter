package com.github.oslokommune.oslonokkelen.adapter.action

import java.time.Duration
import java.time.Instant
import java.util.UUID

data class AdapterActionRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val actionId: ActionId,
    val timeBudget: Duration,
    val attachments: List<AdapterAttachment>
) {

    inline fun <reified A : AdapterAttachment> require(): A {
        return attachments.filterIsInstance<A>().firstOrNull()
            ?: throw InvalidAdapterRequestException("Missing attachment: ${A::class.simpleName}")
    }
}
