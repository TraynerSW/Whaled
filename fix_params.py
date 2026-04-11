import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Remove the settings button from TopAppBar
pattern_topbar = r'\s*IconButton\(onClick = \{ showEditNameDialog = true \}\) \{\s*Icon\(\s*imageVector = Icons\.Default\.Settings,\s*contentDescription = "Paramètres"\s*\)\s*\}\n'
ui = re.sub(pattern_topbar, '', ui)

# Change ColorWheel box size from 300 to 280
ui = ui.replace('Modifier.size(300.dp)', 'Modifier.size(280.dp)')
# Change ColorWheel size from 290 to 280
ui = ui.replace('Modifier.size(290.dp)', 'Modifier.size(280.dp)')

# Insert the Settings button inside the Box
# Look for the end of the ColorWheel call
pattern_insert = r'(onInteractionEnd = \{ isUserDraggingColor = false \}\n\s*\))'
insertion = r"""\g<1>
                            
                            IconButton(
                                onClick = { showEditNameDialog = true },
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Paramètres",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }"""
ui = re.sub(pattern_insert, insertion, ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)

# Update the throttle delays in WledRepository
with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'r') as f:
    repo = f.read()

repo = repo.replace('lastBrightnessSentTime > 5', 'lastBrightnessSentTime > 3')
repo = repo.replace('lastColorSentTime > 5', 'lastColorSentTime > 3')
repo = repo.replace('lastWhiteSentTime > 5', 'lastWhiteSentTime > 3')
repo = repo.replace('kotlinx.coroutines.delay(5)', 'kotlinx.coroutines.delay(3)')
repo = repo.replace('delay(5)', 'delay(3)')

with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'w') as f:
    f.write(repo)
