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
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        // ✅ SURIIN MUNA KUNG MAY MGA KAILANGANG FILES
        verifyAssets()

        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
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
        }

        // ✅ Ina-load ang index.html na nasa ugat ng assets
        val url = "file:///android_asset/index.html"
        Log.d(TAG, "Naglo-load: $url")
        webView.loadUrl(url)
    }

    // ✅ SURIIN ANG LAHAT NG KAILANGANG FILES — KASAMA NA ANG main.html!
    private fun verifyAssets() {
        val requiredFiles = listOf(
            "index.html",
            "main.html",              // ✅ IDINAGDAG — HINDI NA NAWAWALA!
            "style.css",
            "pages/alak.html",
            "pages/sigarilyo.html",
            "pages/shabu.html",
            "pages/heroin.html",
            "pages/marijuana.html",
            "pages/ecstasy.html",
            "pages/cocaine.html",
            "pages/tinuturok-gamot.html",
            "pages/tuklaw.html",
            "pages/sugal.html",
            "pages/inhalants.html",
            "pages/pag-aabuso-gamot.html",
            "pages/lsd.html",
            "pages/ketamine.html",
            "pages/ibang-sugal.html",
            "pages/internet-laro.html",
            "pages/labis-pagkain.html",
            "pages/vaping.html"
        )

        val assetManager = assets
        var allOk = true

        for (filename in requiredFiles) {
            try {
                assetManager.open(filename).close()
                Log.d(TAG, "✅ NANDITO: $filename")
            } catch (e: Exception) {
                Log.e(TAG, "❌ NAWAWALA: $filename — ${e.message}")
                allOk = false
            }
        }

        if (!allOk) {
            runOnUiThread {
                Toast.makeText(this, "⚠️ MAY NAWAWALANG FILE — Tignan ang Logcat!", Toast.LENGTH_LONG).show()
            }
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
