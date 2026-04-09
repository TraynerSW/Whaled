package com.wled.app.data.service

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UdpListenerService(
    private val onColorReceived: (ip: String, brightness: Int, r: Int, g: Int, b: Int) -> Unit
) {
    private var socket: DatagramSocket? = null
    private var isListening = false
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun start(port: Int = 21324) {
        if (isListening) return
        isListening = true

        scope.launch {
            try {
                socket = DatagramSocket(port)
                socket?.soTimeout = 1000
                Log.d("UDP", "Listening on port $port")

                while (isListening) {
                    try {
                        val buffer = ByteArray(1024)
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket?.receive(packet)

                        val data = packet.data
                        val senderIp = packet.address.hostAddress ?: continue

                        if (data.size >= 6) {
                            val brightness = data[2].toInt() and 0xFF
                            val r = data[3].toInt() and 0xFF
                            val g = data[4].toInt() and 0xFF
                            val b = data[5].toInt() and 0xFF

                            if (brightness > 0 || r > 0 || g > 0 || b > 0) {
                                withContext(Dispatchers.Main) {
                                    onColorReceived(senderIp, brightness, r, g, b)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Timeout ou erreur, on continue
                    }
                }
            } catch (e: Exception) {
                Log.e("UDP", "Error: ${e.message}")
            }
        }
    }

    fun stop() {
        isListening = false
        socket?.close()
        socket = null
    }
}
