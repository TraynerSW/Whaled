with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if 'import detectTapGestures' in line:
        lines[i] = 'import androidx.compose.foundation.gestures.detectTapGestures\n'

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.writelines(lines)
