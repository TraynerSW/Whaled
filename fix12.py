import re

with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'r') as f:
    repo = f.read()

# Declare it at the class level instead of inside a function.
if 'private val lastInteractionTimes = mutableMapOf<String, Long>()' not in repo:
    # Look for private val scope = CoroutineScope(Dispatchers.Main + Job())
    # Wait, the previous script might have inserted it poorly. Let's reset the repository or just find it.
    pass

# Read again
with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'r') as f:
    repo = f.read()

# Make sure `lastInteractionTimes` is declared right after `private val _deviceStates`
if 'private val lastInteractionTimes = mutableMapOf<String, Long>()' not in repo:
    repo = repo.replace('private val _deviceStates = MutableStateFlow<Map<String, WledState>>(emptyMap())', 'private val _deviceStates = MutableStateFlow<Map<String, WledState>>(emptyMap())\n    private val lastInteractionTimes = mutableMapOf<String, Long>()')
    
# Remove the old bad declaration if it exists
repo = repo.replace('private val scope = CoroutineScope(Dispatchers.Main + Job())\n    private val lastInteractionTimes = mutableMapOf<String, Long>()', 'private val scope = CoroutineScope(Dispatchers.Main + Job())')

# Fix the parentheses
repo = repo.replace('if (device.ip == recentlyToggledDevice || (System.currentTimeMillis() - (lastInteractionTimes[device.ip] ?: 0L) < 500)) return@forEach', 'if (device.ip == recentlyToggledDevice || (System.currentTimeMillis() - (lastInteractionTimes[device.ip] ?: 0L) < 500L)) return@forEach')

repo = repo.replace('if (device.ip == recentlyToggledDevice || (System.currentTimeMillis() - (lastInteractionTimes[device.ip] ?: 0L) < 500)) return', 'if (device.ip == recentlyToggledDevice || (System.currentTimeMillis() - (lastInteractionTimes[device.ip] ?: 0L) < 500L)) return')

with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'w') as f:
    f.write(repo)
