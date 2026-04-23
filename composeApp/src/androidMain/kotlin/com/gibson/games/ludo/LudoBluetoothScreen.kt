package com.gibson.games.ludo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.lazy.items
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

private enum class BluetoothLobbyState {
    MENU,
    HOSTING,
    JOINING,
    CONNECTED,
    ERROR
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
fun LudoBluetoothScreen(
    onBackClicked: () -> Unit,
    onConnected: (remoteName: String, isHost: Boolean) -> Unit
) {
    val context = LocalContext.current
    val manager = remember { ClassicBluetoothManager(context) }

    var lobbyState by remember { mutableStateOf(BluetoothLobbyState.MENU) }
    var statusMessage by remember { mutableStateOf("Choose how to connect.") }
    var connectedDeviceName by remember { mutableStateOf("Bluetooth Player") }
    var isHost by remember { mutableStateOf(false) }

    val discoveredDevices = remember { mutableStateListOf<BluetoothDevice>() }
    val pairedDevices = remember { mutableStateListOf<BluetoothDevice>() }

    fun deviceLabel(device: BluetoothDevice): String {
        val name = try {
            device.name
        } catch (_: SecurityException) {
            null
        }
        return if (!name.isNullOrBlank()) name else device.address
    }

    fun refreshPairedDevices() {
        pairedDevices.clear()
        pairedDevices.addAll(manager.getPairedDevices())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val allGranted = grants.values.all { it }
        if (allGranted) {
            refreshPairedDevices()
            statusMessage = "Bluetooth permissions granted."
        } else {
            lobbyState = BluetoothLobbyState.ERROR
            statusMessage = "Bluetooth permissions are required."
        }
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (manager.isBluetoothEnabled()) {
            refreshPairedDevices()
            statusMessage = "Bluetooth enabled."
        } else {
            lobbyState = BluetoothLobbyState.ERROR
            statusMessage = "Bluetooth must be enabled."
        }
    }

    val discoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Hosting can continue even if user closes the dialog early,
        // but discoverability helps nearby devices find the host.
    }

    fun ensureBluetoothReady(onReady: () -> Unit) {
        if (!manager.isBluetoothSupported()) {
            lobbyState = BluetoothLobbyState.ERROR
            statusMessage = "Bluetooth is not supported on this device."
            return
        }

        val permissions = manager.requiredRuntimePermissions()
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
            return
        }

        if (!manager.isBluetoothEnabled()) {
            enableBluetoothLauncher.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
            return
        }

        onReady()
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

                        if (device != null && discoveredDevices.none { it.address == device.address }) {
                            discoveredDevices.add(device)
                        }
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        if (lobbyState == BluetoothLobbyState.JOINING) {
                            statusMessage = if (discoveredDevices.isEmpty() && pairedDevices.isEmpty()) {
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
            ContextCompat.RECEIVER_EXPORTED
        )

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
            manager.closeAll()
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
                            text = "📶",
                            fontSize = 40.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Bluetooth Ludo",
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
                BluetoothLobbyState.MENU -> {
                    item {
                        Button(
                            onClick = {
                                ensureBluetoothReady {
                                    isHost = true
                                    lobbyState = BluetoothLobbyState.HOSTING
                                    statusMessage = "Hosting game. Waiting for player..."

                                    val discoverableIntent = Intent(
                                        BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
                                    ).apply {
                                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
                                    }
                                    discoverableLauncher.launch(discoverableIntent)

                                    manager.startServer(
                                        onConnected = { socket ->
                                            connectedDeviceName = socket.remoteDevice?.name
                                                ?: socket.remoteDevice?.address
                                                ?: "Bluetooth Player"
                                            BluetoothSessionHolder.socket = socket
                                            lobbyState = BluetoothLobbyState.CONNECTED
                                            statusMessage = "Connected to $connectedDeviceName"
                                        },
                                        onError = { message ->
                                            lobbyState = BluetoothLobbyState.ERROR
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
                                text = "Host Game",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                ensureBluetoothReady {
                                    isHost = false
                                    lobbyState = BluetoothLobbyState.JOINING
                                    discoveredDevices.clear()
                                    refreshPairedDevices()
                                    statusMessage = "Scanning nearby devices..."
                                    manager.startDiscovery()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Text(
                                text = "Join Game",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                BluetoothLobbyState.HOSTING -> {
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
                                manager.closeAll()
                                lobbyState = BluetoothLobbyState.MENU
                                statusMessage = "Choose how to connect."
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

                BluetoothLobbyState.JOINING -> {
                    if (pairedDevices.isNotEmpty()) {
                        item {
                            Text(
                                text = "Paired Devices",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(pairedDevices, key = { it.address }) { device ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        statusMessage = "Connecting to ${deviceLabel(device)}..."
                                        manager.connectToDevice(
                                            device = device,
                                            onConnected = { socket ->
                                                connectedDeviceName = socket.remoteDevice?.name
                                                    ?: socket.remoteDevice?.address
                                                    ?: deviceLabel(device)
                                                BluetoothSessionHolder.socket = socket
                                                lobbyState = BluetoothLobbyState.CONNECTED
                                                statusMessage = "Connected to $connectedDeviceName"
                                            },
                                            onError = { message ->
                                                lobbyState = BluetoothLobbyState.ERROR
                                                statusMessage = message
                                            }
                                        )
                                    },
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
                                        text = deviceLabel(device),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = device.address,
                                        color = Color(0xFF6B7280),
                                        fontSize = 12.sp
                                    )
                                }
                            }
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

                        items(discoveredDevices, key = { it.address }) { device ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        statusMessage = "Connecting to ${deviceLabel(device)}..."
                                        manager.connectToDevice(
                                            device = device,
                                            onConnected = { socket ->
                                                connectedDeviceName = socket.remoteDevice?.name
                                                    ?: socket.remoteDevice?.address
                                                    ?: deviceLabel(device)
                                                BluetoothSessionHolder.socket = socket
                                                lobbyState = BluetoothLobbyState.CONNECTED
                                                statusMessage = "Connected to $connectedDeviceName"
                                            },
                                            onError = { message ->
                                                lobbyState = BluetoothLobbyState.ERROR
                                                statusMessage = message
                                            }
                                        )
                                    },
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
                                        text = deviceLabel(device),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = device.address,
                                        color = Color(0xFF6B7280),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                discoveredDevices.clear()
                                refreshPairedDevices()
                                statusMessage = "Scanning nearby devices..."
                                manager.startDiscovery()
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
                                manager.cancelDiscovery()
                                lobbyState = BluetoothLobbyState.MENU
                                statusMessage = "Choose how to connect."
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

                BluetoothLobbyState.CONNECTED -> {
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
                            onClick = {
                                onConnected(connectedDeviceName, isHost)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Text(
                                text = "Continue to Game",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                BluetoothLobbyState.ERROR -> {
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
                                lobbyState = BluetoothLobbyState.MENU
                                statusMessage = "Choose how to connect."
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
                        manager.closeAll()
                        BluetoothSessionHolder.clear()
                        onBackClicked()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.18f)
                    )
                ) {
                    Text(
                        text = "Exit Bluetooth",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
