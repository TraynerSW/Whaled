import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Add pager imports
if "import androidx.compose.foundation.pager.HorizontalPager" not in ui:
    ui = ui.replace("import androidx.compose.foundation.lazy.items\n", "import androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.pager.HorizontalPager\nimport androidx.compose.foundation.pager.rememberPagerState\nimport androidx.compose.runtime.rememberCoroutineScope\nimport kotlinx.coroutines.launch\n")

# Replace AnimatedContent
pattern_animated = r'(AnimatedContent\(\s*targetState = selectedTab,[\s\S]*?label = "tab_transition",\s*modifier = Modifier\.fillMaxSize\(\)\s*\) \{ targetTab ->)'

replacement = """val pagerState = rememberPagerState(pageCount = { 4 })
            val coroutineScope = rememberCoroutineScope()
            
            LaunchedEffect(selectedTab) {
                if (pagerState.currentPage != selectedTab) {
                    pagerState.animateScrollToPage(selectedTab)
                }
            }
            
            LaunchedEffect(pagerState.currentPage) {
                if (selectedTab != pagerState.currentPage) {
                    selectedTab = pagerState.currentPage
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { targetTab ->"""

ui = re.sub(pattern_animated, replacement, ui)

# Update BottomBar clicks to use coroutineScope
ui = ui.replace('onClick = { selectedTab = 0 }', 'onClick = { selectedTab = 0; coroutineScope.launch { pagerState.animateScrollToPage(0) } }')
ui = ui.replace('onClick = { selectedTab = 1 }', 'onClick = { selectedTab = 1; coroutineScope.launch { pagerState.animateScrollToPage(1) } }')
ui = ui.replace('onClick = { selectedTab = 2 }', 'onClick = { selectedTab = 2; coroutineScope.launch { pagerState.animateScrollToPage(2) } }')
ui = ui.replace('onClick = { selectedTab = 3 }', 'onClick = { selectedTab = 3; coroutineScope.launch { pagerState.animateScrollToPage(3) } }')


with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'r') as f:
    comp = f.read()

# Make sure ColorWheel consumes drags so it doesn't propagate to the pager
if "change.consume()" not in comp:
    comp = comp.replace('val center = Offset(size.width / 2f, size.height / 2f)\n                    val dx = change.position.x - center.x', 'change.consume()\n                    val center = Offset(size.width / 2f, size.height / 2f)\n                    val dx = change.position.x - center.x')

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'w') as f:
    f.write(comp)
