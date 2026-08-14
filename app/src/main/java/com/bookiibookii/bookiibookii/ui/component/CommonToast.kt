package com.bookiibookii.bookiibookii.ui.component

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.activity.ComponentDialog
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
import androidx.core.view.WindowCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private const val TOAST_DURATION_MS = 2000L

/**
 * Compose Dialog 안에서는 LocalContext가 ContextThemeWrapper라 Activity로 바로 캐스팅되지 않는다.
 * baseContext를 타고 올라가면서 실제 Activity를 찾는다.
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context.showCustomToast(message: String, isSuccess: Boolean) {
    val activity = findActivity() ?: return
    if (activity.isFinishing || activity.isDestroyed) return

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

    // 액티비티 decorView에 붙이면 Dialog/BottomSheet(별도 윈도우) 뒤에 가려진다.
    // 나중에 추가된 윈도우가 위에 쌓이니까 토스트도 자체 윈도우로 띄운다.
    val dialog = ComponentDialog(activity, R.style.Theme_Bookii_Toast)
    dialog.setContentView(composeView)
    dialog.setCancelable(false)
    dialog.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
        )
        clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        // 포커스랑 터치를 안 가져가야 아래 다이얼로그의 입력이랑 IME가 그대로 유지된다.
        addFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )
        setWindowAnimations(0)
        WindowCompat.setDecorFitsSystemWindows(this, false)
    }

    // 액티비티가 먼저 죽으면 WindowLeaked가 뜨니까 같이 정리한다.
    val lifecycleOwner = activity as? LifecycleOwner
    val observer = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            runCatching { dialog.dismiss() }
        }
    }
    lifecycleOwner?.lifecycle?.addObserver(observer)
    dialog.setOnDismissListener { lifecycleOwner?.lifecycle?.removeObserver(observer) }

    dialog.show()

    composeView.postDelayed({
        if (dialog.isShowing && !activity.isFinishing && !activity.isDestroyed) {
            runCatching { dialog.dismiss() }
        }
    }, TOAST_DURATION_MS)
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
