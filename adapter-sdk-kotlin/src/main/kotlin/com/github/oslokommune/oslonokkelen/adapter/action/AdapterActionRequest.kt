package com.github.oslokommune.oslonokkelen.adapter.action

import java.util.UUID
import kotlin.time.Duration

data class AdapterActionRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val actionId: ActionId,
    val timeBudget: Duration,
    val attachments: List<AdapterAttachment>,
    val parameters: Map<String, String>
) {

    inline fun <reified A : AdapterAttachment> require(): A {
        return attachments.filterIsInstance<A>().firstOrNull()
            ?: throw InvalidAdapterRequestException("Missing attachment: ${A::class.simpleName}")
    }

    fun requireParameter(parameterName: String): String {
        return parameters[parameterName]
            ?: throw InvalidAdapterRequestException("Missing parameter: $parameterName")
    }
}
