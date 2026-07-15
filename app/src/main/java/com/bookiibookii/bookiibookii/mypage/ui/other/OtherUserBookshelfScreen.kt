package com.bookiibookii.bookiibookii.mypage.ui.other

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.data.model.mypage.CompletedBook
import com.bookiibookii.bookiibookii.data.model.mypage.FavoriteBook
import com.bookiibookii.bookiibookii.data.model.mypage.OtherRepresentativeBook
import com.bookiibookii.bookiibookii.data.model.mypage.OtherUserBookshelfResult
import com.bookiibookii.bookiibookii.mypage.ui.detail.BookGridView
import com.bookiibookii.bookiibookii.mypage.ui.main.BookSpineItem
import com.bookiibookii.bookiibookii.mypage.vm.OtherUserBookshelfViewModel.BookshelfError
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun OtherUserBookshelfScreen(
    nickname: String,
    bookshelf: OtherUserBookshelfResult?,
    isLoading: Boolean,
    error: BookshelfError?,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
        Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
            OtherBookshelfTopBar(nickname = nickname, onBack = onBack)
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = BookiiBookiiTheme.colors.uiMain,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }

            error != null -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = when (error) {
                            BookshelfError.DEACTIVATED -> "탈퇴한 사용자예요"
                            BookshelfError.NOT_FOUND -> "존재하지 않는 사용자예요"
                            BookshelfError.GENERIC -> "책장을 불러오지 못했어요"
                        },
                        style = BookiiBookiiTheme.typography.regular16,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                }
            }

            else -> {
                val representativeBooks = bookshelf?.representativeBooks
                    ?.sortedBy { it.displayOrder } ?: emptyList()
                val favoriteBooks = bookshelf?.favoriteBooks ?: emptyList()
                val representativeTitles = representativeBooks.map { it.title }.toSet()
                val completedBooks = (bookshelf?.completedBooks ?: emptyList())
                    .sortedByDescending { it.completedAt ?: "" }
                    .sortedByDescending { it.title in representativeTitles }

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OtherRepresentativeBooksSection(nickname = nickname, books = representativeBooks)

                    Spacer(modifier = Modifier.height(8.dp))

                    OtherFavoriteBooksSection(nickname = nickname, favoriteBooks = favoriteBooks)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${completedBooks.size}권",
                            style = BookiiBookiiTheme.typography.medium16,
                            color = BookiiBookiiTheme.colors.grey900,
                        )
                    }

                    if (completedBooks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(BookiiBookiiTheme.colors.white)
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "책장이 비어 있어요.",
                                style = BookiiBookiiTheme.typography.regular16,
                                color = BookiiBookiiTheme.colors.grey600,
                            )
                        }
                    } else {
                        BookGridView(
                            books = completedBooks,
                            representativeTitles = representativeTitles,
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun OtherRepresentativeBooksSection(nickname: String, books: List<OtherRepresentativeBook>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${nickname} 님의 책장",
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey200),
                color = BookiiBookiiTheme.colors.white,
            ) {
                Text(
                    text = "${books.size}/7권",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey700,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (books.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "등록된 책이 없어요",
                    style = BookiiBookiiTheme.typography.regular15,
                    color = BookiiBookiiTheme.colors.grey500,
                )
            }
        } else {
            // 7슬롯 고정 — 빈 슬롯은 Spacer로 채워 스파인 너비를 항상 1/7로 유지
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                repeat(7) { i ->
                    val book = books.getOrNull(i)
                    if (book != null) {
                        BookSpineItem(
                            title = book.title,
                            isOrange = i % 2 == 0,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun OtherFavoriteBooksSection(nickname: String, favoriteBooks: List<FavoriteBook>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${nickname} 님의 인생 책",
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey200),
                color = BookiiBookiiTheme.colors.white,
            ) {
                Text(
                    text = "${favoriteBooks.size}/3권",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey700,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0..2) {
                val book = favoriteBooks.getOrNull(i)
                if (book != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(119f / 170f)) {
                            if (book.image != null) {
                                AsyncImage(
                                    model = book.image,
                                    contentDescription = book.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(10.dp)),
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BookiiBookiiTheme.colors.grey200),
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = book.title.stripBookSubtitle(),
                                style = BookiiBookiiTheme.typography.semibold14,
                                color = BookiiBookiiTheme.colors.grey900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = book.author ?: "",
                                style = BookiiBookiiTheme.typography.regular14,
                                color = BookiiBookiiTheme.colors.grey700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(119f / 170f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BookiiBookiiTheme.colors.grey100),
                        )
                        Spacer(modifier = Modifier.height(52.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OtherBookshelfTopBar(nickname: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.width(88.dp), verticalAlignment = Alignment.CenterVertically) {
                BookiiBackButton(onClick = onBack)
            }
            Text(
                text = "${nickname} 님의 책장",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.width(88.dp))
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 1.dp)
    }
}

// ---- Previews ----

private val previewBookshelf = OtherUserBookshelfResult(
    representativeBooks = listOf(
        OtherRepresentativeBook(title = "데미안", author = "헤르만 헤세", image = null, displayOrder = 0, rating = 4.5),
        OtherRepresentativeBook(title = "1984", author = "조지 오웰", image = null, displayOrder = 1, rating = 5.0),
        OtherRepresentativeBook(title = "작별인사", author = "김영하", image = null, displayOrder = 2, rating = 4.0),
    ),
    favoriteBooks = listOf(
        FavoriteBook(userBookId = 1L, title = "어린 왕자", author = "생텍쥐페리", category = null, image = null),
        FavoriteBook(userBookId = 2L, title = "사피엔스", author = "유발 하라리", category = null, image = null),
    ),
    completedBooks = listOf(
        CompletedBook(memberBookId = 1, groupId = 1, title = "데미안", author = "헤르만 헤세", image = null, category = "(소설)", rating = 4.5, completedAt = "2026-05-01"),
        CompletedBook(memberBookId = 2, groupId = 2, title = "1984", author = "조지 오웰", image = null, category = "(소설)", rating = 5.0, completedAt = "2026-04-20"),
        CompletedBook(memberBookId = 3, groupId = 3, title = "사피엔스", author = "유발 하라리", image = null, category = "(인문)", rating = 4.0, completedAt = "2026-04-10"),
    ),
)

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun OtherUserBookshelfScreenPreview() {
    BookiiBookiiTheme {
        OtherUserBookshelfScreen(
            nickname = "부키",
            bookshelf = previewBookshelf,
            isLoading = false,
            error = null,
            onBack = {},
        )
    }
}

@Preview(name = "빈 책장", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun OtherUserBookshelfEmptyPreview() {
    BookiiBookiiTheme {
        OtherUserBookshelfScreen(
            nickname = "부키",
            bookshelf = OtherUserBookshelfResult(emptyList(), emptyList(), emptyList()),
            isLoading = false,
            error = null,
            onBack = {},
        )
    }
}

@Preview(name = "로딩", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun OtherUserBookshelfLoadingPreview() {
    BookiiBookiiTheme {
        OtherUserBookshelfScreen(
            nickname = "부키",
            bookshelf = null,
            isLoading = true,
            error = null,
            onBack = {},
        )
    }
}

@Preview(name = "탈퇴 사용자 (403)", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun OtherUserBookshelfDeactivatedPreview() {
    BookiiBookiiTheme {
        OtherUserBookshelfScreen(
            nickname = "부키",
            bookshelf = null,
            isLoading = false,
            error = BookshelfError.DEACTIVATED,
            onBack = {},
        )
    }
}
