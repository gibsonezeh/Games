package com.gibson.games.ludo

import android.bluetooth.BluetoothSocket

object BluetoothSessionHolder {
    var socket: BluetoothSocket? = null
    var remoteDeviceName: String = "Bluetooth Player"
    var isHost: Boolean = false

    fun clear() {
        try {
            socket?.close()
        } catch (_: Exception) {
        }
        socket = null
        remoteDeviceName = "Bluetooth Player"
        isHost = false
    }
}
