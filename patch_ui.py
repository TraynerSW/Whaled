import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
"""
if "import androidx.compose.foundation.rememberScrollState" not in content:
    content = content.replace("import androidx.compose.foundation.lazy.items\n", "import androidx.compose.foundation.lazy.items\n" + imports)

# OptIn ExperimentalLayoutApi
content = content.replace("@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)", "@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)")

# Add selectedCct state
cct_state = "var selectedCct by remember { mutableIntStateOf(deviceState?.segments?.firstOrNull()?.cct ?: 127) }"
if "selectedCct" not in content:
    content = content.replace("var selectedValue by remember { mutableFloatStateOf(initialHsv[2]) }\n", "var selectedValue by remember { mutableFloatStateOf(initialHsv[2]) }\n    " + cct_state + "\n")

    sync_cct = "selectedCct = deviceState?.segments?.firstOrNull()?.cct ?: 127"
    content = content.replace("selectedValue = initialHsv[2]\n        }", "selectedValue = initialHsv[2]\n            " + sync_cct + "\n        }")

# Re-structure the tabs to each have their own Column.
# Currently they are inside a common Column.

# The current code:
#             ) { targetTab ->
#                 Column(
#                     modifier = Modifier.fillMaxSize(),
#                     horizontalAlignment = Alignment.CenterHorizontally
#                 ) {
#                     when (targetTab) {
#                 0 -> {
#                     Spacer(modifier = Modifier.height(4.dp))
# ...

content = content.replace("""            ) { targetTab ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (targetTab) {""", """            ) { targetTab ->
                when (targetTab) {""")

content = content.replace("""                0 -> {
                    Spacer(modifier = Modifier.height(4.dp))""", """                0 -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))""")

# Now replace the LazyVerticalGrid with FlowRow and add sliders
# Find from "Spacer(modifier = Modifier.height(16.dp))" right after ColorWheel
# down to the end of tab 0 (which ends before "1 -> {")

# Regex to match the block starting after ColorWheel box and ending before 1 -> {
pattern = r'(Spacer\(modifier = Modifier\.height\(16\.dp\)\)\n\n\s*Text\(\n\s*text = "Couleurs prédéfinies".*?\n\s*\}.*?\n\s*\})(?=\s*1 -> \{)'

replacement = """Spacer(modifier = Modifier.height(24.dp))

                        // Sliders block
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
                                startColor = Color(0xFFFFB46B), // Warm orange
                                endColor = Color(0xFFE3EFFF) // Cool white
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
                            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }"""

match = re.search(pattern, content, re.DOTALL)
if match:
    content = content.replace(match.group(1), replacement)

content = content.replace("""                1 -> {
                    var effectSearchQuery""", """                1 -> {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        var effectSearchQuery""")

content = content.replace("""                        }
                    }
                }
                2 -> {""", """                        }
                    }
                    }
                }
                2 -> {""")

content = content.replace("""                2 -> {
                    Spacer(modifier = Modifier.height(8.dp))""", """                2 -> {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(8.dp))""")

content = content.replace("""                        }
                    }
                }
                3 -> {""", """                        }
                    }
                    }
                }
                3 -> {""")

content = content.replace("""                3 -> {
                    var presetSearchQuery""", """                3 -> {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        var presetSearchQuery""")

# End of tabs: there is a closing brace for the `when` we need to fix
content = content.replace("""                        }
                    }
                }
            }
        }
    }
}
}
}""", """                        }
                    }
                    }
                }
            }
        }
    }
}
""")

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)

