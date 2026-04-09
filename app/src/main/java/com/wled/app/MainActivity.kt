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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            var currentTheme by remember { mutableStateOf(themeRepository.currentTheme) }
            var selectedDevice by remember { mutableStateOf<com.wled.app.data.model.WledDevice?>(null) }

            WLEDTheme(theme = currentTheme, dynamicColor = true) {
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
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                repository = repository,
                                currentTheme = currentTheme,
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onNavigateToWled = { device ->
                                    selectedDevice = device
                                    navController.navigate("wled")
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
                                        navController.navigate("wled_full")
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

    override fun onDestroy() {
        super.onDestroy()
        udpListener.stop()
    }
}
