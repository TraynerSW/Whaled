#!/bin/bash
sed -i 's/val trackColor by animateColorAsState(/val trackColor by animateColorAsState(\n        targetValue = if (isOn) {\n            if (isLightTheme) primaryColor.copy(alpha = 0.5f) else primaryColor\n        } else Color.Transparent,/g' app/src/main/java/com/wled/app/ui/components/Components.kt
sed -i 's/targetValue = if (isOn) primaryColor else Color.Transparent,//g' app/src/main/java/com/wled/app/ui/components/Components.kt
sed -i 's/if (isLightTheme) Color.LightGray else androidx.compose.ui.graphics.lerp(primaryColor, Color.Black, 0.6f)/if (isLightTheme) primaryColor else androidx.compose.ui.graphics.lerp(primaryColor, Color.Black, 0.6f)/g' app/src/main/java/com/wled/app/ui/components/Components.kt
