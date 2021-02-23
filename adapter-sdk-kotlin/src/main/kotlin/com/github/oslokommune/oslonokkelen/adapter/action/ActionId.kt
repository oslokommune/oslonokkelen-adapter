package com.github.oslokommune.oslonokkelen.adapter.action

import com.github.oslokommune.oslonokkelen.adapter.thing.ThingId


data class ActionId(val thingId: ThingId, val value: String) {

    constructor(thingId: String, actionId: String) : this(ThingId(thingId), actionId)

    val short = "${thingId.value}/$value"

    override fun toString(): String {
        return "action: ${thingId.value}/$value"
    }
}
