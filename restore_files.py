import os

components_content = r"""package com.wled.app.ui.components

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
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.drawscope.rotate
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

@Composable
fun RgbSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    var targetValue by remember { mutableStateOf(value) }
    
    LaunchedEffect(value) {
        if (value != targetValue) {
            targetValue = value
        }
    }

    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        label = "rgb",
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val trackHeight = 12.dp.toPx()
            val cornerRadius = trackHeight / 2
            val thumbRadius = 10.dp.toPx()
            
            // Background track (full width, rounded)
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = Offset(0f, (size.height - trackHeight) / 2),
                size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )
            
            // Colored portion (from start to current value)
            val thumbX = (animatedValue / 255f) * size.width
            if (animatedValue > 0) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0f), color),
                        endX = thumbX
                    ),
                    topLeft = Offset(0f, (size.height - trackHeight) / 2),
                    size = androidx.compose.ui.geometry.Size(thumbX, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )
            }
            
            // Draw larger round thumb
            val thumbY = size.height / 2
            
            // Shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = thumbRadius + 2.dp.toPx(),
                center = Offset(thumbX, thumbY + 1.dp.toPx())
            )
            
            // Colored circle
            drawCircle(
                color = color,
                radius = thumbRadius,
                center = Offset(thumbX, thumbY)
            )
        }
        
        Slider(
            value = animatedValue,
            onValueChange = { newValue ->
                targetValue = newValue
                onValueChange(newValue)
            },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        when (interaction) {
                            is DragInteraction.Start -> { }
                            is DragInteraction.Stop -> { }
                            is DragInteraction.Cancel -> { }
                        }
                    }
                }
            },
            colors = SliderDefaults.colors(
                thumbColor = color.copy(alpha = 0f),
                activeTrackColor = color.copy(alpha = 0f),
                inactiveTrackColor = color.copy(alpha = 0f)
            )
        )
    }
}

@Composable
fun ColorSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    baseColor: Color,
    startColor: Color = Color.Black.copy(alpha = 0f),
    endColor: Color = baseColor,
    drawFullTrackGradient: Boolean = false,
    modifier: Modifier = Modifier
) {
    var targetValue by remember { mutableStateOf(value) }
    
    LaunchedEffect(value) {
        if (value != targetValue) {
            targetValue = value
        }
    }

    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        label = "colorSlider",
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val trackHeight = 12.dp.toPx()
            val cornerRadius = trackHeight / 2
            val thumbRadius = 10.dp.toPx()
            
            // Background track (full width, rounded)
            if (drawFullTrackGradient) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(startColor, endColor),
                        startX = 0f,
                        endX = size.width
                    ),
                    topLeft = Offset(0f, (size.height - trackHeight) / 2),
                    size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )
            } else {
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.3f),
                    topLeft = Offset(0f, (size.height - trackHeight) / 2),
                    size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )
            }
            
            // Colored portion (from start to current value)
            val thumbX = (animatedValue / 255f) * size.width
            if (!drawFullTrackGradient && animatedValue > 0) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(startColor, endColor),
                        endX = thumbX
                    ),
                    topLeft = Offset(0f, (size.height - trackHeight) / 2),
                    size = androidx.compose.ui.geometry.Size(thumbX, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )
            }
            
            // Draw larger round thumb
            val thumbY = size.height / 2
            
            // Shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = thumbRadius + 2.dp.toPx(),
                center = Offset(thumbX, thumbY + 1.dp.toPx())
            )
            
            // Colored circle
            val currentThumbColor = if (drawFullTrackGradient) {
                androidx.compose.ui.graphics.lerp(startColor, endColor, animatedValue / 255f)
            } else {
                baseColor
            }
            
            drawCircle(
                color = currentThumbColor,
                radius = thumbRadius,
                center = Offset(thumbX, thumbY)
            )
        }
        
        Slider(
            value = animatedValue,
            onValueChange = { newValue ->
                targetValue = newValue
                onValueChange(newValue)
            },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = remember { MutableInteractionSource() },
            colors = SliderDefaults.colors(
                thumbColor = baseColor.copy(alpha = 0f),
                activeTrackColor = baseColor.copy(alpha = 0f),
                inactiveTrackColor = baseColor.copy(alpha = 0f)
            )
        )
    }
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
    
    var targetHue by remember { mutableStateOf(hue) }
    var targetSaturation by remember { mutableStateOf(saturation) }
    
    LaunchedEffect(hue) {
        if (!isDragging) {
            targetHue = hue
        }
    }
    
    LaunchedEffect(saturation) {
        if (!isDragging) {
            targetSaturation = saturation
        }
    }
    
    val animatedHue by animateFloatAsState(
        targetValue = targetHue,
        label = "hue",
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
    )
    
    val animatedSaturation by animateFloatAsState(
        targetValue = targetSaturation,
        label = "saturation",
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
    )

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
                        isDragging = true
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val radius = minOf(size.width, size.height) / 2f
                        val distance = hypot(dx, dy)
                        
                        if (distance <= radius) {
                            var angle = atan2(dy, dx) * 180f / PI.toFloat() + 90f
                            if (angle < 0) angle += 360f
                            targetHue = angle
                            targetSaturation = (distance / radius).coerceIn(0f, 1f)
                            onHueChange(angle)
                            onSaturationChange((distance / radius).coerceIn(0f, 1f))
                        }
                        isDragging = false
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
                    
                    var angle = atan2(dy, dx) * 180f / PI.toFloat() + 90f
                    if (angle < 0) angle += 360f
                    targetHue = angle
                    targetSaturation = (distance / radius).coerceIn(0f, 1f)
                    onHueChange(angle)
                    onSaturationChange((distance / radius).coerceIn(0f, 1f))
                }
            }
    ) {
        val radius = minOf(size.width, size.height) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        rotate(-90f, center) {
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
            val angleRad = animatedHue * PI.toFloat() / 180f
            val thumbDistance = animatedSaturation * radius
            val thumbX = center.x + cos(angleRad) * thumbDistance
            val thumbY = center.y + sin(angleRad) * thumbDistance

            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = 12.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            drawCircle(
                color = Color(android.graphics.Color.HSVToColor(floatArrayOf(animatedHue, animatedSaturation, 1f))),
                radius = 8.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }
    }
}
"""

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'w') as f:
    f.write(components_content)

