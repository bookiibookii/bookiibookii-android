package com.bookiibookii.bookiibookii.group.ui.detail

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.group.GroupDetailResponse
import com.bookiibookii.bookiibookii.data.model.group.GroupRule
import com.bookiibookii.bookiibookii.data.model.group.ParticipantSlot
import com.bookiibookii.bookiibookii.group.model.GroupDetailActionButton
import com.bookiibookii.bookiibookii.group.model.GroupDetailUiState
import com.bookiibookii.bookiibookii.group.vm.GroupDetailViewModel
import com.bookiibookii.bookiibookii.ui.component.BookCover
import com.bookiibookii.bookiibookii.ui.component.CardButton
import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 그룹 상세 화면 — VM 주입/상태 수집 (stateful)
// 댓글 sheet는 BottomSheetScaffold 대신 직접 Box stack으로 구현:
//   - 본문은 항상 bottom 170dp (peek 높이) padding을 받아 sheet 아래로 가려지지 않음
//   - sheet 높이 3단계: peek 170 / expanded 544 / keyboard 580
//   - swipe up/down으로 expand 토글 (누적 drag 50dp 초과 시)
//   - 키보드: sheet는 안 올리고 높이만 580으로 키움 + 입력창만 imePadding (입력창 위 댓글 1개 노출)
@Composable
fun GroupDetailRoute(
    onBack: () -> Unit,
    onActionClick: () -> Unit,
    viewModel: GroupDetailViewModel = viewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    // 키보드 표시 여부 — 키보드 뜨면 sheet 높이를 키워 입력창 위 댓글 공간 확보
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    // 3단계 높이: keyboard(580) > expanded(544) > peek(170)
    // sheet 자체엔 imePadding 안 줌 (화면 하단 고정). 입력창만 imePadding으로 키보드 위에 붙고,
    // 높이를 키워서 그 위로 댓글이 보이게 함
    val sheetHeight by animateDpAsState(
        targetValue = when {
            imeVisible -> 600.dp
            expanded -> 544.dp
            else -> 170.dp
        },
        label = "sheetHeight",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GroupDetailScreen(
            uiState = uiState,
            onBack = onBack,
            onActionClick = onActionClick,
            onRetry = viewModel::retry,
            // 본문 하단은 항상 peek 170dp만큼 padding (sheet 아래로 가지 않게)
            modifier = Modifier.padding(bottom = 170.dp),
        )
        GroupCommentBottomSheetRoute(
            expanded = expanded,
            onExpand = { expanded = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sheetHeight)
                .pointerInput(Unit) {
                    // 누적 drag가 50dp 임계 넘으면 토글. drag 끝에서 결정
                    val triggerPx = 50.dp.toPx()
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragEnd = {
                            when {
                                totalDrag < -triggerPx -> expanded = true   // swipe up
                                totalDrag > triggerPx -> expanded = false   // swipe down
                            }
                        },
                        onDragCancel = { totalDrag = 0f },
                    ) { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                },
        )
    }
}

// 그룹 상세 화면
@Composable
fun GroupDetailScreen(
    uiState: GroupDetailUiState,
    onBack: () -> Unit,
    onActionClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        GroupDetailHeader(onBack = onBack)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.loading && uiState.detail == null -> {
                    CircularProgressIndicator(color = BookiiBookiiTheme.colors.uiMain)
                }

                uiState.error != null && uiState.detail == null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = uiState.error,
                            style = BookiiBookiiTheme.typography.regular14,
                            color = BookiiBookiiTheme.colors.grey500,
                        )
                        Text(
                            text = "다시 시도",
                            style = BookiiBookiiTheme.typography.medium14,
                            color = BookiiBookiiTheme.colors.uiMain,
                            modifier = Modifier.clickable(onClick = onRetry),
                        )
                    }
                }

                uiState.detail != null -> {
                    GroupDetailContent(
                        detail = uiState.detail,
                        actionButton = uiState.actionButton,
                        onActionClick = onActionClick,
                    )
                }
            }
        }
    }
}

// 성공 상태 본문 (스크롤)
@Composable
private fun GroupDetailContent(
    detail: GroupDetailResponse,
    actionButton: GroupDetailActionButton?,
    onActionClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        GroupDetailInfoSection(
            detail = detail,
            actionButton = actionButton,
            onActionClick = onActionClick,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GroupDetailDescriptionCard(
                title = "그룹 소개",
                body = detail.groupComment.orEmpty(),
                exchangePlaceName = detail.placeName,
                exchangePlaceAddress = detail.address,
            )
            GroupDetailDescriptionCard(
                title = "그룹 규칙",
                body = detail.rules.joinToString("\n") { it.content },
            )
            GroupDetailMembersCard(
                matchedCount = detail.matchedCount,
                maxCapacity = detail.maxCapacity,
                participantSlots = detail.participantSlots,
            )
        }
    }
}

