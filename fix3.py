import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

# 1. Imports
imports = """import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.graphics.toArgb
"""
content = content.replace("import androidx.compose.foundation.lazy.items\n", "import androidx.compose.foundation.lazy.items\n" + imports)

# 2. OptIn
content = content.replace("@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)", "@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)")

# 3. Add selectedCct state
cct_state = "var selectedCct by remember { mutableIntStateOf(deviceState?.segments?.firstOrNull()?.cct ?: 127) }"
content = content.replace("var selectedValue by remember { mutableFloatStateOf(initialHsv[2]) }", "var selectedValue by remember { mutableFloatStateOf(initialHsv[2]) }\n    " + cct_state)

sync_cct = "selectedCct = deviceState?.segments?.firstOrNull()?.cct ?: 127"
content = content.replace("selectedValue = initialHsv[2]\n        }", "selectedValue = initialHsv[2]\n            " + sync_cct + "\n        }")


# 4. Modify tab 0 ONLY.
# We look for "0 -> {" and the next "1 -> {". We will replace everything in between.
pattern = r'(                0 -> \{.*?)(?=\s*1 -> \{)'
match = re.search(pattern, content, re.DOTALL)

if match:
    old_tab0 = match.group(1)
    
    new_tab0 = """                0 -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.fillMaxWidth()) {
                            com.wled.app.ui.components.ColorSlider(
                                value = selectedValue * 255f,
                                onValueChange = { 
                                    selectedValue = it / 255f
                                    isUserDraggingColor = true
                                    val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, selectedValue))
                                    repository.setColor(device.ip, colorInt)
                                },
                                baseColor = displayColor,
                                drawFullTrackGradient = true,
                                startColor = Color.Black,
                                endColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f))),
                                interpolationCurve = { fraction -> kotlin.math.sqrt(fraction.toDouble()).toFloat() }
                            )
                            com.wled.app.ui.components.ColorSlider(
                                value = selectedCct.toFloat(),
                                onValueChange = { 
                                    selectedCct = it.toInt()
                                    isUserDraggingColor = true
                                    repository.setWhiteTemperature(device.ip, selectedCct)
                                },
                                baseColor = Color.White,
                                drawFullTrackGradient = true,
                                startColor = Color(0xFFFFB46B),
                                endColor = Color(0xFFE3EFFF)
                            )
                            com.wled.app.ui.components.RgbSlider(
                                value = (displayColor.red * 255).coerceIn(0f, 255f),
                                onValueChange = { r ->
                                    val newColor = Color(r / 255f, displayColor.green, displayColor.blue)
                                    val hsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(newColor.toArgb(), hsv)
                                    selectedHue = hsv[0]; selectedSaturation = hsv[1]; selectedValue = hsv[2]
                                    repository.setColor(device.ip, newColor.toArgb())
                                },
                                color = Color.Red
                            )
                            com.wled.app.ui.components.RgbSlider(
                                value = (displayColor.green * 255).coerceIn(0f, 255f),
                                onValueChange = { g ->
                                    val newColor = Color(displayColor.red, g / 255f, displayColor.blue)
                                    val hsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(newColor.toArgb(), hsv)
                                    selectedHue = hsv[0]; selectedSaturation = hsv[1]; selectedValue = hsv[2]
                                    repository.setColor(device.ip, newColor.toArgb())
                                },
                                color = Color.Green
                            )
                            com.wled.app.ui.components.RgbSlider(
                                value = (displayColor.blue * 255).coerceIn(0f, 255f),
                                onValueChange = { b ->
                                    val newColor = Color(displayColor.red, displayColor.green, b / 255f)
                                    val hsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(newColor.toArgb(), hsv)
                                    selectedHue = hsv[0]; selectedSaturation = hsv[1]; selectedValue = hsv[2]
                                    repository.setColor(device.ip, newColor.toArgb())
                                },
                                color = Color.Blue
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Couleurs prédéfinies",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            presetColors.forEach { color ->
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
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Couleurs personnalisées",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            customColors.forEach { color ->
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
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
"""

    content = content.replace(old_tab0, new_tab0)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)
