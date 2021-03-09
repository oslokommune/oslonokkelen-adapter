package com.github.oslokommune.oslonokkelen.adapter.thing

interface ComparableThingState {

    fun hasSameStateAs(other: ThingState): Boolean

}