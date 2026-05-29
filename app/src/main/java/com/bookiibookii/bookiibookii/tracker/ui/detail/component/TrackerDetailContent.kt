package com.bookiibookii.bookiibookii.tracker.ui.detail.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.tracker.model.TrackerProfileItem
import com.bookiibookii.bookiibookii.tracker.model.TrackerStepLabelStyle
import com.bookiibookii.bookiibookii.tracker.ui.component.TrackerBookCover
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private val OuterCardPadding = 20.dp

// 트래커 상세 본문 (택배/직접교환 공통)
// 헤더 + 그룹 정보 카드 + 단계 리스트
@Composable
fun TrackerDetailContent(
    groupName: String,
    dDay: String,
    statusLabel: String,
    currentStepLabel: String,
    currentStepLabelStyle: TrackerStepLabelStyle,
    myProfile: TrackerProfileItem,
    partnerProfile: TrackerProfileItem,
    exchangeLabel: String,
    secondaryActionLabel: String,
    primaryActionLabel: String,
    steps: List<TrackerStep>,
    onBackClick: () -> Unit,
    onMessageClick: () -> Unit,
    onMoreClick: () -> Unit,
    onSecondaryActionClick: () -> Unit,
    onPrimaryActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        TrackerDetailHeader(
            onBackClick = onBackClick,
            onMessageClick = onMessageClick,
            onMoreClick = onMoreClick,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(OuterCardPadding),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                GroupInfoSection(
                    groupName = groupName,
                    dDay = dDay,
                    statusLabel = statusLabel,
                    currentStepLabel = currentStepLabel,
                    currentStepLabelStyle = currentStepLabelStyle,
                )
                TwoProfileSection(
                    myProfile = myProfile,
                    partnerProfile = partnerProfile,
                    exchangeLabel = exchangeLabel,
                )
                ActionButtonsRow(
                    secondaryLabel = secondaryActionLabel,
                    primaryLabel = primaryActionLabel,
                    onSecondaryClick = onSecondaryActionClick,
                    onPrimaryClick = onPrimaryActionClick,
                )
            }
            TrackerStepList(
                steps = steps,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun TrackerDetailHeader(
    onBackClick: () -> Unit,
    onMessageClick: () -> Unit,
    onMoreClick: () -> Unit,
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
                iconRes = R.drawable.ic_back,
                onClick = onBackClick,
            )
            Text(
                text = "교환 현황",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconCircleButton(
                    iconRes = R.drawable.ic_message,
                    onClick = onMessageClick,
                )
                IconCircleButton(
                    iconRes = R.drawable.ic_meetball,
                    onClick = onMoreClick,
                )
            }
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

@Composable
private fun GroupInfoSection(
    groupName: String,
    dDay: String,
    statusLabel: String,
    currentStepLabel: String,
    currentStepLabelStyle: TrackerStepLabelStyle,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = groupName,
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey900,
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DDayChip(text = dDay)
                    Text(
                        text = statusLabel,
                        style = BookiiBookiiTheme.typography.regular16,
                        color = BookiiBookiiTheme.colors.grey800,
                    )
                }
                StatusProgressBar(
                    currentStepLabel = currentStepLabel,
                    style = currentStepLabelStyle,
                )
            }
            HorizontalDivider(
                thickness = 0.8.dp,
                color = BookiiBookiiTheme.colors.grey100,
            )
        }
    }
}

@Composable
private fun DDayChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = BookiiBookiiTheme.colors.grey100,
                shape = BookiiBookiiTheme.shape.round8,
            )
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
private fun StatusProgressBar(
    currentStepLabel: String,
    style: TrackerStepLabelStyle,
) {
    val bg = when (style) {
        TrackerStepLabelStyle.Main -> BookiiBookiiTheme.colors.uiMainPale
        TrackerStepLabelStyle.Sub -> BookiiBookiiTheme.colors.uiMainSubPale
    }
    val fg = when (style) {
        TrackerStepLabelStyle.Main -> BookiiBookiiTheme.colors.uiMain
        TrackerStepLabelStyle.Sub -> BookiiBookiiTheme.colors.uiMainSub
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .background(color = bg, shape = BookiiBookiiTheme.shape.round8)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = currentStepLabel,
                style = BookiiBookiiTheme.typography.regular10,
                color = fg,
            )
        }
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey100),
            )
        }
    }
}

