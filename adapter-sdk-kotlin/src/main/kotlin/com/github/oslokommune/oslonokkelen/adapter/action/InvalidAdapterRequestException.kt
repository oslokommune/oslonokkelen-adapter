package com.github.oslokommune.oslonokkelen.adapter.action


class InvalidAdapterRequestException(override val message: String) : Exception(message) {

    val response: ActionResponseMessage
        get() = ActionResponseMessage.permanentError("request.invalid", message)
}
