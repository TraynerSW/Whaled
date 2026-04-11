import re

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'r') as f:
    ui = f.read()

# Remove them from where they were incorrectly placed
pattern = r'val pagerState = rememberPagerState\(pageCount = \{ 4 \}\)\s*val coroutineScope = rememberCoroutineScope\(\)\s*LaunchedEffect\(selectedTab\) \{[\s\S]*?\}\n\s*LaunchedEffect\(pagerState\.currentPage\) \{[\s\S]*?\}\n'
ui = re.sub(pattern, '', ui)

# Find the start of Scaffold and put them there
scaffold_pattern = r'(Scaffold\(\s*containerColor = androidx\.compose\.ui\.graphics\.Color\.Transparent,)'
insertion = """val pagerState = rememberPagerState(pageCount = { 4 })
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

    \g<1>"""

ui = re.sub(scaffold_pattern, insertion, ui)

with open('app/src/main/java/com/wled/app/ui/screens/WledControlScreen.kt', 'w') as f:
    f.write(ui)
