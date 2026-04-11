import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

# Replace the switchColors declaration
old_colors = r"""val basePrimary = MaterialTheme\.colorScheme\.primary
\s*val hsv = FloatArray\(3\)
\s*android\.graphics\.Color\.colorToHSV\(basePrimary\.toArgb\(\), hsv\)
\s*// Rendre la couleur Material You plus colorée \(\+50% saturation\) et plus foncée \(-30% luminosité\)
\s*hsv\[1\] = \(hsv\[1\] \* 1\.5f\)\.coerceAtMost\(1f\)
\s*hsv\[2\] = \(hsv\[2\] \* 0\.7f\)\.coerceAtMost\(1f\)
\s*val vividDarkPrimary = Color\(android\.graphics\.Color\.HSVToColor\(hsv\)\)
\s*
\s*val isLightTheme = MaterialTheme\.colorScheme\.surface\.luminance\(\) > 0\.5f
\s*val switchColors = androidx\.compose\.material3\.SwitchDefaults\.colors\(
\s*checkedThumbColor = if \(isLightTheme\) vividDarkPrimary else Color\.White,
\s*checkedTrackColor = vividDarkPrimary\.copy\(alpha = 0\.5f\),
\s*checkedBorderColor = Color\.Transparent
\s*\)"""

new_colors = """val basePrimary = MaterialTheme.colorScheme.primary
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(basePrimary.toArgb(), hsv)
        hsv[1] = 1f // Saturation maximale
        hsv[2] = 1f // Luminosité maximale
        val vividPrimary = Color(android.graphics.Color.HSVToColor(hsv))
        
        val switchColors = androidx.compose.material3.SwitchDefaults.colors(
            checkedThumbColor = vividPrimary,
            checkedTrackColor = vividPrimary.copy(alpha = 0.6f),
            checkedBorderColor = Color.Transparent
        )"""

content = re.sub(old_colors, new_colors, content)

# Add colors = switchColors back to all Switches in the settings
switch_pattern = r"""(Switch\(\s*checked = (?:showIntensitySlider|showCctSlider|showRgbSliders|showHexInput|showPresetColors|showCustomColors),\s*onCheckedChange = \{ [^}]+\}\s*)\)"""
content = re.sub(switch_pattern, r'\1, colors = switchColors\n                            )', content)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)
