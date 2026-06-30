package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.common.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import androidx.compose.ui.window.Dialog
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.ui.theme.MaruBuri

enum class ReadingCardType { PHOTO, QUOTE }

data class ReadingCard(
    val cardId: Long = 0L,
    val memberBookId: Int = 0,
    val username: String,
    val content: String,
    val page: String,
    val type: ReadingCardType,
    val isBookmarked: Boolean = false,
    val date: String = "",
    val completedAt: String? = null,
    val genre: String = "",
    val totalPages: Int? = null,
    val bookTitle: String = "",
    val quotation: String = "",
    val imageUrl: String? = null,
    val s3Key: String? = null,
    val myReactions: List<String> = emptyList(),
    val reactionCounts: Map<String, Int> = emptyMap(),
    val creatorProfileImageUrl: String? = null,
    val isMine: Boolean = false,
)

internal val reactionIconByApiKey: Map<String, Int> = mapOf(
    "LIKE"    to R.drawable.ic_good,
    "SAD"     to R.drawable.ic_sad,
    "CHEERUP" to R.drawable.ic_angry,
    "FEELYOU" to R.drawable.ic_empathy,
    "FUN"     to R.drawable.ic_fun,
)

data class LibraryDetailBook(
    val groupId: Int = 0,
    val memberBookId: Int = 0,
    val groupName: String,
    val title: String,
    val author: String,
    val genre: String = "",
    val coverUrl: String? = null,
    val isDone: Boolean = false,
    val progressRate: Int = 0,
    val rating: Double = 0.0,
    val startDate: String = "",
    val endDate: String? = null,
    val completedAt: String? = null,
)

@Composable
fun LibraryDetailRoute(
    groupId: Int,
    memberBookId: Int,
    groupName: String,
    bookTitle: String,
    author: String,
    genre: String,
    coverUrl: String,
    startDate: String,
    endDate: String,
    completedAt: String,
    rating: Double,
    isDone: Boolean,
    progressRate: Int,
    totalPages: Int,
    onBackClick: () -> Unit,
    onAddTextCard: () -> Unit,
    onAddPhotoCard: () -> Unit,
    onCardClick: (initialIndex: Int, sortedCards: List<ReadingCard>, sortByLatest: Boolean) -> Unit,
    onReviewClick: () -> Unit,
    viewModel: com.bookiibookii.bookiibookii.library.vm.LibraryDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.uiState.collectAsState()

    val book = if (bookTitle.isNotBlank()) {
        LibraryDetailBook(
            groupId = groupId,
            memberBookId = memberBookId,
            groupName = groupName,
            title = bookTitle,
            author = author,
            genre = genre,
            coverUrl = coverUrl.ifBlank { null },
            isDone = isDone,
            progressRate = progressRate,
            rating = rating,
            startDate = startDate,
            endDate = endDate.ifBlank { null },
            completedAt = completedAt.ifBlank { null },
        )
    } else {
        null
    }

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        if (groupId != -1) viewModel.fetchGroupCards(groupId, bookTitle)
        if (memberBookId != -1 && bookTitle.isNotBlank()) {
            viewModel.checkRepresentativeStatus(memberBookId, bookTitle)
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            context.showCustomToast(event.message, event.isSuccess)
        }
    }

    LibraryDetailScreen(
        book = book,
        cards = state.cards,
        isRepresentative = state.isRepresentative,
        onBackClick = onBackClick,
        onAddTextCard = onAddTextCard,
        onAddPhotoCard = onAddPhotoCard,
        onCardClick = onCardClick,
        onReviewClick = onReviewClick,
        onRepresentativeAdd = { viewModel.addRepresentative(memberBookId) },
        onRepresentativeRemove = { viewModel.removeRepresentative(state.representativeUserBookId) },
        onAladinClick = { title ->
            val url = "https://www.aladin.co.kr/search/wsearchresult.aspx?SearchWord=${android.net.Uri.encode(title)}"
            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
        },
        onDeleteBook = { viewModel.deleteMemberBook(memberBookId, onBackClick) },
    )
}

