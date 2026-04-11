import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

hex_old = r"""decorationBox = \{ innerTextField ->
\s*Box\(contentAlignment = Alignment\.Center\) \{
\s*innerTextField\(\)
\s*\}
\s*\}"""

hex_new = """decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        innerTextField()
                                    }
                                }"""

content = re.sub(hex_old, hex_new, content)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)
