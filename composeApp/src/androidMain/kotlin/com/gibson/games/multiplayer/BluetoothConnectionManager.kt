package com.gibson.games.multiplayer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.io.IOException
import java.util.UUID

class BluetoothConnectionManager(
    context: Context
) {
    companion object {
        private const val SERVICE_NAME = "KadwiseGamesBluetooth"
        val SERVICE_UUID: UUID = UUID.fromString("f84a6b1d-4c48-4c1b-a8d3-5f6d7e8c9a10")
    }

    private val appContext = context.applicationContext
    private val bluetoothManager =
        appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    val adapter: BluetoothAdapter? = bluetoothManager.adapter

    private val mainHandler = Handler(Looper.getMainLooper())

    private var serverThread: Thread? = null
    private var clientThread: Thread? = null

    fun isBluetoothSupported(): Boolean = adapter != null

    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        val btAdapter = adapter ?: return emptyList()

        return try {
            btAdapter.bondedDevices?.toList().orEmpty()
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery(): Boolean {
        val btAdapter = adapter ?: return false

        return try {
            if (btAdapter.isDiscovering) {
                btAdapter.cancelDiscovery()
            }

            btAdapter.startDiscovery()
        } catch (_: SecurityException) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun cancelDiscovery() {
        val btAdapter = adapter ?: return

        try {
            if (btAdapter.isDiscovering) {
                btAdapter.cancelDiscovery()
            }
        } catch (_: SecurityException) {
        }
    }

    @SuppressLint("MissingPermission")
    fun startServer(
        onConnected: (BluetoothSocket) -> Unit,
        onError: (String) -> Unit
    ) {
        stopServer()

        val btAdapter = adapter
        if (btAdapter == null) {
            onError("Bluetooth is not supported on this device.")
            return
        }

        serverThread = Thread {
            try {
                val serverSocket =
                    btAdapter.listenUsingRfcommWithServiceRecord(
                        SERVICE_NAME,
                        SERVICE_UUID
                    )

                val socket = serverSocket.accept()
                serverSocket.close()

                if (socket != null) {
                    mainHandler.post {
                        onConnected(socket)
                    }
                } else {
                    mainHandler.post {
                        onError("No Bluetooth connection was accepted.")
                    }
                }
            } catch (e: IOException) {
                mainHandler.post {
                    onError("Failed to host Bluetooth session: ${e.message ?: "unknown error"}")
                }
            } catch (_: SecurityException) {
                mainHandler.post {
                    onError("Bluetooth permission denied.")
                }
            }
        }.also { it.start() }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(
        device: BluetoothDevice,
        onConnected: (BluetoothSocket) -> Unit,
        onError: (String) -> Unit
    ) {
        stopClient()

        val btAdapter = adapter
        if (btAdapter == null) {
            onError("Bluetooth is not supported on this device.")
            return
        }

        clientThread = Thread {
            try {
                cancelDiscovery()

                val socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                socket.connect()

                mainHandler.post {
                    onConnected(socket)
                }
            } catch (e: IOException) {
                mainHandler.post {
                    onError("Failed to connect: ${e.message ?: "unknown error"}")
                }
            } catch (_: SecurityException) {
                mainHandler.post {
                    onError("Bluetooth permission denied.")
                }
            }
        }.also { it.start() }
    }

    fun stopServer() {
        serverThread?.interrupt()
        serverThread = null
    }

    fun stopClient() {
        clientThread?.interrupt()
        clientThread = null
    }

    fun closeAll() {
        cancelDiscovery()
        stopServer()
        stopClient()
    }

    fun requiredRuntimePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
}
