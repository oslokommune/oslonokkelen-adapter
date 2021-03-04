package com.github.oslokommune.oslonokkelen.adapter.error

/**
 *
 */
data class ErrorCodeDescription(val code: String, val description: String) {

    init {
        if (code.length > CODE_MAX_LENGTH) {
            throw IllegalArgumentException("Error code is too long: $code")
        }
        if (description.length > DESCRIPTION_MAX_LENGTH) {
            throw IllegalArgumentException("Description of '$code' is too long: $description")
        }

        val parts = code.split(".")

        for (part in parts) {
            if (!part.matches(validationPattern)) {
                throw IllegalArgumentException("Invalid error code: $code")
            }
        }
    }


    companion object {

        const val CODE_MAX_LENGTH = 100
        const val DESCRIPTION_MAX_LENGTH = 1000

        private val validationPattern = Regex("^[a-z0-9\\-]{1,30}$")

    }

}