// 헤더
@Composable
private fun GroupDetailHeader(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = BookiiBookiiTheme.colors.black,
                )
            }
            Text(
                text = "그룹 상세",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        HorizontalDivider(thickness = 1.dp, color = BookiiBookiiTheme.colors.grey200)
    }
}

// 그룹 정보 카드 + 하단 액션 버튼
@Composable
private fun GroupDetailInfoSection(
    detail: GroupDetailResponse,
    actionButton: GroupDetailActionButton?,
    onActionClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GroupDetailBookInfo(
            title = detail.title,
            author = detail.author,
            category = detail.genre,
            exchangeType = tradeTypeLabel(detail.tradeType),
            expectedDays = detail.readingPeriod,
            nickname = detail.hostNickname,
            groupName = detail.groupName,
            bookImage = detail.bookImage,
            hostProfileImageUrl = detail.hostProfileImageUrl,
        )
        // buttonStatus가 TRACKER/FULL/unknown이면 actionButton이 null → 버튼 미표시
        if (actionButton != null) {
            CardButton(
                text = actionButton.text,
                style = actionButton.style,
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GroupDetailBookInfo(
    title: String,
    author: String,
    category: String,
    exchangeType: String,
    expectedDays: Int,
    nickname: String,
    groupName: String,
    bookImage: String?,
    hostProfileImageUrl: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BookCover(
            imageUrl = bookImage,
            modifier = Modifier.size(width = 72.dp, height = 100.dp),
        )
        Column(
            modifier = Modifier
                .height(100.dp)
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GroupDetailExchangeBadge(text = exchangeType)
                    Text(
                        text = title,
                        style = BookiiBookiiTheme.typography.medium16,
                        color = BookiiBookiiTheme.colors.grey900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "$author($category)",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "독서 기간",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey700,
                    )
                    Row {
                        Text(
                            text = "$expectedDays",
                            style = BookiiBookiiTheme.typography.regular14,
                            color = BookiiBookiiTheme.colors.grey800,
                        )
                        Text(
                            text = "일",
                            style = BookiiBookiiTheme.typography.regular14,
                            color = BookiiBookiiTheme.colors.grey700,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfilePlaceholder(
                        modifier = Modifier.size(20.dp),
                        imageUrl = hostProfileImageUrl,
                    )
                    Text(
                        text = nickname,
                        style = BookiiBookiiTheme.typography.regular15,
                        color = BookiiBookiiTheme.colors.grey700,
                    )
                    Text(
                        text = "·",
                        style = BookiiBookiiTheme.typography.regular15,
                        color = BookiiBookiiTheme.colors.grey700,
                    )
                    Text(
                        text = groupName,
                        style = BookiiBookiiTheme.typography.regular15,
                        color = BookiiBookiiTheme.colors.grey500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// 카드 내부 교환 방식 라벨 배지
@Composable
private fun GroupDetailExchangeBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(BookiiBookiiTheme.shape.round8)
            .background(BookiiBookiiTheme.colors.uiMainPale)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Text(
            text = "$text 교환",
            style = BookiiBookiiTheme.typography.medium11,
            color = BookiiBookiiTheme.colors.uiMain,
        )
    }
}

// 그룹 소개 / 그룹 규칙 카드
@Composable
private fun GroupDetailDescriptionCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    exchangePlaceName: String? = null,
    exchangePlaceAddress: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .background(BookiiBookiiTheme.colors.white)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = BookiiBookiiTheme.typography.medium16,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            HorizontalDivider(thickness = 1.dp, color = BookiiBookiiTheme.colors.grey100)
        }
        Text(
            text = body,
            style = BookiiBookiiTheme.typography.regular15,
            color = BookiiBookiiTheme.colors.grey700,
            modifier = Modifier.fillMaxWidth(),
        )
        if (exchangePlaceName != null) {
            Column(modifier = Modifier.fillMaxWidth()) {
                DashedDivider(color = BookiiBookiiTheme.colors.grey100)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(BookiiBookiiTheme.shape.round8)
                            .background(BookiiBookiiTheme.colors.grey100)
                            .padding(horizontal = 6.dp),
                    ) {
                        Text(
                            text = "교환 희망 장소",
                            style = BookiiBookiiTheme.typography.regular14,
                            color = BookiiBookiiTheme.colors.grey500,
                        )
                    }
                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = exchangePlaceName,
                            style = BookiiBookiiTheme.typography.regular15,
                            color = BookiiBookiiTheme.colors.grey600,
                            textDecoration = TextDecoration.Underline,
                        )
                        if (exchangePlaceAddress != null) {
                            Text(
                                text = exchangePlaceAddress,
                                style = BookiiBookiiTheme.typography.regular12,
                                color = BookiiBookiiTheme.colors.grey400,
                            )
                        }
                    }
                }
            }
        }
    }
}

