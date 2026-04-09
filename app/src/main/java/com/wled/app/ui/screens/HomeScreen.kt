package com.wled.app.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.wled.app.R
import com.wled.app.data.model.WledDevice
import com.wled.app.data.repository.WledRepository
import com.wled.app.ui.components.DeviceCard
import com.wled.app.ui.components.EditDeviceDialog
import com.wled.app.ui.components.getDeviceColor
import com.wled.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: WledRepository,
    currentTheme: AppTheme,
    onNavigateToSettings: () -> Unit,
    onNavigateToWled: (WledDevice) -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }
    
    val devices by repository.devices.collectAsState()
    val deviceStates by repository.deviceStates.collectAsState()
    val realtimeColors by repository.realtimeColors.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var ipInput by remember { mutableStateOf("") }
    var deviceToEdit by remember { mutableStateOf<WledDevice?>(null) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.wled_logo_akemi),
                        contentDescription = "WLED",
                        modifier = Modifier.height(55.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(25.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(25.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,

                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { repository.discoverDevices() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (devices.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Aucun appareil WLED trouvé",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { repository.discoverDevices() }) {
                            Text("Rechercher")
                        }
                    }
                }
            } else {
                val sortedDevices = remember(devices, repository.showOfflineLast) {
                    devices.sortedWith(
                        compareBy<WledDevice> { if (repository.showOfflineLast && !it.isOnline) 1 else 0 }
                        .thenBy { it.effectiveIcon.ordinal }
                        .thenBy { it.name.lowercase() }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(sortedDevices, key = { it.ip }) { device ->
                        val state = deviceStates[device.ip]
                        val realtimeColor = realtimeColors[device.ip]
                        val savedColor = device.color
                        val baseColor = getDeviceColor(state, realtimeColor, savedColor)

                        DeviceCard(
                            device = device,
                            state = state,
                            realtimeColor = realtimeColor,
                            baseColor = baseColor,
                            isOn = state?.isOn ?: device.savedIsOn,
                            isOnline = device.isOnline,
                            theme = currentTheme,
                            onCardClick = {
                                repository.selectDevice(device)
                                onNavigateToWled(device)
                            },
                            onToggle = {
                                repository.selectDevice(device)
                                repository.togglePower()
                                vibrator?.let { v ->
                                    try {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                                            if (v.hasVibrator()) {
                                                v.vibrate(VibrationEffect.createOneShot(10, 100))
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Vibration not available
                                    }
                                }
                            },
                            onBrightnessChange = { brightness, isDragging ->
                                repository.selectDevice(device)
                                repository.setBrightness(brightness, isDragging)
                            },
                            onHapticFeedback = {
                                vibrator?.let { v ->
                                    try {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                                            if (v.hasVibrator()) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                    v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                                                } else {
                                                    v.vibrate(VibrationEffect.createOneShot(3, 150))
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Vibration not available
                                    }
                                }
                            },
                            onLongPress = {
                                deviceToEdit = device
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Ajouter un appareil") },
            text = {
                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("Adresse IP") },
                    placeholder = { Text("192.168.1.100") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ipInput.isNotBlank()) {
                            repository.addManualDevice(ipInput.trim())
                            showAddDialog = false
                            ipInput = ""
                        }
                    }
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    deviceToEdit?.let { device ->
        val realtimeColor = realtimeColors[device.ip]
        val baseColor = getDeviceColor(deviceStates[device.ip], realtimeColor, device.color)
        
        EditDeviceDialog(
            currentName = device.name,
            currentIcon = device.effectiveIcon,
            baseColor = baseColor,
            onDismiss = { deviceToEdit = null },
            onConfirm = { newName, newIcon ->
                repository.updateDeviceName(device.ip, newName)
                repository.updateDeviceIcon(device.ip, newIcon)
                deviceToEdit = null
            }
        )
    }
}
