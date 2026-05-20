package com.bookiibookii.bookiibookii.tracker.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.tracker.model.TrackerCardModel
import com.bookiibookii.bookiibookii.tracker.model.TrackerNotificationItem
import com.bookiibookii.bookiibookii.tracker.model.TrackerProfileItem
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.component.CardButton
import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
private fun TrackerHeader(
    onProfileClick: () -> Unit,
    onAlertClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconCircleButton(
                iconRes = R.drawable.ic_person_fill,
                onClick = onProfileClick,
            )
            Text(
                text = "트래커",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
            )
            IconCircleButton(
                iconRes = R.drawable.ic_alert_32,
                onClick = onAlertClick,
            )
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = BookiiBookiiTheme.colors.grey200,
        )
    }
}

@Composable
private fun IconCircleButton(
    iconRes: Int,
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
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = Color.Unspecified,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerHeaderPreview() {
    BookiiPreview {
        TrackerHeader(
            onProfileClick = {},
            onAlertClick = {},
        )
    }
}

@Composable
private fun TrackerNoticeBanner(
    nickname: String,
    modifier: Modifier = Modifier,
) {
    val noticeText = buildAnnotatedString {
        withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) {
            append(nickname)
        }
        withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) {
            append("님의\n교환독서 현황을 알려드려요")
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white),
    ) {
        Text(
            text = noticeText,
            style = BookiiBookiiTheme.typography.regular28,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = BookiiBookiiTheme.colors.grey100,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerNoticeBannerPreview() {
    BookiiPreview {
        TrackerNoticeBanner(nickname = "sayo")
    }
}

@Composable
private fun TrackerNotificationCard(
    notifications: List<TrackerNotificationItem>,
    modifier: Modifier = Modifier,
) {
    if (notifications.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { notifications.size })
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val item = notifications[page]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.dDay,
                    style = BookiiBookiiTheme.typography.semibold18,
                    color = BookiiBookiiTheme.colors.uiMainSub,
                )
                Text(
                    text = item.body,
                    style = BookiiBookiiTheme.typography.regular18,
                )
                Text(
                    text = item.subText,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey400,
                )
            }
        }
        CarouselIndicator(
            total = notifications.size,
            current = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 24.dp),
        )
    }
}

@Composable
private fun CarouselIndicator(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == current) {
                            BookiiBookiiTheme.colors.grey500
                        } else {
                            BookiiBookiiTheme.colors.grey200
                        },
                    ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerNotificationCardPreview() {
    BookiiPreview {
        val body1 = buildAnnotatedString {
            withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) {
                append("noshel")
            }
            withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey700)) {
                append("님께 ")
            }
            withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) {
                append("살인자의 기억법")
            }
            withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey700)) {
                append("을 발송해주세요")
            }
        }
        val body2 = buildAnnotatedString {
            withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) {
                append("작별인사")
            }
            withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey700)) {
                append("의 독서 진행률을 기록해보세요")
            }
        }
        TrackerNotificationCard(
            notifications = listOf(
                TrackerNotificationItem(
                    dDay = "D-1",
                    body = body1,
                    subText = "책이 파손되지 않도록 꼼꼼히 포장해주세요",
                ),
                TrackerNotificationItem(
                    dDay = "D-5",
                    body = body2,
                    subText = "오늘 읽은 페이지를 기록해주세요",
                ),
                TrackerNotificationItem(
                    dDay = "D-3",
                    body = buildAnnotatedString { append("샘플 알림 3") },
                    subText = "샘플 서브 텍스트",
                ),
            ),
        )
    }
}

@Composable
private fun TrackerCountBoard(
    total: Int,
    reading: Int,
    exchanging: Int,
    review: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .background(BookiiBookiiTheme.colors.white)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CountColumn(label = "전체", count = total, modifier = Modifier.weight(1f))
        CountDivider()
        CountColumn(label = "읽는 중", count = reading, modifier = Modifier.weight(1f))
        CountDivider()
        CountColumn(label = "교환 중", count = exchanging, modifier = Modifier.weight(1f))
        CountDivider()
        CountColumn(label = "후기", count = review, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CountColumn(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey700,
        )
        Text(
            text = count.toString(),
            style = BookiiBookiiTheme.typography.regular24,
            color = if (count == 0) {
                BookiiBookiiTheme.colors.grey300
            } else {
                BookiiBookiiTheme.colors.grey900
            },
        )
    }
}

@Composable
private fun CountDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(48.dp)
            .background(BookiiBookiiTheme.colors.grey100),
    )
}

