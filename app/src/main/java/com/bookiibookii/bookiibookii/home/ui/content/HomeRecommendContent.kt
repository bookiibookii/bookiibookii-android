package com.bookiibookii.bookiibookii.home.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.data.model.group.GroupItem
import com.bookiibookii.bookiibookii.data.model.group.HomeLayoutType
import com.bookiibookii.bookiibookii.data.model.group.HomeSection
import com.bookiibookii.bookiibookii.data.model.group.HomeSectionItem
import com.bookiibookii.bookiibookii.home.ui.component.BookThumbnail
import com.bookiibookii.bookiibookii.home.ui.component.RecommendBookRow
import com.bookiibookii.bookiibookii.home.ui.component.RecommendGroupRow
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

internal fun LazyListScope.homeRecommendContent(
    sections: List<HomeSection>,
    onGroupClick: (Long) -> Unit,
    onBookClick: (HomeSectionItem) -> Unit = {},
) {
    item(
        key = "recommend-top-spacer",
        contentType = "spacer",
    ) {
        Box(Modifier.fillMaxWidth().height(8.dp))
    }

    // 아이템 없는 섹션 + 미지원 레이아웃은 숨김. 나머지는 응답 순서대로 렌더.
    val visibleSections = sections.filter {
        it.items.isNotEmpty() && it.layoutType in SUPPORTED_LAYOUTS
    }

    visibleSections.forEachIndexed { index, section ->
        if (index > 0) {
            item(
                key = "${section.sectionType}-$index-spacer",
                contentType = "spacer",
            ) { Box(Modifier.fillMaxWidth().height(8.dp)) }
        }

        when (section.layoutType) {
            HomeLayoutType.GROUP_CARD_CAROUSEL -> groupCarouselSectionItems(index, section, onGroupClick)
            HomeLayoutType.BOOK_THUMBNAIL_CAROUSEL -> bookCarouselSectionItems(index, section, onBookClick)
            HomeLayoutType.BOOK_THUMBNAIL_GRID -> bookGridSectionItems(index, section, onBookClick)
        }
    }

}

private val SUPPORTED_LAYOUTS = setOf(
    HomeLayoutType.GROUP_CARD_CAROUSEL,
    HomeLayoutType.BOOK_THUMBNAIL_CAROUSEL,
    HomeLayoutType.BOOK_THUMBNAIL_GRID,
)

private fun LazyListScope.sectionHeaderItem(
    sectionIndex: Int,
    section: HomeSection,
) {
    item(
        key = "${section.sectionType}-$sectionIndex-header",
        contentType = "section-header",
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
        ) {
            HomeSectionHeader(
                title = section.title,
                subtitle = section.subtitle,
            )
        }
    }
}

private fun LazyListScope.groupCarouselSectionItems(
    sectionIndex: Int,
    section: HomeSection,
    onGroupClick: (Long) -> Unit,
) {
    sectionHeaderItem(sectionIndex, section)
    item(
        key = "${section.sectionType}-$sectionIndex-content",
        contentType = HomeLayoutType.GROUP_CARD_CAROUSEL,
    ) {
        val groups = remember(section.items) { section.items.map { it.toGroupItem() } }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .padding(bottom = 16.dp),
        ) {
            Box(Modifier.height(12.dp))
            RecommendGroupRow(
                groups = groups,
                onGroupClick = onGroupClick,
            )
        }
    }
}

private fun LazyListScope.bookCarouselSectionItems(
    sectionIndex: Int,
    section: HomeSection,
    onBookClick: (HomeSectionItem) -> Unit,
) {
    sectionHeaderItem(sectionIndex, section)
    item(
        key = "${section.sectionType}-$sectionIndex-content",
        contentType = HomeLayoutType.BOOK_THUMBNAIL_CAROUSEL,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .padding(bottom = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            RecommendBookRow(
                books = section.items,
                onBookClick = onBookClick,
            )
        }
    }
}

