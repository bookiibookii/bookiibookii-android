package com.bookiibookii.bookiibookii.tracker.ui.comment

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.model.group.CommentItem
import com.bookiibookii.bookiibookii.data.model.group.CommentWriter
import com.bookiibookii.bookiibookii.error.ErrorActivity
import com.bookiibookii.bookiibookii.error.model.ErrorType
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.tracker.vm.TrackerCommentViewModel
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.DeletePopover
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch

// 바텀 pull-up 당김 저항 계수
private const val PULL_RESISTANCE = 0.5f

// 트래커 댓글 화면 — VM 주입/상태 수집/이벤트 구독 (stateful)
// - groupId: 그룹 댓글과 동일 API groupId 기반. 트래커 상세에서 전달
// - title: 헤더 타이틀을 네비 인자로 전달받아 그대로 내림
// - currentUserId: TokenManager에서 받아 본인 댓글 판별에 사용
@Composable
fun TrackerCommentRoute(
    groupId: Long,
    title: String,
    onBackClick: () -> Unit,
    viewModel: TrackerCommentViewModel = viewModel(
        factory = TrackerCommentViewModel.factory(groupId),
    ),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentUserId = remember { TokenManager.getUserId(context) }
    var showCoachMark by remember { mutableStateOf(!TokenManager.isTrackerCommentCoachMarkDone(context)) }

    // 바텀 네비 표시는 TrackerNavHost에서 현재 라우트 기준으로 일괄 제어
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is TrackerCommentViewModel.Event.ShowError -> {
                    context.showCustomToast(event.message, isSuccess = false)
                }
                is TrackerCommentViewModel.Event.NotFound ->
                    context.startActivity(ErrorActivity.newIntent(context, ErrorType.GROUP_CLOSED))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TrackerCommentScreen(
            title = title,
            comments = uiState.comments,
            currentUserId = currentUserId,
            draft = uiState.draft,
            submitting = uiState.submitting,
            isRefreshing = uiState.isRefreshing,
            onBackClick = onBackClick,
            onDraftChange = viewModel::onDraftChange,
            onSubmit = viewModel::submit,
            onDelete = viewModel::delete,
            onRefresh = viewModel::refresh,
        )
        if (showCoachMark) {
            TrackerCommentCoachMarkOverlay(
                onDismiss = {
                    TokenManager.saveTrackerCommentCoachMarkDone(context)
                    showCoachMark = false
                },
            )
        }
    }
}

