package com.markjann.bisyo

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.DownloadListener
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val TAG = "bisyo"

    // ✅ HINGIN ANG PAHINTULOT SA PAG-IMBAKAN
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allOk = result.all { it.value }
            if (allOk) {
                Toast.makeText(this, "✅ Pahintulot ibinigay — Simulan na ang pag-download", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "⚠️ Kailangan ng pahintulot para makapag-download", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            webView = findViewById(R.id.webview)

            // ✅ MGA SETTING — WALA NANG BINAGO
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

            // ✅ 📥 DOWNLOAD LISTENER
            webView.setDownloadListener(DownloadListener { url, _, _, _, _ ->
                Log.d(TAG, "📥 Dapat i-download: $url")
                downloadApk(url)
            })

            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e(TAG, "❌ HINDI MA-LOAD: ${request?.url}")
                }

                // ✅ KAPAG APK — HUWAG SA BROWSER!
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url.toString()
                    Log.d(TAG, "🔗 Binuksan: $url")

                    if (url.endsWith(".apk", ignoreCase = true)) {
                        Log.d(TAG, "📱 NAKITANG APK — HINDI SA BROWSER")
                        downloadApk(url)
                        return true // ✅ HINDI ILILIPAT SA BROWSER
                    }
                    return false
                }
            }

            // ✅ HINGIN MUNA ANG PAHINTULOT BAGO LAHAT
            checkPermissions()

            // ✅ I-LOAD ANG PAHINA
            webView.loadUrl("file:///android_asset/index.html")
            Log.d(TAG, "Naglo-load: file:///android_asset/index.html")

        } catch (e: Exception) {
            Log.e(TAG, "💥 CRASH: ${e.message}", e)
            Toast.makeText(this, "CRASH: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // ✅ SURIIN AT HINGIN ANG PAHINTULOT
    private fun checkPermissions() {
        val list = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ — kailangan ng pahintulot sa pag-install
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES)
                != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
            }
        } else {
            // Android 12 pababa — kailangan ng pagbasa/pagsulat sa imbakan
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (list.isNotEmpty()) {
            Log.d(TAG, "📋 Hinihingi ang pahintulot: $list")
            requestPermissions.launch(list.toTypedArray())
        }
    }

    // ✅ TOTOONG PAG-DOWNLOAD AT PAG-INSTALL — HINDI SA BROWSER!
    private fun downloadApk(apkUrl: String) {
        Log.d(TAG, "📥 PAG-DOWNLOAD NG APK: $apkUrl")

        // 🔒 SURIIN MUNA ANG PAHINTULOT
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "⚠️ Kailangan muna ng pahintulot", Toast.LENGTH_LONG).show()
            checkPermissions()
            return
        }

        Toast.makeText(this, "📤 Sinisimulan ang pag-download...", Toast.LENGTH_SHORT).show()

        // ✅ GAMITIN ANG DownloadManager — makikita ang TOTOONG PROGRESS sa abiso
        try {
            val uri = Uri.parse(apkUrl)
            val request = DownloadManager.Request(uri).apply {
                setTitle("Pag-update ng Aplikasyon")
                setDescription("Proyekto ni Mark Jann Tampok")
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "BisyoApp-update.apk")
                setMimeType("application/vnd.android.package-archive")
                allowScanningByMediaScanner()
            }

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            Toast.makeText(this, "✅ Nagsimula ang pag-download — Tignan ang abiso sa ibaba", Toast.LENGTH_LONG).show()
            Log.d(TAG, "✅ Ipinasa sa DownloadManager")

        } catch (e: Exception) {
            Log.e(TAG, "❌ DownloadManager nabigo: ${e.message}")
            // ⚠️ KUNG HINDI GUMANA — SIGURADUHING INSTALLER ANG BUBUKSAN, HINDI BROWSER
            openApkInstaller(apkUrl)
        }
    }

    // ✅ SIGURADUHING APK INSTALLER ANG BUBUKSAN — HINDI BROWSER!
    private fun openApkInstaller(url: String) {
        try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
            Log.d(TAG, "✅ APK Installer binuksan")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Hindi mabuksan ang Installer: ${e.message}")
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
