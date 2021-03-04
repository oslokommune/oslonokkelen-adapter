package com.github.oslokommune.oslonokkelen.adapter.error

/**
 *
 */
data class ErrorCodeDescription(val code: ErrorCode, val description: String) {

    constructor(code: String, description: String) : this(ErrorCode(code), description)

    init {
        if (description.length > DESCRIPTION_MAX_LENGTH) {
            throw IllegalArgumentException("Description of '${code.code}' is too long: $description")
        }
    }

    companion object {

        const val DESCRIPTION_MAX_LENGTH = 1000


    }

}