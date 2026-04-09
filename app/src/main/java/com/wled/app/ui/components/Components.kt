package com.wled.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WbIridescent
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wled.app.data.model.DeviceIcon
import com.wled.app.data.model.WledDevice
import com.wled.app.data.model.WledState
import com.wled.app.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

fun getDeviceColor(state: WledState?, realtimeColor: Int?, savedColor: Int?): Color {
    val stateColor = state?.segments?.firstOrNull()?.colors?.firstOrNull()?.toArgb()
    val colorInt = realtimeColor ?: stateColor ?: savedColor ?: 0xFFFF5722.toInt()
    return Color(colorInt)
}

@Composable
fun SettingsToggle(
    isLightTheme: Boolean = false,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor by animateColorAsState(
        targetValue = if (isOn) {
            if (isLightTheme) primaryColor.copy(alpha = 0.5f) else primaryColor
        } else {
            if (isLightTheme) Color.LightGray.copy(alpha = 0.5f) else Color.Transparent
        },
        label = "trackColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isOn) primaryColor else {
            if (isLightTheme) Color.Gray.copy(alpha = 0.5f) else primaryColor.copy(alpha = 0.5f)
        },
        label = "borderColor"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (isOn) 28.dp else 4.dp,
        label = "thumbOffset"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (isOn) {
            if (isLightTheme) primaryColor else androidx.compose.ui.graphics.lerp(primaryColor, Color.Black, 0.6f)
        } else {
            if (isLightTheme) Color.Gray else primaryColor.copy(alpha = 0.5f)
        },
        label = "thumbColor"
    )
    val thumbSize by animateDpAsState(
        targetValue = if (isOn) 20.dp else 14.dp,
        label = "thumbSize"
    )

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onToggle(!isOn) }
            )
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
fun PowerToggle(
    isOn: Boolean,
    baseColor: Color,
    theme: AppTheme,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enabled by remember { mutableStateOf(true) }
    var lastToggleTime by remember { mutableStateOf(0L) }
    
    LaunchedEffect(enabled) {
        if (!enabled) {
            kotlinx.coroutines.delay(500)
            enabled = true
        }
    }

    val darkLedColor = androidx.compose.ui.graphics.lerp(baseColor, Color.Black, 0.65f)
    val lightLedColor = androidx.compose.ui.graphics.lerp(baseColor, Color.White, 0.9f)
    
    val thumbColor = if (theme == AppTheme.LIGHT) lightLedColor else darkLedColor
    val offTrackColor = if (theme == AppTheme.LIGHT) 
        androidx.compose.ui.graphics.lerp(baseColor, Color.Black, 0.3f) 
    else 
        androidx.compose.ui.graphics.lerp(baseColor, Color.Black, 0.8f)
        
    val offThumbColor = if (theme == AppTheme.LIGHT)
        androidx.compose.ui.graphics.lerp(baseColor, Color.Black, 0.5f)
    else
        androidx.compose.ui.graphics.lerp(baseColor, Color.Black, 0.5f)

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = thumbColor,
        checkedTrackColor = baseColor,
        checkedBorderColor = Color.Transparent,
        uncheckedThumbColor = offThumbColor,
        uncheckedTrackColor = offTrackColor,
        uncheckedBorderColor = baseColor.copy(alpha = 0.5f)
    )

    Switch(
        checked = isOn,
        onCheckedChange = {
            if (enabled) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastToggleTime > 400) {
                    lastToggleTime = currentTime
                    enabled = false
                    onToggle()
                }
            }
        },
        modifier = modifier,
        colors = switchColors
    )
}

