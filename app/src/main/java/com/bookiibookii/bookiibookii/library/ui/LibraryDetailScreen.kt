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
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
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
    val completedAt: String? = null,  // 독서 종료일 (책 정보 보강용)
    val genre: String = "",           // 장르 (책 정보 보강용)
    val totalPages: Int? = null,      // 전체 페이지 수
    val bookTitle: String = "",
    val quotation: String = "",
    val imageUrl: String? = null,
    val s3Key: String? = null,        // 기존 이미지 키 (수정 시 사진 미교체면 그대로 재전송)
    val myReactions: List<String> = emptyList(),      // 내가 누른 리액션 API key 목록
    val reactionCounts: Map<String, Int> = emptyMap(), // API key → 전체 인원 수
    val creatorProfileImageUrl: String? = null,
    val isMine: Boolean = false,
)

// 서재 상세 카드 아이템에서 사용 (리액션 API key → 아이콘 drawable)
// 5종 이모지 개편(공감해요/좋아요/웃겨요/슬퍼요/화나요)에 맞춰 아이콘 갱신.
// CHEERUP은 더 이상 "힘나요"가 아닌 "화나요" 용도로 재사용됨. AWESOME(멋져요)은 개편으로 사라진 값이라
// 매핑에서 제외 — 개편 이전 과거 데이터에 남아있어도 목록에 아이콘이 표시되지 않는다.
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
    val genre: String = "",         // 서재 목록 API(getLibraryBooks)에서 전달
    val coverUrl: String? = null,
    val isDone: Boolean = false,    // true: 완료(별점), false: 읽는 중(프로그래스바)
    val progressRate: Int = 0,      // 0-100 (isDone=false 일 때 사용)
    val rating: Double = 0.0,       // 0-5 (isDone=true 일 때 사용)
    val startDate: String = "",
    val endDate: String? = null,    // 완료됐을 때만 값 존재
    val completedAt: String? = null, // 독서 종료일 — 표시용 종료일은 이 값을 사용
)

// 라우트 진입점 — 구 LibraryDetailFragment의 onCreateView/onResume/onViewCreated 로직을 그대로 이식
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

    // 구 Fragment의 onResume()처럼 화면이 다시 보일 때마다(최초 진입 포함) 재조회
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        if (groupId != -1) viewModel.fetchGroupCards(groupId, bookTitle)
        if (memberBookId != -1 && bookTitle.isNotBlank()) {
            viewModel.checkRepresentativeStatus(memberBookId, bookTitle)
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.event.collect { msg ->
            context.showCustomToast(msg, !msg.contains("실패") && !msg.contains("오류"))
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

    val displayedCards = if (myCardsOnly) cards.filter { it.isMine } else cards
    val sortedCards = if (sortByLatest) {
        // date는 일(day) 단위라 같은 날 카드는 동률 → cardId(생성 순서)로 2차 정렬해 최신이 먼저 오게 함
        displayedCards.sortedWith(
            compareByDescending<ReadingCard> { it.date }.thenByDescending { it.cardId },
        )
    } else {
        displayedCards.sortedBy { it.page.toIntOrNull() ?: 0 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 딤 (FAB 메뉴 열렸을 때)
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
            // 상단 고정 헤더 (스크롤 영역 밖)
            DetailHeader(title = book?.title ?: "", onBackClick = onBackClick, onMenuClick = { showBookSheet = true })
            // 헤더 아래만 스크롤 (weight(1f)로 남은 공간 채움)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 104.dp),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                if (book != null) {
                    // genre·시작/종료 날짜 모두 서재 목록 API(getLibraryBooks)에서 args로 전달됨
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
                CardGrid(
                    cards = sortedCards,
                    onCardClick = { index -> onCardClick(index, sortedCards, sortByLatest) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // FAB 메뉴 + FAB 버튼 영역
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 확장 메뉴 항목들
            AnimatedVisibility(
                visible = showFabMenu,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
            ) {
                // width(IntrinsicSize.Max): 두 항목 중 넓은 쪽 기준으로 통일
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 이미지 카드 → 위쪽
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
                    // 인용구 카드 → 아래쪽
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

            // 메인 FAB
            // FAB 색: Grey900 검정 (피그마 스펙)
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
                isCompleted = book.isDone,   // 완료(groupStatus COMPLETED)일 때만 리뷰/대표/삭제 노출
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
                    onDeleteBook()
                },
            )
        }
    }
}

// 아이콘과 글씨가 검정 원형(pill) 배경에 함께 둘러싸인 FAB 메뉴 아이템
// 피그마 스펙: 151x48 고정, 좌 여백 12 / 아이콘-글자 간격 8 / 글자 우 여백 16, 세로 중앙 정렬
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
    // 종료일은 completedAt 사용 — 값 있으면 "시작 ~ 종료", 없으면 "시작 ~"
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
        // 책 표지
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
                // 장르가 오면 "(장르)" 추가, 현재 API 미제공으로 저자명만 표시
                Text(
                    text = if (book.genre.isBlank()) book.author else "${book.author} (${book.genre})",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (book.isDone) {
                    // 완료: 별점 표시
                    Row {
                        for (i in 1..5) {
                            val filled = i.toDouble() <= book.rating; Icon(painter = painterResource(if (filled) R.drawable.ic_star_fill else R.drawable.ic_star), contentDescription = null, tint = if (filled) BookiiBookiiTheme.colors.uiMainSub else BookiiBookiiTheme.colors.grey200, modifier = Modifier.size(20.dp))
                        }
                    }
                } else {
                    // 읽는 중: 프로그래스바 표시
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
            // 각 라벨은 해당 정렬로 '설정'한다 (토글 아님). 이미 선택된 라벨을 눌러도 그대로 유지됨.
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
                // 리액션 아이콘
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                    card.reactionCounts
                        .filter { it.value > 0 }
                        .keys
                        .forEach { apiKey ->
                            reactionIconByApiKey[apiKey]?.let { iconRes ->
                                // 풀컬러 이모지 아이콘이라 독서카드 상세와 동일하게 원래 색 그대로 표시(tint 미적용)
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

// 읽는 중 (진행률 바)
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun LibraryDetailScreenReadingPreview() {
    BookiiPreview {
        LibraryDetailScreen(book = previewReadingBook, cards = previewDetailCards)
    }
}

// 완료 (별점)
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