@Preview(showBackground = true)
@Composable
private fun TrackerCountBoardEmptyPreview() {
    BookiiPreview {
        TrackerCountBoard(
            total = 0,
            reading = 0,
            exchanging = 0,
            review = 0,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerCountBoardFilledPreview() {
    BookiiPreview {
        TrackerCountBoard(
            total = 3,
            reading = 2,
            exchanging = 1,
            review = 0,
        )
    }
}

@Composable
private fun TrackerEmptyCard(
    onCreateGroupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "참여 중인 그룹이 없어요 😭\n읽고 싶은 책으로 그룹을 만들어보세요",
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey900,
            textAlign = TextAlign.Center,
        )
        CardButton(
            text = "그룹 만들기",
            style = CardButtonStyle.Main,
            onClick = onCreateGroupClick,
            modifier = Modifier.fillMaxWidth(),
            shape = BookiiBookiiTheme.shape.round20,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerEmptyCardPreview() {
    BookiiPreview {
        TrackerEmptyCard(onCreateGroupClick = {})
    }
}

@Composable
private fun TrackerCard(
    card: TrackerCardModel,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TrackerCardHeader(card = card)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TrackerProfileColumn(
                profile = card.left,
                modifier = Modifier.weight(1f),
            )
            TrackerProfileColumn(
                profile = card.right,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BottomSheetTwoBtnShort(
                text = card.secondaryActionLabel,
                style = BottomSheetBtnStyle.White,
                onClick = onSecondaryAction,
                modifier = Modifier.weight(1f),
            )
            BottomSheetTwoBtnShort(
                text = card.primaryActionLabel,
                style = BottomSheetBtnStyle.Orange,
                onClick = onPrimaryAction,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TrackerCardHeader(card: TrackerCardModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = card.groupName,
                    style = BookiiBookiiTheme.typography.medium16,
                    color = BookiiBookiiTheme.colors.grey800,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = card.bookTitle,
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                    Text(
                        text = "·",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                    Text(
                        text = card.progressLabel,
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                }
            }
            DDayChip(text = card.dDay)
        }
        HorizontalDivider(
            thickness = 0.8.dp,
            color = BookiiBookiiTheme.colors.grey100,
        )
    }
}

@Composable
private fun DDayChip(text: String) {
    Box(
        modifier = Modifier
            .clip(BookiiBookiiTheme.shape.round8)
            .background(BookiiBookiiTheme.colors.grey100)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium11,
            color = BookiiBookiiTheme.colors.grey700,
        )
    }
}

@Composable
private fun TrackerProfileColumn(
    profile: TrackerProfileItem,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BookCover(
                bookCoverUrl = profile.bookCoverUrl,
                isMine = profile.isMine,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = profile.nickname,
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey700,
                    )
                    Text(
                        text = profile.bookTitle,
                        style = BookiiBookiiTheme.typography.medium16,
                        color = BookiiBookiiTheme.colors.grey800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TrackerProgressBar(percent = profile.progressPercent)
                    Text(
                        text = "${profile.progressPercent}%",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey800,
                    )
                }
            }
        }
        Box(
            // TODO: 나중에 정확한 값으로
            modifier = Modifier
                .offset(x = 17.dp, y = 0.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(BookiiBookiiTheme.colors.grey300),
        )
    }
}

@Composable
private fun BookCover(
    bookCoverUrl: String?,
    isMine: Boolean,
) {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 132.dp)
            .clip(BookiiBookiiTheme.shape.round8)
            .background(BookiiBookiiTheme.colors.uiBg),
        contentAlignment = Alignment.BottomEnd,
    ) {
        if (isMine) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(BookiiBookiiTheme.shape.round4)
                    .background(Color.White.copy(alpha = 0.75f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "내 책",
                    style = BookiiBookiiTheme.typography.regular10,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }
        }
    }
}

