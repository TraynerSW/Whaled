import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# 1. Rapprocher le bouton paramètre / Réduire marge max-height
# Replace Box size 340.dp with 300.dp
ui = ui.replace('Modifier.size(340.dp)', 'Modifier.size(300.dp)')

# 2. Réduire marge hexadécimal
pattern_hex_margin = r'(Box\([\s\S]*?Modifier\.size\(300\.dp\)[\s\S]*?\}\n\s*)Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n\s*Spacer\(modifier = Modifier\.height\(16\.dp\)\)'
ui = re.sub(pattern_hex_margin, r'\1Spacer(modifier = Modifier.height(8.dp))', ui)
# In case the exact spacers changed:
ui = re.sub(r'Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n\s*Spacer\(modifier = Modifier\.height\(16\.dp\)\)\n\s*// Hex input', r'Spacer(modifier = Modifier.height(8.dp))\n                        // Hex input', ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'r') as f:
    comp = f.read()

# 4. Fix ColorWheel thumb color
# Replace `color = Color(android.graphics.Color.HSVToColor(floatArrayOf(animatedHue, animatedSaturation, 1f))),`
# with a normalized hue.
pattern_thumb = r'color = Color\(android\.graphics\.Color\.HSVToColor\(floatArrayOf\(animatedHue, animatedSaturation, 1f\)\)\)'
replacement = 'color = Color(android.graphics.Color.HSVToColor(floatArrayOf(((animatedHue % 360f) + 360f) % 360f, animatedSaturation, 1f)))'
comp = comp.replace(pattern_thumb, replacement)

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'w') as f:
    f.write(comp)
