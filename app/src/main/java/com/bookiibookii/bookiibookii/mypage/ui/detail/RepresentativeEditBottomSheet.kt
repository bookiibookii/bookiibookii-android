package com.bookiibookii.bookiibookii.mypage.ui.detail

import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.RepresentativeBook
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepresentativeEditBottomSheet(
    bookList: List<RepresentativeBook>,
    onDismiss: () -> Unit,
    onRemove: (RepresentativeBook) -> Unit,
    onMove: (Int, Int) -> Unit,
    onReorder: (userBookId: Long, newOrder: Int) -> Unit = { _, _ -> },
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BookiiBookiiTheme.colors.white,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp).height(4.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(BookiiBookiiTheme.colors.grey200),
                )
            }
        },
    ) {
        RepresentativeEditContent(
            bookList = bookList,
            onRemove = onRemove,
            onMove = onMove,
            onReorder = onReorder,
        )
    }
}

@Composable
private fun RepresentativeEditContent(
    bookList: List<RepresentativeBook>,
    onRemove: (RepresentativeBook) -> Unit,
    onMove: (Int, Int) -> Unit,
    onReorder: (userBookId: Long, newOrder: Int) -> Unit,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    var dragStartIndex by remember { mutableStateOf<Int?>(null) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var draggingItemOffset by remember { mutableStateOf(0f) }

    val itemHeightPx = with(LocalDensity.current) { 80.dp.toPx() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = screenHeight * 0.7f)
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "순서 변경",
            style = BookiiBookiiTheme.typography.semibold20,
            color = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "나를 잘 보여주는 책을 최대 7권까지 고를 수 있어요.\n인생 책은 최소 1권 포함해주세요.",
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey500,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            itemsIndexed(bookList) { index, book ->
                val isDragged = draggedIndex == index
                val translationY = if (isDragged) draggingItemOffset else 0f

                RepresentativeEditListItem(
                    book = book,
                    modifier = Modifier
                        .zIndex(if (isDragged) 1f else 0f)
                        .graphicsLayer {
                            this.translationY = translationY
                            scaleX = if (isDragged) 1.02f else 1f
                            scaleY = if (isDragged) 1.02f else 1f
                            shadowElevation = if (isDragged) 8f else 0f
                            // 그림자가 카드의 둥근 모서리(20dp)를 따르도록 shape 지정 (미지정 시 사각 그림자)
                            shape = RoundedCornerShape(20.dp)
                            clip = false
                        },
                    onRemoveClick = { onRemove(book) },
                    onDragStart = {
                        dragStartIndex = index
                        draggedIndex = index
                        draggingItemOffset = 0f
                    },
                    onDragEnd = {
                        // 드래그 종료 시점의 최종 위치로 즉시 API 호출
                        val finalIdx = draggedIndex
                        val startIdx = dragStartIndex
                        if (finalIdx != null && startIdx != null && startIdx != finalIdx) {
                            val movedBook = bookList.getOrNull(finalIdx)
                            if (movedBook != null) {
                                onReorder(movedBook.userBookId, finalIdx + 1)
                            }
                        }
                        dragStartIndex = null
                        draggedIndex = null
                        draggingItemOffset = 0f
                    },
                    onDrag = { dragAmount ->
                        // draggedIndex(옮겨진 뒤의 실제 위치) 기준으로 계산해 여러 칸 연속 이동 지원.
                        // while로 처리해 한 이벤트에서 여러 칸 넘어가는 빠른 드래그도 모두 반영(끊김 방지)
                        if (draggedIndex != null) {
                            draggingItemOffset += dragAmount
                            var current = draggedIndex!!

                            while (draggingItemOffset > itemHeightPx / 2 && current < bookList.lastIndex) {
                                onMove(current, current + 1)
                                current += 1
                                draggedIndex = current
                                draggingItemOffset -= itemHeightPx
                            }
                            while (draggingItemOffset < -itemHeightPx / 2 && current > 0) {
                                onMove(current, current - 1)
                                current -= 1
                                draggedIndex = current
                                draggingItemOffset += itemHeightPx
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun RepresentativeEditListItem(
    book: RepresentativeBook,
    modifier: Modifier = Modifier,
    onRemoveClick: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Float) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = BookiiBookiiTheme.colors.white,
        border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey200),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hamburger),
                contentDescription = "순서 변경",
                tint = BookiiBookiiTheme.colors.grey700,
                modifier = Modifier
                    .size(20.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount)
                            },
                        )
                    },
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // 상단: 책 제목 semibold14 grey900
                Text(
                    text = book.title.stripBookSubtitle(),
                    style = BookiiBookiiTheme.typography.semibold14,
                    color = BookiiBookiiTheme.colors.grey900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // 하단: 저자명 regular14 grey900 (API 미제공 시 빈 문자열)
                Text(
                    text = "",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // 완독책(rating 있음)만 별점 표시. 인생책(rating null)은 미표시
            book.rating?.let { rating ->
                Spacer(modifier = Modifier.width(12.dp))
                RepresentativeStarRatingRow(rating = rating)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(BookiiBookiiTheme.colors.grey100, CircleShape)
                    .clip(CircleShape)
                    .clickable { onRemoveClick() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "목록에서 제거",
                    tint = BookiiBookiiTheme.colors.black,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// 대표책 편집 행 별점 (16dp). 완독책에만 표시
@Composable
private fun RepresentativeStarRatingRow(rating: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy((-2).dp)) {
        for (i in 1..5) {
            RepresentativeStarIcon(starValue = (rating - (i - 1)).coerceIn(0.0, 1.0))
        }
    }
}

@Composable
private fun RepresentativeStarIcon(starValue: Double) {
    val colors = BookiiBookiiTheme.colors
    when {
        // 가득 채운 별
        starValue >= 0.75 -> Icon(painter = painterResource(R.drawable.ic_star_fill), contentDescription = null, tint = colors.uiMainSub, modifier = Modifier.size(16.dp))
        // 반 별: 외곽선 MainSub + 안쪽 MainSubPale
        starValue >= 0.25 -> Box(modifier = Modifier.size(16.dp)) {
            Icon(painter = painterResource(R.drawable.ic_star_fill), contentDescription = null, tint = colors.uiMainSubPale, modifier = Modifier.size(16.dp))
            Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = colors.uiMainSub, modifier = Modifier.size(16.dp))
        }
        // 빈 별
        else -> Icon(painter = painterResource(R.drawable.ic_star), contentDescription = null, tint = colors.grey200, modifier = Modifier.size(16.dp))
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun RepresentativeEditContentPreview() {
    BookiiPreview {
        RepresentativeEditContent(
            bookList = listOf(
                RepresentativeBook(userBookId = 1L, title = "데미안", displayOrder = 0, isFavorite = true, rating = null),
                RepresentativeBook(userBookId = 2L, title = "1984", displayOrder = 1, isFavorite = false, rating = 4.0),
                RepresentativeBook(userBookId = 3L, title = "사피엔스", displayOrder = 2, isFavorite = false, rating = 3.5),
            ),
            onRemove = {},
            onMove = { _, _ -> },
            onReorder = { _, _ -> },
        )
    }
}
