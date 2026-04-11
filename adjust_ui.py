import re

# 1. Update WledRepository.kt delays from 3 to 5
with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'r') as f:
    repo = f.read()
repo = repo.replace('lastBrightnessSentTime > 3', 'lastBrightnessSentTime > 5')
repo = repo.replace('lastColorSentTime > 3', 'lastColorSentTime > 5')
repo = repo.replace('lastWhiteSentTime > 3', 'lastWhiteSentTime > 5')
repo = repo.replace('kotlinx.coroutines.delay(3)', 'kotlinx.coroutines.delay(5)')
repo = repo.replace('delay(3)', 'delay(5)')
with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'w') as f:
    f.write(repo)

# 2. Update WledControlScreen.kt
with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Fix Settings icon tint
ui = ui.replace('tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)', 'tint = MaterialTheme.colorScheme.onSurface')

# Fix oval shape (48 -> 40)
ui = ui.replace('Modifier.width(48.dp).height(32.dp)', 'Modifier.width(40.dp).height(32.dp)')

# Move RGB sliders
# First, extract the RGB sliders
pattern_rgb = r'(\s*com\.wled\.app\.ui\.components\.RgbSlider\([\s\S]*?color = Color\.Red\n\s*\)\s*com\.wled\.app\.ui\.components\.RgbSlider\([\s\S]*?color = Color\.Green\n\s*\)\s*com\.wled\.app\.ui\.components\.RgbSlider\([\s\S]*?color = Color\.Blue\n\s*\))'
match = re.search(pattern_rgb, ui)
if match:
    rgb_sliders = match.group(1)
    ui = ui.replace(rgb_sliders, '')
    
    # We want to put them after the custom colors FlowRow.
    # The custom colors end with:
    #                             }
    #                         }
    #                         
    #                         Spacer(modifier = Modifier.height(24.dp))
    #                     }
    #                 }
    #                 1 -> {
    # Let's find the end of tab 0:
    end_tab0_pattern = r'(Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n\s*\})(\n\s*\})'
    
    # We will inject the RGB sliders inside a new Column right before the last Spacer of Tab 0
    
    # Wait, looking at WledControlScreen, after custom colors there is:
    # Spacer(modifier = Modifier.height(24.dp))
    # }
    # }
    # Let's locate the last Spacer of tab 0 precisely.
    
    # We know the add custom color button ends with:
    add_btn_end = r'(Icon\(\n\s*Icons\.Default\.Add,[\s\S]*?modifier = Modifier\.size\(20\.dp\)\n\s*\)\n\s*\}\n\s*\})'
    
    rgb_column = f"""
                        Spacer(modifier = Modifier.height(24.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.fillMaxWidth()) {{{rgb_sliders}
                        }}"""
    
    # We'll replace the add_btn_end by add_btn_end + rgb_column
    ui = re.sub(add_btn_end, r'\1' + rgb_column, ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)
