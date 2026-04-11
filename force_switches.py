import re
with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('onCheckedChange = { showIntensitySlider = it; sharedPrefs.edit().putBoolean("show_intensity", it).apply() }', 'onCheckedChange = { showIntensitySlider = it; sharedPrefs.edit().putBoolean("show_intensity", it).apply() },\n                                colors = switchColors')
content = content.replace('onCheckedChange = { showCctSlider = it; sharedPrefs.edit().putBoolean("show_cct", it).apply() }', 'onCheckedChange = { showCctSlider = it; sharedPrefs.edit().putBoolean("show_cct", it).apply() },\n                                colors = switchColors')
content = content.replace('onCheckedChange = { showRgbSliders = it; sharedPrefs.edit().putBoolean("show_rgb", it).apply() }', 'onCheckedChange = { showRgbSliders = it; sharedPrefs.edit().putBoolean("show_rgb", it).apply() },\n                                colors = switchColors')
content = content.replace('onCheckedChange = { showHexInput = it; sharedPrefs.edit().putBoolean("show_hex", it).apply() }', 'onCheckedChange = { showHexInput = it; sharedPrefs.edit().putBoolean("show_hex", it).apply() },\n                                colors = switchColors')
content = content.replace('onCheckedChange = { showPresetColors = it; sharedPrefs.edit().putBoolean("show_preset_colors", it).apply() }', 'onCheckedChange = { showPresetColors = it; sharedPrefs.edit().putBoolean("show_preset_colors", it).apply() },\n                                colors = switchColors')
content = content.replace('onCheckedChange = { showCustomColors = it; sharedPrefs.edit().putBoolean("show_custom_colors", it).apply() }', 'onCheckedChange = { showCustomColors = it; sharedPrefs.edit().putBoolean("show_custom_colors", it).apply() },\n                                colors = switchColors')

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)
