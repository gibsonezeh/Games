package com.gibson.games.multiplayer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

class MultiplayerMessageManager {
    private var listenScope: CoroutineScope? = null
    private var listenJob: Job? = null

    private val onlineManager = OnlineConnectionManager()

    fun startListening(
        onMessage: (String) -> Unit,
        onDisconnected: (() -> Unit)? = null
    ) {
        stop()

        if (MultiplayerSession.connectionType == MultiplayerConnectionType.ONLINE) {
            onlineManager.startListening(
                onMessage = onMessage,
                onError = {
                    onDisconnected?.invoke()
                }
            )
            return
        }

        val inputStream = when (MultiplayerSession.connectionType) {
            MultiplayerConnectionType.BLUETOOTH -> MultiplayerSession.bluetoothSocket?.inputStream
            MultiplayerConnectionType.WIFI -> MultiplayerSession.wifiSocket?.getInputStream()
            MultiplayerConnectionType.ONLINE -> null
            MultiplayerConnectionType.NONE -> null
        } ?: return

        val scope = CoroutineScope(Dispatchers.IO)
        listenScope = scope

        listenJob = scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(inputStream))

                while (true) {
                    val line = reader.readLine() ?: break
                    onMessage(line)
                }
            } catch (_: Exception) {
            } finally {
                onDisconnected?.invoke()
            }
        }
    }

    fun send(message: String) {
        when (MultiplayerSession.connectionType) {
            MultiplayerConnectionType.BLUETOOTH -> sendBluetooth(message)
            MultiplayerConnectionType.WIFI -> sendWifi(message)
            MultiplayerConnectionType.ONLINE -> onlineManager.send(message)
            MultiplayerConnectionType.NONE -> Unit
        }
    }

    private fun sendBluetooth(message: String) {
        val socket = MultiplayerSession.bluetoothSocket ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val writer = PrintWriter(socket.outputStream, true)
                writer.println(message)
                writer.flush()
            } catch (_: Exception) {
            }
        }
    }

    private fun sendWifi(message: String) {
        val socket = MultiplayerSession.wifiSocket ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println(message)
                writer.flush()
            } catch (_: Exception) {
            }
        }
    }

    fun stop() {
        onlineManager.stopListening()

        listenJob?.cancel()
        listenJob = null

        listenScope?.cancel()
        listenScope = null
    }
}
