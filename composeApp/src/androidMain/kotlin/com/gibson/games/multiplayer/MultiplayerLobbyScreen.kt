package com.gibson.games.multiplayer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private enum class MultiplayerLobbyState {
    MENU,
    BLUETOOTH_HOSTING,
    BLUETOOTH_JOINING,
    WIFI_HOSTING,
    WIFI_JOINING,
    ONLINE_HOSTING,
    ONLINE_JOINING,
    CONNECTED,
    ERROR
}

@Composable
fun MultiplayerLobbyScreen(
    onBackClicked: () -> Unit,
    onConnected: () -> Unit
) {
    val context = LocalContext.current
    val bluetoothManager = remember { BluetoothConnectionManager(context) }
    val wifiManager = remember { WifiConnectionManager() }
    val onlineManager = remember { OnlineConnectionManager() }

    var lobbyState by remember { mutableStateOf(MultiplayerLobbyState.MENU) }
    var statusMessage by remember { mutableStateOf("Choose a multiplayer connection.") }
    var connectedDeviceName by remember { mutableStateOf("Player") }
    var hostIp by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }

    val discoveredDevices = remember { mutableStateListOf<BluetoothDevice>() }
    val pairedDevices = remember { mutableStateListOf<BluetoothDevice>() }

    fun safeDeviceName(device: BluetoothDevice): String {
        return try {
            device.name ?: "Unknown Device"
        } catch (_: SecurityException) {
            "Unknown Device"
        }
    }

    fun safeDeviceAddress(device: BluetoothDevice): String {
        return try {
            device.address ?: "Hidden address"
        } catch (_: SecurityException) {
            "Hidden address"
        }
    }

    fun deviceLabel(device: BluetoothDevice): String {
        val name = safeDeviceName(device)
        return if (name.isNotBlank() && name != "Unknown Device") name else safeDeviceAddress(device)
    }

    fun sameDevice(a: BluetoothDevice, b: BluetoothDevice): Boolean {
        return safeDeviceAddress(a) == safeDeviceAddress(b)
    }

    fun refreshPairedDevices() {
        pairedDevices.clear()
        pairedDevices.addAll(bluetoothManager.getPairedDevices())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            refreshPairedDevices()
            statusMessage = "Bluetooth permissions granted."
            lobbyState = MultiplayerLobbyState.MENU
        } else {
            lobbyState = MultiplayerLobbyState.ERROR
            statusMessage = "Bluetooth permissions are required."
        }
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (bluetoothManager.isBluetoothEnabled()) {
            refreshPairedDevices()
            statusMessage = "Bluetooth enabled."
            lobbyState = MultiplayerLobbyState.MENU
        } else {
            lobbyState = MultiplayerLobbyState.ERROR
            statusMessage = "Bluetooth must be enabled."
        }
    }

    val discoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    fun ensureBluetoothReady(onReady: () -> Unit) {
        if (!bluetoothManager.isBluetoothSupported()) {
            lobbyState = MultiplayerLobbyState.ERROR
            statusMessage = "Bluetooth is not supported on this device."
            return
        }

        val missing = bluetoothManager.requiredRuntimePermissions().filter {
            ContextCompat.checkSelfPermission(context, it) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
            return
        }

        if (!bluetoothManager.isBluetoothEnabled()) {
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        onReady()
    }

    fun connectToBluetoothDevice(device: BluetoothDevice) {
        val label = deviceLabel(device)
        statusMessage = "Connecting to $label..."

        bluetoothManager.connectToDevice(
            device = device,
            onConnected = { socket ->
                connectedDeviceName = try {
                    socket.remoteDevice?.name ?: socket.remoteDevice?.address ?: label
                } catch (_: SecurityException) {
                    label
                }

                MultiplayerSession.setBluetoothConnection(
                    socket = socket,
                    role = MultiplayerRole.JOINER,
                    remoteName = connectedDeviceName
                )

                lobbyState = MultiplayerLobbyState.CONNECTED
                statusMessage = "Connected to $connectedDeviceName"
            },
            onError = { message ->
                lobbyState = MultiplayerLobbyState.ERROR
                statusMessage = message
            }
        )
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                intent.getParcelableExtra(
                                    BluetoothDevice.EXTRA_DEVICE,
                                    BluetoothDevice::class.java
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            }

                        if (device != null && discoveredDevices.none { sameDevice(it, device) }) {
                            discoveredDevices.add(device)
                        }
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        if (lobbyState == MultiplayerLobbyState.BLUETOOTH_JOINING) {
                            statusMessage =
                                if (discoveredDevices.isEmpty() && pairedDevices.isEmpty()) {
                                    "No nearby Bluetooth devices found."
                                } else {
                                    "Tap a device to connect."
                                }
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }

            bluetoothManager.closeAll()
            wifiManager.closeAll()
            onlineManager.stopListening()
        }
    }

    LaunchedEffect(Unit) {
        ensureBluetoothReady {
            refreshPairedDevices()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B4FAF),
                        Color(0xFF1D4ED8),
                        Color(0xFF2563EB)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎮", fontSize = 40.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Multiplayer",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = statusMessage,
                            color = Color.White.copy(alpha = 0.90f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            when (lobbyState) {
                MultiplayerLobbyState.MENU -> {
                    item {
                        MultiplayerButton(
                            text = "Bluetooth Host",
                            color = Color(0xFF06B6D4),
                            onClick = {
                                ensureBluetoothReady {
                                    lobbyState = MultiplayerLobbyState.BLUETOOTH_HOSTING
                                    statusMessage = "Hosting Bluetooth session. Waiting for player..."

                                    discoverableLauncher.launch(
                                        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
                                        }
                                    )

                                    bluetoothManager.startServer(
                                        onConnected = { socket ->
                                            connectedDeviceName = try {
                                                socket.remoteDevice?.name
                                                    ?: socket.remoteDevice?.address
                                                    ?: "Player"
                                            } catch (_: SecurityException) {
                                                "Player"
                                            }

                                            MultiplayerSession.setBluetoothConnection(
                                                socket = socket,
                                                role = MultiplayerRole.HOST,
                                                remoteName = connectedDeviceName
                                            )

                                            lobbyState = MultiplayerLobbyState.CONNECTED
                                            statusMessage = "Connected to $connectedDeviceName"
                                        },
                                        onError = { message ->
                                            lobbyState = MultiplayerLobbyState.ERROR
                                            statusMessage = message
                                        }
                                    )
                                }
                            }
                        )
                    }

                    item {
                        MultiplayerButton(
                            text = "Bluetooth Join",
                            color = Color(0xFF10B981),
                            onClick = {
                                ensureBluetoothReady {
                                    lobbyState = MultiplayerLobbyState.BLUETOOTH_JOINING
                                    discoveredDevices.clear()
                                    refreshPairedDevices()
                                    statusMessage = "Scanning nearby Bluetooth devices..."
                                    bluetoothManager.startDiscovery()
                                }
                            }
                        )
                    }

                    item {
                        MultiplayerButton(
                            text = "WiFi Host",
                            color = Color(0xFFF59E0B),
                            onClick = {
                                val ip = wifiManager.getLocalIpAddress()
                                lobbyState = MultiplayerLobbyState.WIFI_HOSTING
                                statusMessage = "WiFi hosting started. Connect using IP: $ip"
                                connectedDeviceName = "WiFi Player"

                                wifiManager.startServer(
                                    onConnected = { socket ->
                                        MultiplayerSession.setWifiConnection(
                                            socket = socket,
                                            role = MultiplayerRole.HOST,
                                            remoteName = "WiFi Player"
                                        )

                                        lobbyState = MultiplayerLobbyState.CONNECTED
                                        statusMessage = "WiFi player connected"
                                    },
                                    onError = { message ->
                                        lobbyState = MultiplayerLobbyState.ERROR
                                        statusMessage = message
                                    }
                                )
                            }
                        )
                    }

                    item {
                        MultiplayerButton(
                            text = "WiFi Join",
                            color = Color(0xFFA855F7),
                            onClick = {
                                lobbyState = MultiplayerLobbyState.WIFI_JOINING
                                statusMessage = "Enter host IP address."
                            }
                        )
                    }

                    item {
                        MultiplayerButton(
                            text = "Online Host",
                            color = Color(0xFFEF4444),
                            onClick = {
                                lobbyState = MultiplayerLobbyState.ONLINE_HOSTING
                                statusMessage = "Creating online room..."

                                onlineManager.createRoom(
                                    onRoomCreated = { code ->
                                        roomCode = code
                                        connectedDeviceName = "Online Player"
                                        statusMessage = "Room created. Share this code: $code"
                                    },
                                    onError = { message ->
                                        lobbyState = MultiplayerLobbyState.ERROR
                                        statusMessage = message
                                    }
                                )
                            }
                        )
                    }

                    item {
                        MultiplayerButton(
                            text = "Online Join",
                            color = Color(0xFF22C55E),
                            onClick = {
                                roomCode = ""
                                lobbyState = MultiplayerLobbyState.ONLINE_JOINING
                                statusMessage = "Enter online room code."
                            }
                        )
                    }
                }

                MultiplayerLobbyState.BLUETOOTH_HOSTING -> {
                    item {
                        Text(
                            text = "Waiting for Bluetooth player...",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                MultiplayerLobbyState.BLUETOOTH_JOINING -> {
                    if (pairedDevices.isNotEmpty()) {
                        item {
                            Text(
                                "Paired Devices",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        itemsIndexed(pairedDevices) { _, device ->
                            MultiplayerDeviceCard(
                                name = deviceLabel(device),
                                address = safeDeviceAddress(device),
                                onClick = { connectToBluetoothDevice(device) }
                            )
                        }
                    }

                    if (discoveredDevices.isNotEmpty()) {
                        item {
                            Text(
                                "Nearby Devices",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        itemsIndexed(discoveredDevices) { _, device ->
                            MultiplayerDeviceCard(
                                name = deviceLabel(device),
                                address = safeDeviceAddress(device),
                                onClick = { connectToBluetoothDevice(device) }
                            )
                        }
                    }
                }

                MultiplayerLobbyState.WIFI_HOSTING -> {
                    item {
                        Text(
                            text = "WiFi Host IP:\n${wifiManager.getLocalIpAddress()}\n\nPort: ${WifiConnectionManager.DEFAULT_PORT}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Text(
                            text = "Both devices must be on the same WiFi or hotspot network.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                    }
                }

                MultiplayerLobbyState.WIFI_JOINING -> {
                    item {
                        OutlinedTextField(
                            value = hostIp,
                            onValueChange = { hostIp = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Host IP Address") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }

                    item {
                        MultiplayerButton(
                            text = "Connect",
                            color = Color(0xFF10B981),
                            onClick = {
                                if (hostIp.isBlank()) {
                                    statusMessage = "Enter host IP address first."
                                    return@MultiplayerButton
                                }

                                statusMessage = "Connecting to WiFi host..."

                                wifiManager.connectToHost(
                                    hostIp = hostIp,
                                    onConnected = { socket ->
                                        MultiplayerSession.setWifiConnection(
                                            socket = socket,
                                            role = MultiplayerRole.JOINER,
                                            remoteName = "WiFi Host"
                                        )

                                        connectedDeviceName = "WiFi Host"
                                        lobbyState = MultiplayerLobbyState.CONNECTED
                                        statusMessage = "Connected to WiFi host"
                                    },
                                    onError = { message ->
                                        lobbyState = MultiplayerLobbyState.ERROR
                                        statusMessage = message
                                    }
                                )
                            }
                        )
                    }
                }

                MultiplayerLobbyState.ONLINE_HOSTING -> {
                    item {
                        Text(
                            text = "Room Code:\n$roomCode",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    item {
                        Text(
                            text = "Share this code with the other player.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                    }

                    item {
                        MultiplayerButton(
                            text = "Continue",
                            color = Color(0xFF10B981),
                            onClick = onConnected
                        )
                    }
                }

                MultiplayerLobbyState.ONLINE_JOINING -> {
                    item {
                        OutlinedTextField(
                            value = roomCode,
                            onValueChange = { roomCode = it.uppercase() },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Room Code") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters
                            )
                        )
                    }

                    item {
                        MultiplayerButton(
                            text = "Join Room",
                            color = Color(0xFF10B981),
                            onClick = {
                                if (roomCode.isBlank()) {
                                    statusMessage = "Enter room code first."
                                    return@MultiplayerButton
                                }

                                statusMessage = "Joining online room..."

                                onlineManager.joinRoom(
                                    code = roomCode,
                                    onJoined = {
                                        connectedDeviceName = "Online Host"
                                        lobbyState = MultiplayerLobbyState.CONNECTED
                                        statusMessage = "Joined room $roomCode"
                                    },
                                    onError = { message ->
                                        lobbyState = MultiplayerLobbyState.ERROR
                                        statusMessage = message
                                    }
                                )
                            }
                        )
                    }
                }

                MultiplayerLobbyState.CONNECTED -> {
                    item {
                        Text(
                            text = "Connected to $connectedDeviceName",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        MultiplayerButton(
                            text = "Continue",
                            color = Color(0xFF10B981),
                            onClick = onConnected
                        )
                    }
                }

                MultiplayerLobbyState.ERROR -> {
                    item {
                        Text(
                            text = statusMessage,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))

                MultiplayerButton(
                    text = "Back to Multiplayer Menu",
                    color = Color.White.copy(alpha = 0.18f),
                    onClick = {
                        bluetoothManager.closeAll()
                        wifiManager.closeAll()
                        onlineManager.stopListening()
                        MultiplayerSession.clear()
                        lobbyState = MultiplayerLobbyState.MENU
                        statusMessage = "Choose a multiplayer connection."
                    }
                )

                MultiplayerButton(
                    text = "Exit Multiplayer",
                    color = Color.White.copy(alpha = 0.18f),
                    onClick = {
                        bluetoothManager.closeAll()
                        wifiManager.closeAll()
                        onlineManager.stopListening()
                        MultiplayerSession.clear()
                        onBackClicked()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MultiplayerButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MultiplayerDeviceCard(
    name: String,
    address: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = name,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = address,
                color = Color(0xFF6B7280),
                fontSize = 12.sp
            )
        }
    }
}
