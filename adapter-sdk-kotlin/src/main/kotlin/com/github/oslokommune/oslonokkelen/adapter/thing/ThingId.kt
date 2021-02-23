package com.github.oslokommune.oslonokkelen.adapter.thing

import com.github.oslokommune.oslonokkelen.adapter.action.ActionId

data class ThingId(val value: String) {

    fun createActionId(actionId: String): ActionId {
        return ActionId(this, actionId)
    }

    override fun toString(): String {
        return "thing: $value"
    }
}
