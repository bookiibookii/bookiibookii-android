package com.bookiibookii.bookiibookii.mypage.ui.setting

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import androidx.compose.ui.viewinterop.AndroidView
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton

@Composable
fun WebViewScreen(
    title: String,
    assetFileName: String,
    onBackClick: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.white)) {
        WebViewTopBar(title = title, onBackClick = onBackClick)
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = false
                    loadUrl("file:///android_asset/$assetFileName")
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun WebViewTopBar(title: String, onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookiiBackButton(onClick = onBackClick)
            Text(
                text = title,
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Box(modifier = Modifier.size(40.dp))
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200)
    }
}

@Preview(showBackground = true)
@Composable
private fun TermsScreenPreview() {
    BookiiPreview {
        WebViewScreen(title = "서비스 이용 약관", assetFileName = "service_terms.html")
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacyScreenPreview() {
    BookiiPreview {
        WebViewScreen(title = "개인정보 처리 방침", assetFileName = "privacy_policy.html")
    }
}
