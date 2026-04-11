with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.ui.graphics.toArgb" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color\n", "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.toArgb\n")

# Let's fix the structure. AnimatedNavIcon is right after the WledControlScreen function.
# Find the line where AnimatedNavIcon is defined.
idx = content.find("@Composable\nprivate fun AnimatedNavIcon")
if idx != -1:
    # Everything before this should have closed WledControlScreen.
    # We can just count the braces of WledControlScreen and ensure they match.
    pass

# Easiest way to fix is just rewrite the end of WledControlScreen.
# Let's find the end of tab 3.
target_str = """                3 -> {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {"""
end_idx = content.find(target_str)
if end_idx != -1:
    tab3_part = content[end_idx:]
    anim_icon_idx = tab3_part.find("@Composable\nprivate fun AnimatedNavIcon")
    if anim_icon_idx != -1:
        tab3_content = tab3_part[:anim_icon_idx]
        rest = tab3_part[anim_icon_idx:]
        
        # We need to make sure tab3_content ends with exactly the right number of braces
        # tab3_content starts with `3 -> { Column() {`
        # Let's count opening and closing braces in tab 3 content
        # It's better to just replace the tail.
        new_tab3_content = tab3_content.replace('''                        }
                    }
                    }
                }
            }
        }
    }
}
''', '''                        }
                    }
                }
            }
        }
    }
}
''')
        content = content[:end_idx] + new_tab3_content + rest

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(content)