// 점선 구분선
@Composable
private fun DashedDivider(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
        )
    }
}

// 참여 멤버 카드
@Composable
private fun GroupDetailMembersCard(
    matchedCount: Int,
    maxCapacity: Int,
    participantSlots: List<ParticipantSlot>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .background(BookiiBookiiTheme.colors.white)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "참여 멤버",
                    style = BookiiBookiiTheme.typography.medium16,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = "$matchedCount/$maxCapacity 명",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.uiMain,
                )
            }
            HorizontalDivider(thickness = 1.dp, color = BookiiBookiiTheme.colors.grey100)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            participantSlots.forEach { slot ->
                ParticipantSlotRow(slot = slot)
            }
        }
    }
}

// 슬롯 한 줄 (HOST / GUEST / EMPTY)
@Composable
private fun ParticipantSlotRow(slot: ParticipantSlot) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfilePlaceholder(
            modifier = Modifier.size(40.dp),
            imageUrl = slot.profileImageUrl,
        )
        when (slot.role) {
            "HOST" -> Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = slot.nickname.orEmpty(),
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                GroupDetailHostChip()
            }

            "EMPTY" -> Text(
                text = "모집 중",
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey600,
            )

            // GUEST 등 그 외 — 닉네임만 표시
            else -> Text(
                text = slot.nickname.orEmpty(),
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey900,
            )
        }
    }
}

@Composable
private fun GroupDetailHostChip() {
    Box(
        modifier = Modifier
            .clip(BookiiBookiiTheme.shape.round8)
            .background(BookiiBookiiTheme.colors.uiMainPale)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = "HOST",
            style = BookiiBookiiTheme.typography.regular11,
            color = BookiiBookiiTheme.colors.uiMain,
        )
    }
}

// tradeType 코드 → 표시 라벨 (Search 화면과 동일 규칙)
private fun tradeTypeLabel(tradeType: String): String = when (tradeType) {
    "DIRECT" -> "직접"
    "DELIVERY" -> "택배"
    else -> ""
}

@Preview(widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun GroupDetailScreenPreview() {
    BookiiPreview {
        GroupDetailScreen(
            uiState = GroupDetailUiState(
                detail = GroupDetailResponse(
                    groupId = 12,
                    groupStatus = "RECRUITING",
                    isHost = true,
                    tradeType = "DELIVERY",
                    placeName = "자취방",
                    address = "서울특별시",
                    title = "녹나무의 여신",
                    bookImage = null,
                    author = "히가시노 게이고",
                    genre = "기타",
                    readingPeriod = 14,
                    matchedCount = 1,
                    maxCapacity = 2,
                    waitingCount = 0,
                    isHot = false,
                    createdAt = "2026. 05. 24.",
                    startDate = null,
                    hostNickname = "이중희카카오",
                    hostProfileImageUrl = null,
                    groupComment = "잠수는 안됩니다",
                    groupName = "안녕하세요",
                    rules = listOf(
                        GroupRule(tag = "MEMO", content = "책에 직접 코멘트를 남겨요!"),
                        GroupRule(tag = "CUSTOM", content = "책을 소중히 다룹시다"),
                        GroupRule(tag = "CUSTOM", content = "반갑습니다"),
                    ),
                    participantSlots = listOf(
                        ParticipantSlot(
                            nickname = "이중희카카오",
                            profileImageUrl = null,
                            role = "HOST",
                            isMe = true,
                        ),
                        ParticipantSlot(
                            nickname = null,
                            profileImageUrl = null,
                            role = "EMPTY",
                            isMe = false,
                        ),
                    ),
                    buttonStatus = "MANAGE",
                ),
                actionButton = GroupDetailActionButton(
                    text = "참여 요청 관리 0",
                    style = CardButtonStyle.Main,
                ),
                loading = false,
                error = null,
            ),
            onBack = {},
            onActionClick = {},
            onRetry = {},
        )
    }
}
