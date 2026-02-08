package com.youtube.auto.ui.activity

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.youtube.auto.R
import com.youtube.auto.data.model.Video
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoPlayerActivity : AppCompatActivity() {

    private var webView: WebView? = null

    companion object {
        const val EXTRA_VIDEO_ID = "video_id"
        const val EXTRA_VIDEO_TITLE = "video_title"
        private val VIDEO_ID_REGEX = Regex("^[a-zA-Z0-9_-]{1,20}$")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        val videoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: run {
            finish()
            return
        }

        if (!VIDEO_ID_REGEX.matches(videoId)) {
            finish()
            return
        }

        setupWebView(videoId)
    }

    private fun setupWebView(videoId: String) {
        val container = findViewById<FrameLayout>(R.id.playerContainer)
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.domStorageEnabled = true

            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()

            val html = buildPlayerHtml(videoId)
            loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
        }
        container.addView(webView)
    }

    private fun buildPlayerHtml(videoId: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { margin: 0; padding: 0; }
                    html, body { width: 100%; height: 100%; background: #000; }
                    iframe { width: 100%; height: 100%; border: 0; }
                </style>
            </head>
            <body>
                <iframe
                    src="https://www.youtube.com/embed/$videoId?autoplay=1&rel=0&modestbranding=1"
                    allow="autoplay; encrypted-media"
                    allowfullscreen>
                </iframe>
            </body>
            </html>
        """.trimIndent()
    }

    override fun onDestroy() {
        webView?.apply {
            evaluateJavascript("window.stop()", null)
            stopLoading()
            destroy()
        }
        webView = null
        super.onDestroy()
    }

    override fun onPause() {
        webView?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }
}