@Composable
fun LibraryDetailScreen(
    book: LibraryDetailBook? = null,
    cards: List<ReadingCard> = emptyList(),
    isRepresentative: Boolean = false,
    onBackClick: () -> Unit = {},
    onAddTextCard: () -> Unit = {},
    onAddPhotoCard: () -> Unit = {},
    onCardClick: (initialIndex: Int, sortedCards: List<ReadingCard>, sortByLatest: Boolean) -> Unit = { _, _, _ -> },
    onReviewClick: () -> Unit = {},
    onRepresentativeAdd: () -> Unit = {},
    onRepresentativeRemove: () -> Unit = {},
    onAladinClick: (title: String) -> Unit = {},
    onDeleteBook: () -> Unit = {},
) {
    var myCardsOnly by remember { mutableStateOf(false) }
    var sortByLatest by remember { mutableStateOf(true) }
    var showBookSheet by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val displayedCards = if (myCardsOnly) cards.filter { it.isMine } else cards
    val sortedCards = if (sortByLatest) {
        displayedCards.sortedWith(
            compareByDescending<ReadingCard> { it.date }.thenByDescending { it.cardId },
        )
    } else {
        displayedCards.sortedBy { it.page.toIntOrNull() ?: 0 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showFabMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000))
                    .clickable { showFabMenu = false },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BookiiBookiiTheme.colors.uiBg),
        ) {
            DetailHeader(title = book?.title ?: "", onBackClick = onBackClick, onMenuClick = { showBookSheet = true })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 104.dp),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                if (book != null) {
                    BookInfoCard(book = book, modifier = Modifier.padding(horizontal = 16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                FilterRow(
                    myCardsOnly = myCardsOnly,
                    sortByLatest = sortByLatest,
                    onMyCardsToggle = { myCardsOnly = !myCardsOnly },
                    onSortChange = { latest -> sortByLatest = latest },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (sortedCards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(BookiiBookiiTheme.colors.white)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "아직 독서카드가 없어요.\n기록하고 싶은 페이지를 남겨주세요.",
                            style = BookiiBookiiTheme.typography.regular16,
                            color = BookiiBookiiTheme.colors.grey600,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                } else {
                    CardGrid(
                        cards = sortedCards,
                        onCardClick = { index -> onCardClick(index, sortedCards, sortByLatest) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = showFabMenu,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FabMenuItem(
                        label = "이미지 카드 추가",
                        onClick = {
                            showFabMenu = false
                            onAddPhotoCard()
                        },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_image),
                                    contentDescription = null,
                                    tint = BookiiBookiiTheme.colors.white,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        },
                    )
                    FabMenuItem(
                        label = "인용구 카드 추가",
                        onClick = {
                            showFabMenu = false
                            onAddTextCard()
                        },
                        icon = {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "T",
                                    color = BookiiBookiiTheme.colors.white,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                )
                            }
                        },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey900)
                    .clickable { showFabMenu = !showFabMenu },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (showFabMenu) "닫기" else "독서카드 추가",
                    tint = BookiiBookiiTheme.colors.white,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        if (showBookSheet && book != null) {
            LibraryBookBottomSheet(
                title = book.title,
                author = book.author,
                genre = book.genre,
                isRepresentative = isRepresentative,
                isCompleted = book.isDone,
                onDismiss = { showBookSheet = false },
                onReviewClick = {
                    showBookSheet = false
                    onReviewClick()
                },
                onToggleRepresentativeClick = {
                    showBookSheet = false
                    if (isRepresentative) onRepresentativeRemove() else onRepresentativeAdd()
                },
                onAladinClick = {
                    showBookSheet = false
                    onAladinClick(book.title)
                },
                onDeleteClick = {
                    showBookSheet = false
                    showDeleteDialog = true
                },
            )
        }

        if (showDeleteDialog) {
            Dialog(onDismissRequest = { showDeleteDialog = false }) {
                LibraryDeleteDialog(
                    onDismiss = { showDeleteDialog = false },
                    onConfirm = {
                        showDeleteDialog = false
                        onDeleteBook()
                    },
                )
            }
        }
    }
}

@Composable
private fun LibraryDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "서재 삭제",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey100)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "닫기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = "서재에서 삭제하시겠습니까? 삭제하면 즉시 사라지며, 이후 되돌릴 수 없습니다.",
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier.padding(top = 24.dp),
        )
        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BottomSheetTwoBtnShort(
                text = "취소",
                style = BottomSheetBtnStyle.White,
                textStyle = BookiiBookiiTheme.typography.regular16,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            BottomSheetTwoBtnShort(
                text = "삭제",
                style = BottomSheetBtnStyle.Red,
                textStyle = BookiiBookiiTheme.typography.regular16,
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FabMenuItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .width(151.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(BookiiBookiiTheme.colors.grey900)
            .clickable { onClick() }
            .padding(start = 12.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        icon()
        Text(
            text = label,
            style = BookiiBookiiTheme.typography.medium14,
            color = BookiiBookiiTheme.colors.white,
        )
    }
}

@Composable
private fun DetailHeader(title: String, onBackClick: () -> Unit, onMenuClick: () -> Unit) {
    Column(
        modifier = Modifier
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
            BookiiBackButton(onClick = onBackClick)
            Text(
                text = title.stripBookSubtitle().let { if (it.length > 12) it.take(12) + "…" else it },
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                maxLines = 1,
            )
            IconButton(onClick = onMenuClick, modifier = Modifier.size(40.dp)) {
                Icon(painter = painterResource(R.drawable.ic_meetball), contentDescription = "메뉴", tint = BookiiBookiiTheme.colors.grey900, modifier = Modifier.size(32.dp))
            }
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 1.dp)
    }
}

@Composable
private fun BookInfoCard(book: LibraryDetailBook, modifier: Modifier = Modifier) {
    val startFmt = DateUtils.formatDate(book.startDate)
    val dateText = if (!book.completedAt.isNullOrBlank()) {
        "$startFmt ~ ${DateUtils.formatDate(book.completedAt)}"
    } else {
        "$startFmt ~"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .width(102.dp)
                .height(146.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BookiiBookiiTheme.colors.grey200),
        ) {
            if (!book.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }

        Column(modifier = Modifier.weight(1f).height(146.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "${book.groupName}", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey600, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = book.title.stripBookSubtitle(), style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = if (book.genre.isBlank()) book.author else "${book.author} (${book.genre})",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (book.isDone) {
                    Row {
                        for (i in 1..5) {
                            val filled = i.toDouble() <= book.rating; Icon(painter = painterResource(if (filled) R.drawable.ic_star_fill else R.drawable.ic_star), contentDescription = null, tint = if (filled) BookiiBookiiTheme.colors.uiMainSub else BookiiBookiiTheme.colors.grey300, modifier = Modifier.size(20.dp))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50.dp)).background(BookiiBookiiTheme.colors.grey200),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(book.progressRate / 100f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(BookiiBookiiTheme.colors.grey800),
                            )
                        }
                        Text(text = "${book.progressRate}%", style = BookiiBookiiTheme.typography.semibold12, color = BookiiBookiiTheme.colors.grey800)
                    }
                }
            }
            Text(text = dateText, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
        }
    }
}

