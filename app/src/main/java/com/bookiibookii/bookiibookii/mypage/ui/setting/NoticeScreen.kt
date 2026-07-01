package com.bookiibookii.bookiibookii.mypage.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.model.mypage.NoticeSummary
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography

@Composable
fun NoticeRoute(
    onBackClick: () -> Unit,
    onNoticeClick: (noticeId: Long, title: String) -> Unit,
    viewModel: com.bookiibookii.bookiibookii.mypage.vm.SettingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val context = LocalContext.current
    val notices by viewModel.notices.observeAsState(emptyList())

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.fetchNotices()
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is com.bookiibookii.bookiibookii.mypage.vm.SettingViewModel.Event.ShowToast ->
                    context.showCustomToast(event.message, event.isSuccess)
                else -> Unit
            }
        }
    }

    NoticeScreen(
        notices = notices,
        onBackClick = onBackClick,
        onNoticeClick = onNoticeClick,
    )
}

@Composable
fun NoticeScreen(
    notices: List<NoticeSummary> = emptyList(),
    onBackClick: () -> Unit = {},
    onNoticeClick: (noticeId: Long, title: String) -> Unit = { _, _ -> },
) {
    Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
        NoticeTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            if (notices.isEmpty()) {
                NoticeEmptyCard()
            } else {
                notices.forEach { notice ->
                    NoticeItemCard(notice = notice, onClick = { onNoticeClick(notice.id, notice.title) })
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun NoticeTopBar(onBackClick: () -> Unit) {
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
                text = "공지사항",
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

@Composable
private fun NoticeEmptyCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "아직 공지사항이 없어요.",
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey900,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "공지가 생기면 바로 전달드릴게요.",
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey600,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NoticeItemCard(notice: NoticeSummary, onClick: () -> Unit) {
    val colors = BookiiBookiiTheme.colors
    val displayTime = DateUtils.formatNoticeTime(notice.updatedAt)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.white)
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 제목 + 읽지 않음 dot + 화살표
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = notice.title,
                    style = BookiiBookiiTheme.typography.semibold16,
                    color = colors.grey900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (!notice.isRead) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.uiMain),
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = colors.grey900,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { scaleX = -1f },
            )
        }

        // summary 마크다운
        Markdown(
            content = notice.summary,
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
                h1 = TextStyle(fontSize = 20.sp),
                h2 = TextStyle(fontSize = 18.sp),
                h3 = TextStyle(fontSize = 16.sp),
                h4 = TextStyle(fontSize = 15.sp),
                h5 = TextStyle(fontSize = 14.sp),
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

        // 작성자 정보 + 시간
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 0.dp),
        ) {
            ProfilePlaceholder(
                modifier = Modifier.size(20.dp),
                imageUrl = notice.authorProfileImageUrl,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = notice.authorNickname,
                style = BookiiBookiiTheme.typography.medium14,
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
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeScreenPreview() {
    BookiiPreview {
        NoticeScreen()
    }
}
