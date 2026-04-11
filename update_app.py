import re

# 1. Update WledRepository.kt
with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'r') as f:
    repo_content = f.read()

repo_content = repo_content.replace('lastBrightnessSentTime > 10', 'lastBrightnessSentTime > 5')
repo_content = repo_content.replace('lastColorSentTime > 10', 'lastColorSentTime > 5')
repo_content = repo_content.replace('lastWhiteSentTime > 10', 'lastWhiteSentTime > 5')
repo_content = repo_content.replace('kotlinx.coroutines.delay(10)', 'kotlinx.coroutines.delay(5)')
repo_content = repo_content.replace('delay(10)', 'delay(5)')

with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'w') as f:
    f.write(repo_content)

# 2. Update WledControlScreen.kt
with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Make sure imports are there
if "import androidx.compose.material.icons.filled.Settings" not in ui:
    ui = ui.replace("import androidx.compose.material.icons.filled.Check", "import androidx.compose.material.icons.filled.Settings\nimport androidx.compose.material.icons.filled.Check")

if "import androidx.compose.foundation.layout.height" not in ui:
    ui = ui.replace("import androidx.compose.foundation.layout.height", "import androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.width")

# Size of color wheel
ui = ui.replace("Modifier.size(220.dp)", "Modifier.size(300.dp)")
ui = ui.replace("Modifier.size(200.dp)", "Modifier.size(290.dp)")

# Title colors removal
ui = re.sub(r'Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n+\s*Text\(\n\s*text = "Couleurs prédéfinies",\n\s*style = MaterialTheme\.typography\.labelMedium,\n\s*color = MaterialTheme\.colorScheme\.onSurface\.copy\(alpha = 0\.7f\)\n\s*\)\n+\s*Spacer\(modifier = Modifier\.height\(16\.dp\)\)', 'Spacer(modifier = Modifier.height(24.dp))', ui, flags=re.DOTALL)

ui = re.sub(r'Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n+\s*Text\(\n\s*text = "Couleurs personnalisées",\n\s*style = MaterialTheme\.typography\.labelMedium,\n\s*color = MaterialTheme\.colorScheme\.onSurface\.copy\(alpha = 0\.7f\)\n\s*\)\n+\s*Spacer\(modifier = Modifier\.height\(16\.dp\)\)', 'Spacer(modifier = Modifier.height(24.dp))', ui, flags=re.DOTALL)

# Oval instead of Circle
# For preset colors
ui = ui.replace("Modifier.size(32.dp).clip(CircleShape)", "Modifier.width(48.dp).height(32.dp).clip(RoundedCornerShape(50))")
# There are 3 instances (preset, custom, and the "add" button). 
# But wait, it's spread across lines. 
ui = re.sub(r'Modifier\s*\n\s*\.size\(32\.dp\)\s*\n\s*\.clip\(CircleShape\)', 'Modifier.width(48.dp).height(32.dp).clip(RoundedCornerShape(50))', ui)

# Hex input
# I'll add the hex input just below the ColorWheel Box.
# Let's find "Spacer(modifier = Modifier.height(24.dp))" right after ColorWheel.
hex_input = """Spacer(modifier = Modifier.height(16.dp))
                        
                        // Hex input
                        var hexText by remember(displayColor) { 
                            val r = (displayColor.red * 255).toInt()
                            val g = (displayColor.green * 255).toInt()
                            val b = (displayColor.blue * 255).toInt()
                            mutableStateOf(String.format("#%02X%02X%02X", r, g, b))
                        }
                        
                        OutlinedTextField(
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
                                        selectedValue = hsv[2]
                                        repository.setColor(device.ip, colorInt)
                                    } catch (e: Exception) {
                                        // Ignore invalid hex
                                    }
                                }
                            },
                            modifier = Modifier.width(150.dp).height(56.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = displayColor,
                                focusedLabelColor = displayColor,
                                cursorColor = displayColor
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))"""

if "import androidx.compose.ui.text.font.FontWeight" not in ui:
    ui = ui.replace("import androidx.compose.ui.graphics.toArgb\n", "import androidx.compose.ui.graphics.toArgb\nimport androidx.compose.ui.text.font.FontWeight\n")

pattern = r'(ColorWheel\([\s\S]*?Modifier\.size\(290\.dp\),[\s\S]*?onInteractionEnd = \{ isUserDraggingColor = false \}\n\s*\)\n\s*\}\n\s*)Spacer\(modifier = Modifier\.height\(24\.dp\)\)'
match = re.search(pattern, ui)
if match:
    ui = ui[:match.end()] + "\n" + hex_input + ui[match.end():]
else:
    print("Could not find insertion point for hex input.")

# Add settings button
settings_btn = """IconButton(onClick = { showEditNameDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres"
                        )
                    }"""

pattern2 = r'actions = \{\n\s*IconButton\(onClick = onOpenFullWled\) \{'
ui = re.sub(pattern2, 'actions = {\n                    ' + settings_btn + '\n                    IconButton(onClick = onOpenFullWled) {', ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)