@Composable
private fun FilterRow(
    myCardsOnly: Boolean,
    sortByLatest: Boolean,
    onMyCardsToggle: () -> Unit,
    onSortChange: (latest: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onMyCardsToggle() }) {
            Box(
                modifier = Modifier.size(20.dp).clip(RoundedCornerShape(5.dp)).background(if (myCardsOnly) BookiiBookiiTheme.colors.uiMainSub else BookiiBookiiTheme.colors.grey200),
                contentAlignment = Alignment.Center,
            ) {
                if (myCardsOnly) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BookiiBookiiTheme.colors.white, modifier = Modifier.size(16.dp))
                }
            }
            Text(text = "내 독서카드만 보기", style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.grey500)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "최신순", style = if (sortByLatest) BookiiBookiiTheme.typography.semibold14 else BookiiBookiiTheme.typography.regular14, color = if (sortByLatest) BookiiBookiiTheme.colors.grey800 else BookiiBookiiTheme.colors.grey500, modifier = Modifier.clickable { onSortChange(true) })
            Text(text = "|", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
            Text(text = "페이지순", style = if (!sortByLatest) BookiiBookiiTheme.typography.semibold14 else BookiiBookiiTheme.typography.regular14, color = if (!sortByLatest) BookiiBookiiTheme.colors.grey800 else BookiiBookiiTheme.colors.grey500, modifier = Modifier.clickable { onSortChange(false) })
        }
    }
}

