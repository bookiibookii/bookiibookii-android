package com.bookiibookii.bookiibookii.group.ui.detail

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.ui.component.showCustomToast
import com.bookiibookii.bookiibookii.data.model.group.CommentItem
import com.bookiibookii.bookiibookii.data.model.group.CommentWriter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.group.model.GroupCommentUiState
import com.bookiibookii.bookiibookii.group.vm.GroupCommentViewModel
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.ui.component.DeletePopover
import com.bookiibookii.bookiibookii.ui.component.bottomSheetTopShadow
import com.bookiibookii.bookiibookii.ui.component.LocalOnProfileClick
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 댓글 바텀시트 — VM 주입/상태 수집/이벤트 구독 (stateful)
// - expanded: GroupDetailRoute가 관리하는 sheet 확장 상태
// - currentUserId: TokenManager에서 받아 Content로 내림 (백엔드 isMe 추가 시 제거 예정)
// - eventFlow.ShowError → CommonToast (isSuccess = false → info 아이콘)
@Composable
fun GroupCommentBottomSheetRoute(
    expanded: Boolean = false,
    onExpand: () -> Unit = {},
    viewModel: GroupCommentViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentUserId = remember { TokenManager.getUserId(context) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is GroupCommentViewModel.Event.ShowError -> {
                    context.showCustomToast(event.message, isSuccess = false)
                }
            }
        }
    }

    // 답글 모드 진입
    // replyRequestId 기반이라 같은 댓글 재클릭에도 재실행
    LaunchedEffect(uiState.replyRequestId) {
        if (uiState.replyRequestId > 0 && uiState.replyTargetId != null) {
            onExpand()
        }
    }

    GroupCommentBottomSheetContent(
        uiState = uiState,
        currentUserId = currentUserId,
        expanded = expanded,
        onInputClick = onExpand,
        onStartReply = viewModel::startReply,
        onDelete = viewModel::delete,
        onDraftChange = viewModel::onDraftChange,
        onToggleSecret = viewModel::toggleSecret,
        onSubmit = viewModel::submit,
        modifier = modifier,
    )
}

// 그룹 상세 화면의 댓글 바텀시트
// - expanded == false → drag handle + 헤더 + 입력 필드만 보임
// - expanded == true → drag handle + 헤더 + 댓글 리스트 + 입력 필드
// - 입력 필드는 키보드 위에 항상 떠 있음 (댓글 위에 오버레이)
@Composable
fun GroupCommentBottomSheetContent(
    uiState: GroupCommentUiState,
    currentUserId: Long?,
    expanded: Boolean,
    onInputClick: () -> Unit,
    onStartReply: (parentId: Long, nickname: String) -> Unit,
    onDelete: (commentId: Long) -> Unit,
    onDraftChange: (String) -> Unit,
    onToggleSecret: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val imeBottom = with(density) { WindowInsets.ime.getBottom(density).toDp() }

    // 답글 모드 진입 시 대상 부모 댓글로 스크롤 (입력창/키보드 위에 보이도록)
    // key에 imeBottom 포함 → 키보드가 떠서 하단 여백이 생긴 뒤에도 다시 스크롤 (마지막 댓글도 올라감)
    // replyRequestId 기반이라 같은 댓글 재클릭에도 다시 스크롤
    LaunchedEffect(uiState.replyRequestId, imeBottom) {
        val targetId = uiState.replyTargetId ?: return@LaunchedEffect
        val targetIndex = uiState.comments.indexOfFirst { it.id == targetId }
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    val sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .bottomSheetTopShadow(cornerRadius = 20.dp)
            .clip(sheetShape)
            .background(BookiiBookiiTheme.colors.white),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                DragHandle()
                CommentSheetHeader(count = uiState.totalCount)
            }
            if (expanded) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 56.dp + imeBottom),
                ) {
                    items(uiState.comments, key = { it.id }) { comment ->
                        CommentRow(
                            comment = comment,
                            currentUserId = currentUserId,
                            onStartReply = onStartReply,
                            onDelete = onDelete,
                        )
                    }
                }
            }
        }
        // 입력 필드 오버레이 — 입력창만 키보드 위로
        CommentInputField(
            draft = uiState.draft,
            draftSecret = uiState.draftSecret,
            mentionNickname = uiState.mentionNickname,
            replyRequestId = uiState.replyRequestId,
            submitting = uiState.submitting,
            expanded = expanded,
            onInputClick = onInputClick,
            onDraftChange = onDraftChange,
            onToggleSecret = onToggleSecret,
            onSubmit = onSubmit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .background(BookiiBookiiTheme.colors.white)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        )
    }
}

// 드래그 핸들
@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 4.dp)
                .clip(BookiiBookiiTheme.shape.round50)
                .background(BookiiBookiiTheme.colors.grey200),
        )
    }
}

