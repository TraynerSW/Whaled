import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Add Switch import
if "import androidx.compose.material3.Switch" not in ui:
    ui = ui.replace("import androidx.compose.material3.Scaffold", "import androidx.compose.material3.Scaffold\nimport androidx.compose.material3.Switch")

# Add state variables after `val sharedPrefs = ...`
state_vars = """
    var showColorSettingsDialog by remember { mutableStateOf(false) }
    var showHexInput by remember { mutableStateOf(sharedPrefs.getBoolean("show_hex", true)) }
    var showIntensitySlider by remember { mutableStateOf(sharedPrefs.getBoolean("show_intensity", true)) }
    var showCctSlider by remember { mutableStateOf(sharedPrefs.getBoolean("show_cct", true)) }
    var showRgbSliders by remember { mutableStateOf(sharedPrefs.getBoolean("show_rgb", true)) }
"""
ui = ui.replace('val sharedPrefs = context.getSharedPreferences("wled_custom_colors", Context.MODE_PRIVATE)', 'val sharedPrefs = context.getSharedPreferences("wled_custom_colors", Context.MODE_PRIVATE)' + state_vars)

# Update onClick for settings button
ui = ui.replace('onClick = { showEditNameDialog = true },\n                                modifier = Modifier.align(Alignment.TopEnd)', 'onClick = { showColorSettingsDialog = true },\n                                modifier = Modifier.align(Alignment.TopEnd)')

# Add Color Settings Dialog right before Scaffold
dialog_code = """
    if (showColorSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showColorSettingsDialog = false },
            title = { Text("Paramètres d'affichage") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Code Hexadécimal")
                        Switch(checked = showHexInput, onCheckedChange = { showHexInput = it; sharedPrefs.edit().putBoolean("show_hex", it).apply() })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Curseur d'intensité")
                        Switch(checked = showIntensitySlider, onCheckedChange = { showIntensitySlider = it; sharedPrefs.edit().putBoolean("show_intensity", it).apply() })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Température (CCT)")
                        Switch(checked = showCctSlider, onCheckedChange = { showCctSlider = it; sharedPrefs.edit().putBoolean("show_cct", it).apply() })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Curseurs RGB")
                        Switch(checked = showRgbSliders, onCheckedChange = { showRgbSliders = it; sharedPrefs.edit().putBoolean("show_rgb", it).apply() })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorSettingsDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
    
    Scaffold"""
ui = ui.replace('    Scaffold', dialog_code)

# Hex input block wrap
pattern_hex = r'(Spacer\(modifier = Modifier\.height\(16\.dp\)\)\s*// Hex input\s*var hexText by remember\(displayColor\) \{[\s\S]*?Spacer\(modifier = Modifier\.height\(24\.dp\)\))'
match = re.search(pattern_hex, ui)
if match:
    wrapped_hex = "                        if (showHexInput) {\n" + match.group(1).replace('\n', '\n    ') + "\n                        }"
    ui = ui.replace(match.group(1), wrapped_hex)

# Intensity & CCT wrap
pattern_intensity = r'(com\.wled\.app\.ui\.components\.ColorSlider\(\s*value = selectedValue \* 255f,[\s\S]*?interpolationCurve = \{ fraction -> kotlin\.math\.sqrt\(fraction\.toDouble\(\)\)\.toFloat\(\) \}\n\s*\))'
match = re.search(pattern_intensity, ui)
if match:
    wrapped_int = "                            if (showIntensitySlider) {\n" + match.group(1).replace('\n', '\n    ') + "\n                            }"
    ui = ui.replace(match.group(1), wrapped_int)

pattern_cct = r'(com\.wled\.app\.ui\.components\.ColorSlider\(\s*value = selectedCct\.toFloat\(\),[\s\S]*?endColor = Color\(0xFFE3EFFF\)\n\s*\))'
match = re.search(pattern_cct, ui)
if match:
    wrapped_cct = "                            if (showCctSlider) {\n" + match.group(1).replace('\n', '\n    ') + "\n                            }"
    ui = ui.replace(match.group(1), wrapped_cct)

# RGB wrap
pattern_rgb_col = r'(Column\(verticalArrangement = Arrangement\.spacedBy\(1\.dp\), modifier = Modifier\.fillMaxWidth\(\)\) \{\s*com\.wled\.app\.ui\.components\.RgbSlider\(\s*value = \(displayColor\.red \* 255\)\.coerceIn\(0f, 255f\),[\s\S]*?color = Color\.Blue\n\s*\)\s*\}\s*Spacer\(modifier = Modifier\.height\(24\.dp\)\))'
match = re.search(pattern_rgb_col, ui)
if match:
    wrapped_rgb = "                        if (showRgbSliders) {\n" + match.group(1).replace('\n', '\n    ') + "\n                        }"
    ui = ui.replace(match.group(1), wrapped_rgb)

# Ovals
ui = ui.replace('Modifier.width(40.dp).height(32.dp)', 'Modifier.width(36.dp).height(32.dp)')

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)
