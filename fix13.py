import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# 1. Ile flottante (Bottom Bar)
# Descendre l'ile (padding bottom 16 -> 8)
ui = ui.replace('.navigationBarsPadding()\n                    .padding(bottom = 16.dp)', '.navigationBarsPadding()\n                    .padding(bottom = 8.dp)')
# Réduire largeur/hauteur (padding horizontal 24 -> 16, vertical 12 -> 8, spacedBy 16 -> 8)
ui = ui.replace('modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),\n                        horizontalArrangement = Arrangement.spacedBy(16.dp),', 'modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),\n                        horizontalArrangement = Arrangement.spacedBy(8.dp),')

# 2. Marge haut (Color Wheel)
# Remove Spacer(modifier = Modifier.height(4.dp)) at the start of Tab 0
ui = ui.replace('Spacer(modifier = Modifier.height(4.dp))\n\n                        Box(', 'Box(')

# 3. Marges entre les blocs (Tab 0)
# Spacers of 24.dp in Tab 0:
# Hex -> Sliders
ui = re.sub(r'(\}\n\s*)Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n(\s*if \(showIntensitySlider\) \{)', r'\1Spacer(modifier = Modifier.height(12.dp))\n\2', ui)
# Sliders -> Preset
ui = re.sub(r'(\}\n\s*)Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n(\s*FlowRow)', r'\1Spacer(modifier = Modifier.height(12.dp))\n\2', ui)
# Preset -> Custom
ui = re.sub(r'(FlowRow[\s\S]*?\}\n\s*)Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n(\s*FlowRow)', r'\1Spacer(modifier = Modifier.height(12.dp))\n\2', ui)
# Custom -> RGB
ui = re.sub(r'(FlowRow[\s\S]*?\}\n\s*)Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n(\s*if \(showRgbSliders\) \{)', r'\1Spacer(modifier = Modifier.height(12.dp))\n\2', ui)

# In case the regex misses due to slight differences, let's just globally replace the 24.dp spacers in the tab 0 area.
# The tab 0 content is roughly from "0 -> {" to "1 -> {". We can just replace all 24.dp with 16.dp or 12.dp.
def reduce_spacers(match):
    text = match.group(0)
    text = text.replace('Spacer(modifier = Modifier.height(24.dp))', 'Spacer(modifier = Modifier.height(12.dp))')
    return text

ui = re.sub(r'0 -> \{[\s\S]*?1 -> \{', reduce_spacers, ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)
