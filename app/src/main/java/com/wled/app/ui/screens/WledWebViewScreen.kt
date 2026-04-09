package com.wled.app.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.wled.app.data.model.WledDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WledWebViewScreen(
    device: WledDevice,
    onNavigateBack: () -> Unit
) {
    var reloadTrigger by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,

        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(device.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { reloadTrigger++ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,

                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        WledWebView(
            url = device.url,
            reloadTrigger = reloadTrigger,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WledWebView(
    url: String,
    reloadTrigger: Int,
    modifier: Modifier = Modifier
) {
    var lastTrigger by remember { mutableIntStateOf(reloadTrigger) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    builtInZoomControls = true
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                    }
                }
                
                webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                    }
                }
                
                loadUrl(url)
            }
        },
        modifier = modifier,
        update = { webView ->
            if (reloadTrigger != lastTrigger) {
                webView.reload()
                lastTrigger = reloadTrigger
            } else if (webView.url != url && webView.url != "$url/") {
                webView.loadUrl(url)
            }
        }
    )
}