// 트래커 1:1 댓글 화면 (풀스크린, stateless)
// - 리스트 렌더링은 그룹 댓글과 동일: 트리(children) + 비밀(secret) 자물쇠 + 역할별 닉네임 색
// - 입력은 축소: 비밀토글/답글(멘션) 없이 최상위·공개 댓글만 작성
// - 댓글 탭 → 동작 없음 (1:1이라 답글 모드 없음). 본인 댓글 long-press 삭제만 동일
@Composable
fun TrackerCommentScreen(
    title: String,
    comments: List<CommentItem>,
    currentUserId: Long?,
    draft: String,
    submitting: Boolean,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDelete: (commentId: Long) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 바텀 pull-up 새로고침
    // 임계치 넘겨 손 떼면 onRefresh
    val density = LocalDensity.current
    val refreshThresholdPx = with(density) { 72.dp.toPx() }
    // 새로고침 중 유지할 하단 빈 공간 높이 / 당기는 동안 빈 공간 최대치
    val refreshingGapPx = with(density) { 64.dp.toPx() }
    val maxPullPx = with(density) { 96.dp.toPx() }
    var pullPx by remember { mutableFloatStateOf(0f) }
    val refreshingState = rememberUpdatedState(isRefreshing)
    val onRefreshState = rememberUpdatedState(onRefresh)

    // 새로고침 시작 시 pullPx 정리(gap은 refreshingGapPx가 담당), 완료(true→false) 시 맨 아래로 스크롤
    var wasRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullPx = 0f
        } else if (wasRefreshing && comments.isNotEmpty()) {
            listState.animateScrollToItem(comments.lastIndex)
        }
        wasRefreshing = isRefreshing
    }

    val pullConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (refreshingState.value) return Offset.Zero
                if (available.y > 0f && pullPx > 0f) {
                    val consumed = minOf(available.y, pullPx)
                    pullPx -= consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (refreshingState.value) return Offset.Zero
                if (source == NestedScrollSource.UserInput && available.y < 0f) {
                    pullPx += -available.y * PULL_RESISTANCE
                    return available
                }
                return Offset.Zero
            }

            // 손을 떼는 순간: 임계치 넘었으면 새로고침 트리거.
            // 트리거 시 pullPx는 그대로 둬서 gap 유지 → isRefreshing이 켜지며 LaunchedEffect가 0으로 정리(깜빡임 방지)
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!refreshingState.value && pullPx >= refreshThresholdPx) {
                    onRefreshState.value()
                } else {
                    pullPx = 0f
                }
                return Velocity.Zero
            }
        }
    }

    Scaffold(
        topBar = { TrackerCommentHeader(title = title, onBackClick = onBackClick) },
        bottomBar = {
            TrackerCommentInput(
                draft = draft,
                submitting = submitting,
                onDraftChange = onDraftChange,
                onSubmit = onSubmit,
                modifier = Modifier
                    .imePadding()
                    .background(BookiiBookiiTheme.colors.uiBg)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            )
        },
        containerColor = BookiiBookiiTheme.colors.uiBg,
    ) { innerPadding ->
        // 당김량만큼 리스트를 위로 밀어 하단에 빈 공간(gap)을 만든다. 그 공간에 reload 아이콘 배치
        val gapPx = if (isRefreshing) refreshingGapPx else pullPx.coerceAtMost(maxPullPx)
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(pullConnection),
        ) {
            if (comments.isEmpty()) {
                EmptyCommentCard(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(BookiiBookiiTheme.shape.round20)
                        .background(BookiiBookiiTheme.colors.white)
                        .clipToBounds(),
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { translationY = -gapPx },
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        itemsIndexed(comments, key = { _, item -> item.id }) { index, comment ->
                            CommentRow(
                                comment = comment,
                                currentUserId = currentUserId,
                                onDelete = onDelete,
                            )
                            // 댓글마다 하단 divider — 마지막 댓글은 제외
                            if (index < comments.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    thickness = 1.dp,
                                    color = BookiiBookiiTheme.colors.grey100,
                                )
                            }
                        }
                    }
                    // 하단 빈 공간(gap) 안에 reload 아이콘 — 리스트가 위로 밀린 만큼의 영역에 중앙 배치
                    BottomReloadIndicator(
                        gapPx = gapPx,
                        pullFraction = (pullPx / refreshThresholdPx).coerceIn(0f, 1f),
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ScrollChip(rotationZ = 90f, contentDescription = "맨 위로") {
                        scope.launch { listState.animateScrollToItem(0) }
                    }
                    ScrollChip(rotationZ = -90f, contentDescription = "맨 아래로") {
                        scope.launch { listState.animateScrollToItem(comments.lastIndex.coerceAtLeast(0)) }
                    }
                }
            }
        }
    }
}

// 빈 상태 카드
@Composable
private fun EmptyCommentCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(vertical = 24.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "댓글이 없어요.",
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey600,
        )
    }
}

// 하단 빈 공간 안의 새로고침 인디케이터 — 리스트가 밀린 높이(gapPx)만큼의 영역을 차지하고 그 안에 ic_reload 중앙 배치
// 당기는 동안 진행률만큼 회전, 새로고침 중엔 무한 회전
@Composable
private fun BottomReloadIndicator(
    gapPx: Float,
    pullFraction: Float,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "reload")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing)),
        label = "reloadAngle",
    )
    if (gapPx <= 0f) return

    val rotation = if (isRefreshing) spin else pullFraction * 360f
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(with(density) { gapPx.toDp() }),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_reload),
            contentDescription = "새로고침",
            tint = BookiiBookiiTheme.colors.grey400,
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer { rotationZ = rotation },
        )
    }
}

