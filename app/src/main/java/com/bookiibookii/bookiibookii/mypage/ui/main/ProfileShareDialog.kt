package com.bookiibookii.bookiibookii.mypage.ui.main

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.UserBookDto
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder

@Composable
fun ProfileShareDialog(
    name: String = "",
    motto: String = "",
    imageUrl: String? = null,
    representativeBooks: List<UserBookDto> = emptyList(),
    onDismiss: () -> Unit = {},
    onInstagramClick: () -> Unit = {},
    onXClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onLinkCopyClick: () -> Unit = {},
) {
    var isDark by remember { mutableStateOf(false) }

    val shareLabelColor = if (isDark) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey900

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73000000))
                .clickable { onDismiss() },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .heightIn(max = screenHeight * 0.85f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) BookiiBookiiTheme.colors.grey900 else BookiiBookiiTheme.colors.white,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "프로필 공유",
                                style = BookiiBookiiTheme.typography.semibold20,
                                color = if (isDark) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey900,
                            )
                            DayNightToggle(isDark = isDark, onToggle = { isDark = it })
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BookiiBookiiTheme.colors.grey100)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_x),
                                contentDescription = "닫기",
                                tint = BookiiBookiiTheme.colors.grey900,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileShareCardContent(
                        name = name,
                        motto = motto,
                        imageUrl = imageUrl,
                        representativeBooks = representativeBooks,
                        isDark = isDark,
                    )

                    // Share actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        ShareActionItem(
                            iconRes = R.drawable.ic_insta,
                            label = "인스타그램",
                            iconTint = Color.Unspecified,
                            iconSize = 56.dp,
                            labelColor = shareLabelColor,
                            onClick = onInstagramClick,
                        )
                        ShareActionItem(
                            iconRes = R.drawable.img_share_x,
                            label = "X",
                            iconTint = Color.Unspecified,
                            iconSize = 56.dp,
                            labelColor = shareLabelColor,
                            onClick = onXClick,
                        )
                        ShareActionItem(
                            iconRes = R.drawable.ic_download,
                            label = "다운로드",
                            bgColor = BookiiBookiiTheme.colors.grey100,
                            iconTint = BookiiBookiiTheme.colors.grey900,
                            labelColor = shareLabelColor,
                            onClick = onDownloadClick,
                        )
                        ShareActionItem(
                            iconRes = R.drawable.ic_link,
                            label = "링크 복사",
                            bgColor = BookiiBookiiTheme.colors.grey100,
                            iconTint = BookiiBookiiTheme.colors.grey900,
                            labelColor = shareLabelColor,
                            onClick = onLinkCopyClick,
                        )
                    }
                }
            }
        }
    }
}

// Card content extracted so MypageFragment can render it off-screen for bitmap capture
@Composable
internal fun ProfileShareCardContent(
    name: String,
    motto: String,
    imageUrl: String? = null,
    representativeBooks: List<UserBookDto> = emptyList(),
    isDark: Boolean = false,
) {
    val cardBg = if (isDark) BookiiBookiiTheme.colors.grey900 else BookiiBookiiTheme.colors.white
    val textColor = if (isDark) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey900
    val mottoBg = if (isDark) BookiiBookiiTheme.colors.grey800 else BookiiBookiiTheme.colors.uiBg
    val mottoBorder = if (isDark) BookiiBookiiTheme.colors.grey700 else BookiiBookiiTheme.colors.grey200
    val mottoText = if (isDark) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey600
    val separatorBg = if (isDark) BookiiBookiiTheme.colors.grey800.copy(alpha = 0.3f) else BookiiBookiiTheme.colors.grey100

    val bookCount = representativeBooks.size
    val (row1Count, row2Count) = when {
        bookCount <= 4 -> Pair(bookCount, 0)
        bookCount == 5 -> Pair(2, 3)
        bookCount == 6 -> Pair(3, 3)
        else -> Pair(3, minOf(bookCount - 3, 4))
    }
    // 1권도 7권(최대 4열) 기준과 동일 크기로 고정
    val maxRowCount = maxOf(row1Count, row2Count, 4)

    Column(modifier = Modifier.fillMaxWidth().background(cardBg)) {
        // Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_bookii_text),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.width(113.dp).height(12.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile photo + name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProfilePlaceholder(imageUrl = imageUrl, modifier = Modifier.size(92.dp))
            Text(
                text = name,
                style = BookiiBookiiTheme.typography.semibold20,
                color = textColor,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Motto box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(mottoBg)
                .border(1.dp, mottoBorder, RoundedCornerShape(16.dp))
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_quote),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.uiMain,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = motto.ifBlank { "한 줄 소개를 입력해주세요" },
                style = BookiiBookiiTheme.typography.medium15,
                color = mottoText,
                textAlign = TextAlign.Center,
            )
        }

        // 나를 대표하는 책 label
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_repressentative_book),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.width(167.dp).height(24.dp),
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(separatorBg))

        if (row1Count > 0) {
            BookCoverRow(
                books = representativeBooks.take(row1Count),
                maxRowCount = maxRowCount,
                cardBg = cardBg,
            )
        }

        if (row2Count > 0) {
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(separatorBg))
            BookCoverRow(
                books = representativeBooks.drop(row1Count).take(row2Count),
                maxRowCount = maxRowCount,
                cardBg = cardBg,
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(separatorBg))
    }
}

@Composable
private fun BookCoverRow(
    books: List<UserBookDto>,
    maxRowCount: Int,
    cardBg: Color,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBg)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp),
    ) {
        val gap = 4.dp
        val bookWidth = (maxWidth - gap * (maxRowCount - 1)) / maxRowCount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            books.forEach { book ->
                Box(
                    modifier = Modifier
                        .width(bookWidth)
                        .aspectRatio(72f / 104f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BookiiBookiiTheme.colors.grey200),
                ) {
                    if (!book.image.isNullOrBlank()) {
                        AsyncImage(
                            model = book.image,
                            contentDescription = book.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayNightToggle(
    isDark: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .width(52.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(300.dp))
            .background(if (isDark) BookiiBookiiTheme.colors.grey800 else BookiiBookiiTheme.colors.grey300)
            .clickable { onToggle(!isDark) }
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isDark) {
            Icon(
                painter = painterResource(R.drawable.ic_dark),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.white,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.white),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.white),
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.ic_light),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.white,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun ShareActionItem(
    iconRes: Int,
    label: String,
    bgColor: Color = Color.Unspecified,
    iconTint: Color = Color.Unspecified,
    iconSize: Dp = 24.dp,
    labelColor: Color = BookiiBookiiTheme.colors.grey900,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .then(
                    if (bgColor != Color.Unspecified) Modifier.clip(CircleShape).background(bgColor)
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(iconSize),
            )
        }
        Text(
            text = label,
            style = BookiiBookiiTheme.typography.regular14,
            color = labelColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileShareDialogLightPreview() {
    ProfileShareDialog(name = "김스카이", motto = "역시나 누군가를 사랑하고\n사랑해야 할 당신을 위해")
}
