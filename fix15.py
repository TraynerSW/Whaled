import re

# 1. Update Components.kt
with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'r') as f:
    comp = f.read()

# Fix getDeviceColor to incorporate master brightness
pattern_getdevicecolor = r'fun getDeviceColor\([\s\S]*?\}\n'
new_getdevicecolor = """fun getDeviceColor(state: WledState?, realtimeColor: Int?, savedColor: Int?): Color {
    val stateColor = state?.segments?.firstOrNull()?.colors?.firstOrNull()?.toArgb()
    val colorInt = realtimeColor ?: stateColor ?: savedColor ?: 0xFFFF5722.toInt()
    val brightness = state?.brightness ?: 255
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(colorInt, hsv)
    hsv[2] = hsv[2] * (brightness / 255f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}
"""
comp = re.sub(pattern_getdevicecolor, new_getdevicecolor, comp, count=1)

# Modify ColorWheel signature to include `value`
comp = comp.replace('fun ColorWheel(\n    hue: Float,\n    onHueChange: (Float) -> Unit,\n    saturation: Float,\n    onSaturationChange: (Float) -> Unit,\n',
                    'fun ColorWheel(\n    hue: Float,\n    onHueChange: (Float) -> Unit,\n    saturation: Float,\n    onSaturationChange: (Float) -> Unit,\n    value: Float = 1f,\n')

# Change spring to tween in ColorWheel to avoid mini-circle (bounce)
comp = comp.replace('animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)', 'animationSpec = tween(durationMillis = 200)')

# Update ColorWheel thumb to use `value`
pattern_thumb_color = r'color = Color\(android\.graphics\.Color\.HSVToColor\(floatArrayOf\(currentHue, currentSat, 1f\)\)\)'
comp = comp.replace(pattern_thumb_color, 'color = Color(android.graphics.Color.HSVToColor(floatArrayOf(currentHue, currentSat, value)))')

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'w') as f:
    f.write(comp)


# 2. Update WledControlScreen.kt
with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Pass value to ColorWheel
pattern_cw_call = r'ColorWheel\(\s*hue = selectedHue,[\s\S]*?onInteractionEnd = \{ isUserDraggingColor = false \}\n\s*\)'
def repl_cw(match):
    text = match.group(0)
    return text.replace('modifier = Modifier.size(280.dp),', 'value = selectedValue,\n                                modifier = Modifier.size(280.dp),')

ui = re.sub(pattern_cw_call, repl_cw, ui)

# Change intensity slider back to controlling `selectedValue`
pattern_intensity = r'com\.wled\.app\.ui\.components\.ColorSlider\(\s*value = Math\.cbrt\(\(deviceState\?\.brightness \?: 128\) / 255\.0\)\.toFloat\(\) \* 255f,[\s\S]*?repository\.setBrightness\(b, true\)\n\s*\},[\s\S]*?endColor = Color\(android\.graphics\.Color\.HSVToColor\(floatArrayOf\(selectedHue, selectedSaturation, 1f\)\)\)\n\s*\)'

new_intensity = """com.wled.app.ui.components.ColorSlider(
                                    value = Math.cbrt(selectedValue.toDouble()).toFloat() * 255f,
                                    onValueChange = { 
                                        val f = it / 255f
                                        selectedValue = (f * f * f).coerceIn(0f, 1f)
                                        isUserDraggingColor = true
                                        val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, selectedValue))
                                        repository.setColor(device.ip, colorInt)
                                    },
                                    baseColor = Color.White,
                                    thumbColor = Color.White,
                                    drawFullTrackGradient = true,
                                    startColor = Color.Black,
                                    endColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f)))
                                )"""

ui = re.sub(pattern_intensity, new_intensity, ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)

