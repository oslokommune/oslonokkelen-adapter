package com.github.oslokommune.oslonokkelen.adapter.thing

import java.net.URI
import java.time.Duration


data class ThingDescription(
    val id: ThingId,
    val description: String,
    val adminRole: String,
    val tags: Set<String> = emptySet(),
    val link: URI? = null,
    val secondsWithoutMessageBeforeAlert: Duration? = null
)