@Composable
private fun TrackerProgressBar(percent: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(BookiiBookiiTheme.colors.grey200),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                .height(3.dp)
                .background(BookiiBookiiTheme.colors.grey800),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerCardPreview() {
    BookiiPreview {
        TrackerCard(
            card = TrackerCardModel(
                groupName = "김영하 도장깨기 하실 분",
                bookTitle = "살인자의 기억법",
                progressLabel = "읽는 중",
                dDay = "D-5",
                left = TrackerProfileItem(
                    nickname = "나",
                    bookTitle = "살인자의 기억법 살인자의 기억법",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 48,
                    isMine = true,
                ),
                right = TrackerProfileItem(
                    nickname = "noshel",
                    bookTitle = "작별인사",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 48,
                    isMine = false,
                ),
                primaryActionLabel = "진행률 기록",
                secondaryActionLabel = "독서카드 작성",
            ),
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}

@Composable
fun TrackerMainScreen(
    nickname: String,
    total: Int,
    reading: Int,
    exchanging: Int,
    review: Int,
    notifications: List<TrackerNotificationItem>,
    groups: List<TrackerCardModel>,
    onProfileClick: () -> Unit,
    onAlertClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onPrimaryAction: (groupIndex: Int) -> Unit,
    onSecondaryAction: (groupIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white),
        ) {
            TrackerHeader(
                onProfileClick = onProfileClick,
                onAlertClick = onAlertClick,
            )
            TrackerNoticeBanner(nickname = nickname)
            if (groups.isNotEmpty()) {
                TrackerNotificationCard(notifications = notifications)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TrackerCountBoard(
                total = total,
                reading = reading,
                exchanging = exchanging,
                review = review,
            )
            if (groups.isEmpty()) {
                TrackerEmptyCard(onCreateGroupClick = onCreateGroupClick)
            } else {
                groups.forEachIndexed { index, group ->
                    TrackerCard(
                        card = group,
                        onPrimaryAction = { onPrimaryAction(index) },
                        onSecondaryAction = { onSecondaryAction(index) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun TrackerMainScreenEmptyPreview() {
    BookiiPreview {
        TrackerMainScreen(
            nickname = "sayo",
            total = 0,
            reading = 0,
            exchanging = 0,
            review = 0,
            notifications = emptyList(),
            groups = emptyList(),
            onProfileClick = {},
            onAlertClick = {},
            onCreateGroupClick = {},
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun TrackerMainScreenWithGroupsPreview() {
    BookiiPreview {
        val notifications = listOf(
            TrackerNotificationItem(
                dDay = "D-1",
                body = buildAnnotatedString {
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) { append("noshel") }
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey700)) { append("님께 ") }
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) { append("살인자의 기억법") }
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey700)) { append("을 발송해주세요") }
                },
                subText = "책이 파손되지 않도록 꼼꼼히 포장해주세요",
            ),
            TrackerNotificationItem(
                dDay = "D-5",
                body = buildAnnotatedString {
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) { append("작별인사") }
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey700)) { append("의 진행률을 기록해보세요") }
                },
                subText = "오늘 읽은 페이지를 기록해주세요",
            ),
            TrackerNotificationItem(
                dDay = "D-3",
                body = buildAnnotatedString {
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) { append("데미안") }
                    withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey700)) { append("의 후기를 작성해주세요") }
                },
                subText = "이번 주말까지 작성을 권장드려요",
            ),
        )
        val groups = listOf(
            TrackerCardModel(
                groupName = "김영하 도장깨기 하실 분",
                bookTitle = "살인자의 기억법",
                progressLabel = "읽는 중",
                dDay = "D-5",
                left = TrackerProfileItem(
                    nickname = "나",
                    bookTitle = "살인자의 기억법",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 48,
                    isMine = true,
                ),
                right = TrackerProfileItem(
                    nickname = "noshel",
                    bookTitle = "작별인사",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 48,
                    isMine = false,
                ),
                primaryActionLabel = "진행률 기록",
                secondaryActionLabel = "독서카드 작성",
            ),
            TrackerCardModel(
                groupName = "김영하 도장깨기 하실 분",
                bookTitle = "살인자의 기억법",
                progressLabel = "후기 작성",
                dDay = "D-3",
                left = TrackerProfileItem(
                    nickname = "나",
                    bookTitle = "살인자의 기억법",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 100,
                    isMine = true,
                ),
                right = TrackerProfileItem(
                    nickname = "noshel",
                    bookTitle = "작별인사",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 100,
                    isMine = false,
                ),
                primaryActionLabel = "진행률 기록",
                secondaryActionLabel = "독서카드 작성",
            ),
            TrackerCardModel(
                groupName = "독서 모임 셋째",
                bookTitle = "데미안",
                progressLabel = "교환 중",
                dDay = "D-7",
                left = TrackerProfileItem(
                    nickname = "나",
                    bookTitle = "데미안",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 20,
                    isMine = true,
                ),
                right = TrackerProfileItem(
                    nickname = "partner3",
                    bookTitle = "1984",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 35,
                    isMine = false,
                ),
                primaryActionLabel = "진행률 기록",
                secondaryActionLabel = "독서카드 작성",
            ),
        )
        TrackerMainScreen(
            nickname = "sayo",
            total = 3,
            reading = 2,
            exchanging = 1,
            review = 0,
            notifications = notifications,
            groups = groups,
            onProfileClick = {},
            onAlertClick = {},
            onCreateGroupClick = {},
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}