// 헤더 — 뒤로가기 + 가운데 타이틀(트래커명)
@Composable
private fun TrackerCommentHeader(
    title: String,
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(BookiiBookiiTheme.colors.white)
                .padding(horizontal = 16.dp),
        ) {
            BookiiBackButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            Text(
                text = title,
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BookiiBookiiTheme.colors.grey200),
        )
    }
}

// 최상위 댓글 + 답글들 (그룹과 동일하게 트리로 렌더)
// 트래커에서는 탭으로 답글 진입하지 않으므로 onTap = no-op
@Composable
private fun CommentRow(
    comment: CommentItem,
    currentUserId: Long?,
    onDelete: (commentId: Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CommentItemRow(
            comment = comment,
            isMine = isMine(currentUserId, comment.writer.userId),
            profileSize = 36.dp,
            indentStart = 0.dp,
            onDelete = { onDelete(comment.id) },
        )
        comment.children?.forEach { reply ->
            CommentItemRow(
                comment = reply,
                isMine = isMine(currentUserId, reply.writer.userId),
                profileSize = 28.dp,
                indentStart = 52.dp,
                onDelete = { onDelete(reply.id) },
            )
        }
    }
}

private fun isMine(currentUserId: Long?, writerId: Long): Boolean =
    currentUserId != null && writerId == currentUserId

// 댓글 한 줄 — 본인 댓글 long-press 시 삭제 팝오버
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CommentItemRow(
    comment: CommentItem,
    isMine: Boolean,
    profileSize: Dp,
    indentStart: Dp,
    onDelete: () -> Unit,
) {
    var showPopover by remember { mutableStateOf(false) }
    var rowSize by remember { mutableStateOf(IntSize.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(modifier = Modifier.padding(start = indentStart)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { rowSize = it }
                .background(
                    if (isPressed) BookiiBookiiTheme.colors.grey100 else Color.Transparent,
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {},
                    // 본인 댓글에만 long-press 허용. 타인은 null이라 무반응
                    onLongClick = if (isMine) ({ showPopover = true }) else null,
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            ProfilePlaceholder(
                modifier = Modifier.size(profileSize),
                imageUrl = comment.writer.profileImage,
            )
            CommentMetaAndBody(
                nickname = comment.writer.name,
                nicknameColor = nicknameColorFor(comment.writer.role),
                createdAt = comment.createdAt,
                secret = comment.secret,
                content = comment.content,
            )
        }
        if (showPopover) {
            val density = LocalDensity.current
            // 행 기준 왼쪽 208dp / 오른쪽 28dp. 대댓글은 보정해 댓글과 동일 크기로 맞춤
            val popoverWidth = with(density) { rowSize.width.toDp() } + indentStart - 208.dp - 28.dp
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = with(density) { (216.dp - indentStart).roundToPx() },
                    y = rowSize.height,
                ),
                onDismissRequest = { showPopover = false },
                properties = PopupProperties(focusable = true),
            ) {
                DeletePopover(
                    onDeleteClick = {
                        onDelete()
                        showPopover = false
                    },
                    modifier = Modifier.width(popoverWidth),
                )
            }
        }
    }
}

// 작성자 role에 따른 닉네임 색상 — HOST 강조
@Composable
private fun nicknameColorFor(role: String): Color = when (role) {
    "HOST" -> BookiiBookiiTheme.colors.uiMain
    else -> BookiiBookiiTheme.colors.grey900
}

// 닉네임 + 시간 + (secret이면) lock 아이콘 + 본문
@Composable
private fun CommentMetaAndBody(
    nickname: String,
    nicknameColor: Color,
    createdAt: String,
    secret: Boolean,
    content: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = nickname,
                style = BookiiBookiiTheme.typography.regular14,
                color = nicknameColor,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = DateUtils.calculateTimeAgo(createdAt),
                    style = BookiiBookiiTheme.typography.regular12,
                    color = BookiiBookiiTheme.colors.grey500,
                )
                if (secret) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lock),
                        contentDescription = "비공개",
                        tint = BookiiBookiiTheme.colors.grey500,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Text(
            text = content,
            style = BookiiBookiiTheme.typography.regular15,
            color = BookiiBookiiTheme.colors.grey700,
        )
    }
}

