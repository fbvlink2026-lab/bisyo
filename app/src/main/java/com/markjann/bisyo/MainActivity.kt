package com.markjann.bisyo

import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val TAG = "bisyo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            webView = findViewById(R.id.webview)

            // ✅ TAMA ANG MGA SETTING
            webView.settings.apply {
                javaScriptEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
            }

            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    val errMsg = "❌ HINDI MA-LOAD: ${request?.url}"
                    Log.e(TAG, errMsg)
                    Toast.makeText(this@MainActivity, errMsg, Toast.LENGTH_LONG).show()
                }

                // ✅ SIGURADUHING HINDI ILILIPAT SA IBANG BROWSER
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }

            // ✅ DIRETSONG INA-LOAD — WALANG SURIING NAGBUBUKAS NG FILES
            val url = "file:///android_asset/index.html"
            Log.d(TAG, "Naglo-load: $url")
            webView.loadUrl(url)

        } catch (e: Exception) {
            Log.e(TAG, "💥 CRASH SA onCreate: ${e.message}", e)
            Toast.makeText(this, "CRASH: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            // ✅ Hindi isasara — mananatili para makita ang mensahe
        }
    }

    // ✅ TINANGGAL ANG verifyAssets — DAHIL ITO ANG NAGDUDULOT NG CRASH!
    // Hindi kailangang buksan ang lahat ng file bago ipakita — hayaan na lang ang WebView ang gagawa!

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
