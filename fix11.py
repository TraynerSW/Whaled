import re

with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'r') as f:
    repo = f.read()

# Add a map for last interaction times if not present
if "private val lastInteractionTimes = mutableMapOf<String, Long>()" not in repo:
    repo = repo.replace('private val scope = CoroutineScope(Dispatchers.Main + Job())', 'private val scope = CoroutineScope(Dispatchers.Main + Job())\n    private val lastInteractionTimes = mutableMapOf<String, Long>()')

# Update lastInteractionTimes in setters
def add_interaction(ip_var, code):
    return code.replace(ip_var, ip_var + '\n        lastInteractionTimes[' + ip_var.split('.')[0] + '.ip] = System.currentTimeMillis()') if '.ip' in ip_var else code.replace(ip_var, ip_var + '\n        lastInteractionTimes[' + ip_var + '] = System.currentTimeMillis()')

# setBrightness
repo = repo.replace('val device = _selectedDevice.value ?: return', 'val device = _selectedDevice.value ?: return\n        lastInteractionTimes[device.ip] = System.currentTimeMillis()')

# setColor
repo = repo.replace('val currentState = _deviceStates.value[ip]', 'val currentState = _deviceStates.value[ip]\n        lastInteractionTimes[ip] = System.currentTimeMillis()')

# setWhiteTemperature
repo = repo.replace('fun setWhiteTemperature(ip: String, cct: Int) {\n', 'fun setWhiteTemperature(ip: String, cct: Int) {\n        lastInteractionTimes[ip] = System.currentTimeMillis()\n')

# setEffect
repo = repo.replace('fun setEffect(ip: String, effectId: Int) {\n', 'fun setEffect(ip: String, effectId: Int) {\n        lastInteractionTimes[ip] = System.currentTimeMillis()\n')

# setPreset
repo = repo.replace('fun setPreset(ip: String, presetId: Int) {\n', 'fun setPreset(ip: String, presetId: Int) {\n        lastInteractionTimes[ip] = System.currentTimeMillis()\n')

# Modify refreshAllDeviceStates and fetchDeviceState
# In refreshAllDeviceStates: `if (device.ip == recentlyToggledDevice) return@forEach`
repo = repo.replace('if (device.ip == recentlyToggledDevice) return@forEach', 'if (device.ip == recentlyToggledDevice || (System.currentTimeMillis() - (lastInteractionTimes[device.ip] ?: 0L) < 500)) return@forEach')

# In fetchDeviceState: `if (device.ip == recentlyToggledDevice) return`
repo = repo.replace('if (device.ip == recentlyToggledDevice) return', 'if (device.ip == recentlyToggledDevice || (System.currentTimeMillis() - (lastInteractionTimes[device.ip] ?: 0L) < 500)) return')

with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'w') as f:
    f.write(repo)
