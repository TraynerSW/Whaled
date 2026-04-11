import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

# For preset colors
preset_pattern = re.compile(r'(presetColors\.forEach \{ color ->\n\s+)Box\(\n\s+modifier = Modifier\n\s+\.width\(42\.dp\)\n\s+\.height\(36\.dp\)\n\s+\.clip\(CircleShape\)\n\s+\.then\(\n\s+if \(color == Color\.Transparent\) \{\n\s+Modifier\.background\(\n\s+androidx\.compose\.ui\.graphics\.Brush\.linearGradient\(\n\s+listOf\(Color\.Red, Color\.Yellow, Color\.Green, Color\.Cyan, Color\.Blue, Color\.Magenta, Color\.Red\)\n\s+\)\n\s+\)\n\s+\} else if \(color == Color\(0xFF000000\) \|\| color == Color\.Black\) \{\n\s+Modifier\.background\(color\)\.border\(1\.dp, Color\.White, CircleShape\)\n\s+\} else \{\n\s+Modifier\.background\(color\)\n\s+\}\n\s+\)\n\s+\.clickable \{\n(.*?)\n\s+\},\n\s+contentAlignment = Alignment\.Center\n\s+\) \{\n\s+\}', re.DOTALL)

def preset_repl(m):
    indent = m.group(1)
    clickable_body = m.group(2)
    return f"""{indent}val isSelected = remember(selectedHue, selectedSaturation, color) {{
                                            if (color == Color.Transparent) false
                                            else {{
                                                val hsv = FloatArray(3)
                                                android.graphics.Color.colorToHSV(color.toArgb(), hsv)
                                                kotlin.math.abs(hsv[0] - selectedHue) < 2f && kotlin.math.abs(hsv[1] - selectedSaturation) < 0.05f
                                            }}
                                        }}
                                        Box(
                                            modifier = Modifier
                                                .width(42.dp)
                                                .height(36.dp)
                                                .then(
                                                    if (isSelected) Modifier.border(2.dp, if (color == Color(0xFF000000) || color == Color.Black) Color.White else color, CircleShape).padding(3.dp)
                                                    else Modifier
                                                )
                                                .clip(CircleShape)
                                                .then(
                                                    if (color == Color.Transparent) {{
                                                        Modifier.background(
                                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                                listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                                                            )
                                                        )
                                                    }} else if (color == Color(0xFF000000) || color == Color.Black) {{
                                                        Modifier.background(color).border(1.dp, Color.White, CircleShape)
                                                    }} else {{
                                                        Modifier.background(color)
                                                    }}
                                                )
                                                .clickable {{
{clickable_body}
                                                }},
                                            contentAlignment = Alignment.Center
                                        ) {{
                                        }}"""

content = preset_pattern.sub(preset_repl, content)

# For custom colors
custom_pattern = re.compile(r'(customColors\.forEach \{ color ->\n\s+)Box\(\n\s+modifier = Modifier\n\s+\.width\(42\.dp\)\n\s+\.height\(36\.dp\)\n\s+\.clip\(CircleShape\)\n\s+\.background\(color\)\n\s+\.combinedClickable\(\n(.*?)\n\s+\)\n\s+\)', re.DOTALL)

def custom_repl(m):
    indent = m.group(1)
    clickable_body = m.group(2)
    return f"""{indent}val isSelected = remember(selectedHue, selectedSaturation, color) {{
                                            val hsv = FloatArray(3)
                                            android.graphics.Color.colorToHSV(color.toArgb(), hsv)
                                            kotlin.math.abs(hsv[0] - selectedHue) < 2f && kotlin.math.abs(hsv[1] - selectedSaturation) < 0.05f
                                        }}
                                        Box(
                                            modifier = Modifier
                                                .width(42.dp)
                                                .height(36.dp)
                                                .then(
                                                    if (isSelected) Modifier.border(2.dp, color, CircleShape).padding(3.dp)
                                                    else Modifier
                                                )
                                                .clip(CircleShape)
                                                .background(color)
                                                .combinedClickable(
{clickable_body}
                                                )
                                        )"""

content = custom_pattern.sub(custom_repl, content)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)
