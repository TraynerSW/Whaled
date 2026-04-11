import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

pattern = r'Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n\s*if \(showHexInput\) \{\nSpacer\(modifier = Modifier\.height\(16\.dp\)\)'
replacement = r'if (showHexInput) {\nSpacer(modifier = Modifier.height(8.dp))'
ui = re.sub(pattern, replacement, ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)