@Composable
private fun CardGrid(cards: List<ReadingCard>, onCardClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        cards.chunked(2).forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEachIndexed { colIndex, card ->
                    ReadingCardItem(card = card, onClick = { onCardClick(rowIndex * 2 + colIndex) }, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ReadingCardItem(card: ReadingCard, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.height(275.dp).clip(RoundedCornerShape(20.dp)).background(BookiiBookiiTheme.colors.white).clickable { onClick() }) {
        Column(
            modifier = Modifier.fillMaxWidth().height(147.dp).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfilePlaceholder(imageUrl = card.creatorProfileImageUrl, modifier = Modifier.size(24.dp))
                    Text(text = card.username, style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.grey800)
                }
                if (card.isBookmarked) {
                    Box(
                        modifier = Modifier.clip(CircleShape).background(BookiiBookiiTheme.colors.uiMainPale).border(0.5.dp, BookiiBookiiTheme.colors.uiMain, CircleShape).padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_bookmark_fill), contentDescription = null, tint = BookiiBookiiTheme.colors.uiMain, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Text(text = card.content, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey800, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth().weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                    card.reactionCounts
                        .filter { it.value > 0 }
                        .keys
                        .forEach { apiKey ->
                            reactionIconByApiKey[apiKey]?.let { iconRes ->
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }
                }
                Text(
                    text = if (card.page.isNotBlank() && card.page != "0") "p.${card.page}" else "",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey400,
                )
            }
        }

        when (card.type) {
            ReadingCardType.PHOTO -> Box(
                modifier = Modifier.fillMaxWidth().weight(1f).background(BookiiBookiiTheme.colors.grey200),
            ) {
                if (!card.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model              = card.imageUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.matchParentSize(),
                    )
                }
            }
            ReadingCardType.QUOTE -> Box(
                modifier = Modifier.fillMaxWidth().weight(1f).background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f),
                    )
                ).padding(8.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = BookiiBookiiTheme.colors.white, modifier = Modifier.size(16.dp))
                    val displayText = card.quotation.ifBlank { card.content }
                    if (displayText.isNotBlank()) {
                        Text(text = "“$displayText”", style = TextStyle(fontFamily = MaruBuri, fontWeight = FontWeight.Bold, fontSize = 12.sp), color = BookiiBookiiTheme.colors.white, maxLines = 5, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

private val previewDetailCards = listOf(
    ReadingCard(
        username = "북이",
        content = "다시 읽어도 오래 남는 문장이었다.",
        page = "123",
        type = ReadingCardType.QUOTE,
        bookTitle = "데미안",
        quotation = "새는 알에서 나오려고 투쟁한다.",
        date = "2026.06.05",
        isMine = true,
    ),
    ReadingCard(
        username = "부키",
        content = "이 장면이 특히 인상 깊었어요.",
        page = "45",
        type = ReadingCardType.PHOTO,
        bookTitle = "데미안",
        date = "2026.06.04",
    ),
)

private val previewReadingBook = LibraryDetailBook(
    groupName = "숭실대 경제 독서모임",
    title = "일이삼사오육칠팔구십일이...",
    author = "헤르만 헤세",
    genre = "소설",
    isDone = false,
    progressRate = 64,
    startDate = "2026-05-20",
)

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LibraryDetailScreenReadingPreview() {
    BookiiPreview {
        LibraryDetailScreen(book = previewReadingBook, cards = previewDetailCards)
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LibraryDetailScreenDonePreview() {
    BookiiPreview {
        LibraryDetailScreen(
            book = previewReadingBook.copy(isDone = true, rating = 4.5, completedAt = "2026-06-01"),
            cards = previewDetailCards,
        )
    }
}
