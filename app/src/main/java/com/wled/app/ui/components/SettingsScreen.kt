package com.wled.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import android.content.pm.PackageManager
import com.wled.app.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: AppTheme,
    autoDiscovery: Boolean,
    showHiddenDevices: Boolean,
    showOfflineLast: Boolean,
    wledVersion: String,
    onThemeChange: (AppTheme) -> Unit,
    onAutoDiscoveryChange: (Boolean) -> Unit,
    onShowHiddenDevicesChange: (Boolean) -> Unit,
    onShowOfflineLastChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showThemePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    
    val appVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "0.4.1"
        } catch (e: PackageManager.NameNotFoundException) {
            "0.4.1"
        }
    }

    var localAutoDiscovery by remember { mutableStateOf(autoDiscovery) }
    var localShowHiddenDevices by remember { mutableStateOf(showHiddenDevices) }
    var localShowOfflineLast by remember { mutableStateOf(showOfflineLast) }

    val isDark = currentTheme == AppTheme.DARK || currentTheme == AppTheme.OLED || (currentTheme == AppTheme.SYSTEM && isSystemInDarkTheme())
    val darkLedColor = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.primary, Color.Black, 0.65f)
    
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = if (isDark) darkLedColor else Color.White,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        checkedBorderColor = Color.Transparent,
        uncheckedThumbColor = if (isDark) Color.LightGray else Color.DarkGray,
        uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f),
        uncheckedBorderColor = Color.Gray
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "APPAREILS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        localAutoDiscovery = !localAutoDiscovery
                        onAutoDiscoveryChange(localAutoDiscovery)
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recherche automatique",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = localAutoDiscovery,
                    onCheckedChange = {
                        localAutoDiscovery = it
                        onAutoDiscoveryChange(it)
                    },
                    colors = switchColors
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        localShowHiddenDevices = !localShowHiddenDevices
                        onShowHiddenDevicesChange(localShowHiddenDevices)
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Affichers les appareils cachés",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = localShowHiddenDevices,
                    onCheckedChange = {
                        localShowHiddenDevices = it
                        onShowHiddenDevicesChange(it)
                    },
                    colors = switchColors
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        localShowOfflineLast = !localShowOfflineLast
                        onShowOfflineLastChange(localShowOfflineLast)
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Appareils hors-ligne en fin de liste",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = localShowOfflineLast,
                    onCheckedChange = {
                        localShowOfflineLast = it
                        onShowOfflineLastChange(it)
                    },
                    colors = switchColors
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "APPARENCE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemePicker = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Thème",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = currentTheme.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "À PROPOS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Whaled",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "v$appVersion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri("https://github.com/wled/WLED") }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Basé sur WLED",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "v$wledVersion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri("https://github.com/TraynerSW") }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Développé par",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Trayner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showThemePicker) {
        ModalBottomSheet(
            onDismissRequest = { showThemePicker = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Choisir un thème",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                AppTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onThemeChange(theme)
                                scope.launch {
                                    sheetState.hide()
                                    showThemePicker = false
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = theme.label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (theme == currentTheme) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
