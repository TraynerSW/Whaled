package com.wled.app.data.service

import com.google.gson.JsonObject
import com.wled.app.data.model.WledColor
import com.wled.app.data.model.WledDevice
import com.wled.app.data.model.WledInfo
import com.wled.app.data.model.WledSegment
import com.wled.app.data.model.WledState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WledApiService {

    suspend fun getInfo(ip: String, port: Int = 80): WledInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$ip:$port/json/info")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = com.google.gson.JsonParser.parseString(response).asJsonObject
                val leds = json.getAsJsonObject("leds")
                WledInfo(
                    version = json.get("ver")?.asString ?: "Unknown",
                    ledsCount = leds?.get("count")?.asInt ?: 0,
                    name = json.get("name")?.asString ?: "WLED"
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getState(ip: String, port: Int = 80): WledState? = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$ip:$port/json/state")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                parseState(response)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

private fun parseState(jsonString: String): WledState {
        val json = com.google.gson.JsonParser.parseString(jsonString).asJsonObject
        
        // Check if state is nested under "state" key or at root level
        val stateObj = json.getAsJsonObject("state") ?: json

        val isOn = stateObj.get("on")?.asBoolean ?: false
        val brightness = stateObj.get("bri")?.asInt ?: 128
        val transition = stateObj.get("transition")?.asInt ?: 7

        val segments = mutableListOf<WledSegment>()
        stateObj.getAsJsonArray("seg")?.forEachIndexed { index, segElement ->
            val seg = segElement.asJsonObject
            val colors = mutableListOf<WledColor>()
            seg.getAsJsonArray("col")?.forEach { colorElement ->
                val colorArray = colorElement.asJsonArray
                colors.add(WledColor(
                    r = colorArray.get(0).asInt,
                    g = colorArray.get(1).asInt,
                    b = colorArray.get(2).asInt,
                    w = if (colorArray.size() > 3) colorArray.get(3).asInt else 0
                ))
            }
            segments.add(WledSegment(
                id = index,
                start = seg.get("start")?.asInt ?: 0,
                stop = seg.get("stop")?.asInt ?: 0,
                name = seg.get("n")?.asString,
                currentEffect = seg.get("fx")?.asInt ?: 0,
                currentPalette = seg.get("pal")?.asInt ?: 0,
                speed = seg.get("speed")?.asInt ?: 128,
                intensity = seg.get("int")?.asInt ?: 128,
                colors = colors,
                isSelected = seg.get("sel")?.asBoolean ?: false
            ))
        }

        return WledState(isOn = isOn, brightness = brightness, transition = transition, segments = segments)
    }

    suspend fun setPower(ip: String, port: Int, on: Boolean): Boolean = withContext(Dispatchers.IO) {
        sendCommand(ip, port, """{"on":$on}""")
    }

    suspend fun setBrightness(ip: String, port: Int, brightness: Int, currentlyOn: Boolean): Boolean = withContext(Dispatchers.IO) {
        sendCommand(ip, port, """{"on":$currentlyOn,"bri":${brightness.coerceIn(0, 255)}}""")
    }

    suspend fun setColor(ip: String, port: Int, r: Int, g: Int, b: Int, brightness: Int = 255, currentlyOn: Boolean = true): Boolean = withContext(Dispatchers.IO) {
        sendCommand(ip, port, """{"on":$currentlyOn,"bri":$brightness,"seg":[{"col":[[$r,$g,$b]]}]}""")
    }

    suspend fun updateSegment(ip: String, port: Int, segmentId: Int, start: Int, stop: Int, name: String?): Boolean = withContext(Dispatchers.IO) {
        val namePart = if (name != null) ""","n":"$name"""" else ""
        sendCommand(ip, port, """{"seg":[{"id":$segmentId,"start":$start,"stop":$stop$namePart}]}""")
    }

    suspend fun deleteSegment(ip: String, port: Int, segmentId: Int): Boolean = withContext(Dispatchers.IO) {
        // Setting stop to 0 deletes the segment
        sendCommand(ip, port, """{"seg":{"id":$segmentId,"stop":0}}""")
    }

    suspend fun getEffects(ip: String, port: Int = 80): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$ip:$port/json/eff")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                val jsonArray = com.google.gson.JsonParser.parseString(response).asJsonArray
                jsonArray.map { it.asString }
            } else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getPresets(ip: String, port: Int = 80): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$ip:$port/presets.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                val jsonObj = com.google.gson.JsonParser.parseString(response).asJsonObject
                val presets = mutableMapOf<String, String>()
                jsonObj.entrySet().forEach { (key, value) ->
                    if (key != "0" && value.isJsonObject) {
                        val name = value.asJsonObject.get("n")?.asString ?: "Preset $key"
                        presets[key] = name
                    }
                }
                presets
            } else emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    suspend fun setEffect(ip: String, port: Int, effectId: Int): Boolean = withContext(Dispatchers.IO) {
        sendCommand(ip, port, """{"seg":[{"id":0,"fx":$effectId}]}""")
    }

    suspend fun setPreset(ip: String, port: Int, presetId: Int): Boolean = withContext(Dispatchers.IO) {
        sendCommand(ip, port, """{"ps":$presetId}""")
    }

    private suspend fun sendCommand(ip: String, port: Int, command: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$ip:$port/json/state")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            connection.outputStream.write(command.toByteArray())
            connection.responseCode == 200
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
