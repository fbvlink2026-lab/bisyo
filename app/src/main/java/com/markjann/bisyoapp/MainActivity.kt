package com.markjann.bisyoapp

import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val TAG = "BisyoApp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        try {
            webView.settings.apply {
                javaScriptEnabled = true
                allowFileAccess = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
            }

            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e(TAG, "WebView Error: ${error?.description}")
                }
            }

            // ✅ Subukan ang index.html — kung wala, gamitin ang main.html
            val url = "file:///android_asset/index.html"
            Log.d(TAG, "Naglo-load: $url")
            webView.loadUrl(url)

        } catch (e: Exception) {
            Log.e(TAG, "Error sa WebView: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
