package com.wled.app.data.model

enum class DeviceIcon(val iconName: String) {
    STAR("Favori"),
    LIGHTBULB("Lampe"),
    RGB_RIBBON("Ruban RGB"),
    HOME("Intérieur"),
    FLOWER("Extérieur"),
    NONE("Aucun")
}

data class WledDevice(
    val name: String,
    val ip: String,
    val port: Int = 80,
    val isOnline: Boolean = true,
    val info: WledInfo? = null,
    val icon: DeviceIcon? = null,
    val color: Int? = null,
    val savedBrightness: Int = 128,
    val savedIsOn: Boolean = false,
    val savedColor: Int? = null
) {
    val url: String get() = "http://$ip:$port"
    val wsUrl: String get() = "ws://$ip:$port/ws"
    val effectiveIcon: DeviceIcon get() = icon ?: DeviceIcon.LIGHTBULB
    val effectiveColor: Int get() = color ?: savedColor ?: 0xFFFF5722.toInt()
}

data class WledInfo(
    val version: String,
    val ledsCount: Int,
    val name: String
)

data class WledState(
    val isOn: Boolean = false,
    val brightness: Int = 128,
    val transition: Int = 7,
    val segments: List<WledSegment> = emptyList()
)

data class WledSegment(
    val id: Int,
    val start: Int = 0,
    val stop: Int = 0,
    val name: String? = null,
    val currentEffect: Int = 0,
    val currentPalette: Int = 0,
    val speed: Int = 128,
    val intensity: Int = 128,
    val colors: List<WledColor> = emptyList(),
    val isSelected: Boolean = false
)

data class WledColor(
    val r: Int,
    val g: Int,
    val b: Int,
    val w: Int = 0
) {
    fun toArgb(): Int = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}
