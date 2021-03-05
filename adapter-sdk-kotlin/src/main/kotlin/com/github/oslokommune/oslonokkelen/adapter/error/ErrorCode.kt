package com.github.oslokommune.oslonokkelen.adapter.error

data class ErrorCode(val code: String) {

    init {
        if (code.length > CODE_MAX_LENGTH) {
            throw IllegalArgumentException("Error code is too long: $code")
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

        private val validationPattern = Regex("^[a-z0-9\\-]{1,30}$")

    }
}