// "댓글 N" 헤더 — count == 0이면 grey500, 양수면 main 컬러
@Composable
private fun CommentSheetHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "댓글",
            style = BookiiBookiiTheme.typography.semibold20,
            color = BookiiBookiiTheme.colors.grey900,
        )
        Text(
            text = "$count",
            style = BookiiBookiiTheme.typography.regular16,
            color = if (count > 0) {
                BookiiBookiiTheme.colors.uiMain
            } else {
                BookiiBookiiTheme.colors.grey500
            },
        )
    }
}

// 최상위 댓글 + 답글들
// 답글 short-tap도 부모 댓글에 답글 (트리 2단계 제약)
@Composable
private fun CommentRow(
    comment: CommentItem,
    currentUserId: Long?,
    onStartReply: (parentId: Long, nickname: String) -> Unit,
    onDelete: (commentId: Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 최상위 댓글
        CommentItemRow(
            comment = comment,
            isMine = isMine(currentUserId, comment.writer.userId),
            profileSize = 36.dp,
            indentStart = 0.dp,
            onTap = { onStartReply(comment.id, comment.writer.name) },
            onDelete = { onDelete(comment.id) },
        )
        // 답글
        comment.children?.forEach { reply ->
            CommentItemRow(
                comment = reply,
                isMine = isMine(currentUserId, reply.writer.userId),
                profileSize = 28.dp,
                indentStart = 52.dp,
                onTap = { onStartReply(comment.id, reply.writer.name) },
                onDelete = { onDelete(reply.id) },
            )
        }
    }
}

private fun isMine(currentUserId: Long?, writerId: Long): Boolean =
    currentUserId != null && writerId == currentUserId

// 댓글 한 줄
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CommentItemRow(
    comment: CommentItem,
    isMine: Boolean,
    profileSize: Dp,
    indentStart: Dp,
    onTap: () -> Unit,
    onDelete: () -> Unit,
) {
    val onProfileClick = LocalOnProfileClick.current
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
                    onClick = onTap,
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
                onClick = { onProfileClick(comment.writer.name) },
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
    else -> BookiiBookiiTheme.colors.grey900   // GUEST / NONE / 그 외
}

// 닉네임 + 시간 + (secret이면) lock 아이콘 + 본문 — 댓글/답글 공용
@Composable
private fun CommentMetaAndBody(
    nickname: String,
    nicknameColor: androidx.compose.ui.graphics.Color,
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

// 입력 필드 — BasicTextField + 멘션 prefix + 자동 포커스(expand 진입 시)
// 멘션 prefix 한글자라도 깨지면 초기화
@Composable
private fun CommentInputField(
    draft: String,
    draftSecret: Boolean,
    mentionNickname: String?,
    replyRequestId: Int,
    submitting: Boolean,
    expanded: Boolean,
    onInputClick: () -> Unit,
    onDraftChange: (String) -> Unit,
    onToggleSecret: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val transformation = rememberMentionVisualTransformation(mentionNickname)

    // 커서 위치 제어를 위해 TextFieldValue 사용
    var fieldValue by remember { mutableStateOf(TextFieldValue(draft, TextRange(draft.length))) }
    // draft가 외부에서 바뀌면(답글 진입 "@닉네임 ", submit 클리어 등) 동기화 + 커서를 끝으로
    // 답글 진입 시 커서 "@닉네임 " prefix 바로 뒤
    LaunchedEffect(draft) {
        if (fieldValue.text != draft) {
            fieldValue = fieldValue.copy(text = draft, selection = TextRange(draft.length))
        }
    }

    // Sheet expand 시에는 자동 키보드 X — 사용자가 직접 입력 필드 탭해야 활성
    // Sheet collapse 시에는 활성된 키보드 내려감
    LaunchedEffect(expanded) {
        if (!expanded) {
            keyboard?.hide()
        }
    }

    // 답글 모드 진입 시 자동 focus
    // replyRequestId 기반이라 같은 댓글을 다시 클릭해도 매번 재실행됨
    LaunchedEffect(replyRequestId) {
        if (replyRequestId > 0 && mentionNickname != null) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    // 댓글 작성 직후 키보드 내려감 — 작성 성공/실패와 무관
    val submitAndHide: () -> Unit = {
        onSubmit()
        keyboard?.hide()
    }

    // 제출 가능 여부 — 멘션 prefix만 남은 경우(또는 빈 텍스트)는 비활성
    val effectiveContent = if (mentionNickname != null) {
        draft.removePrefix("@$mentionNickname ").trim()
    } else {
        draft.trim()
    }
    val canSubmit = effectiveContent.isNotEmpty() && !submitting

    // 외부 modifier가 background/padding/imePadding을 책임 (중복 X)
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BookiiBookiiTheme.colors.white)
                .border(
                    width = 1.dp,
                    color = BookiiBookiiTheme.colors.grey300,
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LockChip(active = draftSecret, onClick = onToggleSecret)
            BasicTextField(
                value = fieldValue,
                onValueChange = { newValue ->
                    fieldValue = newValue
                    // text가 실제로 바뀐 경우에만 VM에 통지
                    if (newValue.text != draft) onDraftChange(newValue.text)
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                visualTransformation = transformation,
                textStyle = BookiiBookiiTheme.typography.regular16.copy(
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
                                style = BookiiBookiiTheme.typography.regular16,
                                color = BookiiBookiiTheme.colors.grey500,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            UploadChip(enabled = canSubmit, onClick = submitAndHide)
        }
        // Peek 상태에서는 입력 필드 영역의 모든 클릭을 가로채서 expand만 트리거
        // BasicTextField/Chip들로 클릭이 닿지 않게 함
        if (!expanded) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onInputClick),
            )
        }
    }
}

