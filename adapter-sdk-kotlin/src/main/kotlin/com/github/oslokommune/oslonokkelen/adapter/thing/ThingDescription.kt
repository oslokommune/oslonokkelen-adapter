package com.github.oslokommune.oslonokkelen.adapter.thing

import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter


data class ThingDescription(
    val id: ThingId,
    val description: String,
    val adminRole: String,
    val supportedStateTypes: Set<Adapter.ThingStateType> = emptySet(),
    val tags: Set<String> = emptySet()
)