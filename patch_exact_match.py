import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

# For presetColors
preset_old = r"""val isSelected = remember\(selectedHue, selectedSaturation, color\) \{
\s*if \(color == Color\.Transparent\) false
\s*else \{
\s*val hsv = FloatArray\(3\)
\s*android\.graphics\.Color\.colorToHSV\(color\.toArgb\(\), hsv\)
\s*kotlin\.math\.abs\(hsv\[0\] - selectedHue\) < 2f && kotlin\.math\.abs\(hsv\[1\] - selectedSaturation\) < 0\.05f
\s*\}
\s*\}"""

preset_new = """val isSelected = remember(selectedHue, selectedSaturation, color) {
                                            if (color == Color.Transparent) false
                                            else {
                                                val c = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f)))
                                                color.red == c.red && color.green == c.green && color.blue == c.blue
                                            }
                                        }"""

content = re.sub(preset_old, preset_new, content)

# For customColors
custom_old = r"""val isSelected = remember\(selectedHue, selectedSaturation, color\) \{
\s*val hsv = FloatArray\(3\)
\s*android\.graphics\.Color\.colorToHSV\(color\.toArgb\(\), hsv\)
\s*kotlin\.math\.abs\(hsv\[0\] - selectedHue\) < 2f && kotlin\.math\.abs\(hsv\[1\] - selectedSaturation\) < 0\.05f
\s*\}"""

custom_new = """val isSelected = remember(selectedHue, selectedSaturation, color) {
                                            val c = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f)))
                                            color.red == c.red && color.green == c.green && color.blue == c.blue
                                        }"""

content = re.sub(custom_old, custom_new, content)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)
