package com.github.oslokommune.oslonokkelen.adapter.error

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentHashMap

data class ErrorCodes(val codes: PersistentMap<ErrorCode, ErrorCodeDescription> = persistentMapOf()) {

    constructor(vararg codes: ErrorCodeDescription) : this(codes.associateBy { it.code }.toPersistentHashMap())

    operator fun plus(description: ErrorCodeDescription): ErrorCodes {
        return if (codes[description.code] == description) {
            this
        } else {
            copy(
                codes = codes.put(description.code, description)
            )
        }
    }

    operator fun minus(code: ErrorCode): ErrorCodes {
        return if (codes.containsKey(code)) {
            copy(
                codes = codes.remove(code)
            )
        } else {
            this
        }
    }

}