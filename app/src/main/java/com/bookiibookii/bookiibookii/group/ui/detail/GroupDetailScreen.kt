package com.bookiibookii.bookiibookii.group.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.CardButton
import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 그룹 상세 화면
// groupId: 라우트로 전달받는 그룹 식별자 (현재는 미사용, 다음 VM 연결 단계에서 사용)
@Suppress("UNUSED_PARAMETER")
@Composable
fun GroupDetailScreen(
    groupId: Long,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        GroupDetailHeader(onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            GroupDetailInfoSection()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GroupDetailDescriptionCard(
                    title = "그룹 소개",
                    body = "고전 문학이랑 교환 원해요. 민음사 전집 도장 깨기 중입니다...",
                    exchangePlaceName = "숭실대학교정보과학관",
                    exchangePlaceAddress = "서울 동작구 사당로 50",
                )
                GroupDetailDescriptionCard(
                    title = "그룹 규칙",
                    body = "책을 자유롭게 읽어주세요.\n독서카드 많이많이 올려주셨으면 좋겠어요!",
                )
                GroupDetailMembersCard(
                    currentCount = 1,
                    maxCount = 2,
                    hostName = "noshel",
                )
            }
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

// 그룹 정보 카드 + 하단 버튼
@Composable
private fun GroupDetailInfoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GroupDetailBookInfo(
            title = "살인자의 기억법",
            author = "김영하",
            category = "한국소설",
            exchangeType = "직접",
            expectedDays = 7,
            nickname = "닉네임",
            groupName = "그룹명",
        )
        CardButton(
            text = "참여 신청하기",
            style = CardButtonStyle.Main,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
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
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 100.dp)
                .clip(BookiiBookiiTheme.shape.round8)
                .background(BookiiBookiiTheme.colors.uiBg),
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
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(BookiiBookiiTheme.shape.round50)
                            .background(BookiiBookiiTheme.colors.uiBg),
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
    currentCount: Int,
    maxCount: Int,
    hostName: String,
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
                    text = "$currentCount/$maxCount 명",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.uiMain,
                )
            }
            HorizontalDivider(thickness = 1.dp, color = BookiiBookiiTheme.colors.grey100)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // 호스트
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(BookiiBookiiTheme.shape.round16)
                        .background(BookiiBookiiTheme.colors.uiBg),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = hostName,
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey900,
                    )
                    GroupDetailHostChip()
                }
            }
            // 모집 중 빈 자리
            repeat(maxCount - currentCount) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(BookiiBookiiTheme.shape.round16)
                            .background(BookiiBookiiTheme.colors.uiBg),
                    )
                    Text(
                        text = "모집 중",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey600,
                    )
                }
            }
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

@Preview(widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun GroupDetailScreenPreview() {
    BookiiPreview {
        GroupDetailScreen(groupId = 0L, onBack = {})
    }
}
