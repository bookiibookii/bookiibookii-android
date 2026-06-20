package com.bookiibookii.bookiibookii.mypage.ui.setting

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.common.DateUtils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview

// 라우트 진입점 — 구 NoticeDetailFragment의 onCreateView/onViewCreated 로직을 그대로 이식.
// 공지 상세는 한 번 들어가면 다시 안으로 진입하지 않는 leaf 화면이라 noticeId 기준 1회 조회로 충분.
@Composable
fun NoticeDetailRoute(
    noticeId: Long,
    title: String,
    onBackClick: () -> Unit,
    viewModel: com.bookiibookii.bookiibookii.mypage.vm.SettingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val context = LocalContext.current
    val noticeDetail by viewModel.noticeDetail.observeAsState()

    LaunchedEffect(noticeId) { viewModel.fetchNoticeDetail(noticeId) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is com.bookiibookii.bookiibookii.mypage.vm.SettingViewModel.Event.ShowToast ->
                    context.showCustomToast(event.message, !event.message.contains("실패") && !event.message.contains("오류"))
                else -> Unit
            }
        }
    }

    NoticeDetailScreen(
        title = title,
        content = noticeDetail?.content ?: "",
        createdAt = noticeDetail?.createdAt ?: "",
        onBackClick = onBackClick,
    )
}

@Composable
fun NoticeDetailScreen(
    title: String = "",
    content: String = "",
    createdAt: String = "",
    onBackClick: () -> Unit = {},
) {
    val displayDate = DateUtils.formatDate(createdAt)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg)
    ) {
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
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(40.dp))
            }
            HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 1.dp)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(top = 20.dp, start = 20.dp, end = 32.dp, bottom = 20.dp)
            ) {
                Text(
                    text = displayDate,
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey500
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = content,
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey700
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeDetailScreenPreview() {
    BookiiPreview {
        NoticeDetailScreen(
            title = "공지사항",
            content = "부키부키를 이용해 주셔서 감사합니다.\n더 나은 서비스를 위해 일부 기능이 업데이트되었습니다.",
            createdAt = "2026. 06. 10.",
        )
    }
}