private fun LazyListScope.bookGridSectionItems(
    sectionIndex: Int,
    section: HomeSection,
    onBookClick: (HomeSectionItem) -> Unit,
    columns: Int = 3,
) {
    sectionHeaderItem(sectionIndex, section)
    item(
        key = "${section.sectionType}-$sectionIndex-grid-spacer",
        contentType = "section-content-spacer",
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white),
        ) {
            Spacer(Modifier.height(16.dp))
        }
    }

    val rows = section.items.chunked(columns)
    rows.forEachIndexed { rowIndex, rowBooks ->
        item(
            key = "${section.sectionType}-$sectionIndex-row-$rowIndex",
            contentType = "book-grid-row",
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookiiBookiiTheme.colors.white)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (rowIndex == rows.lastIndex) 16.dp else 8.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowBooks.forEach { book ->
                    BookThumbnail(
                        book = book,
                        onClick = { onBookClick(book) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - rowBooks.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// 홈 그룹 아이템 → 추천 카드(GroupItem) 매핑.
private fun HomeSectionItem.toGroupItem(): GroupItem = GroupItem(
    groupId = groupId ?: 0L,
    groupName = groupName.orEmpty(),
    title = bookTitle,
    author = author,
    genre = genre.orEmpty(),
    bookImage = bookImage,
    hostNickname = hostNickname,
    hostProfileImageUrl = hostProfileImageUrl,
    groupStatus = "",
    currentCount = 0,
    maxCapacity = 0,
    waitingCount = 0,
    isHot = false,
    tradeType = tradeType, // "DIRECT"/"DELIVERY" → 카드에서 한글 뱃지 변환
    readingPeriod = readingPeriod ?: 0,
    pictureBadge = null,
)

@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val typography = BookiiBookiiTheme.typography
    val colors = BookiiBookiiTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = typography.regular20,
            color = colors.grey900,
        )
        Text(
            text = subtitle,
            style = typography.regular16,
            color = colors.grey600,
        )
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

private val mockGroupItem = HomeSectionItem(
    groupId = 1L, groupName = "책과 함께", bookTitle = "소년이 온다", author = "한강",
    bookImage = null, hostNickname = "부키", hostProfileImageUrl = null, readingPeriod = 7,
)
private val mockGroupItems = listOf(
    mockGroupItem,
    mockGroupItem.copy(groupId = 2L, bookTitle = "채식주의자"),
    mockGroupItem.copy(groupId = 3L, bookTitle = "아몬드"),
)
private val mockBookItem = HomeSectionItem(
    isbn13 = "1", title = "소년이 온다", author = "한강", bookImage = null,
)
private val mockBookItems = listOf(
    mockBookItem,
    mockBookItem.copy(isbn13 = "2", title = "채식주의자", author = "한강"),
    mockBookItem.copy(isbn13 = "3", title = "아몬드", author = "손원평"),
    mockBookItem.copy(isbn13 = "4", title = "달러구트 꿈 백화점", author = "이미예"),
    mockBookItem.copy(isbn13 = "5", title = "불편한 편의점", author = "김호연"),
)
private val mockSections = listOf(
    HomeSection(
        sectionType = "NEW_GROUPS",
        title = "신규 그룹을 확인해보세요.",
        subtitle = "오늘 만들어진 따끈따끈한 그룹들만 모았어요.",
        layoutType = HomeLayoutType.GROUP_CARD_CAROUSEL,
        items = mockGroupItems,
    ),
    HomeSection(
        sectionType = "POPULAR_BOOKS_TOP5",
        title = "인기 도서 TOP 5",
        subtitle = "부키부키에서 핫한 도서를 지금 바로 확인해보세요",
        layoutType = HomeLayoutType.BOOK_THUMBNAIL_CAROUSEL,
        items = mockBookItems,
    ),
    HomeSection(
        sectionType = "BESTSELLER_BOOKS",
        title = "나 빼고 다 읽은 책 여기 있어요.",
        subtitle = "이번 기회에 베스트셀러를 읽어볼까요?",
        layoutType = HomeLayoutType.BOOK_THUMBNAIL_GRID,
        items = mockBookItems + mockBookItem.copy(isbn13 = "6", title = "파과", author = "구병모"),
    ),
)

@Preview(showBackground = true, name = "HomeRecommendContent - 전체 섹션", heightDp = 1600)
@Composable
private fun HomeRecommendContentPreview() {
    BookiiPreview {
        LazyColumn {
            homeRecommendContent(
                sections = mockSections,
                onGroupClick = {},
                onBookClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "HomeRecommendContent - 빈 상태")
@Composable
private fun HomeRecommendContentEmptyPreview() {
    BookiiPreview {
        LazyColumn {
            homeRecommendContent(
                sections = emptyList(),
                onGroupClick = {},
            )
        }
    }
}
