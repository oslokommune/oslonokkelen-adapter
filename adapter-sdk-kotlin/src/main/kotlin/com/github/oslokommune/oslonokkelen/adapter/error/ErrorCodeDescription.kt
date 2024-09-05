package com.github.oslokommune.oslonokkelen.adapter.error

import com.github.oslokommune.oslonokkelen.adapter.action.ActionResponseMessage
import com.github.oslokommune.oslonokkelen.adapter.action.AdapterAttachment
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorType.DENIED
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorType.PERMANENT_ERROR
import com.github.oslokommune.oslonokkelen.adapter.error.ErrorType.TEMPORARY_ERROR

/**
 *
 */
data class ErrorCodeDescription(
    val code: ErrorCode,
    val description: String,
    val type: ErrorType = PERMANENT_ERROR
) {

    fun createActionResponse(trouble: Throwable) : ActionResponseMessage {
        return createActionResponse("${trouble.javaClass.canonicalName}: ${trouble.message}")
    }

    fun createActionResponse(debugMessage: String = description): ActionResponseMessage {
        return ActionResponseMessage(
            when (type) {
                DENIED -> {
                    AdapterAttachment.DeniedReason(
                        code = code.code,
                        debugMessage = debugMessage
                    )
                }
                PERMANENT_ERROR -> {
                    AdapterAttachment.ErrorDescription(
                        debugMessage = debugMessage,
                        permanent = true,
                        code = code.code,
                        errorSource = null
                    )
                }
                TEMPORARY_ERROR -> {
                    AdapterAttachment.ErrorDescription(
                        debugMessage = debugMessage,
                        permanent = false,
                        code = code.code,
                        errorSource = null
                    )
                }
            }
        )
    }

    constructor(code: String, description: String, type: ErrorType = PERMANENT_ERROR) : this(
        ErrorCode(code),
        description,
        type
    )

    init {
        if (description.length > DESCRIPTION_MAX_LENGTH) {
            throw IllegalArgumentException("Description of '${code.code}' is too long: $description")
        }
    }


    companion object {

        const val DESCRIPTION_MAX_LENGTH = 1000

    }

}