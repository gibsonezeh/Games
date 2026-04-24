package com.gibson.games.multiplayer

import android.os.Handler
import android.os.Looper
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

class WifiConnectionManager {
    companion object {
        const val DEFAULT_PORT = 50555
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private var serverSocket: ServerSocket? = null
    private var serverThread: Thread? = null
    private var clientThread: Thread? = null

    fun getLocalIpAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()

            for (networkInterface in interfaces) {
                val addresses = networkInterface.inetAddresses

                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "Unknown IP"
                    }
                }
            }

            "Unknown IP"
        } catch (_: Exception) {
            "Unknown IP"
        }
    }

    fun startServer(
        port: Int = DEFAULT_PORT,
        onConnected: (Socket) -> Unit,
        onError: (String) -> Unit
    ) {
        stopServer()

        serverThread = Thread {
            try {
                val server = ServerSocket(port)
                serverSocket = server

                val socket = server.accept()

                mainHandler.post {
                    onConnected(socket)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    onError("Failed to host WiFi session: ${e.message ?: "unknown error"}")
                }
            }
        }.also { it.start() }
    }

    fun connectToHost(
        hostIp: String,
        port: Int = DEFAULT_PORT,
        onConnected: (Socket) -> Unit,
        onError: (String) -> Unit
    ) {
        stopClient()

        clientThread = Thread {
            try {
                val socket = Socket(hostIp.trim(), port)

                mainHandler.post {
                    onConnected(socket)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    onError("Failed to connect to WiFi host: ${e.message ?: "unknown error"}")
                }
            }
        }.also { it.start() }
    }

    fun stopServer() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }

        serverSocket = null
        serverThread?.interrupt()
        serverThread = null
    }

    fun stopClient() {
        clientThread?.interrupt()
        clientThread = null
    }

    fun closeAll() {
        stopServer()
        stopClient()
    }
}
