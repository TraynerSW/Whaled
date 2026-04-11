with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'r') as f:
    repo = f.read()

method = """    fun setWhiteTemperature(ip: String, cct: Int) {
        val device = deviceList.value.find { it.ip == ip } ?: return
        
        setColorJobs[ip]?.cancel()
        setColorJobs[ip] = scope.launch {
            try {
                apiService.setWhiteTemperature(ip, 80, cct)
                refreshDeviceState(device)
            } catch (e: Exception) {
                // Silently fail if network request fails
            }
        }
    }

"""

if "fun setWhiteTemperature" not in repo:
    idx = repo.find("fun setEffect(ip: String, effectId: Int) {")
    if idx != -1:
        repo = repo[:idx] + method + repo[idx:]
        with open('app/src/main/java/com/wled/app/data/repository/WledRepository.kt', 'w') as f:
            f.write(repo)