@Composable
fun BrightnessSlider(
    brightness: Int,
    baseColor: Color,
    onBrightnessChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onHapticFeedback: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isDragging by remember { mutableStateOf(false) }
    var targetValue by remember { mutableStateOf(brightness.toFloat()) }
    var updateJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val currentBrightness by androidx.compose.runtime.rememberUpdatedState(brightness)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        targetValue = brightness.toFloat()
    }

    LaunchedEffect(brightness) {
        if (!isDragging && updateJob == null) {
            targetValue = brightness.toFloat()
        }
    }

    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        label = "brightness",
        animationSpec = if (isDragging) tween(0) else spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium
        )
    )

    var lastHapticBrightness by remember { mutableStateOf(brightness) }

    Slider(
        value = animatedValue,
        enabled = enabled,
        onValueChange = { newValue ->
            isDragging = true
            updateJob?.cancel()
            updateJob = null
            targetValue = newValue
            onBrightnessChange(newValue.toInt(), true)

            val intValue = newValue.toInt()
            if (onHapticFeedback != null && intValue != lastHapticBrightness) {
                lastHapticBrightness = intValue
                onHapticFeedback()
            }
        },
        onValueChangeFinished = {
            isDragging = false
            onBrightnessChange(targetValue.toInt(), false)

            updateJob?.cancel()
            updateJob = scope.launch {
                delay(2000)
                updateJob = null
                if (currentBrightness.toFloat() != targetValue) {
                    targetValue = currentBrightness.toFloat()
                }
            }
        },
        valueRange = 1f..255f,
        modifier = modifier.fillMaxWidth(),
        interactionSource = interactionSource,
        colors = SliderDefaults.colors(
            thumbColor = baseColor,
            activeTrackColor = baseColor,
            inactiveTrackColor = baseColor.copy(alpha = 0.3f),
            activeTickColor = baseColor,
            inactiveTickColor = baseColor.copy(alpha = 0.3f)
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceCard(
    device: WledDevice,
    state: WledState?,
    realtimeColor: Int?,
    baseColor: Color,
    isOn: Boolean,
    isOnline: Boolean,
    theme: AppTheme = AppTheme.SYSTEM,
    onCardClick: () -> Unit,
    onToggle: () -> Unit,
    onBrightnessChange: (Int, Boolean) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    onHapticFeedback: (() -> Unit)? = null
) {
    val isOled = theme == AppTheme.OLED
    
    val iconColor by animateColorAsState(
        targetValue = if (isOnline) {
            if (isOn) baseColor else baseColor.copy(alpha = 0.4f)
        } else {
            Color.Gray
        },
        label = "iconColor",
        animationSpec = tween(durationMillis = 200)
    )
    
    val cardBackgroundColor = when {
        !isOnline -> Color.Gray.copy(alpha = 0.3f)
        isOled -> Color.Transparent
        theme == AppTheme.LIGHT -> androidx.compose.ui.graphics.lerp(baseColor, Color.Black, 0.75f)
        else -> baseColor.copy(alpha = 0.08f)
    }
    
        Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .then(
                if (isOled) Modifier.border(1.dp, baseColor, RoundedCornerShape(24.dp)) else Modifier
            )
            .then(
                if (isOnline) {
                    Modifier.combinedClickable(
                        onClick = onCardClick,
                        onLongClick = onLongPress
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = if (isOnline) 4.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isOnline) {
                            if (isOled) baseColor else if (theme == AppTheme.LIGHT) Color.White else MaterialTheme.colorScheme.onSurface
                        } else {
                            Color.Gray
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isOn && isOnline) baseColor.copy(alpha = 0.2f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    when (device.effectiveIcon) {
                        DeviceIcon.LIGHTBULB -> Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                        DeviceIcon.RGB_RIBBON -> Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                        DeviceIcon.HOME -> Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                        DeviceIcon.FLOWER -> Icon(
                            imageVector = Icons.Default.LocalFlorist,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                        DeviceIcon.STAR -> Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                        DeviceIcon.NONE -> Spacer(modifier = Modifier.size(28.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                PowerToggle(
                    isOn = isOn,
                    baseColor = if (isOnline) baseColor else Color.Gray,
                    theme = theme,
                    onToggle = if (isOnline) onToggle else { {} }
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            BrightnessSlider(
                brightness = state?.brightness ?: device.savedBrightness,
                baseColor = if (isOnline) baseColor else Color.Gray,
                onBrightnessChange = if (isOnline) onBrightnessChange else { _, _ -> },
                enabled = isOnline,
                onHapticFeedback = onHapticFeedback
            )
        }
    }
}

@Composable
fun EditDeviceDialog(
    currentName: String,
    currentIcon: DeviceIcon,
    baseColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String, DeviceIcon) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var selectedIcon by remember { mutableStateOf(currentIcon) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier l'appareil") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Icône", style = MaterialTheme.typography.labelMedium)
                
                DeviceIcon.entries.forEach { icon ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIcon = icon }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconVector = when (icon) {
                            DeviceIcon.LIGHTBULB -> Icons.Default.Lightbulb
                            DeviceIcon.RGB_RIBBON -> Icons.Default.Timeline
                            DeviceIcon.HOME -> Icons.Default.Home
                            DeviceIcon.FLOWER -> Icons.Default.LocalFlorist
                            DeviceIcon.STAR -> Icons.Default.Star
                            DeviceIcon.NONE -> Icons.Default.Lightbulb
                        }
                        if (icon != DeviceIcon.NONE) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = if (selectedIcon == icon) baseColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = icon.iconName,
                            color = if (selectedIcon == icon) baseColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, selectedIcon) },
                enabled = name.isNotBlank()
            ) {
                Text("Enregistrer", color = baseColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    )
}

val presetColors = listOf(
    Color(0xFFFF0000), // Red
    Color(0xFFFF8000), // Orange
    Color(0xFFFFFF00), // Yellow
    Color(0xFF00FF00), // Green
    Color(0xFF00FFFF), // Cyan
    Color(0xFF0000FF), // Blue
    Color(0xFF8000FF), // Purple
    Color(0xFFFF00FF), // Magenta
    Color(0xFFFFFFFF)  // White
)

@Composable
fun ColorWheel(
    hue: Float,
    onHueChange: (Float) -> Unit,
    saturation: Float,
    onSaturationChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onInteractionStart: () -> Unit = {},
    onInteractionEnd: () -> Unit = {}
) {
    var isDragging by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onInteractionStart()
                        tryAwaitRelease()
                        onInteractionEnd()
                    },
                    onTap = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val radius = minOf(size.width, size.height) / 2f
                        val distance = hypot(dx, dy)
                        
                        if (distance <= radius) {
                            var angle = atan2(dy, dx) * 180f / PI.toFloat()
                            if (angle < 0) angle += 360f
                            onHueChange(angle)
                            onSaturationChange((distance / radius).coerceIn(0f, 1f))
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        isDragging = true
                        onInteractionStart() 
                    },
                    onDragEnd = { 
                        isDragging = false
                        onInteractionEnd() 
                    },
                    onDragCancel = { 
                        isDragging = false
                        onInteractionEnd() 
                    }
                ) { change, _ ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = change.position.x - center.x
                    val dy = change.position.y - center.y
                    val radius = minOf(size.width, size.height) / 2f
                    val distance = hypot(dx, dy)
                    
                    var angle = atan2(dy, dx) * 180f / PI.toFloat()
                    if (angle < 0) angle += 360f
                    onHueChange(angle)
                    onSaturationChange((distance / radius).coerceIn(0f, 1f))
                }
            }
    ) {
        val radius = minOf(size.width, size.height) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Draw color wheel background (Sweep Gradient)
        val sweepColors = listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red
        )

        drawCircle(
            brush = Brush.sweepGradient(sweepColors, center = center),
            radius = radius,
            center = center
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.White.copy(alpha = 0f)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // Draw thumb indicator
        val angleRad = hue * PI.toFloat() / 180f
        val thumbDistance = saturation * radius
        val thumbX = center.x + cos(angleRad) * thumbDistance
        val thumbY = center.y + sin(angleRad) * thumbDistance

        drawCircle(
            color = Color.Black.copy(alpha = 0.2f),
            radius = 14.dp.toPx(),
            center = Offset(thumbX, thumbY)
        )
        drawCircle(
            color = Color.White,
            radius = 12.dp.toPx(),
            center = Offset(thumbX, thumbY)
        )
        drawCircle(
            color = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, 1f))),
            radius = 10.dp.toPx(),
            center = Offset(thumbX, thumbY)
        )
    }
}