// 입력 필드 좌측 잠금 칩 — secret 토글 (active일 때 main 컬러)
@Composable
private fun LockChip(active: Boolean, onClick: () -> Unit) {
    val bg = if (active) BookiiBookiiTheme.colors.uiMainSubPale else BookiiBookiiTheme.colors.grey200
    val tint = if (active) BookiiBookiiTheme.colors.uiMainSub else BookiiBookiiTheme.colors.grey500
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(BookiiBookiiTheme.shape.round16)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = "비공개 토글",
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}

// 입력 필드 우측 업로드 칩 — 제출 가능 시 main 컬러로 활성화
@Composable
private fun UploadChip(enabled: Boolean, onClick: () -> Unit) {
    val bg = if (enabled) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey400
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(BookiiBookiiTheme.shape.round50)
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

// 멘션 prefix "@{nickname} "를 main 컬러로 표시하는 VisualTransformation
@Composable
private fun rememberMentionVisualTransformation(mentionNickname: String?): VisualTransformation {
    val mainColor = BookiiBookiiTheme.colors.uiMain
    return remember(mentionNickname, mainColor) {
        if (mentionNickname == null) {
            VisualTransformation.None
        } else {
            val prefix = "@$mentionNickname "
            VisualTransformation { text ->
                val raw = text.text
                val styled = buildAnnotatedString {
                    if (raw.startsWith(prefix)) {
                        withStyle(SpanStyle(color = mainColor)) {
                            append(prefix)
                        }
                        append(raw.substring(prefix.length))
                    } else {
                        append(raw)
                    }
                }
                TransformedText(styled, OffsetMapping.Identity)
            }
        }
    }
}

@Preview(widthDp = 412, heightDp = 170, showBackground = true, backgroundColor = 0xFFF6F6F6)
@Composable
private fun GroupCommentBottomSheetPeekPreview() {
    BookiiPreview {
        GroupCommentBottomSheetContent(
            uiState = GroupCommentUiState(comments = emptyList(), totalCount = 0),
            currentUserId = null,
            expanded = false,
            onInputClick = {},
            onStartReply = { _, _ -> },
            onDelete = {},
            onDraftChange = {},
            onToggleSecret = {},
            onSubmit = {},
        )
    }
}

@Preview(widthDp = 412, heightDp = 544, showBackground = true, backgroundColor = 0xFFF6F6F6)
@Composable
private fun GroupCommentBottomSheetExpandedPreview() {
    BookiiPreview {
        GroupCommentBottomSheetContent(
            uiState = GroupCommentUiState(comments = previewComments, totalCount = 30),
            currentUserId = 2L,
            expanded = true,
            onInputClick = {},
            onStartReply = { _, _ -> },
            onDelete = {},
            onDraftChange = {},
            onToggleSecret = {},
            onSubmit = {},
        )
    }
}

// Preview용 더미 댓글
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
            userId = 3,
            name = "minjee_87",
            profileImage = null,
            role = "GUEST",
        ),
        createdAt = "2026-05-03T01:15:00Z",
        children = listOf(
            CommentItem(
                id = 4,
                deleted = false,
                secret = true,
                content = "네, 매장에 오시면 바로 교환 도와드릴게요!",
                parentId = 3,
                writer = CommentWriter(
                    userId = 1,
                    name = "kanghunsim",
                    profileImage = null,
                    role = "GUEST",
                ),
                createdAt = "2026-05-26T08:55:00Z",
                children = null,
            ),
        ),
    ),
    CommentItem(
        id = 5,
        deleted = false,
        secret = true,
        content = "배송비는 누가 부담하나요?",
        parentId = null,
        writer = CommentWriter(
            userId = 4,
            name = "sohyun_park",
            profileImage = null,
            role = "GUEST",
        ),
        createdAt = "2026-05-03T01:40:00Z",
        children = null,
    ),
    CommentItem(
        id = 6,
        deleted = false,
        secret = true,
        content = "반품은 어떻게 진행되나요?",
        parentId = null,
        writer = CommentWriter(
            userId = 5,
            name = "minseok_lee",
            profileImage = null,
            role = "GUEST",
        ),
        createdAt = "2026-05-03T02:15:00Z",
        children = null,
    ),
)