// 우측 플로팅 스크롤 칩 — ic_chevron(좌향)을 회전시켜 위/아래 표시
@Composable
private fun ScrollChip(
    rotationZ: Float,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(BookiiBookiiTheme.colors.white)
            .border(1.dp, BookiiBookiiTheme.colors.grey200, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron),
            contentDescription = contentDescription,
            tint = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer { this.rotationZ = rotationZ },
        )
    }
}

// 입력 필드 — 비밀토글/멘션 없음
@Composable
private fun TrackerCommentInput(
    draft: String,
    submitting: Boolean,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val canSubmit = draft.trim().isNotEmpty() && !submitting

    val submitAndHide: () -> Unit = {
        onSubmit()
        keyboard?.hide()
    }

    val inputShape = RoundedCornerShape(
        topStart = 20.dp,
        bottomStart = 20.dp,
        topEnd = 30.dp,
        bottomEnd = 30.dp,
    )

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(inputShape)
                .background(BookiiBookiiTheme.colors.white)
                .border(
                    width = 1.dp,
                    color = BookiiBookiiTheme.colors.grey300,
                    shape = inputShape,
                )
                .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                textStyle = BookiiBookiiTheme.typography.regular15.copy(
                    color = BookiiBookiiTheme.colors.grey900,
                ),
                cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSubmit) submitAndHide() }),
                decorationBox = { innerTextField ->
                    Box {
                        if (draft.isEmpty()) {
                            Text(
                                text = "텍스트 입력 전",
                                style = BookiiBookiiTheme.typography.regular15,
                                color = BookiiBookiiTheme.colors.grey500,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            SendChip(enabled = canSubmit, onClick = submitAndHide)
        }
    }
}

// 전송 칩 — 활성 시 main 컬러
@Composable
private fun SendChip(enabled: Boolean, onClick: () -> Unit) {
    val bg = if (enabled) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey300
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_up),
            contentDescription = "등록",
            tint = BookiiBookiiTheme.colors.white,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun TrackerCommentScreenPreview() {
    BookiiPreview {
        TrackerCommentScreen(
            title = "김영하 도장깨기 하실 분",
            comments = previewComments,
            currentUserId = 2L,
            draft = "",
            submitting = false,
            isRefreshing = false,
            onBackClick = {},
            onDraftChange = {},
            onSubmit = {},
            onDelete = {},
            onRefresh = {},
        )
    }
}

@Preview(widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun TrackerCommentScreenEmptyPreview() {
    BookiiPreview {
        TrackerCommentScreen(
            title = "김영하 도장깨기 하실 분",
            comments = emptyList(),
            currentUserId = 2L,
            draft = "",
            submitting = false,
            isRefreshing = false,
            onBackClick = {},
            onDraftChange = {},
            onSubmit = {},
            onDelete = {},
            onRefresh = {},
        )
    }
}

// Preview용 더미
private val previewComments: List<CommentItem> = listOf(
    CommentItem(
        id = 1,
        deleted = false,
        secret = false,
        content = "혹시 택배로도 교환 가능하실까용?",
        parentId = null,
        writer = CommentWriter(
            userId = 1,
            name = "kanghunsim",
            profileImage = null,
            role = "GUEST",
        ),
        createdAt = "2026-05-03T00:52:00Z",
        children = listOf(
            CommentItem(
                id = 2,
                deleted = false,
                secret = true,
                content = "어려울 거 같아요 죄송합니다 ㅠㅠ",
                parentId = 1,
                writer = CommentWriter(
                    userId = 2,
                    name = "noshel",
                    profileImage = null,
                    role = "HOST",
                ),
                createdAt = "2026-05-26T08:48:00Z",
                children = null,
            ),
        ),
    ),
    CommentItem(
        id = 3,
        deleted = false,
        secret = false,
        content = "그럼 직접 방문 수령은 가능한가요?",
        parentId = null,
        writer = CommentWriter(
            userId = 2,
            name = "noshel",
            profileImage = null,
            role = "HOST",
        ),
        createdAt = "2026-05-29T01:15:00Z",
        children = null,
    ),
)
