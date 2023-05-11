package com.github.oslokommune.oslonokkelen.adapter.thing


data class ThingDescription(
    val id: ThingId,
    val description: String,
    val adminRole: String,
    val tags: Set<String> = emptySet()
)