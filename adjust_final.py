import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# 1. Update Intensity slider to use master brightness instead of selectedValue
# We look for the ColorSlider block with interpolationCurve (which is the intensity slider)
intensity_pattern = r'com\.wled\.app\.ui\.components\.ColorSlider\(\s*value = selectedValue \* 255f,[\s\S]*?onValueChange = \{[\s\S]*?repository\.setColor\(device\.ip, colorInt\)[\s\S]*?\},[\s\S]*?baseColor = displayColor,[\s\S]*?interpolationCurve = \{ fraction -> kotlin\.math\.sqrt\(fraction\.toDouble\(\)\)\.toFloat\(\) \}\n\s*\)'

new_intensity = """com.wled.app.ui.components.ColorSlider(
                                    value = (deviceState?.brightness ?: 128).toFloat(),
                                    onValueChange = { 
                                        repository.setBrightness(it.toInt(), true)
                                    },
                                    baseColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, (deviceState?.brightness ?: 128) / 255f))),
                                    drawFullTrackGradient = true,
                                    startColor = Color.Black,
                                    endColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f))),
                                    interpolationCurve = { fraction -> kotlin.math.sqrt(fraction.toDouble()).toFloat() }
                                )"""

ui = re.sub(intensity_pattern, new_intensity, ui)

# 2. Add horizontal margins to sliders
# First block (Intensity, CCT)
ui = ui.replace('Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.fillMaxWidth()) {', 'Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {')
# RGB block (already has the same signature)

# 3. Reduce shape of custom/preset colors from 36x32 to 35x31
ui = ui.replace('Modifier.width(36.dp).height(32.dp)', 'Modifier.width(35.dp).height(31.dp)')

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)

# 4. Color Wheel shortest path animation
with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'r') as f:
    comp = f.read()

# Currently targetHue is updated via LaunchEffect and drag
pattern_wheel_effect = r'LaunchedEffect\(hue\) \{\n\s*if \(\!isDragging\) \{\n\s*targetHue = hue\n\s*\}\n\s*\}'
new_wheel_effect = """LaunchedEffect(hue) {
        if (!isDragging) {
            var diff = (hue - targetHue) % 360f
            if (diff > 180f) diff -= 360f
            if (diff < -180f) diff += 360f
            targetHue += diff
        }
    }"""
comp = re.sub(pattern_wheel_effect, new_wheel_effect, comp)

# Replace targetHue = angle with the diff logic in detectTapGestures
tap_pattern = r'if \(angle < 0\) angle \+= 360f\n\s*targetHue = angle'
new_tap = """if (angle < 0) angle += 360f
                            var diff = (angle - targetHue) % 360f
                            if (diff > 180f) diff -= 360f
                            if (diff < -180f) diff += 360f
                            targetHue += diff"""
comp = re.sub(tap_pattern, new_tap, comp)

# Wrap HSV color with normalized hue
draw_pattern = r'color = Color\(android\.graphics\.Color\.HSVToColor\(floatArrayOf\(animatedHue, animatedSaturation, 1f\)\)\)'
new_draw = 'color = Color(android.graphics.Color.HSVToColor(floatArrayOf(((animatedHue % 360f) + 360f) % 360f, animatedSaturation, 1f)))'
comp = comp.replace(draw_pattern, new_draw)

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'w') as f:
    f.write(comp)
