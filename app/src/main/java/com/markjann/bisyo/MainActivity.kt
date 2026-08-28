package com.markjann.bisyo

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    private var installInProgress = false

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            if (id == currentDownloadId) {
                checkDownloadAndInstall(id)
            }
        }
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.all { it.value }
            if (allGranted) {
                Toast.makeText(this, "✅ Pahintulot ibinigay", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "⚠️ Kailangan ng pahintulot para mag-download", Toast.LENGTH_LONG).show()
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

        webView.addJavascriptInterface(this, "AndroidApp")

        webView.setDownloadListener { url, _, _, _, _ ->
            downloadApk(url)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                Log.e(TAG, "❌ Error loading: ${request?.url} — ${error?.description}")
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                if (url.endsWith(".apk", ignoreCase = true)) {
                    downloadApk(url)
                    return true
                }
                return false
            }
        }

        registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

        checkPermissions()
        webView.loadUrl("file:///android_asset/index.html") // ✅ PUMUNTA AGAD SA MAIN.HTML
    }

    private fun checkPermissions() {
        val neededPermissions = mutableListOf<String>()

        // Android 13+ — kailangan ng pahintulot na mag-install
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
            }
        } else {
            // Android 12 at mas luma — kailangan ng imbakan
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                neededPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (neededPermissions.isNotEmpty()) {
            requestPermissions.launch(neededPermissions.toTypedArray())
        }
    }

    private fun downloadApk(apkUrl: String) {
        if (installInProgress) {
            Toast.makeText(this, "⚠️ May pag-update na ginagawa...", Toast.LENGTH_SHORT).show()
            return
        }

        // Suriin ang pahintulot bago magpatuloy
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "⚠️ Kailangan muna ng pahintulot — subukan muli", Toast.LENGTH_LONG).show()
            checkPermissions()
            return
        }

        // Android 8+ — pahintulot mula sa "Hindi Kilalang Pinagmulan"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                val intent = Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(
                    this,
                    "⚠️ Pahintulutan muna ang pag-install mula sa app na ito",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        installInProgress = true
        Toast.makeText(this, "📤 Sinisimulan ang pag-update...", Toast.LENGTH_SHORT).show()

        try {
            val uri = Uri.parse(apkUrl)
            val request = DownloadManager.Request(uri).apply {
                setTitle("Pag-update — Proyekto ni Mark Jann Tampok")
                setDescription("Awtomatikong nag-i-install...")
                setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
                )
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "BisyoApp-update.apk"
                )
                setMimeType("application/vnd.android.package-archive")
            }

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            currentDownloadId = dm.enqueue(request)

            Toast.makeText(this, "✅ Nagsimula ang pag-download...", Toast.LENGTH_SHORT).show()
            observeDownloadProgress(currentDownloadId)

        } catch (e: Exception) {
            installInProgress = false
            Log.e(TAG, "❌ Nabigo: ${e.message}", e)
            Toast.makeText(this, "❌ Hindi makapagsimula: ${e.message}", Toast.LENGTH_LONG).show()
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
                        delay(300)
                        continue
                    }

                    val status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                    )
                    val downloaded = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                    val total = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    )

                    if (total > 0) {
                        val percent = ((downloaded * 100) / total).toInt()
                        runOnUiThread {
                            webView.evaluateJavascript("updateProgressFromApp($percent);", null)
                        }
                    }

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        runOnUiThread {
                            webView.evaluateJavascript("downloadCompleteFromApp();", null)
                        }
                        cursor.close()
                        break
                    }

                    if (status == DownloadManager.STATUS_FAILED) {
                        runOnUiThread {
                            webView.evaluateJavascript("downloadErrorFromApp();", null)
                            Toast.makeText(this@MainActivity, "❌ Nabigo ang pag-download", Toast.LENGTH_LONG).show()
                        }
                        installInProgress = false
                        cursor.close()
                        break
                    }

                    cursor.close()
                    delay(300)
                } catch (e: Exception) {
                    Log.e(TAG, "Progress check error: ${e.message}")
                    break
                }
            }
        }
    }

    private fun checkDownloadAndInstall(downloadId: Long) {
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = dm.query(query)

        if (cursor?.moveToFirst() == true) {
            val status = cursor.getInt(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )
            cursor.close()

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                installApkDirectly()
            } else {
                runOnUiThread {
                    Toast.makeText(this, "❌ Nabigo ang pag-download", Toast.LENGTH_LONG).show()
                    webView.evaluateJavascript("downloadErrorFromApp();", null)
                }
                installInProgress = false
            }
        }
    }

    private fun installApkDirectly() {
        try {
            val apkFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "BisyoApp-update.apk"
            )

            if (!apkFile.exists()) {
                Toast.makeText(this, "❌ Hindi mahanap ang APK", Toast.LENGTH_LONG).show()
                installInProgress = false
                return
            }

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    this,
                    "$packageName.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Toast.makeText(
                this,
                "✅ Tapos na! Bubukas ang installer...\nMagsasara ang app at mag-uupdate.",
                Toast.LENGTH_LONG
            ).show()

            startActivity(installIntent)

            // ✅ ISARA ANG APP — HINTAY BAGO TAPUSIN
            CoroutineScope(Dispatchers.Main).launch {
                delay(800)
                finishAffinity() // ✅ MAGSASARA ANG APP — papalitan ng bago
            }

        } catch (e: Exception) {
            Log.e(TAG, "Install error: ${e.message}", e)
            Toast.makeText(this, "❌ Hindi mabuksan ang installer: ${e.message}", Toast.LENGTH_LONG).show()
            installInProgress = false
        }
    }

    @JavascriptInterface
    fun openDownloadedApk() {
        Toast.makeText(this, "📤 Sinisimulan ang awtomatikong pag-update...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(downloadReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Receiver unregister error: ${e.message}")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
