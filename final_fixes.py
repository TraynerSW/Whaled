import re

# 1. Update WledControlScreen.kt
with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Fix Card ripples in Tab 1, 2, 3
ui = ui.replace('modifier = Modifier\n                                    .fillMaxWidth()\n                                    .clickable { repository.setEffect(device.ip, effectId) }',
                'modifier = Modifier\n                                    .fillMaxWidth()\n                                    .clip(RoundedCornerShape(16.dp))\n                                    .clickable { repository.setEffect(device.ip, effectId) }')

ui = ui.replace('modifier = Modifier\n                                        .fillMaxWidth()\n                                        .clickable { segmentToEdit = segment }',
                'modifier = Modifier\n                                        .fillMaxWidth()\n                                        .clip(RoundedCornerShape(16.dp))\n                                        .clickable { segmentToEdit = segment }')

ui = ui.replace('modifier = Modifier\n                                    .fillMaxWidth()\n                                    .clickable { presetId.toIntOrNull()?.let { repository.setPreset(device.ip, it) } }',
                'modifier = Modifier\n                                    .fillMaxWidth()\n                                    .clip(RoundedCornerShape(16.dp))\n                                    .clickable { presetId.toIntOrNull()?.let { repository.setPreset(device.ip, it) } }')

# Ensure color shapes have clip BEFORE clickable. 
# They actually do! `Modifier.width(35.dp).height(31.dp).clip(RoundedCornerShape(50))\n                                        .background(color)\n                                        .clickable`
# Let's ensure the background is applied properly. Compose clip applies to the canvas. Ripple should be clipped.

# Fix TopAppBar transparent overlap
ui = ui.replace('containerColor = Color.Transparent,\n                    navigationIconContentColor', 'containerColor = MaterialTheme.colorScheme.background,\n                    navigationIconContentColor')

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)

# 2. Update Components.kt
with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'r') as f:
    comp = f.read()

# Fix getDeviceColor to always be fully bright
new_getDeviceColor = """fun getDeviceColor(state: WledState?, realtimeColor: Int?, savedColor: Int?): Color {
    val stateColor = state?.segments?.firstOrNull()?.colors?.firstOrNull()?.toArgb()
    val colorInt = realtimeColor ?: stateColor ?: savedColor ?: 0xFFFF5722.toInt()
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(colorInt, hsv)
    hsv[2] = 1f
    return Color(android.graphics.Color.HSVToColor(hsv))
}"""

comp = re.sub(r'fun getDeviceColor\([\s\S]*?\}\n', new_getDeviceColor + '\n', comp)

# Fix DeviceCard ripple
comp = comp.replace('if (isOled) Modifier.border(1.dp, baseColor, RoundedCornerShape(24.dp)) else Modifier\n            )\n            .then(', 'if (isOled) Modifier.border(1.dp, baseColor, RoundedCornerShape(24.dp)) else Modifier\n            )\n            .clip(RoundedCornerShape(24.dp))\n            .then(')

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'w') as f:
    f.write(comp)
