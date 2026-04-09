package com.wled.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.wled.app.data.model.DeviceIcon
import com.wled.app.data.model.WledDevice
import com.wled.app.data.model.WledState
import com.wled.app.data.service.WledApiService
import com.wled.app.data.service.WledDiscoveryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WledRepository(private val context: Context) {

    private val apiService = WledApiService()
    private val discoveryService = WledDiscoveryService(context)
    private val prefs: SharedPreferences = context.getSharedPreferences("wled_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _devices = MutableStateFlow<List<WledDevice>>(emptyList())
    val devices: StateFlow<List<WledDevice>> = _devices.asStateFlow()

    private val _deviceStates = MutableStateFlow<Map<String, WledState>>(emptyMap())
    val deviceStates: StateFlow<Map<String, WledState>> = _deviceStates

    private val _deviceEffects = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val deviceEffects: StateFlow<Map<String, List<String>>> = _deviceEffects

    private val _devicePresets = MutableStateFlow<Map<String, Map<String, String>>>(emptyMap())
    val devicePresets: StateFlow<Map<String, Map<String, String>>> = _devicePresets.asStateFlow()

    private val _realtimeColors = MutableStateFlow<Map<String, Int>>(emptyMap())
    val realtimeColors: StateFlow<Map<String, Int>> = _realtimeColors.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedDevice = MutableStateFlow<WledDevice?>(null)
    val selectedDevice: StateFlow<WledDevice?> = _selectedDevice.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var recentlyToggledDevice: String? = null

    var autoDiscovery: Boolean
        get() = prefs.getBoolean("auto_discovery", true)
        set(value) = prefs.edit().putBoolean("auto_discovery", value).apply()

    var showHiddenDevices: Boolean
        get() = prefs.getBoolean("show_hidden", false)
        set(value) = prefs.edit().putBoolean("show_hidden", value).apply()

    var showOfflineLast: Boolean
        get() = prefs.getBoolean("offline_last", true)
        set(value) = prefs.edit().putBoolean("offline_last", value).apply()

    var wledVersion: String
        get() = prefs.getString("wled_version", "") ?: ""
        set(value) = prefs.edit().putString("wled_version", value).apply()

    init {
        loadSavedDevicesAtStartup()
        startStatePolling()
        if (autoDiscovery) {
            scope.launch {
                val discovered = discoveryService.discoverServices()
                val currentDevices = _devices.value.toMutableList()
                discovered.forEach { device ->
                    if (!currentDevices.any { it.ip == device.ip }) {
                        currentDevices.add(device)
                    }
                }
                _devices.value = currentDevices
                saveDevices(currentDevices)
            }
        }
        scope.launch {
            _devices.collect { deviceList ->
                deviceList.forEach { device ->
                    fetchDeviceState(device)
                }
            }
        }
    }

    private fun loadSavedDevicesAtStartup() {
        val savedDevices = loadSavedDevices()
        if (savedDevices.isNotEmpty()) {
            _devices.value = savedDevices
            savedDevices.forEach { device ->
                scope.launch {
                    fetchDeviceState(device)
                    fetchDeviceInfo(device)
                }
            }
        }
    }

    private fun startStatePolling() {
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(500)
                refreshAllDeviceStates()
            }
        }
    }

    private suspend fun refreshAllDeviceStates() {
        withContext(Dispatchers.IO) {
            _devices.value.forEach { device ->
                try {
                    if (device.ip == recentlyToggledDevice) return@forEach
                    val state = apiService.getState(device.ip, device.port)
                    if (state != null) {
                        _deviceStates.value = _deviceStates.value + (device.ip to state)
                        saveDeviceState(device.ip, state.isOn, state.brightness)
                    }
                } catch (e: Exception) {}
            }
        }
    }

    fun discoverDevices(showLoading: Boolean = true) {
        if (!autoDiscovery) return
        
        scope.launch {
            if (showLoading) _isLoading.value = true
            _error.value = null
            try {
                val discovered = discoveryService.discoverServices()
                val savedDevices = loadSavedDevices()

                val allDevices = (discovered + savedDevices).associateBy { it.ip }.values.toList()
                _devices.value = allDevices

                discovered.forEach { device ->
                    if (!savedDevices.any { it.ip == device.ip }) {
                        saveDevice(device)
                    }
                    device.info?.version?.let { version ->
                        if (wledVersion.isEmpty()) {
                            wledVersion = version
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erreur de découverte: ${e.message}"
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }

    private suspend fun fetchDeviceState(device: WledDevice) {
        if (device.ip == recentlyToggledDevice) return
        withContext(Dispatchers.IO) {
            val state = apiService.getState(device.ip, device.port)
            if (state != null) {
                _deviceStates.value = _deviceStates.value + (device.ip to state)
                saveDeviceState(device.ip, state.isOn, state.brightness)
            }
        }
    }

    private suspend fun fetchDeviceInfo(device: WledDevice) {
        val ip = device.ip
        try {
            val info = apiService.getInfo(ip)
            if (info != null) {
                val updatedDevices = _devices.value.map {
                    if (it.ip == ip) it.copy(info = info, isOnline = true) else it
                }
                _devices.value = updatedDevices
                saveDevice(updatedDevices.find { it.ip == ip } ?: device)
            }
            
            // Also fetch effects and presets
            val effects = apiService.getEffects(ip)
            if (effects.isNotEmpty()) {
                _deviceEffects.value = _deviceEffects.value + (ip to effects)
            }
            val presets = apiService.getPresets(ip)
            if (presets.isNotEmpty()) {
                _devicePresets.value = _devicePresets.value + (ip to presets)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectDevice(device: WledDevice?) {
        _selectedDevice.value = device
    }

    fun togglePower() {
        val device = _selectedDevice.value ?: return
        val currentState = _deviceStates.value[device.ip] ?: return
        val newState = !currentState.isOn

        // Mark this device as recently toggled to skip polling updates
        recentlyToggledDevice = device.ip
        scope.launch {
            delay(500)
            recentlyToggledDevice = null
        }

        // Optimistic update - only the tapped device
        val newDeviceStates = _deviceStates.value.mapValues { (ip, state) ->
            if (ip == device.ip) {
                state.copy(isOn = newState)
            } else {
                state
            }
        }
        _deviceStates.value = newDeviceStates

        // Send API call immediately
        scope.launch {
            withContext(Dispatchers.IO) {
                apiService.setPower(device.ip, device.port, newState)
            }
        }
        
        // Save the state
        saveDeviceState(device.ip, newState, currentState.brightness)
    }

    private var setBrightnessJob: kotlinx.coroutines.Job? = null
    private var lastBrightnessSentTime = 0L

    fun setBrightness(brightness: Int, isDragging: Boolean = false) {
        val device = _selectedDevice.value ?: return
        val currentState = _deviceStates.value[device.ip] ?: return

        // Throttle state updates during drag to avoid heavy UI stutter (max 10 fps)
        val currentTime = System.currentTimeMillis()
        if (!isDragging || currentTime - lastBrightnessSentTime > 100) {
            val newDeviceStates = _deviceStates.value.mapValues { (ip, state) ->
                if (ip == device.ip) {
                    state.copy(brightness = brightness)
                } else {
                    state
                }
            }
            _deviceStates.value = newDeviceStates
            lastBrightnessSentTime = currentTime
        }

        if (!isDragging) {
            setBrightnessJob?.cancel()
            setBrightnessJob = scope.launch {
                withContext(Dispatchers.IO) {
                    apiService.setBrightness(device.ip, device.port, brightness, currentState.isOn)
                }
                saveDeviceState(device.ip, currentState.isOn, brightness)
            }
        } else {
            // Throttle API calls while dragging (max 10 per second)
            if (setBrightnessJob?.isActive != true) {
                setBrightnessJob = scope.launch {
                    withContext(Dispatchers.IO) {
                        apiService.setBrightness(device.ip, device.port, brightness, currentState.isOn)
                    }
                    kotlinx.coroutines.delay(100) // Keep the job active for 100ms to block new calls
                }
            }
        }
    }

    fun setColor(ip: String, color: Int) {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val currentState = _deviceStates.value[ip]
        val brightness = currentState?.brightness ?: 128
        val currentlyOn = currentState?.isOn ?: true

        scope.launch {
            delay(100)
            withContext(Dispatchers.IO) {
                apiService.setColor(ip, 80, r, g, b, brightness, currentlyOn)
            }
        }

        updateRealtimeColor(ip, brightness, color)
    }

    private fun isSynchronized(sourceIp: String, targetIp: String): Boolean {
        // All devices are considered synchronized in this simple implementation
        // In a more complex setup, you'd check WLED's group settings
        return true
    }

    fun updateRealtimeColor(ip: String, brightness: Int, color: Int) {
        // Update all synchronized devices with the new color
        val updatedColors = _realtimeColors.value.toMutableMap()
        _devices.value.forEach { device ->
            if (device.ip == ip || isSynchronized(ip, device.ip)) {
                updatedColors[device.ip] = color
            }
        }
        _realtimeColors.value = updatedColors
        
        // Also update deviceStates for synchronized devices
        val updatedStates = _deviceStates.value.mapValues { (deviceIp, state) ->
            if (deviceIp == ip || isSynchronized(ip, deviceIp)) {
                val newState = if (brightness > 0) state.copy(brightness = brightness) else state
                if (newState.segments.isNotEmpty()) {
                    val segment = newState.segments.first()
                    val wledColor = com.wled.app.data.model.WledColor(
                        r = (color shr 16) and 0xFF,
                        g = (color shr 8) and 0xFF,
                        b = color and 0xFF
                    )
                    newState.copy(segments = listOf(segment.copy(colors = listOf(wledColor))))
                } else newState
            } else state
        }
        _deviceStates.value = updatedStates
        
        // Also persist the color for this device
        updateDeviceColor(ip, color)
        
        // Save the color in device
        val updatedDevices = _devices.value.map { device ->
            if (device.ip == ip || isSynchronized(ip, device.ip)) {
                device.copy(savedColor = color)
            } else {
                device
            }
        }
        _devices.value = updatedDevices
        saveDevices(updatedDevices)
    }

    fun addManualDevice(ip: String) {
        scope.launch {
            _isLoading.value = true
            val info = withContext(Dispatchers.IO) {
                apiService.getInfo(ip)
            }
            if (info != null) {
                val device = WledDevice(
                    name = info.name,
                    ip = ip,
                    port = 80,
                    isOnline = true,
                    info = info
                )
                _devices.value = _devices.value + device
                saveDevice(device)

                val state = withContext(Dispatchers.IO) {
                    apiService.getState(ip)
                }
                if (state != null) {
                    _deviceStates.value = _deviceStates.value + (ip to state)
                    saveDeviceState(ip, state.isOn, state.brightness)
                }
            } else {
                _error.value = "Impossible de se connecter à $ip"
            }
            _isLoading.value = false
        }
    }

    fun updateDeviceName(ip: String, newName: String) {
        val updatedDevices = _devices.value.map { device ->
            if (device.ip == ip) {
                device.copy(name = newName)
            } else {
                device
            }
        }
        _devices.value = updatedDevices
        
        val selected = _selectedDevice.value
        if (selected?.ip == ip) {
            _selectedDevice.value = selected.copy(name = newName)
        }
        
        saveDevices(updatedDevices)
    }

    fun updateDeviceIcon(ip: String, icon: DeviceIcon) {
        val updatedDevices = _devices.value.map { device ->
            if (device.ip == ip) {
                device.copy(icon = icon)
            } else {
                device
            }
        }
        _devices.value = updatedDevices
        
        val selected = _selectedDevice.value
        if (selected?.ip == ip) {
            _selectedDevice.value = selected.copy(icon = icon)
        }
        
        saveDevices(updatedDevices)
    }

    fun updateDeviceColor(ip: String, color: Int) {
        val updatedDevices = _devices.value.map { device ->
            if (device.ip == ip) {
                device.copy(color = color)
            } else {
                device
            }
        }
        _devices.value = updatedDevices
        
        val selected = _selectedDevice.value
        if (selected?.ip == ip) {
            _selectedDevice.value = selected.copy(color = color)
        }
        
        saveDevices(updatedDevices)
    }

    fun updateSegment(ip: String, segmentId: Int, start: Int, stop: Int, name: String?) {
        scope.launch {
            withContext(Dispatchers.IO) {
                apiService.updateSegment(ip, 80, segmentId, start, stop, name)
            }
            _devices.value.find { it.ip == ip }?.let { fetchDeviceState(it) }
        }
    }

    fun deleteSegment(ip: String, segmentId: Int) {
        scope.launch {
            withContext(Dispatchers.IO) {
                apiService.deleteSegment(ip, 80, segmentId)
            }
            _devices.value.find { it.ip == ip }?.let { fetchDeviceState(it) }
        }
    }

    fun setEffect(ip: String, effectId: Int) {
        scope.launch {
            withContext(Dispatchers.IO) {
                apiService.setEffect(ip, 80, effectId)
            }
        }
    }

    fun setPreset(ip: String, presetId: Int) {
        scope.launch {
            withContext(Dispatchers.IO) {
                apiService.setPreset(ip, 80, presetId)
            }
        }
    }

    private fun saveDevice(device: WledDevice) {
        val json = gson.toJson(device)
        val devices = loadSavedDevicesJson().toMutableSet()
        devices.add(json)
        prefs.edit().putStringSet("saved_devices", devices).apply()
    }

    private fun saveDevices(devices: List<WledDevice>) {
        val devicesJson = devices.map { gson.toJson(it) }.toSet()
        prefs.edit().putStringSet("saved_devices", devicesJson).apply()
    }
    
    private fun saveDeviceState(ip: String, isOn: Boolean, brightness: Int) {
        val currentDevice = _devices.value.find { it.ip == ip }
        if (currentDevice?.savedIsOn == isOn && currentDevice.savedBrightness == brightness) return

        val updatedDevices = _devices.value.map { device ->
            if (device.ip == ip) {
                device.copy(savedIsOn = isOn, savedBrightness = brightness)
            } else {
                device
            }
        }
        _devices.value = updatedDevices
        saveDevices(updatedDevices)
    }

    private fun loadSavedDevices(): List<WledDevice> {
        return loadSavedDevicesJson().mapNotNull { json ->
            try {
                gson.fromJson(json, WledDevice::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun loadSavedDevicesJson(): Set<String> {
        return prefs.getStringSet("saved_devices", emptySet()) ?: emptySet()
    }
}
