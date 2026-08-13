package com.bookiibookii.bookiibookii.ui.component

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

fun Context.showCustomToast(message: String, isSuccess: Boolean) {
    val activity = this as? Activity ?: return
    val decorView = activity.window.decorView as? FrameLayout ?: return

    val composeView = ComposeView(activity).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setContent {
            BookiiBookiiTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    BookiiToastContent(message = message, isSuccess = isSuccess)
                }
            }
        }
    }

    decorView.addView(
        composeView,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
    )

    composeView.postDelayed({
        decorView.removeView(composeView)
    }, 2000L)
}

@Composable
fun BookiiToastContent(
    message: String,
    isSuccess: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(BookiiBookiiTheme.colors.white, RoundedCornerShape(20.dp))
            .border(1.dp, BookiiBookiiTheme.colors.grey200, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(if (isSuccess) R.drawable.ic_check else R.drawable.ic_info),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = message,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey700,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(name = "성공 토스트", showBackground = true)
@Composable
private fun BookiiToastSuccessPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(24.dp)) {
            BookiiToastContent(message = "독서카드가 등록되었습니다.", isSuccess = true)
        }
    }
}

@Preview(name = "실패 토스트", showBackground = true)
@Composable
private fun BookiiToastFailPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(24.dp)) {
            BookiiToastContent(message = "오류가 발생했어요.", isSuccess = false)
        }
    }
}
