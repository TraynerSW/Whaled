with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    lines = f.readlines()

# Remove the broken imports at the top
while lines[0].startswith('import '):
    lines.pop(0)

# Find the package line
pkg_idx = 0
for i, line in enumerate(lines):
    if line.startswith('package '):
        pkg_idx = i
        break

# Insert the imports right after the package
lines.insert(pkg_idx + 1, 'import androidx.compose.foundation.gestures.detectTapGestures\n')
lines.insert(pkg_idx + 2, 'import androidx.compose.ui.unit.sp\n')

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.writelines(lines)
