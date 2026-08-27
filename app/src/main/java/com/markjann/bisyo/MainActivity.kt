package com.markjann.bisyo

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val TAG = "bisyo"
    private var currentDownloadId: Long = -1

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allOk = result.all { it.value }
            if (allOk) {
                Toast.makeText(this, "✅ Pahintulot ibinigay", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "⚠️ Kailangan ng pahintulot", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
        }

        // ✅ I-EXPOSE ANG INTERFACE SA HTML
        webView.addJavascriptInterface(this, "AndroidApp")

        webView.setDownloadListener(DownloadListener { url, _, _, _, _ ->
            downloadApk(url)
        })

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                Log.e(TAG, "❌ Error: ${request?.url}")
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.endsWith(".apk", ignoreCase = true)) {
                    downloadApk(url)
                    return true
                }
                return false
            }
        }

        checkPermissions()
        webView.loadUrl("file:///android_asset/index.html")
    }

    private fun checkPermissions() {
        val list = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES)
                != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        if (list.isNotEmpty()) requestPermissions.launch(list.toTypedArray())
    }

    private fun downloadApk(apkUrl: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "⚠️ Kailangan muna ng pahintulot", Toast.LENGTH_LONG).show()
            checkPermissions()
            return
        }

        Toast.makeText(this, "📤 Sinisimulan...", Toast.LENGTH_SHORT).show()

        try {
            val uri = Uri.parse(apkUrl)
            val request = DownloadManager.Request(uri).apply {
                setTitle("Pag-update — Proyekto ni Mark Jann Tampok")
                setDescription("Nagda-download...")
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "BisyoApp-update.apk")
                setMimeType("application/vnd.android.package-archive")
                // ✅ TINANGGAL — luma na: allowScanningByMediaScanner()
            }

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            currentDownloadId = dm.enqueue(request)

            Toast.makeText(this, "✅ Nagsimula — Tignan ang abiso sa itaas", Toast.LENGTH_LONG).show()
            observeDownloadProgress(currentDownloadId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Nabigo: ${e.message}")
            Toast.makeText(this, "❌ Hindi makapagsimula", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeDownloadProgress(downloadId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            while (true) {
                try {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor: Cursor? = dm.query(query)
                    if (cursor == null || !cursor.moveToFirst()) {
                        cursor?.close()
                        delay(400)
                        continue
                    }

                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    if (total > 0) {
                        val percent = ((downloaded * 100) / total).toInt()
                        runOnUiThread {
                            webView.evaluateJavascript("updateProgressFromApp($percent);", null)
                        }
                    }

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            runOnUiThread {
                                webView.evaluateJavascript("downloadCompleteFromApp();", null)
                            }
                            cursor.close()
                            break
                        }
                        DownloadManager.STATUS_FAILED -> {
                            runOnUiThread {
                                webView.evaluateJavascript("downloadErrorFromApp();", null)
                            }
                            cursor.close()
                            break
                        }
                    }
                    cursor.close()
                    delay(400)

                } catch (e: Exception) {
                    Log.e(TAG, "Progress error: ${e.message}")
                    break
                }
            }
        }
    }

    // ✅ TAWAGIN MULA SA HTML — BUBUKSAN ANG APK!
    @JavascriptInterface
    fun openDownloadedApk() {
        try {
            val apkFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "BisyoApp-update.apk"
            )

            if (!apkFile.exists()) {
                Toast.makeText(this, "❌ Hindi mahanap:\nBisyoApp-update.apk sa Downloads", Toast.LENGTH_LONG).show()
                return
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(
                        this@MainActivity,
                        "$packageName.fileprovider",
                        apkFile
                    )
                } else {
                    Uri.fromFile(apkFile)
                }
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)

        } catch (e: Exception) {
            Log.e(TAG, "Install error: ${e.message}")
            Toast.makeText(this, "❌ Hindi mabuksan:\n${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
