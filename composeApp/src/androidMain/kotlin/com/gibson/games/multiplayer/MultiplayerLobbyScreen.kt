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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private enum class MultiplayerLobbyState {
    MENU,
    HOSTING,
    JOINING,
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

    var lobbyState by remember { mutableStateOf(MultiplayerLobbyState.MENU) }
    var statusMessage by remember { mutableStateOf("Choose a multiplayer connection.") }
    var connectedDeviceName by remember { mutableStateOf("Player") }

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
        return if (name.isNotBlank() && name != "Unknown Device") {
            name
        } else {
            safeDeviceAddress(device)
        }
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
        val allGranted = grants.values.all { it }

        if (allGranted) {
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

        val permissions = bluetoothManager.requiredRuntimePermissions()
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
            return
        }

        if (!bluetoothManager.isBluetoothEnabled()) {
            enableBluetoothLauncher.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
            return
        }

        onReady()
    }

    fun connectToDevice(device: BluetoothDevice) {
        val label = deviceLabel(device)
        statusMessage = "Connecting to $label..."

        bluetoothManager.connectToDevice(
            device = device,
            onConnected = { socket ->
                connectedDeviceName = try {
                    socket.remoteDevice?.name
                        ?: socket.remoteDevice?.address
                        ?: label
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
                        if (lobbyState == MultiplayerLobbyState.JOINING) {
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
                brush = Brush.verticalGradient(
                    colors = listOf(
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
                        Text(
                            text = "🎮",
                            fontSize = 40.sp
                        )

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
                        Button(
                            onClick = {
                                ensureBluetoothReady {
                                    lobbyState = MultiplayerLobbyState.HOSTING
                                    statusMessage = "Hosting Bluetooth session. Waiting for player..."

                                    val discoverableIntent = Intent(
                                        BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
                                    ).apply {
                                        putExtra(
                                            BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                                            120
                                        )
                                    }

                                    discoverableLauncher.launch(discoverableIntent)

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
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF06B6D4)
                            )
                        ) {
                            Text(
                                text = "Bluetooth Host",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                ensureBluetoothReady {
                                    lobbyState = MultiplayerLobbyState.JOINING
                                    discoveredDevices.clear()
                                    refreshPairedDevices()
                                    statusMessage = "Scanning nearby Bluetooth devices..."
                                    bluetoothManager.startDiscovery()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Text(
                                text = "Bluetooth Join",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                MultiplayerLobbyState.HOSTING -> {
                    item {
                        Text(
                            text = "Waiting for another player to connect...",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                bluetoothManager.closeAll()
                                MultiplayerSession.clear()
                                lobbyState = MultiplayerLobbyState.MENU
                                statusMessage = "Choose a multiplayer connection."
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.18f)
                            )
                        ) {
                            Text(
                                text = "Cancel Hosting",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                MultiplayerLobbyState.JOINING -> {
                    if (pairedDevices.isNotEmpty()) {
                        item {
                            Text(
                                text = "Paired Devices",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        itemsIndexed(pairedDevices) { _, device ->
                            MultiplayerDeviceCard(
                                name = deviceLabel(device),
                                address = safeDeviceAddress(device),
                                onClick = { connectToDevice(device) }
                            )
                        }
                    }

                    if (discoveredDevices.isNotEmpty()) {
                        item {
                            Text(
                                text = "Nearby Devices",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        itemsIndexed(discoveredDevices) { _, device ->
                            MultiplayerDeviceCard(
                                name = deviceLabel(device),
                                address = safeDeviceAddress(device),
                                onClick = { connectToDevice(device) }
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                discoveredDevices.clear()
                                refreshPairedDevices()
                                statusMessage = "Scanning nearby Bluetooth devices..."
                                bluetoothManager.startDiscovery()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B)
                            )
                        ) {
                            Text(
                                text = "Scan Again",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                bluetoothManager.cancelDiscovery()
                                MultiplayerSession.clear()
                                lobbyState = MultiplayerLobbyState.MENU
                                statusMessage = "Choose a multiplayer connection."
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.18f)
                            )
                        ) {
                            Text(
                                text = "Back",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                        Button(
                            onClick = onConnected,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Text(
                                text = "Continue",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
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

                    item {
                        Button(
                            onClick = {
                                MultiplayerSession.clear()
                                lobbyState = MultiplayerLobbyState.MENU
                                statusMessage = "Choose a multiplayer connection."
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.18f)
                            )
                        ) {
                            Text(
                                text = "Back",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        bluetoothManager.closeAll()
                        MultiplayerSession.clear()
                        onBackClicked()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.18f)
                    )
                ) {
                    Text(
                        text = "Exit Multiplayer",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
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
