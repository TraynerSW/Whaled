package com.wled.app.data.service

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import android.os.Looper
import com.wled.app.data.model.WledDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class WledDiscoveryService(private val context: Context) {

    private val nsdManager: NsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    private val apiService = WledApiService()
    private val discoveredDevices = mutableMapOf<String, WledDevice>()

    suspend fun discoverServices(): List<WledDevice> = withContext(Dispatchers.IO) {
        withTimeoutOrNull(8000L) {
            suspendCancellableCoroutine { continuation ->
                val services = mutableListOf<NsdServiceInfo>()
                var discoveryFailed = false

                val discoveryListener = object : NsdManager.DiscoveryListener {
                    override fun onDiscoveryStarted(serviceType: String) {}

                    override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                        services.add(serviceInfo)
                    }

                    override fun onServiceLost(serviceInfo: NsdServiceInfo) {}

                    override fun onDiscoveryStopped(serviceType: String) {}

                    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                        discoveryFailed = true
                        if (continuation.isActive) {
                            continuation.resume(services)
                        }
                    }

                    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
                }

                try {
                    nsdManager.discoverServices(
                        "_wled._tcp.",
                        NsdManager.PROTOCOL_DNS_SD,
                        discoveryListener
                    )
                } catch (e: Exception) {
                    if (continuation.isActive) {
                        continuation.resume(services)
                    }
                    return@suspendCancellableCoroutine
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        nsdManager.stopServiceDiscovery(discoveryListener)
                    } catch (e: Exception) {}
                    if (continuation.isActive) {
                        continuation.resume(services)
                    }
                }, 5000)
            }
        }?.let { services ->
            services.forEach { serviceInfo ->
                try {
                    val resolved = resolveService(serviceInfo)
                    resolved?.let { resolvedInfo ->
                        val host = resolvedInfo.host?.hostAddress
                        val port = resolvedInfo.port
                        if (host != null && host != "127.0.0.1") {
                            val info = apiService.getInfo(host, port)
                            if (info != null) {
                                val device = WledDevice(
                                    name = info.name,
                                    ip = host,
                                    port = port,
                                    isOnline = true,
                                    info = info
                                )
                                discoveredDevices[host] = device
                            }
                        }
                    }
                } catch (e: Exception) {}
            }
        }

        discoveredDevices.values.toList()
    }

    private suspend fun resolveService(serviceInfo: NsdServiceInfo): NsdServiceInfo? = suspendCancellableCoroutine { continuation ->
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }

            override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                if (continuation.isActive) {
                    continuation.resume(resolvedInfo)
                }
            }
        }

        try {
            nsdManager.resolveService(serviceInfo, resolveListener)
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }
}
