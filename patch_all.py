import re

# 1. Update version in build.gradle.kts
with open('app/build.gradle.kts', 'r') as f:
    content = f.read()
content = content.replace('versionCode = 6', 'versionCode = 7')
content = content.replace('versionName = "0.5.0"', 'versionName = "0.5.1"')
with open('app/build.gradle.kts', 'w') as f:
    f.write(content)

# 2. Update WledControlScreen.kt
with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

# Change label
content = content.replace('Text("Couleurs (par défaut)")', 'Text("Couleurs (de base)")')

# Remove custom switch colors
# We'll just replace 'colors = switchColors' with '' (or remove the line)
content = re.sub(r',\s*colors\s*=\s*switchColors', '', content)

# Replace OutlinedTextField with BasicTextField for Hex input
hex_old = r"""OutlinedTextField\(
\s*value = hexText,
\s*onValueChange = \{ 
\s*hexText = it\.uppercase\(\)
\s*if \(it\.length == 7 && it\.startsWith\("#"\)\) \{
\s*try \{
\s*val colorInt = android\.graphics\.Color\.parseColor\(it\)
\s*val newColor = Color\(colorInt\)
\s*val hsv = FloatArray\(3\)
\s*android\.graphics\.Color\.colorToHSV\(newColor\.toArgb\(\), hsv\)
\s*selectedHue = hsv\[0\]
\s*selectedSaturation = hsv\[1\]
\s*selectedValue = kotlin\.math\.sqrt\(hsv\[2\]\.toDouble\(\)\)\.toFloat\(\)
\s*
\s*val newColorInt = android\.graphics\.Color\.HSVToColor\(floatArrayOf\(hsv\[0\], hsv\[1\], selectedValue \* selectedValue\)\)
\s*repository\.setColor\(device\.ip, newColorInt\)
\s*\} catch \(e: Exception\) \{
\s*// Ignore invalid hex
\s*\}
\s*\}
\s*\},
\s*modifier = Modifier\.width\(110\.dp\)\.height\(48\.dp\),
\s*singleLine = true,
\s*textStyle = androidx\.compose\.ui\.text\.TextStyle\(textAlign = androidx\.compose\.ui\.text\.style\.TextAlign\.Center, fontWeight = FontWeight\.Bold\),
\s*shape = RoundedCornerShape\(16\.dp\),
\s*colors = androidx\.compose\.material3\.OutlinedTextFieldDefaults\.colors\(
\s*focusedBorderColor = displayColor,
\s*focusedLabelColor = displayColor,
\s*cursorColor = displayColor
\s*\)
\s*\)"""

hex_new = """androidx.compose.foundation.text.BasicTextField(
                                value = hexText,
                                onValueChange = { 
                                    hexText = it.uppercase()
                                    if (it.length == 7 && it.startsWith("#")) {
                                        try {
                                            val colorInt = android.graphics.Color.parseColor(it)
                                            val newColor = Color(colorInt)
                                            val hsv = FloatArray(3)
                                            android.graphics.Color.colorToHSV(newColor.toArgb(), hsv)
                                            selectedHue = hsv[0]
                                            selectedSaturation = hsv[1]
                                            selectedValue = kotlin.math.sqrt(hsv[2].toDouble()).toFloat()
                                            
                                            val newColorInt = android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], selectedValue * selectedValue))
                                            repository.setColor(device.ip, newColorInt)
                                        } catch (e: Exception) {
                                            // Ignore invalid hex
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(48.dp)
                                    .border(1.dp, displayColor, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center, 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.Center) {
                                        innerTextField()
                                    }
                                }
                            )"""

content = re.sub(hex_old, hex_new, content, flags=re.MULTILINE)

# Add focusManager logic
if 'val focusManager = androidx.compose.ui.platform.LocalFocusManager.current' not in content:
    content = content.replace('val haptic = LocalHapticFeedback.current', 
                              'val haptic = LocalHapticFeedback.current\n    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current')

# Add pointerInput to Scaffold
content = content.replace('Scaffold(\n        containerColor = androidx.compose.ui.graphics.Color.Transparent,',
                          'Scaffold(\n        modifier = Modifier.pointerInput(Unit) { androidx.compose.foundation.gestures.detectTapGestures(onTap = { focusManager.clearFocus() }) },\n        containerColor = androidx.compose.ui.graphics.Color.Transparent,')

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)
