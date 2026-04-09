#!/bin/bash
cat << 'INNER_EOF' > app/src/main/java/com/wled/app/ui/theme/Theme.kt
package com.wled.app.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import android.os.Build

enum class AppTheme(val label: String) {
    SYSTEM("Material You (système)"),
    LIGHT("Material You (clair)"),
    OLED_BLACK("OLED Noir")
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF5722),
    secondary = Color(0xFFFF9800),
    tertiary = Color(0xFF795548),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF5722),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCE),
    onPrimaryContainer = Color(0xFF380D00),
    secondary = Color(0xFFFF9800),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF2D1600),
    tertiary = Color(0xFF795548),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD7CCC8),
    onTertiaryContainer = Color(0xFF1B0F0C),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFEFEFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFEFEFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454E),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFFFB59E)
)

private val OledBlackColorScheme = darkColorScheme(
    primary = Color(0xFFFF5722),
    secondary = Color(0xFFFF9800),
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

class ThemeRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    var currentTheme: AppTheme
        get() {
            val saved = prefs.getString("app_theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
            return AppTheme.entries.find { it.name == saved } ?: AppTheme.SYSTEM
        }
        set(value) {
            prefs.edit().putString("app_theme", value.name).apply()
        }

    var autoDiscovery: Boolean
        get() = prefs.getBoolean("auto_discovery", true)
        set(value) {
            prefs.edit().putBoolean("auto_discovery", value).apply()
        }

    var showHiddenDevices: Boolean
        get() = prefs.getBoolean("show_hidden_devices", false)
        set(value) {
            prefs.edit().putBoolean("show_hidden_devices", value).apply()
        }

    var showOfflineLast: Boolean
        get() = prefs.getBoolean("show_offline_last", true)
        set(value) {
            prefs.edit().putBoolean("show_offline_last", value).apply()
        }

    var wledVersion: String
        get() = prefs.getString("wled_version", "") ?: ""
        set(value) {
            prefs.edit().putString("wled_version", value).apply()
        }
}

@Composable
fun WLEDTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.SYSTEM -> {
            // Material You (système) -> Adapte le fond au système (clair/sombre) et force l'écriture en BLANC
            val baseSystem = if (dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (darkTheme) DarkColorScheme else LightColorScheme
            
            baseSystem.copy(
                onPrimary = Color.White,
                onSecondary = Color.White,
                onTertiary = Color.White,
                onBackground = Color.White,
                onSurface = Color.White,
                onSurfaceVariant = Color.White,
                onPrimaryContainer = Color.White,
                onSecondaryContainer = Color.White,
                onTertiaryContainer = Color.White,
                onError = Color.White,
                onErrorContainer = Color.White
            )
        }
        AppTheme.LIGHT -> {
            // Material You (clair) -> Force le fond CLAIR et l'écriture en GRIS TRES FONCE
            val baseLight = if (dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val context = LocalContext.current
                dynamicLightColorScheme(context)
            } else {
                LightColorScheme
            }
            
            val darkGrey = Color(0xFF1A1A1A)
            baseLight.copy(
                background = Color(0xFFFEFEFE), // Force fond clair
                surface = Color(0xFFFEFEFE), // Force surface claire
                onPrimary = darkGrey,
                onSecondary = darkGrey,
                onTertiary = darkGrey,
                onBackground = darkGrey,
                onSurface = darkGrey,
                onSurfaceVariant = darkGrey,
                onPrimaryContainer = darkGrey,
                onSecondaryContainer = darkGrey,
                onTertiaryContainer = darkGrey,
                onError = darkGrey,
                onErrorContainer = darkGrey
            )
        }
        AppTheme.OLED_BLACK -> OledBlackColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                view.isForceDarkAllowed = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
INNER_EOF

cat << 'INNER_EOF_MAIN' > app/src/main/java/com/wled/app/MainActivity.kt
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
                    color = Color.Transparent
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
INNER_EOF_MAIN
