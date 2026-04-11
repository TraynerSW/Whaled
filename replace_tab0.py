import sys

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

start_marker = "0 -> {\n                    Spacer(modifier = Modifier.height(4.dp))"
end_marker = "                1 -> {\n                    var effectSearchQuery by remember { mutableStateOf(\"\") }"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx == -1 or end_idx == -1:
    print("Markers not found")
    sys.exit(1)

new_content = """0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
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
                                    val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(it, selectedSaturation, selectedValue))
                                    repository.setColor(device.ip, colorInt)
                                },
                                saturation = selectedSaturation,
                                onSaturationChange = { 
                                    selectedSaturation = it
                                    isUserDraggingColor = true 
                                    val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, it, selectedValue))
                                    repository.setColor(device.ip, colorInt)
                                },
                                modifier = Modifier.size(200.dp),
                                onInteractionStart = { isUserDraggingColor = true },
                                onInteractionEnd = { isUserDraggingColor = false }
                            )
                        }

                        // Un écart important (24dp) doit séparer la roue chromatique de la grille de couleurs personnalisées
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Couleurs prédéfinies",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
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
                                            
                                            val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], selectedValue))
                                            repository.setColor(device.ip, colorInt)
                                        }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Couleurs personnalisées",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
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
                                                
                                                val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], selectedValue))
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

                        // Un écart important (24dp) doit séparer la grille des curseurs RGB
                        Spacer(modifier = Modifier.height(24.dp))

                        // Sliders: Intensity, Température, RGB
                        // Les espacements entre les curseurs doivent être de 1dp.
                        // La hauteur interne des curseurs est fixée à 28dp. (Already in ColorSlider/RgbSlider)
                        
                        // Intensité Slider
                        val pureColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f)))
                        ColorSlider(
                            value = selectedValue * 255f,
                            onValueChange = { 
                                selectedValue = it / 255f
                                val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, selectedValue))
                                repository.setColor(device.ip, colorInt)
                            },
                            baseColor = pureColor,
                            startColor = Color.Black,
                            endColor = pureColor,
                            drawFullTrackGradient = true,
                            interpolationCurve = { fraction -> kotlin.math.sqrt(fraction.toDouble()).toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(1.dp))
                        
                        // CCT (Température) Slider
                        // 0 = Orange (1900K), 255 = Blanc/Bleu (10091K)
                        // Assuming deviceState?.cct or fallback.
                        val currentCct = deviceState?.segments?.firstOrNull()?.cct ?: 128
                        ColorSlider(
                            value = currentCct.toFloat(),
                            onValueChange = { 
                                val cctInt = it.toInt()
                                repository.setWhiteTemperature(device.ip, cctInt)
                            },
                            baseColor = Color.White,
                            startColor = Color(0xFFFF9900), // Orange
                            endColor = Color.White, // Or slight blue
                            drawFullTrackGradient = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(1.dp))

                        // R Slider
                        RgbSlider(
                            value = displayColor.red * 255f,
                            onValueChange = { r ->
                                val colorInt = android.graphics.Color.rgb(
                                    r.toInt(),
                                    (displayColor.green * 255).toInt(),
                                    (displayColor.blue * 255).toInt()
                                )
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(colorInt, hsv)
                                selectedHue = hsv[0]
                                selectedSaturation = hsv[1]
                                selectedValue = hsv[2]
                                repository.setColor(device.ip, colorInt)
                            },
                            color = Color.Red,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(1.dp))

                        // G Slider
                        RgbSlider(
                            value = displayColor.green * 255f,
                            onValueChange = { g ->
                                val colorInt = android.graphics.Color.rgb(
                                    (displayColor.red * 255).toInt(),
                                    g.toInt(),
                                    (displayColor.blue * 255).toInt()
                                )
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(colorInt, hsv)
                                selectedHue = hsv[0]
                                selectedSaturation = hsv[1]
                                selectedValue = hsv[2]
                                repository.setColor(device.ip, colorInt)
                            },
                            color = Color.Green,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(1.dp))

                        // B Slider
                        RgbSlider(
                            value = displayColor.blue * 255f,
                            onValueChange = { b ->
                                val colorInt = android.graphics.Color.rgb(
                                    (displayColor.red * 255).toInt(),
                                    (displayColor.green * 255).toInt(),
                                    b.toInt()
                                )
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(colorInt, hsv)
                                selectedHue = hsv[0]
                                selectedSaturation = hsv[1]
                                selectedValue = hsv[2]
                                repository.setColor(device.ip, colorInt)
                            },
                            color = Color.Blue,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
"""

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content[:start_idx] + new_content + content[end_idx:])