wledcontrol_content = r"""package com.wled.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.wled.app.data.model.WledDevice
import com.wled.app.data.model.WledSegment
import com.wled.app.data.repository.WledRepository
import com.wled.app.ui.components.ColorWheel
import com.wled.app.ui.components.presetColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WledControlScreen(
    device: WledDevice,
    repository: WledRepository,
    onNavigateBack: () -> Unit,
    onOpenFullWled: () -> Unit
) {
    val state by repository.deviceStates.collectAsState()
    val realtimeColors by repository.realtimeColors.collectAsState()
    val effectsMap by repository.deviceEffects.collectAsState()
    val presetsMap by repository.devicePresets.collectAsState()
    
    val deviceState = state[device.ip]
    val realtimeColor = realtimeColors[device.ip]
    val effects = effectsMap[device.ip] ?: emptyList()
    val presets = presetsMap[device.ip] ?: emptyMap()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val currentColor = if (realtimeColor != null) {
        Color(realtimeColor)
    } else if (deviceState?.segments?.isNotEmpty() == true) {
        val colors = deviceState.segments.first().colors
        if (colors.isNotEmpty()) {
            val c = colors.first()
            Color(c.r, c.g, c.b)
        } else {
            Color(255, 87, 34)
        }
    } else {
        Color(255, 87, 34)
    }
    
    val initialHsv = remember(currentColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(
            android.graphics.Color.rgb(
                (currentColor.red * 255).toInt(),
                (currentColor.green * 255).toInt(),
                (currentColor.blue * 255).toInt()
            ),
            hsv
        )
        hsv
    }
    
    var selectedHue by remember { mutableFloatStateOf(initialHsv[0]) }
    var selectedSaturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var selectedValue by remember { mutableFloatStateOf(initialHsv[2]) }
    
    // We don't want to trigger network request just because the screen was opened
    // or when polling updates the color from another device.
    // Instead of LaunchedEffect(selectedHue), we should use a flag that ensures we only
    // update when the user drags the wheel.
    var isUserDraggingColor by remember { mutableStateOf(false) }
    
    val baseColor = ensureColorNotTooDark(currentColor)

    val displayColor = Color(
        android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, selectedValue))
    )

    // Synchronize UI sliders with actual colors when polling changes it externally, 
    // unless the user is currently interacting.
    LaunchedEffect(initialHsv[0], initialHsv[1], initialHsv[2]) {
        if (!isUserDraggingColor) {
            selectedHue = initialHsv[0]
            selectedSaturation = initialHsv[1]
            selectedValue = initialHsv[2]
        }
    }

    // Network requests are now sent directly via onHueChange and onSaturationChange
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(device.name) }
    
    var segmentToEdit by remember { mutableStateOf<WledSegment?>(null) }
    var isAddingSegment by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("wled_custom_colors", Context.MODE_PRIVATE)
    
    var customColors by remember {
        mutableStateOf<List<Color>>(
            sharedPrefs.getString("colors", "")?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { it.toULongOrNull()?.let { colorLong -> Color(colorLong) } }
                ?: emptyList()
        )
    }

    var colorToDelete by remember { mutableStateOf<Color?>(null) }

    fun saveCustomColors(colors: List<Color>) {
        val colorsStr = colors.joinToString(",") { it.value.toULong().toString() }
        sharedPrefs.edit().putString("colors", colorsStr).apply()
        customColors = colors
    }
    
    var segmentToDelete by remember { mutableStateOf<WledSegment?>(null) }
    var localDeletedSegments by remember { mutableStateOf(setOf<Int>()) }
    
    if (colorToDelete != null) {
        AlertDialog(
            onDismissRequest = { colorToDelete = null },
            title = { Text("Supprimer la couleur") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Voulez-vous vraiment supprimer cette couleur personnalisée ?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorToDelete!!)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newColors = customColors.toMutableList()
                        newColors.remove(colorToDelete)
                        saveCustomColors(newColors)
                        colorToDelete = null
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { colorToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (segmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { segmentToDelete = null },
            title = { Text("Supprimer le segment") },
            text = {
                Text("Voulez-vous vraiment supprimer le segment ${segmentToDelete?.name ?: segmentToDelete?.id} ?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        segmentToDelete?.let { seg ->
                            localDeletedSegments = localDeletedSegments + seg.id
                            repository.deleteSegment(device.ip, seg.id)
                        }
                        segmentToDelete = null
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { segmentToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Modifier le nom") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.updateDeviceName(device.ip, editedName)
                        showEditNameDialog = false
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (segmentToEdit != null || isAddingSegment) {
        val segment = segmentToEdit
        val maxLeds = device.info?.ledsCount?.toString() ?: "10"
        var name by remember { mutableStateOf(segment?.name ?: "") }
        var start by remember { mutableStateOf(segment?.start?.toString() ?: "0") }
        var stop by remember { mutableStateOf(segment?.stop?.toString() ?: maxLeds) }

        AlertDialog(
            onDismissRequest = {
                segmentToEdit = null
                isAddingSegment = false
            },
            title = { Text(if (isAddingSegment) "Ajouter un segment" else "Modifier le segment") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom (Optionnel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it.filter { char -> char.isDigit() } },
                        label = { Text("Led de début") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = stop,
                        onValueChange = { stop = it.filter { char -> char.isDigit() } },
                        label = { Text("Led de fin") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                IconButton(
                    onClick = {
                        val startInt = start.toIntOrNull() ?: 0
                        val stopInt = stop.toIntOrNull() ?: 0
                        val segId = if (isAddingSegment) {
                            (deviceState?.segments?.maxOfOrNull { it.id } ?: -1) + 1
                        } else {
                            segment?.id ?: 0
                        }
                        
                        repository.updateSegment(
                            ip = device.ip,
                            segmentId = segId,
                            start = startInt,
                            stop = stopInt,
                            name = name.ifBlank { null }
                        )
                        segmentToEdit = null
                        isAddingSegment = false
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Enregistrer", tint = baseColor)
                }
            },
            dismissButton = {
                IconButton(
                    onClick = {
                        segmentToEdit = null
                        isAddingSegment = false
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Annuler", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        )
    }
    
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { showEditNameDialog = true }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenFullWled) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Ouvrir WLED"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedNavIcon(
                            icon = Icons.Default.Palette,
                            description = "Couleurs",
                            isSelected = selectedTab == 0,
                            baseColor = baseColor,
                            onClick = { selectedTab = 0 }
                        )
                        AnimatedNavIcon(
                            icon = Icons.Default.AutoAwesome,
                            description = "Effets",
                            isSelected = selectedTab == 1,
                            baseColor = baseColor,
                            onClick = { selectedTab = 1 }
                        )
                        AnimatedNavIcon(
                            icon = Icons.AutoMirrored.Filled.List,
                            description = "Segments",
                            isSelected = selectedTab == 2,
                            baseColor = baseColor,
                            onClick = { selectedTab = 2 }
                        )
                        AnimatedNavIcon(
                            icon = Icons.Default.Favorite,
                            description = "Presets",
                            isSelected = selectedTab == 3,
                            baseColor = baseColor,
                            onClick = { selectedTab = 3 }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(
                            animationSpec = tween(250),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(250),
                            targetOffsetX = { fullWidth -> -fullWidth }
                        )
                    } else {
                        slideInHorizontally(
                            animationSpec = tween(250),
                            initialOffsetX = { fullWidth -> -fullWidth }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(250),
                            targetOffsetX = { fullWidth -> fullWidth }
                        )
                    }
                },
                label = "tab_transition",
                modifier = Modifier.fillMaxSize()
            ) { targetTab ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (targetTab) {
                0 -> {
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier.size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ColorWheel(
                            hue = selectedHue,
                            onHueChange = { 
                                selectedHue = it
                                isUserDraggingColor = true 
                                val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(it, selectedSaturation, 1f))
                                repository.setColor(device.ip, colorInt)
                            },
                            saturation = selectedSaturation,
                            onSaturationChange = { 
                                selectedSaturation = it
                                isUserDraggingColor = true 
                                val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, it, 1f))
                                repository.setColor(device.ip, colorInt)
                            },
                            modifier = Modifier.size(200.dp),
                            onInteractionStart = { isUserDraggingColor = true },
                            onInteractionEnd = { isUserDraggingColor = false }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Couleurs prédéfinies",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(9),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(presetColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        val hsv = FloatArray(3)
                                        android.graphics.Color.colorToHSV(
                                            android.graphics.Color.rgb(
                                                (color.red * 255).toInt(),
                                                (color.green * 255).toInt(),
                                                (color.blue * 255).toInt()
                                            ),
                                            hsv
                                        )
                                        selectedHue = hsv[0]
                                        selectedSaturation = hsv[1]
                                        selectedValue = hsv[2]
                                        
                                        val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 1f))
                                        repository.setColor(device.ip, colorInt)
                                    }
                            )
                        }
                        
                        item(span = { GridItemSpan(9) }) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        item(span = { GridItemSpan(9) }) {
                            Text(
                                text = "Couleurs personnalisées",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        item(span = { GridItemSpan(9) }) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        items(customColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .combinedClickable(
                                        onClick = {
                                            val hsv = FloatArray(3)
                                            android.graphics.Color.colorToHSV(
                                                android.graphics.Color.rgb(
                                                    (color.red * 255).toInt(),
                                                    (color.green * 255).toInt(),
                                                    (color.blue * 255).toInt()
                                                ),
                                                hsv
                                            )
                                            selectedHue = hsv[0]
                                            selectedSaturation = hsv[1]
                                            selectedValue = hsv[2]
                                            
                                            val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], 1f))
                                            repository.setColor(device.ip, colorInt)
                                        },
                                        onLongClick = {
                                            colorToDelete = color
                                        }
                                    )
                            )
                        }
                        
                        item {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        val c = Color(
                                            android.graphics.Color.HSVToColor(
                                                floatArrayOf(selectedHue, selectedSaturation, 1f)
                                            )
                                        )
                                        // Custom equality to ignore precision issues
                                        val isPreset = presetColors.any { p ->
                                            p.red == c.red && p.green == c.green && p.blue == c.blue
                                        }
                                        val isCustom = customColors.any { p ->
                                            p.red == c.red && p.green == c.green && p.blue == c.blue
                                        }
                                        
                                        if (!isPreset && !isCustom) {
                                            val newColors = customColors.toMutableList()
                                            newColors.add(c)
                                            saveCustomColors(newColors)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Ajouter",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                1 -> {
                    var effectSearchQuery by remember { mutableStateOf("") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Effets",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = effectSearchQuery,
                        onValueChange = { effectSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Rechercher un effet") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val currentEffectId = deviceState?.segments?.firstOrNull()?.currentEffect ?: -1
                    
                    val filteredEffects = remember(effects, effectSearchQuery, currentEffectId) {
                        effects.mapIndexed { index, name -> index to name }
                            .filter { it.second.contains(effectSearchQuery, ignoreCase = true) }
                            .sortedWith(
                                compareBy<Pair<Int, String>> { it.first != currentEffectId }
                                    .thenBy { !it.second.startsWith(effectSearchQuery, ignoreCase = true) }
                                    .thenBy { it.second.lowercase() }
                            )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredEffects, key = { it.first }) { (effectId, effectName) ->
                            val isSelected = currentEffectId == effectId
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) baseColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { repository.setEffect(device.ip, effectId) }
                            ) {
                                Text(
                                    text = effectName,
                                    modifier = Modifier.padding(16.dp),
                                    color = if (isSelected) baseColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                2 -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Segments",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val segments = (deviceState?.segments ?: emptyList()).filter { it.id !in localDeletedSegments }
                        items(segments, key = { it.id }) { segment ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            segmentToEdit = segment
                                            false
                                        }
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            if (segment.id != 0) {
                                                segmentToDelete = segment
                                            }
                                            false
                                        }
                                        else -> false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = segment.id != 0,
                                backgroundContent = {
                                    val direction = if (segment.id == 0 && dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                        SwipeToDismissBoxValue.Settled
                                    } else {
                                        dismissState.dismissDirection
                                    }
                                    
                                    val color by animateColorAsState(
                                        when (direction) {
                                            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                            else -> Color.Transparent
                                        }
                                    )
                                    val alignment = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        else -> Alignment.Center
                                    }
                                    val icon = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                        else -> Icons.Default.Edit
                                    }
                                    val scale by animateFloatAsState(
                                        if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f
                                    )
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp)
                                            .background(color, RoundedCornerShape(16.dp)),
                                        contentAlignment = alignment
                                    ) {
                                        if (direction != SwipeToDismissBoxValue.Settled) {
                                            Icon(
                                                icon,
                                                contentDescription = null,
                                                modifier = Modifier.scale(scale),
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { segmentToEdit = segment }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = segment.name ?: "Segment ${segment.id}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Leds: ${segment.start} à ${segment.stop}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { isAddingSegment = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.lerp(baseColor, Color.Black, 0.6f)
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ajouter un segment", color = Color.White)
                            }
                        }
                    }
                }
                3 -> {
                    var presetSearchQuery by remember { mutableStateOf("") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Presets",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = presetSearchQuery,
                        onValueChange = { presetSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Rechercher un preset") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val presetIds = remember(presets, presetSearchQuery) {
                        if (presetSearchQuery.isEmpty()) {
                            presets.keys.toList().sortedBy { it.toIntOrNull() ?: 0 }
                        } else {
                            presets.keys.toList()
                                .filter { presetId -> 
                                    val name = presets[presetId] ?: "Preset $presetId"
                                    name.contains(presetSearchQuery, ignoreCase = true)
                                }
                                .sortedWith(
                                    compareBy<String> { presetId -> 
                                        val name = presets[presetId] ?: "Preset $presetId"
                                        !name.startsWith(presetSearchQuery, ignoreCase = true)
                                    }
                                    .thenBy { presetId -> 
                                        val name = presets[presetId] ?: "Preset $presetId"
                                        name.lowercase()
                                    }
                                )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presetIds) { presetId ->
                            val presetName = presets[presetId] ?: "Preset $presetId"
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { presetId.toIntOrNull()?.let { repository.setPreset(device.ip, it) } }
                            ) {
                                Text(
                                    text = presetName,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
}

@Composable
private fun AnimatedNavIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    isSelected: Boolean,
    baseColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) baseColor.copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = tween(200),
        label = "nav_bg_color"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) baseColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "nav_icon_color"
    )
    val width by animateDpAsState(
        targetValue = if (isSelected) 64.dp else 48.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "nav_width"
    )

    Box(
        modifier = Modifier
            .height(48.dp)
            .width(width)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = iconColor,
            modifier = Modifier.size(26.dp)
        )
    }
}

private fun ensureColorNotTooDark(color: Color): Color {
    val red = color.red.toFloat()
    val green = color.green.toFloat()
    val blue = color.blue.toFloat()
    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
    if (luminance < 0.25f) {
        val factor = 0.35f / luminance
        return Color(
            red = (red * factor).coerceAtMost(1f),
            green = (green * factor).coerceAtMost(1f),
            blue = (blue * factor).coerceAtMost(1f)
        )
    }
    return color
}
"""

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(wledcontrol_content)
