package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// 공통 버튼용 클릭 디바운스 기본 간격(ms)
private const val DEFAULT_CLICK_INTERVAL_MS = 500L

/**
 * 같은 버튼을 [intervalMs] 안에 다시 눌러도 무시하는 onClick 래퍼.
 *
 * 더블탭으로 인한 중복 API 호출/중복 화면 전환을 버튼 레벨에서 막는 기본 안전망이다.
 * remember가 컴포저블(=버튼 인스턴스) 단위라 타이머가 버튼마다 독립적이므로,
 * 서로 다른 버튼을 빠르게 연달아 누르는 동작에는 영향이 없다.
 */
@Composable
fun rememberDebouncedClick(
    intervalMs: Long = DEFAULT_CLICK_INTERVAL_MS,
    onClick: () -> Unit,
): () -> Unit {
    var lastClickTime by remember { mutableStateOf(0L) }
    return {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= intervalMs) {
            lastClickTime = now
            onClick()
        }
    }
}
