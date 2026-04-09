#!/bin/bash
sed -i 's/androidx.compose.ui.graphics.lerp(primaryColor, Color.Black, 0.6f)/if (isLightTheme) Color.LightGray else androidx.compose.ui.graphics.lerp(primaryColor, Color.Black, 0.6f)/g' app/src/main/java/com/wled/app/ui/components/Components.kt
