import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# 1. Eloigner le bouton paramètre de la ColorWheel
pattern_box = r'Box\(\s*modifier = Modifier\.size\(280\.dp\),\s*contentAlignment = Alignment\.Center\s*\)'
ui = re.sub(pattern_box, 'Box(\n                            modifier = Modifier.fillMaxWidth().height(290.dp),\n                            contentAlignment = Alignment.Center\n                        )', ui)

# 2. Intensity slider : couleur blanche du rond + courbe d'intensité exponentielle
pattern_intensity = r'if \(showIntensitySlider\) \{\s*com\.wled\.app\.ui\.components\.ColorSlider\(\s*value = \(deviceState\?\.brightness \?: 128\)\.toFloat\(\),[\s\S]*?interpolationCurve = \{ fraction -> kotlin\.math\.sqrt\(fraction\.toDouble\(\)\)\.toFloat\(\) \}\n\s*\)\n\s*\}'

new_intensity = """if (showIntensitySlider) {
                                com.wled.app.ui.components.ColorSlider(
                                    value = kotlin.math.sqrt((deviceState?.brightness ?: 128) / 255f) * 255f,
                                    onValueChange = { 
                                        val f = it / 255f
                                        val b = (f * f * 255f).toInt().coerceIn(0, 255)
                                        repository.setBrightness(b, true)
                                    },
                                    baseColor = Color.White,
                                    drawFullTrackGradient = true,
                                    startColor = Color.Black,
                                    endColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f)))
                                )
                            }"""

ui = re.sub(pattern_intensity, new_intensity, ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)
