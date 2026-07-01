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
import coil.imageLoader
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.UserBookDto
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview

@Composable
fun ProfileShareDialog(
    name: String = "",
    motto: String = "",
    imageUrl: String? = null,
    representativeBooks: List<UserBookDto> = emptyList(),
    onDismiss: () -> Unit = {},
    onInstagramClick: (isDark: Boolean) -> Unit = {},
    onXClick: (isDark: Boolean) -> Unit = {},
    onDownloadClick: (isDark: Boolean) -> Unit = {},
    onLinkCopyClick: (isDark: Boolean) -> Unit = {},
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
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

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
                    }

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
                            onClick = { onInstagramClick(isDark) },
                        )
                        ShareActionItem(
                            iconRes = R.drawable.img_share_x,
                            label = "X",
                            iconTint = Color.Unspecified,
                            iconSize = 56.dp,
                            labelColor = shareLabelColor,
                            onClick = { onXClick(isDark) },
                        )
                        ShareActionItem(
                            iconRes = R.drawable.ic_download,
                            label = "다운로드",
                            bgColor = BookiiBookiiTheme.colors.grey100,
                            iconTint = BookiiBookiiTheme.colors.grey900,
                            labelColor = shareLabelColor,
                            onClick = { onDownloadClick(isDark) },
                        )
                        ShareActionItem(
                            iconRes = R.drawable.ic_link,
                            label = "링크 복사",
                            bgColor = BookiiBookiiTheme.colors.grey100,
                            iconTint = BookiiBookiiTheme.colors.grey900,
                            labelColor = shareLabelColor,
                            onClick = { onLinkCopyClick(isDark) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProfileShareCardContent(
    name: String,
    motto: String,
    imageUrl: String? = null,
    representativeBooks: List<UserBookDto> = emptyList(),
    isDark: Boolean = false,
    imageLoader: coil.ImageLoader? = null,
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
    val maxRowCount = maxOf(row1Count, row2Count, 4)

    Column(modifier = Modifier.fillMaxWidth().background(cardBg)) {
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProfilePlaceholder(imageUrl = imageUrl, modifier = Modifier.size(92.dp), imageLoader = imageLoader)
            Text(
                text = name,
                style = BookiiBookiiTheme.typography.semibold20,
                color = textColor,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                imageLoader = imageLoader,
            )
        }

        if (row2Count > 0) {
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(separatorBg))
            BookCoverRow(
                books = representativeBooks.drop(row1Count).take(row2Count),
                maxRowCount = maxRowCount,
                cardBg = cardBg,
                imageLoader = imageLoader,
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
    imageLoader: coil.ImageLoader? = null,
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
                        val ctx = androidx.compose.ui.platform.LocalContext.current
                        AsyncImage(
                            model = book.image,
                            imageLoader = imageLoader ?: ctx.imageLoader,
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

private val previewBooks: List<UserBookDto> = List(5) { index ->
    UserBookDto(title = "책 제목 ${index + 1}", auth = "저자", image = null)
}

@Preview(name = "다이얼로그(라이트)", widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun ProfileShareDialogLightPreview() {
    BookiiPreview {
        ProfileShareDialog(
            name = "김스카이",
            motto = "역시나 누군가를 사랑하고\n사랑해야 할 당신을 위해",
            representativeBooks = previewBooks,
        )
    }
}

@Preview(name = "카드 콘텐츠(다크)", widthDp = 412, showBackground = true)
@Composable
private fun ProfileShareCardContentDarkPreview() {
    BookiiPreview {
        ProfileShareCardContent(
            name = "김스카이",
            motto = "역시나 누군가를 사랑하고\n사랑해야 할 당신을 위해",
            representativeBooks = previewBooks,
            isDark = true,
        )
    }
}
