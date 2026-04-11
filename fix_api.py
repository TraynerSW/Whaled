with open('app/src/main/java/com/wled/app/data/service/WledApiService.kt', 'r') as f:
    api = f.read()

method = """    suspend fun setWhiteTemperature(ip: String, port: Int, cct: Int): Boolean = withContext(Dispatchers.IO) {
        sendCommand(ip, port, \"\"\"{"seg":[{"cct":$cct}]}\"\"\")
    }

"""

if "fun setWhiteTemperature" not in api:
    idx = api.find("suspend fun updateSegment")
    if idx != -1:
        api = api[:idx] + method + api[idx:]
        with open('app/src/main/java/com/wled/app/data/service/WledApiService.kt', 'w') as f:
            f.write(api)
