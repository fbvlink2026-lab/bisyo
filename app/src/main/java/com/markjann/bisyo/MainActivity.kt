package com.markjann.bisyo

import android.content.Intent
import android.net.Uri
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

            // ✅ TAMA ANG MGA SETTING — WALANG BINAGO
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

                // ✅ KAPAG APK — BUBUKSAN NG SISTEMA PARA MA-DOWNLOAD/MA-INSTALL
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url.toString()

                    // 📱 KUNG APK — IBUKSAN SA SISTEMA
                    if (url.endsWith(".apk", ignoreCase = true)) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        return true // ✅ HUWAG SA WEBVIEW — SISTEMA NA ANG HAHANDLE
                    }

                    // ✅ LAHAT IBA — MANATILI SA LOOB NG WEBVIEW
                    return false
                }
            }

            // ✅ DIRETSONG INA-LOAD — GANITO PA RIN
            val url = "file:///android_asset/index.html"
            Log.d(TAG, "Naglo-load: $url")
            webView.loadUrl(url)

        } catch (e: Exception) {
            Log.e(TAG, "💥 CRASH SA onCreate: ${e.message}", e)
            Toast.makeText(this, "CRASH: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // ✅ BALIK — GANITO PA RIN
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
