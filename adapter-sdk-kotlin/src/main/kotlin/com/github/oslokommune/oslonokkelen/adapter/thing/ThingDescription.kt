package com.github.oslokommune.oslonokkelen.adapter.thing

import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter


data class ThingDescription(
    val id: ThingId,
    val description: String,
    val adminRole: String,
    val supportedStateTypes: Set<Adapter.ThingStateType> = emptySet()
) {

    fun remoteConnectionState(
        state: ThingState.RemoteSystemConnection.ConnectionState
    ): ThingState.RemoteSystemConnection {
        return ThingState.RemoteSystemConnection(
            thingId = id,
            state = state,
            timestamp = when (state) {
                is ThingState.RemoteSystemConnection.ConnectionState.Connected -> state.connectedAt
                is ThingState.RemoteSystemConnection.ConnectionState.Disconnected -> state.disconnectedAt
            },
            debugMessage = when (state) {
                is ThingState.RemoteSystemConnection.ConnectionState.Connected -> "Connected"
                is ThingState.RemoteSystemConnection.ConnectionState.Disconnected -> state.reason
            }
        )
    }
}
