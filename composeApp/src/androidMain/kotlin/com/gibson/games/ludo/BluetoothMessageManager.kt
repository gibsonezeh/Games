package com.gibson.games.ludo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

class BluetoothMessageManager {
    private var listenScope: CoroutineScope? = null
    private var listenJob: Job? = null

    fun startListening(
        onMessage: (String) -> Unit,
        onDisconnected: (() -> Unit)? = null
    ) {
        stop()

        val socket = BluetoothSessionHolder.socket ?: return
        val scope = CoroutineScope(Dispatchers.IO)
        listenScope = scope

        listenJob = scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
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
        val socket = BluetoothSessionHolder.socket ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val writer = PrintWriter(socket.outputStream, true)
                writer.println(message)
                writer.flush()
            } catch (_: Exception) {
            }
        }
    }

    fun stop() {
        listenJob?.cancel()
        listenJob = null
        listenScope?.cancel()
        listenScope = null
    }
}
