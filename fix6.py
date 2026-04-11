import re

# 1. Update Components.kt (ColorSlider thumbColor & BrightnessSlider cubic math)
with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'r') as f:
    comp = f.read()

# Add thumbColor to ColorSlider
comp = comp.replace('interpolationCurve: (Float) -> Float = { it },', 'interpolationCurve: (Float) -> Float = { it },\n    thumbColor: Color? = null,')
comp = comp.replace('val currentThumbColor = if (drawFullTrackGradient) {', 'val currentThumbColor = thumbColor ?: if (drawFullTrackGradient) {')

# Modify BrightnessSlider math
comp = re.sub(r'var targetValue by remember \{ mutableStateOf\(brightness\.toFloat\(\)\) \}', 'var targetValue by remember { mutableStateOf(Math.cbrt(brightness / 255.0).toFloat()) }', comp)
comp = re.sub(r'targetValue = brightness\.toFloat\(\)', 'targetValue = Math.cbrt(brightness / 255.0).toFloat()', comp)
comp = re.sub(r'targetValue = currentBrightness\.toFloat\(\)', 'targetValue = Math.cbrt(currentBrightness / 255.0).toFloat()', comp)
comp = re.sub(r'currentBrightness\.toFloat\(\) != targetValue', 'Math.cbrt(currentBrightness / 255.0).toFloat() != targetValue', comp)

# Modify Slider block inside BrightnessSlider
def repl_slider(m):
    s = m.group(0)
    s = s.replace('onBrightnessChange(newValue.toInt(), true)', 'onBrightnessChange((Math.pow(newValue.toDouble(), 3.0) * 255.0).toInt().coerceIn(1, 255), true)')
    s = s.replace('val intValue = newValue.toInt()', 'val intValue = (Math.pow(newValue.toDouble(), 3.0) * 255.0).toInt()')
    s = s.replace('onBrightnessChange(targetValue.toInt(), false)', 'onBrightnessChange((Math.pow(targetValue.toDouble(), 3.0) * 255.0).toInt().coerceIn(1, 255), false)')
    s = s.replace('valueRange = 1f..255f', 'valueRange = 0f..1f')
    return s

slider_pattern = r'Slider\(\s*value = animatedValue,[\s\S]*?valueRange = 1f\.\.255f,'
comp = re.sub(slider_pattern, repl_slider, comp)

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'w') as f:
    f.write(comp)


# 2. Update WledControlScreen.kt (Margins, Box size, cubic intensity math, white thumb)
with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Box around ColorWheel
# Change fillMaxWidth().height(290.dp) to size(340.dp) to be a perfect square larger than the wheel
ui = re.sub(r'Modifier\.fillMaxWidth\(\)\.height\(290\.dp\)', 'Modifier.size(340.dp)', ui)
# Remove padding(8.dp) from the IconButton so it sits exactly at the corner of the 340dp box
ui = ui.replace('modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)', 'modifier = Modifier.align(Alignment.TopEnd)')

# Sliders Margins
ui = ui.replace('padding(horizontal = 24.dp)', 'padding(horizontal = 16.dp)')
ui = ui.replace('padding(horizontal = 8.dp)', 'padding(horizontal = 4.dp)')

# Intensity slider cubic math + white thumb
intensity_pattern = r'com\.wled\.app\.ui\.components\.ColorSlider\(\s*value = kotlin\.math\.sqrt\(\(deviceState\?\.brightness \?: 128\) / 255f\) \* 255f,\s*onValueChange = \{[\s\S]*?repository\.setBrightness\(b, true\)\n\s*\},[\s\S]*?endColor = Color\(android\.graphics\.Color\.HSVToColor\(floatArrayOf\(selectedHue, selectedSaturation, 1f\)\)\)\n\s*\)'

new_intensity = """com.wled.app.ui.components.ColorSlider(
                                    value = Math.cbrt((deviceState?.brightness ?: 128) / 255.0).toFloat() * 255f,
                                    onValueChange = { 
                                        val f = it / 255f
                                        val b = (f * f * f * 255f).toInt().coerceIn(0, 255)
                                        repository.setBrightness(b, true)
                                    },
                                    baseColor = Color.White,
                                    thumbColor = Color.White,
                                    drawFullTrackGradient = true,
                                    startColor = Color.Black,
                                    endColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f)))
                                )"""

ui = re.sub(intensity_pattern, new_intensity, ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)
