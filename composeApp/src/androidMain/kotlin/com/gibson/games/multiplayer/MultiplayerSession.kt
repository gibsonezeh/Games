package com.gibson.games.multiplayer

import android.bluetooth.BluetoothSocket
import java.net.Socket
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

object MultiplayerSession {
    var connectionType: MultiplayerConnectionType = MultiplayerConnectionType.NONE
    var role: MultiplayerRole = MultiplayerRole.NONE

    var localPlayerId: String = UUID.randomUUID().toString()
    var localDisplayName: String = "You"

    var remotePlayerId: String = UUID.randomUUID().toString()
    var remoteDisplayName: String = "Player"

    var bluetoothSocket: BluetoothSocket? = null
    var wifiSocket: Socket? = null

    val isConnected: Boolean
        get() = when (connectionType) {
            MultiplayerConnectionType.BLUETOOTH -> bluetoothSocket?.isConnected == true
            MultiplayerConnectionType.WIFI -> wifiSocket?.isConnected == true && wifiSocket?.isClosed == false
            MultiplayerConnectionType.ONLINE -> true
            MultiplayerConnectionType.NONE -> false
        }

    fun setBluetoothConnection(
        socket: BluetoothSocket,
        role: MultiplayerRole,
        remoteName: String
    ) {
        clearSocketsOnly()
        connectionType = MultiplayerConnectionType.BLUETOOTH
        this.role = role
        bluetoothSocket = socket
        remoteDisplayName = remoteName.ifBlank { "Player" }
    }

    fun setWifiConnection(
        socket: Socket,
        role: MultiplayerRole,
        remoteName: String = "WiFi Player"
    ) {
        clearSocketsOnly()
        connectionType = MultiplayerConnectionType.WIFI
        this.role = role
        wifiSocket = socket
        remoteDisplayName = remoteName.ifBlank { "WiFi Player" }
    }

    private fun clearSocketsOnly() {
        try {
            bluetoothSocket?.close()
        } catch (_: Exception) {
        }

        try {
            wifiSocket?.close()
        } catch (_: Exception) {
        }

        bluetoothSocket = null
        wifiSocket = null
    }

    fun clear() {
        clearSocketsOnly()

        connectionType = MultiplayerConnectionType.NONE
        role = MultiplayerRole.NONE
        localPlayerId = UUID.randomUUID().toString()
        localDisplayName = "You"
        remotePlayerId = UUID.randomUUID().toString()
        remoteDisplayName = "Player"
    }
}
