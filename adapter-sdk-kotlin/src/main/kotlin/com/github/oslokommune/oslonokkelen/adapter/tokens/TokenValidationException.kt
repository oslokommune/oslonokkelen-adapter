package com.github.oslokommune.oslonokkelen.adapter.tokens

sealed class TokenValidationException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause) {

    class Parsing(cause: Exception) : TokenValidationException("Failed to parse token", cause)

    class Invalid(cause: Exception) : TokenValidationException("Invalid token", cause)

    class Unknown(cause: Exception) : TokenValidationException("Unknown token validation exception", cause)

    class TokenReplayDetectorCapacityExceeded(size: Int)
        : TokenValidationException("Token replay detector capacity exceeded ($size tokens)")

    class TokenReplayDetected(id: String)
        : TokenValidationException("Token $id has already been used")

}