package com.gibson.games.multiplayer

import android.bluetooth.BluetoothSocket
import java.util.UUID

enum class MultiplayerConnectionType {
    NONE,
    BLUETOOTH,
    WIFI,
    ONLINE
}

enum class MultiplayerRole {
    NONE,
    HOST,
    JOINER
}

data class MultiplayerPeer(
    val playerId: String,
    val displayName: String
)

object MultiplayerSession {
    var connectionType: MultiplayerConnectionType = MultiplayerConnectionType.NONE
    var role: MultiplayerRole = MultiplayerRole.NONE

    var localPlayerId: String = UUID.randomUUID().toString()
    var localDisplayName: String = "You"

    var remotePlayerId: String = UUID.randomUUID().toString()
    var remoteDisplayName: String = "Player"

    var bluetoothSocket: BluetoothSocket? = null

    val isConnected: Boolean
        get() = bluetoothSocket?.isConnected == true ||
            connectionType == MultiplayerConnectionType.WIFI ||
            connectionType == MultiplayerConnectionType.ONLINE

    fun setBluetoothConnection(
        socket: BluetoothSocket,
        role: MultiplayerRole,
        remoteName: String
    ) {
        this.connectionType = MultiplayerConnectionType.BLUETOOTH
        this.role = role
        this.bluetoothSocket = socket
        this.remoteDisplayName = remoteName.ifBlank { "Player" }
    }

    fun clear() {
        try {
            bluetoothSocket?.close()
        } catch (_: Exception) {
        }

        connectionType = MultiplayerConnectionType.NONE
        role = MultiplayerRole.NONE
        localPlayerId = UUID.randomUUID().toString()
        localDisplayName = "You"
        remotePlayerId = UUID.randomUUID().toString()
        remoteDisplayName = "Player"
        bluetoothSocket = null
    }
}
