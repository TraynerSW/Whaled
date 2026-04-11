package com.wled.app.ui.screens
import androidx.compose.ui.unit.dp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.BackHandler
import com.wled.app.data.model.WledDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WledWebViewScreen(
    device: WledDevice,
    onNavigateBack: () -> Unit
) {
    var reloadTrigger by remember { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }

    val handleBack: () -> Unit = {
        if (canGoBack && webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = true) {
        handleBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(device.name) },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(top = 44.dp),
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { reloadTrigger++ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,

                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            WledWebView(
                url = device.url,
                reloadTrigger = reloadTrigger,
                onWebViewCreated = { webViewRef = it },
                onHistoryChanged = { canGoBack = it },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WledWebView(
    url: String,
    reloadTrigger: Int,
    onWebViewCreated: (WebView) -> Unit = {},
    onHistoryChanged: (Boolean) -> Unit = {},
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
                    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                        onHistoryChanged(view?.canGoBack() == true)
                        super.doUpdateVisitedHistory(view, url, isReload)
                    }
                    override fun onPageFinished(view: WebView?, url: String?) {
                        onHistoryChanged(view?.canGoBack() == true)
                        super.onPageFinished(view, url)
                    }
                }
                
                webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                    }
                }
                
                onWebViewCreated(this)
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
