package com.bookiibookii.bookiibookii.tracker.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.tracker.model.TrackerAction
import com.bookiibookii.bookiibookii.tracker.model.TrackerCardModel
import com.bookiibookii.bookiibookii.tracker.model.ellipsizeTitle
import com.bookiibookii.bookiibookii.tracker.model.TrackerProfileItem
import com.bookiibookii.bookiibookii.tracker.ui.component.TrackerBookCover
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.component.LocalOnProfileClick
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun TrackerMainCard(
    card: TrackerCardModel,
    onCardClick: () -> Unit,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .clickable(onClick = onCardClick)
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
                showProgress = card.showReadingProgress,
                modifier = Modifier.weight(1f),
            )
            TrackerProfileColumn(
                profile = card.right,
                showProgress = card.showReadingProgress,
                modifier = Modifier.weight(1f),
            )
        }
        // secondary가 없으면 primary 단일 풀폭 버튼 (예: 교환독서 후기 작성)
        if (card.secondaryAction.label.isBlank()) {
            BottomSheetTwoBtnShort(
                text = card.primaryAction.label,
                style = BottomSheetBtnStyle.Orange,
                onClick = onPrimaryAction,
                enabled = card.primaryEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BottomSheetTwoBtnShort(
                    text = card.secondaryAction.label,
                    style = BottomSheetBtnStyle.White,
                    onClick = onSecondaryAction,
                    // 약속 등록 대기 상태(WAITING_HOST_MEETING_REGISTER)면 비활성화
                    enabled = card.secondaryEnabled,
                    modifier = Modifier.weight(1f),
                )
                BottomSheetTwoBtnShort(
                    text = card.primaryAction.label,
                    style = BottomSheetBtnStyle.Orange,
                    onClick = onPrimaryAction,
                    enabled = card.primaryEnabled,
                    modifier = Modifier.weight(1f),
                )
            }
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
                        text = ellipsizeTitle(card.displayBookTitle.stripBookSubtitle(), 18),
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
    showProgress: Boolean,
    modifier: Modifier = Modifier,
) {
    val onProfileClick = LocalOnProfileClick.current
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TrackerBookCover(
                modifier = Modifier.size(width = 100.dp, height = 132.dp),
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
                        text = profile.bookTitle.stripBookSubtitle(),
                        style = BookiiBookiiTheme.typography.medium16,
                        color = BookiiBookiiTheme.colors.grey800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (showProgress) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        TrackerProgressBar(percent = profile.progressPercent)
                        Text(
                            text = profile.progressLabelOverride
                                ?: "${profile.progressPercent}%",
                            style = BookiiBookiiTheme.typography.regular14,
                            color = BookiiBookiiTheme.colors.grey800,
                        )
                    }
                }
            }
        }
        ProfilePlaceholder(
            modifier = Modifier
                .offset(x = 17.dp, y = 0.dp)
                .size(44.dp),
            imageUrl = profile.profileImageUrl,
            innerStroke = true,
            onClick = { onProfileClick(profile.nickname) },
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

@Preview(showBackground = true)
@Composable
private fun TrackerMainCardPreview() {
    BookiiPreview {
        TrackerMainCard(
            card = TrackerCardModel(
                groupId = 0L,
                groupName = "일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼",
                displayBookTitle = "살인자의 기억법",
                bookTitle = "살인자의 기억법",
                progressLabel = "읽는 중",
                dDay = "D-5",
                left = TrackerProfileItem(
                    nickname = "나",
                    bookTitle = "살인자의 기억법 살인자의 기억법",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 48,
                    isOwnerBook = true,
                ),
                right = TrackerProfileItem(
                    nickname = "noshel",
                    bookTitle = "작별인사",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 48,
                    isOwnerBook = false,
                ),
                primaryAction = TrackerAction.RecordProgress,
                secondaryAction = TrackerAction.WriteReadingCard,
            ),
            onCardClick = {},
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}
