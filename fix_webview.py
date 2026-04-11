import re

with open('app/src/main/java/com/wled/app/ui/screens/WledWebViewScreen.kt', 'r') as f:
    content = f.read()

# Imports
if "import androidx.compose.foundation.shape.RoundedCornerShape" not in content:
    content = content.replace("import androidx.compose.foundation.layout.padding\n", "import androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.foundation.layout.Box\n")

# Wrap WledWebView in a Box with top rounded corners
pattern = r'(WledWebView\(\s*url = device\.url,\s*reloadTrigger = reloadTrigger,\s*modifier = Modifier\s*\.fillMaxSize\(\)\s*\.padding\(paddingValues\)\s*\))'
replacement = r'''Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            \1
        }'''
content = re.sub(pattern, replacement, content)
content = content.replace('.padding(paddingValues)', '')

# The Scaffold topBar color
content = content.replace('containerColor = androidx.compose.ui.graphics.Color.Transparent,', 'containerColor = MaterialTheme.colorScheme.background,')
content = content.replace('containerColor = androidx.compose.ui.graphics.Color.Transparent', 'containerColor = MaterialTheme.colorScheme.background')

with open('app/src/main/java/com/wled/app/ui/screens/WledWebViewScreen.kt', 'w') as f:
    f.write(content)
