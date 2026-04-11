import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Make the page vertically centered:
# Find:
# 0 -> {
#     Column(
#         modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
#         horizontalAlignment = Alignment.CenterHorizontally
#     ) {
pattern_col = r'(0 -> \{\s*Column\(\s*modifier = Modifier\.fillMaxSize\(\)\.verticalScroll\(rememberScrollState\(\)\),\s*horizontalAlignment = Alignment\.CenterHorizontally\n\s*)(\) \{)'
# We add verticalArrangement = Arrangement.Center to it
ui = re.sub(pattern_col, r'\1, verticalArrangement = Arrangement.Center\n\2', ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'r') as f:
    comp = f.read()

# Fix BrightnessSlider to allow 0 (coerceIn(0, 255))
comp = comp.replace('coerceIn(1, 255)', 'coerceIn(0, 255)')

# Wait, in WledControlScreen for ColorSlider (intensity):
# value is passed as `Math.cbrt((deviceState?.brightness ?: 128) / 255.0).toFloat() * 255f`
# But in `Components.kt`, `ColorSlider` has `valueRange = 0f..255f`. 
# Wait, did I mess up ColorSlider in WledControlScreen or BrightnessSlider?
# The user said: "pourquoi je peux pas mettre le curseur à 0 sur l'intensité ??????"

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'w') as f:
    f.write(comp)