@Composable
private fun TwoProfileSection(
    myProfile: TrackerProfileItem,
    partnerProfile: TrackerProfileItem,
    exchangeLabel: String,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileColumn(profile = myProfile, modifier = Modifier.weight(1f))
            ProfileColumn(profile = partnerProfile, modifier = Modifier.weight(1f))
        }
        ExchangeConnector(
            label = exchangeLabel,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 52.dp),
        )
    }
}

@Composable
private fun ProfileColumn(
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
            TrackerBookCover(
                modifier = Modifier.size(width = 80.dp, height = 104.dp),
                imageUrl = profile.bookCoverUrl,
                isOwnerBook = profile.isOwnerBook,
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
        ProfilePlaceholder(
            modifier = Modifier
                .offset(x = 27.dp, y = 0.dp)
                .size(44.dp),
            imageUrl = profile.profileImageUrl,
            innerStroke = true,
        )
    }
}

@Composable
private fun TrackerProgressBar(percent: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(BookiiBookiiTheme.shape.round4)
            .background(BookiiBookiiTheme.colors.grey200),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(BookiiBookiiTheme.colors.grey800),
        )
    }
}

@Composable
private fun ExchangeConnector(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(113.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
        ) {
            val dash = 4.dp.toPx()
            drawLine(
                color = Color(0xFFC6C5C2),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
            )
        }
        Box(
            modifier = Modifier
                .background(
                    color = BookiiBookiiTheme.colors.white,
                    shape = BookiiBookiiTheme.shape.round8,
                )
                .border(
                    width = 1.dp,
                    color = BookiiBookiiTheme.colors.grey200,
                    shape = BookiiBookiiTheme.shape.round8,
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = label,
                style = BookiiBookiiTheme.typography.medium11,
                color = BookiiBookiiTheme.colors.grey400,
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    secondaryLabel: String,
    primaryLabel: String,
    onSecondaryClick: () -> Unit,
    onPrimaryClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BottomSheetTwoBtnShort(
            text = secondaryLabel,
            style = BottomSheetBtnStyle.White,
            onClick = onSecondaryClick,
            modifier = Modifier.weight(1f),
        )
        BottomSheetTwoBtnShort(
            text = primaryLabel,
            style = BottomSheetBtnStyle.Orange,
            onClick = onPrimaryClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun TrackerDetailContentPreview() {
    BookiiPreview {
        TrackerDetailContent(
            groupName = "김영하 도장깨기 하실 분",
            dDay = "D-2",
            statusLabel = "살인자의 기억법 · 후기 작성",
            currentStepLabel = "내 책 읽기",
            currentStepLabelStyle = TrackerStepLabelStyle.Main,
            myProfile = TrackerProfileItem(
                nickname = "나",
                bookTitle = "살인자의 기억법",
                bookCoverUrl = null,
                profileImageUrl = null,
                progressPercent = 100,
                isOwnerBook = true,
            ),
            partnerProfile = TrackerProfileItem(
                nickname = "noshel",
                bookTitle = "작별인사",
                bookCoverUrl = null,
                profileImageUrl = null,
                progressPercent = 0,
                isOwnerBook = false,
            ),
            exchangeLabel = "택배 교환",
            secondaryActionLabel = "독서카드 작성",
            primaryActionLabel = "책 후기 작성",
            steps = listOf(
                TrackerStep(
                    title = "반납",
                    description = "파트너에게 책을 돌려보내주세요",
                    status = TrackerStepStatus.Pending,
                ),
                TrackerStep(
                    title = "파트너 책 읽기",
                    description = "작별인사를 읽고 진행률을 기록해주세요",
                    status = TrackerStepStatus.InProgress(chipText = "D-2"),
                ),
                TrackerStep(
                    title = "교환",
                    description = "파트너와 책을 교환해주세요",
                    status = TrackerStepStatus.Completed,
                ),
                TrackerStep(
                    title = "살인자의 기억법 읽기",
                    description = "독서카드를 작성하면 교환독서가 더 즐거워져요",
                    status = TrackerStepStatus.Completed,
                ),
            ),
            onBackClick = {},
            onMessageClick = {},
            onMoreClick = {},
            onSecondaryActionClick = {},
            onPrimaryActionClick = {},
        )
    }
}
