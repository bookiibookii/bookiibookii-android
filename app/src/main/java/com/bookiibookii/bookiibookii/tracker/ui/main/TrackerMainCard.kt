package com.bookiibookii.bookiibookii.tracker.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.tracker.model.TrackerCardModel
import com.bookiibookii.bookiibookii.tracker.model.TrackerProfileItem
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun TrackerMainCard(
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
                .offset(x = 17.dp, y = 0.dp)
                .size(44.dp),
            imageUrl = profile.profileImageUrl,
            innerStroke = true,
        )
    }
}

@Composable
private fun BookCover(
    bookCoverUrl: String?,
    isOwnerBook: Boolean,
) {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 132.dp)
            .clip(BookiiBookiiTheme.shape.round8)
            .background(BookiiBookiiTheme.colors.uiBg),
        contentAlignment = Alignment.BottomEnd,
    ) {
        if (!bookCoverUrl.isNullOrBlank()) {
            AsyncImage(
                model = bookCoverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (isOwnerBook) {
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
private fun TrackerMainCardPreview() {
    BookiiPreview {
        TrackerMainCard(
            card = TrackerCardModel(
                groupId = 0L,
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
                primaryActionLabel = "진행률 기록",
                secondaryActionLabel = "독서카드 작성",
            ),
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}
