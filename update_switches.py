import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

# Replace the switchColors declaration
old_colors = r"""val switchColors = androidx\.compose\.material3\.SwitchDefaults\.colors\(
\s*checkedThumbColor = vividPrimary,
\s*checkedTrackColor = vividPrimary\.copy\(alpha = 0\.6f\),
\s*checkedBorderColor = Color\.Transparent
\s*\)"""

new_colors = """val switchColors = androidx.compose.material3.SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = vividPrimary,
            checkedBorderColor = Color.Transparent
        )"""

content = re.sub(old_colors, new_colors, content)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)
