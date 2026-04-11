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
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kotlin.math.sqrt
import kotlin.math.pow

fun getDeviceColor(state: WledState?, realtimeColor: Int?, savedColor: Int?): Color {
    val stateColor = state?.segments?.firstOrNull()?.colors?.firstOrNull()?.toArgb()
    val colorInt = realtimeColor ?: stateColor ?: savedColor ?: 0xFFFF5722.toInt()
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(colorInt, hsv)
    // On force la valeur (intensité/noir) à 1.0f pour que la tuile soit toujours colorée et vive
    hsv[2] = 1f
    return Color(android.graphics.Color.HSVToColor(hsv))
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val isPressed by interactionSource.collectIsPressedAsState()
    val thumbScale by animateFloatAsState(if (isPressed) 1.5f else 1f)
    var isDragging by remember { mutableStateOf(false) }
    
    // On utilise une courbe quadratique (x^2) pour que la luminosité basse soit plus précise
    // tout en évitant les valeurs nulles trop longues de la courbe cubique
    var targetValue by remember { mutableStateOf(kotlin.math.sqrt(brightness.coerceAtLeast(1) / 255.0).toFloat()) }
    var updateJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val currentBrightness by androidx.compose.runtime.rememberUpdatedState(brightness)
    val scope = rememberCoroutineScope()
    
    val minTarget = remember { kotlin.math.sqrt(1.0 / 255.0).toFloat() }

    LaunchedEffect(Unit) {
        targetValue = kotlin.math.sqrt(brightness.coerceAtLeast(1) / 255.0).toFloat()
    }

    LaunchedEffect(brightness) {
        if (!isDragging && updateJob == null) {
            targetValue = kotlin.math.sqrt(brightness.coerceAtLeast(1) / 255.0).toFloat()
        }
    }

    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        label = "brightness",
        animationSpec = if (isDragging) tween(0) else spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMedium
        )
    )

    var lastHapticBrightness by remember { mutableStateOf(brightness) }

    Slider(
        value = animatedValue.coerceIn(minTarget, 1f),
        enabled = enabled,
        onValueChange = { newValue ->
            isDragging = true
            updateJob?.cancel()
            updateJob = null
            targetValue = newValue
            val intValue = (newValue.toDouble().pow(2.0) * 255.0).roundToInt().coerceIn(1, 255)
            onBrightnessChange(intValue, true)

            if (onHapticFeedback != null && intValue != lastHapticBrightness) {
                lastHapticBrightness = intValue
                onHapticFeedback()
            }
        },
        onValueChangeFinished = {
            isDragging = false
            val intValue = (targetValue.toDouble().pow(2.0) * 255.0).roundToInt().coerceIn(1, 255)
            onBrightnessChange(intValue, false)

            updateJob?.cancel()
            updateJob = scope.launch {
                delay(2000)
                updateJob = null
                val expectedTarget = kotlin.math.sqrt(currentBrightness.coerceAtLeast(1) / 255.0).toFloat()
                if (expectedTarget != targetValue) {
                    targetValue = expectedTarget
                }
            }
        },
        valueRange = minTarget..1f,
        modifier = modifier.fillMaxWidth(),
        interactionSource = interactionSource,
        thumb = { 
            Box(
                modifier = Modifier
                    .scale(thumbScale)
                    .width(6.dp)
                    .height(33.dp)
                    .background(
                        if (enabled) baseColor else baseColor.copy(alpha = 0.38f), 
                        androidx.compose.foundation.shape.RoundedCornerShape(50)
                    )
            ) 
        },
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
    onValueChangeFinished: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var targetValue by remember { mutableStateOf(value) }
    var isDragging by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val thumbScale by animateFloatAsState(if (isPressed) 1.5f else 1f)
    
    LaunchedEffect(value) {
        if (!isDragging && value != targetValue) {
            targetValue = value
        }
    }

    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        label = "rgb",
        animationSpec = if (isDragging) tween(0) else spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    val currentDisplayValue = animatedValue

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
            val thumbRadius = 10.dp.toPx() * thumbScale
            
            // Background track (full width, rounded)
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = Offset(0f, (size.height - trackHeight) / 2),
                size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )
            
            // Colored portion (from start to current value)
            val thumbX = (currentDisplayValue / 255f) * size.width
            if (currentDisplayValue > 0) {
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
            
            // Draw trait thumb
            val thumbWidth = 6.dp.toPx() * thumbScale
            val thumbHeight = 24.dp.toPx() * thumbScale
            val thumbCornerRadius = 3.dp.toPx() * thumbScale
            val thumbY = size.height / 2
            
            val thumbTopLeftX = thumbX - thumbWidth / 2
            val thumbTopLeftY = thumbY - thumbHeight / 2
            
            // Shadow
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.2f),
                topLeft = Offset(thumbTopLeftX, thumbTopLeftY + 1.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(thumbWidth, thumbHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(thumbCornerRadius, thumbCornerRadius)
            )
            
            // Colored trait
            drawRoundRect(
                color = color,
                topLeft = Offset(thumbTopLeftX, thumbTopLeftY),
                size = androidx.compose.ui.geometry.Size(thumbWidth, thumbHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(thumbCornerRadius, thumbCornerRadius)
            )
        }
        
        Slider(
            value = currentDisplayValue,
            onValueChange = { newValue ->
                isDragging = true
                targetValue = newValue
                onValueChange(newValue)
            },
            onValueChangeFinished = {
                isDragging = false
                onValueChangeFinished?.invoke()
            },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interactionSource,
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
    interpolationCurve: (Float) -> Float = { it },
    thumbColor: Color? = null,
    onValueChangeFinished: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var targetValue by remember { mutableStateOf(value) }
    var isDragging by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val thumbScale by animateFloatAsState(if (isPressed) 1.5f else 1f)
    
    LaunchedEffect(value) {
        if (!isDragging && value != targetValue) {
            targetValue = value
        }
    }

    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        label = "colorSlider",
        animationSpec = if (isDragging) tween(0) else spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    val currentDisplayValue = animatedValue

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
            val thumbRadius = 10.dp.toPx() * thumbScale
            
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
            val thumbX = (currentDisplayValue / 255f) * size.width
            if (!drawFullTrackGradient && currentDisplayValue > 0) {
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
            val currentThumbColor = thumbColor ?: if (drawFullTrackGradient) {
                androidx.compose.ui.graphics.lerp(startColor, endColor, currentDisplayValue / 255f)
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
            value = currentDisplayValue,
            onValueChange = { newValue ->
                isDragging = true
                targetValue = newValue
                onValueChange(newValue)
            },
            onValueChangeFinished = {
                isDragging = false
                onValueChangeFinished?.invoke()
            },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interactionSource,
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
            .clip(RoundedCornerShape(24.dp))
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
    Color(0xFFFF0000), // Rouge
    Color(0xFFFF8000), // Orange
    Color(0xFFFFFF00), // Jaune
    Color(0xFFE8C396), // Beige
    Color(0xFFFFFFFF), // Blanc
    Color(0xFF000000), // Noir
    Color(0xFFFF00FF), // Rose (Magenta)
    Color(0xFF8000FF), // Violet
    Color(0xFF0000FF), // Bleu foncé
    Color(0xFF00FFFF), // Bleu clair
    Color(0xFF00FF00), // Vert
    Color.Transparent  // Random
)

@Composable
fun ColorWheel(
    hue: Float,
    onHueChange: (Float) -> Unit,
    saturation: Float,
    onSaturationChange: (Float) -> Unit,
    value: Float = 1f,
    modifier: Modifier = Modifier,
    onInteractionStart: () -> Unit = {},
    onInteractionEnd: () -> Unit = {}
) {
    var isDragging by remember { mutableStateOf(false) }

    val angleRad = hue * PI.toFloat() / 180f
    val targetNx = (kotlin.math.cos(angleRad) * saturation).toFloat()
    val targetNy = (kotlin.math.sin(angleRad) * saturation).toFloat()

    val animatedNx by animateFloatAsState(
        targetValue = targetNx,
        label = "nx",
        animationSpec = if (isDragging) tween(0) else spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
    )

    val animatedNy by animateFloatAsState(
        targetValue = targetNy,
        label = "ny",
        animationSpec = if (isDragging) tween(0) else spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)
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
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val radius = minOf(size.width, size.height) / 2f
                        val distance = hypot(dx, dy)
                        
                        var angle = atan2(dy, dx) * 180f / PI.toFloat() + 90f
                        if (angle < 0) angle += 360f
                        onHueChange(angle)
                        onSaturationChange((distance / radius).coerceIn(0f, 1f))
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
                    change.consume()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = change.position.x - center.x
                    val dy = change.position.y - center.y
                    val radius = minOf(size.width, size.height) / 2f
                    val distance = hypot(dx, dy)
                    
                    var angle = atan2(dy, dx) * 180f / PI.toFloat() + 90f
                    if (angle < 0) angle += 360f
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
            val thumbX = center.x + animatedNx * radius
            val thumbY = center.y + animatedNy * radius

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
            
            val currentSat = hypot(animatedNx, animatedNy).coerceIn(0f, 1f)
            var currentHue = (atan2(animatedNy, animatedNx) * 180f / PI.toFloat())
            if (currentHue < 0) currentHue += 360f
            
            drawCircle(
                color = Color(android.graphics.Color.HSVToColor(floatArrayOf(currentHue, currentSat, value))),
                radius = 8.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }
    }
}

