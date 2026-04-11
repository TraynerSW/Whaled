import re
with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('colors = switchColors\n                            , colors = switchColors', 'colors = switchColors')
content = content.replace(', colors = switchColors\n                            , colors = switchColors', ', colors = switchColors')
content = re.sub(r'colors = switchColors\s*, colors = switchColors', 'colors = switchColors', content)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)
