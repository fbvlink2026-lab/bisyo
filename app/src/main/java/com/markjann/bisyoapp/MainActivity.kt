package com.markjann.bisyoapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ✅ WALANG XML, WALANG WEBVIEW — DIRETSONG TEKSTO
        val textView = TextView(this)
        textView.text = "GUMAGANA ANG APP!\n\nIndex.html dapat mabasa sa:\nfile:///android_asset/index.html"
        textView.textSize = 20f
        setContentView(textView)
    }
}
