package com.bookiibookii.bookiibookii.onboarding.steps.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.onboarding.steps.OnbViewModel
import com.bookiibookii.bookiibookii.onboarding.steps.model.OnbState
import com.bookiibookii.bookiibookii.onboarding.steps.ui.component.OnbSubHeadCard
import com.bookiibookii.bookiibookii.onboarding.steps.ui.component.LifeBookSearchDialog
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun OnbStep2Content(vm: OnbViewModel, state: OnbState) {
    val bookSearchState by vm.bookSearchState.observeAsState()

    var showSearchDialog by remember { mutableStateOf(false) }
    var editingSlotIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        OnbSubHeadCard(
            label = stringResource(R.string.onb_label_required),
            title = stringResource(R.string.onb_step2_title),
            description = stringResource(R.string.onb_step2_desc)
        )

        val totalBooks = state.lifeBooks.count { it != null }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) { index ->
                LifeBookSlot(
                    book = state.lifeBooks.getOrNull(index),
                    totalBooks = totalBooks,
                    onClick = {
                        editingSlotIndex = index
                        vm.clearBookSearch()
                        showSearchDialog = true
                    },
                    onRemove = { vm.removeLifeBook(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showSearchDialog) {
        LifeBookSearchDialog(
            bookSearchState = bookSearchState,
            onQueryChange = { vm.onBookSearchQueryChange(it) },
            onSearch = { vm.searchBooks() },
            onBookSelected = { book ->
                if (editingSlotIndex >= 0) vm.setLifeBook(editingSlotIndex, book)
                showSearchDialog = false
            },
            onDismiss = { showSearchDialog = false }
        )
    }
}

@Composable
private fun LifeBookSlot(
    book: BookItem?,
    totalBooks: Int,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    if (book != null) {
        val isSingleBook = totalBooks == 1
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.white)
                    .border(1.dp, colors.grey100, RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = book.image,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp, top = 8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colors.grey100)
                        .clickable(onClick = if (isSingleBook) onClick else onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            if (isSingleBook) R.drawable.ic_edit else R.drawable.ic_x
                        ),
                        contentDescription = if (isSingleBook) "수정" else "삭제",
                        tint = colors.grey700,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = book.title.stripBookSubtitle(),
                style = typography.semibold14,
                color = colors.grey900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${book.author} (${book.categoryLabel})",
                style = typography.regular14,
                color = colors.grey700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.grey200)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.grey400),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = "책 추가",
                    tint = colors.white,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ─── 섹션 프리뷰 ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "LifeBookSlot - 비어있음")
@Composable
private fun LifeBookSlotEmptyPreview() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp).width(120.dp)) {
            LifeBookSlot(book = null, totalBooks = 0, onClick = {}, onRemove = {})
        }
    }
}

@Preview(showBackground = true, name = "LifeBookSlot - 채워짐 (1권, 수정 아이콘)")
@Composable
private fun LifeBookSlotFilledSinglePreview() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp).width(120.dp)) {
            LifeBookSlot(
                book = BookItem("프로젝트 헤일메리", "앤디 위어", "", "알에이치코리아", "9788925563602", "소설", "소설", ""),
                totalBooks = 1,
                onClick = {},
                onRemove = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "LifeBookSlot - 채워짐 (2권+, X 아이콘)")
@Composable
private fun LifeBookSlotFilledMultiPreview() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp).width(120.dp)) {
            LifeBookSlot(
                book = BookItem("채식주의자", "한강", "", "창비", "9788936433598", "소설", "소설", ""),
                totalBooks = 2,
                onClick = {},
                onRemove = {}
            )
        }
    }
}

// ─── 전체 화면 프리뷰 ─────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Step 2 - 전체 (비어있음)")
@Composable
private fun OnbStep2ContentEmptyPreview() {
    BookiiBookiiTheme {
        OnbStep2Content(vm = OnbViewModel(), state = OnbState())
    }
}

@Preview(showBackground = true, name = "Step 2 - 전체 (1권 선택)")
@Composable
private fun OnbStep2ContentOneBookPreview() {
    BookiiBookiiTheme {
        OnbStep2Content(
            vm = OnbViewModel(),
            state = OnbState(
                lifeBooks = listOf(
                    BookItem("프로젝트 헤일메리", "앤디 위어", "", "알에이치코리아", "9788925563602", "소설", "소설", ""),
                    null, null
                )
            )
        )
    }
}

@Preview(showBackground = true, name = "Step 2 - 전체 (2권 선택)")
@Composable
private fun OnbStep2ContentTwoBooksPreview() {
    BookiiBookiiTheme {
        OnbStep2Content(
            vm = OnbViewModel(),
            state = OnbState(
                lifeBooks = listOf(
                    BookItem("프로젝트 헤일메리", "앤디 위어", "", "알에이치코리아", "9788925563602", "소설", "소설", ""),
                    BookItem("채식주의자", "한강", "", "창비", "9788936433598", "소설", "소설", ""),
                    null
                )
            )
        )
    }
}
