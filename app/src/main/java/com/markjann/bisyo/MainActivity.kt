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
import androidx.core.content.FileProvider
import java.io.File

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
                // ✅ PARA HINDI MAHIRAP SA DOWNLOAD NG APK
                setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                    downloadApk(url)
                }
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

                // ✅ KAPAG APK — BUBUKSAN NG SISTEMA — HINDI SA WEBVIEW
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url.toString()

                    // 📱 KUNG APK — IBUKSAN SA SISTEMA PARA I-INSTALL
                    if (url.endsWith(".apk", ignoreCase = true)) {
                        downloadApk(url)
                        return true // ✅ HINDI SA WEBVIEW — SISTEMA ANG HAHANDLE
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

    // 📥 TOTOONG PAG-DOWNLOAD AT PAG-INSTALL NG APK
    private fun downloadApk(apkUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)

            // ✅ KUNG REMOTE URL — IBIGAY SA BROWSER O DOWNLOAD MANAGER
            if (apkUrl.startsWith("http")) {
                intent.data = Uri.parse(apkUrl)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "📤 Sinisimulan ang pag-download...", Toast.LENGTH_SHORT).show()
            }
            // ✅ KUNG LOKAL NA FILE — I-INSTALL AGAD
            else {
                val uri = Uri.parse(apkUrl)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            }
            Log.d(TAG, "APK inilunsad: $apkUrl")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Hindi ma-proseso ang APK: ${e.message}")
            Toast.makeText(this, "❌ Hindi mabuksan ang APK", Toast.LENGTH_LONG).show()
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
