package com.bookiibookii.bookiibookii.mypage.ui.common

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private val DAUM_POSTCODE_HTML = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        html, body { margin: 0; padding: 0; width: 100%; height: 100%; }
        * { -webkit-tap-highlight-color: rgba(0,0,0,0); }
    </style>
</head>
<body>
    <div id="layer" style="display:none; position:fixed; overflow:hidden; z-index:1; -webkit-overflow-scrolling:touch;"></div>
    <script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
    <script>
        function execDaumPostcode() {
            var layer = document.getElementById('layer');
            new daum.Postcode({
                oncomplete: function(data) {
                    window.Android.onAddressSelected(data.address, data.zonecode);
                },
                width: '100%',
                height: '100%'
            }).embed(layer);

            layer.style.display = 'block';
            layer.style.width = '100%';
            layer.style.height = '100%';
            layer.style.top = '0px';
            layer.style.left = '0px';
        }
    </script>
</body>
</html>
""".trimIndent()

@Composable
fun DaumAddressWebView(
    onResult: (address: String, zipCode: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val handler = remember { Handler(Looper.getMainLooper()) }
    val bridge = remember {
        object : Any() {
            @JavascriptInterface
            fun onAddressSelected(address: String, zipCode: String) {
                handler.post { onResult(address, zipCode) }
            }
        }
    }

    BackHandler(onBack = onBack)

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(false)
                }
                addJavascriptInterface(bridge, "Android")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        view.loadUrl("javascript:execDaumPostcode();")
                    }
                }
                loadDataWithBaseURL("https://daum.net", DAUM_POSTCODE_HTML, "text/html", "UTF-8", null)
            }
        },
    )
}
