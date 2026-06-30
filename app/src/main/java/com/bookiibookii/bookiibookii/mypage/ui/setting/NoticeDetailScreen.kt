package com.bookiibookii.bookiibookii.mypage.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography

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
                    context.showCustomToast(event.message, event.isSuccess)
                else -> Unit
            }
        }
    }

    NoticeDetailScreen(
        title = title,
        content = noticeDetail?.content ?: "",
        authorNickname = noticeDetail?.authorNickname ?: "",
        authorProfileImageUrl = noticeDetail?.authorProfileImageUrl,
        updatedAt = noticeDetail?.updatedAt ?: "",
        onBackClick = onBackClick,
    )
}

@Composable
fun NoticeDetailScreen(
    title: String = "",
    content: String = "",
    authorNickname: String = "",
    authorProfileImageUrl: String? = null,
    updatedAt: String = "",
    onBackClick: () -> Unit = {},
) {
    val colors = BookiiBookiiTheme.colors
    val displayTime = DateUtils.formatNoticeTime(updatedAt)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.uiBg),
    ) {
        // 상단 바
        Column(modifier = Modifier.fillMaxWidth().background(colors.white)) {
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
                    color = colors.grey900,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(40.dp))
            }
            HorizontalDivider(color = colors.grey200, thickness = 1.dp)
        }

        // 본문 스크롤 영역
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.white)
                    .padding(20.dp),
            ) {
                // 작성자 정보 행
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfilePlaceholder(
                        modifier = Modifier.size(22.dp),
                        imageUrl = authorProfileImageUrl,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = authorNickname,
                        style = BookiiBookiiTheme.typography.medium16,
                        color = colors.grey800,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "·",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = colors.grey500,
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = displayTime,
                        style = BookiiBookiiTheme.typography.regular14,
                        color = colors.grey500,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 공지 본문 (마크다운)
                Markdown(
                    content = content,
                    modifier = Modifier.fillMaxWidth(),
                    colors = DefaultMarkdownColors(
                        text = colors.grey700,
                        codeText = colors.grey700,
                        inlineCodeText = colors.grey700,
                        linkText = colors.uiMain,
                        codeBackground = colors.grey100,
                        inlineCodeBackground = colors.grey100,
                        dividerColor = colors.grey200,
                    ),
                    typography = DefaultMarkdownTypography(
                        text = TextStyle(fontSize = 14.sp),
                        paragraph = TextStyle(fontSize = 14.sp),
                        h1 = TextStyle(fontSize = 22.sp),
                        h2 = TextStyle(fontSize = 20.sp),
                        h3 = TextStyle(fontSize = 18.sp),
                        h4 = TextStyle(fontSize = 16.sp),
                        h5 = TextStyle(fontSize = 15.sp),
                        h6 = TextStyle(fontSize = 14.sp),
                        code = TextStyle(fontSize = 13.sp),
                        inlineCode = TextStyle(fontSize = 13.sp),
                        quote = TextStyle(fontSize = 14.sp),
                        ordered = TextStyle(fontSize = 14.sp),
                        bullet = TextStyle(fontSize = 14.sp),
                        list = TextStyle(fontSize = 14.sp),
                        link = TextStyle(fontSize = 14.sp),
                    ),
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
            content = "부키부키를 이용해 주셔서 감사합니다.\n\n더 나은 서비스를 위해 일부 기능이 업데이트되었습니다.",
            authorNickname = "부키팀",
            authorProfileImageUrl = null,
            updatedAt = "",
        )
    }
}
