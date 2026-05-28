package com.bookiibookii.bookiibookii.mypage.ui.detail

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepresentativeEditBottomSheet(
    bookList: List<MockBookItem>,
    onDismiss: () -> Unit,
    onRemove: (MockBookItem) -> Unit,
    onMove: (Int, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    // 💡 자연스러운 드래그 앤 드롭을 위한 상태 관리
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var draggingItemOffset by remember { mutableStateOf(0f) }

    // 아이템 하나의 대략적인 높이 (스왑 기준점으로 사용)
    val itemHeightPx = with(LocalDensity.current) { 80.dp.toPx() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BookiiBookiiTheme.colors.white,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        // ❌ 주의: 여기에 fillMaxHeight를 주면 내부 스크롤이 끊어집니다! (제거함)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // 💡 대신 내부 컨테이너의 최대 높이를 화면의 70%로 제한하여 바닥에 붙게 만듭니다.
                .heightIn(max = screenHeight * 0.7f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "나를 대표하는 책",
                style = BookiiBookiiTheme.typography.semibold20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            // 💡 LazyColumn에 weight를 주어 바텀시트 스크롤과 리스트 스크롤이 충돌하지 않게 해결!
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                itemsIndexed(bookList) { index, book ->
                    // 현재 드래그 중인 아이템인지 확인
                    val isDragged = draggedIndex == index
                    val zIndex = if (isDragged) 1f else 0f
                    val translationY = if (isDragged) draggingItemOffset else 0f

                    RepresentativeEditListItem(
                        book = book,
                        modifier = Modifier
                            // 💡 드래그 중인 아이템이 다른 아이템들 위로 뜨고, 손가락을 따라오게 만듭니다.
                            .zIndex(zIndex)
                            .graphicsLayer {
                                this.translationY = translationY
                                // 드래그 중일 때 아이템을 살짝 크게(1.02배) 만들고 그림자를 주면 훨씬 자연스럽습니다.
                                scaleX = if (isDragged) 1.02f else 1f
                                scaleY = if (isDragged) 1.02f else 1f
                                shadowElevation = if (isDragged) 8f else 0f
                            },
                        onRemoveClick = { onRemove(book) },
                        onDragStart = {
                            draggedIndex = index
                            draggingItemOffset = 0f
                        },
                        onDragEnd = {
                            draggedIndex = null
                            draggingItemOffset = 0f
                        },
                        onDrag = { dragAmount ->
                            draggingItemOffset += dragAmount

                            // 손가락이 위/아래 아이템 영역 절반을 넘어갈 때 데이터 순서 스왑!
                            if (draggingItemOffset > itemHeightPx / 2 && index < bookList.lastIndex) {
                                onMove(index, index + 1)
                                draggedIndex = index + 1
                                draggingItemOffset -= itemHeightPx
                            } else if (draggingItemOffset < -itemHeightPx / 2 && index > 0) {
                                onMove(index, index - 1)
                                draggedIndex = index - 1
                                draggingItemOffset += itemHeightPx
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RepresentativeEditListItem(
    book: MockBookItem,
    modifier: Modifier = Modifier,
    onRemoveClick: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Float) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = BookiiBookiiTheme.colors.white,
        border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 좌측 햄버거 버튼 (순서 변경용 드래그 핸들)
            Icon(
                painter = painterResource(R.drawable.ic_hamburger),
                contentDescription = "순서 변경",
                tint = BookiiBookiiTheme.colors.grey700,
                modifier = Modifier
                    .size(20.dp)
                    // 💡 드래그 이벤트를 감지하여 위로 전달합니다.
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume() // 바텀시트 스크롤 이벤트를 가로채서 드래그 충돌 방지!
                                onDrag(dragAmount)
                            }
                        )
                    }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. 중앙 텍스트 (제목 + 저자/장르)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    style = BookiiBookiiTheme.typography.semibold16,
                    color = BookiiBookiiTheme.colors.grey900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${book.author} ${book.genre}",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. 우측 별점 표시
            Row(horizontalArrangement = Arrangement.spacedBy((-2).dp)) {
                for (i in 1..5) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star),
                        contentDescription = null,
                        tint = if (i <= book.rating) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 4. 삭제(X) 버튼
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(BookiiBookiiTheme.colors.grey100, CircleShape)
                    .clip(CircleShape)
                    .clickable { onRemoveClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "목록에서 제거",
                    tint = BookiiBookiiTheme.colors.grey700,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RepresentativeEditBottomSheetPreview() {
    RepresentativeEditBottomSheet(
        bookList = listOf(
            MockBookItem("프로젝트 헤일메리", "앤디 위어", "(소설)", 4),
            MockBookItem("고쳐 쓰는 마음", "김연수", "(소설)", 0),
            MockBookItem("어린 개가 왔다", "김연수", "(소설)", 3)
        ),
        onDismiss = {},
        onRemove = {},
        onMove = { _, _ -> }
    )
}
