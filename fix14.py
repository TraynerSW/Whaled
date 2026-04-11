import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Fix the trailing braces
ui = ui.replace('1065-}\n1066-}\n1067-}', '1065-}\n1066-}') # I can't just replace line numbers

pattern = r'(\s*\}\n\s*\}\n\s*\}\n\s*\}\n\s*\}\n\s*\}\n\s*\}\n\s*\}\n\s*\}\n\s*\}\n)\s*@Composable\nprivate fun AnimatedNavIcon'
# Wait, let's just count braces or manually remove one.
ui = ui.replace('}\n}\n}\n\n@Composable\nprivate fun AnimatedNavIcon', '}\n}\n\n@Composable\nprivate fun AnimatedNavIcon')

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)
