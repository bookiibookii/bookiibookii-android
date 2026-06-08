package com.bookiibookii.bookiibookii.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.notification.model.NotificationIconStyle
import com.bookiibookii.bookiibookii.notification.model.NotificationTab
import com.bookiibookii.bookiibookii.notification.model.NotificationUiModel
import com.bookiibookii.bookiibookii.notification.model.notificationBody
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 알림 센터 화면 (풀스크린, stateless)
// - 헤더: 뒤로가기 / "알림" / + (키워드 알림 설정 진입)
// - 탭: 시스템 알림 / 키워드 알림 (세그먼트 토글)
// - 리스트: 선택된 탭의 알림 카드들
@Composable
fun NotificationScreen(
    selectedTab: NotificationTab,
    notifications: List<NotificationUiModel>,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onTabSelect: (NotificationTab) -> Unit,
    onNotificationClick: (NotificationUiModel) -> Unit,
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    // 바닥 근처(끝에서 3개 전) 도달 시 다음 페이지 요청
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            last >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }
    Scaffold(
        topBar = {
            Column {
                NotificationHeader(onBackClick = onBackClick, onAddClick = onAddClick)
                NotificationTabRow(selectedTab = selectedTab, onTabSelect = onTabSelect)
            }
        },
        containerColor = BookiiBookiiTheme.colors.uiBg,
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(notifications, key = { it.id }) { item ->
                NotificationCard(item = item, onClick = { onNotificationClick(item) })
            }
        }
    }
}

// 상단 헤더
@Composable
private fun NotificationHeader(
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(BookiiBookiiTheme.colors.white)
                .padding(horizontal = 16.dp),
        ) {
            HeaderIconButton(
                iconRes = R.drawable.ic_back,
                contentDescription = "뒤로",
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            Text(
                text = "알림",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp),
            )
            HeaderIconButton(
                iconRes = R.drawable.ic_plus,
                contentDescription = "키워드 알림 설정",
                onClick = onAddClick,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BookiiBookiiTheme.colors.grey200),
        )
    }
}

@Composable
private fun HeaderIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier.size(24.dp),
        )
    }
}

// 세그먼트 토글 — 시스템 알림 / 키워드 알림
@Composable
private fun NotificationTabRow(
    selectedTab: NotificationTab,
    onTabSelect: (NotificationTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.uiBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NotificationTabButton(
            text = "시스템 알림",
            selected = selectedTab == NotificationTab.SYSTEM,
            onClick = { onTabSelect(NotificationTab.SYSTEM) },
            modifier = Modifier.weight(1f),
        )
        NotificationTabButton(
            text = "키워드 알림",
            selected = selectedTab == NotificationTab.KEYWORD,
            onClick = { onTabSelect(NotificationTab.KEYWORD) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NotificationTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(BookiiBookiiTheme.shape.round20)
            .background(
                if (selected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.white,
            )
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = BookiiBookiiTheme.shape.round20,
                    )
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium16,
            color = if (selected) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey900,
        )
    }
}

// 알림 카드 — 좌측 아이콘 + 본문 내용
@Composable
private fun NotificationCard(
    item: NotificationUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .background(BookiiBookiiTheme.colors.white)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        NotificationIcon(style = item.iconStyle)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.title,
                    style = BookiiBookiiTheme.typography.medium15,
                    color = BookiiBookiiTheme.colors.grey900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (item.isUnread) {
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(BookiiBookiiTheme.colors.uiMain),
                    )
                }
            }
            Text(
                text = item.body,
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey700,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.timeText,
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey400,
                )
                Text(
                    text = item.bookTitle,
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey400,
                )
            }
        }
    }
}

// 알림 종류별 40dp 원형 아이콘
@Composable
private fun NotificationIcon(style: NotificationIconStyle) {
    val backgroundColor: Color
    val tint: Color
    when (style) {
        NotificationIconStyle.HIGHLIGHT -> {
            backgroundColor = BookiiBookiiTheme.colors.uiMainPale
            tint = BookiiBookiiTheme.colors.uiMain
        }

        NotificationIconStyle.NORMAL -> {
            backgroundColor = BookiiBookiiTheme.colors.grey200
            tint = BookiiBookiiTheme.colors.grey500
        }
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_book),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun NotificationScreenPreview() {
    BookiiPreview {
        NotificationScreen(
            selectedTab = NotificationTab.SYSTEM,
            notifications = previewNotifications,
            onBackClick = {},
            onAddClick = {},
            onTabSelect = {},
            onNotificationClick = {},
        )
    }
}

private val previewNotifications: List<NotificationUiModel> = listOf(
    NotificationUiModel(
        id = 1,
        title = "똑똑! 새로운 참여 요청이 왔어요.",
        body = notificationBody(
            nickname = "닉네임",
            bookTitle = "책 제목",
            tail = " 그룹에 함께하고 싶어 해요. 프로필을 확인해볼까요?",
        ),
        timeText = "5분 전",
        bookTitle = "책 제목",
        isUnread = true,
        iconStyle = NotificationIconStyle.HIGHLIGHT,
    ),
    NotificationUiModel(
        id = 2,
        title = "새로운 댓글이 달렸어요.",
        body = notificationBody(
            nickname = "닉네임",
            bookTitle = "책 제목",
            tail = " 그룹에 댓글을 남겼어요. 확인해볼까요?",
        ),
        timeText = "5분 전",
        bookTitle = "책 제목",
        isUnread = false,
        iconStyle = NotificationIconStyle.NORMAL,
    ),
)
