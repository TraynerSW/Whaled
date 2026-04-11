package com.wled.app

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.core.view.WindowCompat
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.wled.app.data.repository.WledRepository
import com.wled.app.data.service.UdpListenerService
import com.wled.app.ui.screens.HomeScreen
import com.wled.app.ui.components.SettingsScreen
import com.wled.app.ui.screens.WledControlScreen
import com.wled.app.ui.screens.WledWebViewScreen
import com.wled.app.ui.theme.AppTheme
import com.wled.app.ui.theme.ThemeRepository
import com.wled.app.ui.theme.WLEDTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: WledRepository
    private lateinit var themeRepository: ThemeRepository
    private lateinit var udpListener: UdpListenerService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }

        repository = WledRepository(this)
        themeRepository = ThemeRepository(this)

        udpListener = UdpListenerService { ip, brightness, r, g, b ->
            repository.updateRealtimeColor(ip, brightness, AndroidColor.rgb(r, g, b))
        }
        udpListener.start()

        setContent {
            val devices by repository.devices.collectAsState()
            var currentTheme by remember { mutableStateOf(themeRepository.currentTheme) }
            var selectedDeviceIp by rememberSaveable { mutableStateOf<String?>(null) }
            val selectedDevice = remember(devices, selectedDeviceIp) {
                devices.find { it.ip == selectedDeviceIp }
            }
            
            val context = LocalContext.current
            val safeHapticFeedback = remember {
                object : HapticFeedback {
                    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                        try {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
                                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                if (v != null && v.hasVibrator()) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                                    } else {
                                        v.vibrate(VibrationEffect.createOneShot(5, 100))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Ignorer les erreurs de vibration
                        }
                    }
                }
            }

            WLEDTheme(theme = currentTheme, dynamicColor = true) {
                CompositionLocalProvider(LocalHapticFeedback provides safeHapticFeedback) {
                    // Background now purely relies on the Theme definition!
                    val bgModifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.99f)
                            )
                        )
                    )

                    Surface(
                        modifier = bgModifier,
                        color = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) {
                        val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        enterTransition = { fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400)) },
                        exitTransition = { fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 1.05f, animationSpec = tween(400)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 1.05f, animationSpec = tween(400)) },
                        popExitTransition = { fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.95f, animationSpec = tween(400)) }
                    ) {
                        composable("home") {
                            HomeScreen(
                                repository = repository,
                                currentTheme = currentTheme,
                                onNavigateToSettings = {
                                    navController.navigate("settings") { launchSingleTop = true }
                                },
                                onNavigateToWled = { device ->
                                    selectedDeviceIp = device.ip
                                    navController.navigate("wled") { launchSingleTop = true }
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                currentTheme = currentTheme,
                                autoDiscovery = themeRepository.autoDiscovery,
                                showHiddenDevices = themeRepository.showHiddenDevices,
                                showOfflineLast = themeRepository.showOfflineLast,
                                wledVersion = repository.wledVersion.ifEmpty { "Unknown" },
                                onThemeChange = { newTheme ->
                                    currentTheme = newTheme
                                    themeRepository.currentTheme = newTheme
                                },
                                onAutoDiscoveryChange = { themeRepository.autoDiscovery = it },
                                onShowHiddenDevicesChange = { themeRepository.showHiddenDevices = it },
                                onShowOfflineLastChange = { themeRepository.showOfflineLast = it },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("wled") {
                            selectedDevice?.let { device ->
                                WledControlScreen(
                                    device = device,
                                    repository = repository,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    },
                                    onOpenFullWled = {
                                        navController.navigate("wled_full") { launchSingleTop = true }
                                    }
                                )
                            }
                        }
                        composable("wled_full") {
                            selectedDevice?.let { device ->
                                WledWebViewScreen(
                                    device = device,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        udpListener.stop()
    }
}
