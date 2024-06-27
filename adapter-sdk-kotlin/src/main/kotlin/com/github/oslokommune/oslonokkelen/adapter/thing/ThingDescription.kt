package com.github.oslokommune.oslonokkelen.adapter.thing

import java.net.URI
import java.time.Duration


/**
 * @param id Unique within the adapter
 * @param description Short one-line description (example: door 2 on second floor)
 * @param adminRole Not used
 * @param tags Not used
 * @param link Some systems have relevant status pages
 * @param timeWithoutMessageBeforeAlert How often do we expect to get a message from the thing
 */
data class ThingDescription(
    val id: ThingId,
    val description: String,
    val adminRole: String,
    val tags: Set<String> = emptySet(),
    val link: URI? = null,
    val timeWithoutMessageBeforeAlert: Duration? = null
)