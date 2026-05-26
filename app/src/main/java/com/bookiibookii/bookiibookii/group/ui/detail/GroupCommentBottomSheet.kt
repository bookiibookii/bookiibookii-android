package com.bookiibookii.bookiibookii.group.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.data.model.group.CommentItem
import com.bookiibookii.bookiibookii.data.model.group.CommentWriter
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 그룹 상세 화면의 댓글 바텀시트 (정적 UI)
// - comments 비어 있음 → peek 상태 (헤더 + 입력 필드)
// - comments 채워짐  → expanded 상태 (헤더 + 댓글 리스트 + 입력 필드)
@Composable
fun GroupCommentBottomSheetContent(
    comments: List<CommentItem>,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            DragHandle()
            CommentSheetHeader(count = totalCount)
        }

        // 댓글 리스트 (expanded 상태에서만 노출)
        comments.forEach { comment ->
            CommentRow(comment = comment)
        }

        CommentInputField()
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

// 최상위 댓글 한 줄 (+ 답글 children 함께 렌더)
@Composable
private fun CommentRow(comment: CommentItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ProfilePlaceholder(
            modifier = Modifier.size(36.dp),
            imageUrl = comment.writer.profileImage,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CommentMetaAndBody(
                nickname = comment.writer.name,
                nicknameColor = BookiiBookiiTheme.colors.grey900,
                createdAt = comment.createdAt,
                secret = comment.secret,
                content = comment.content,
            )
            // 답글 (children)
            comment.children?.forEach { reply ->
                ReplyRow(reply = reply)
            }
        }
    }
}

// 답글 한 줄 — 닉네임을 main 컬러
@Composable
private fun ReplyRow(reply: CommentItem) {
    Row(
        modifier = Modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ProfilePlaceholder(
            modifier = Modifier.size(28.dp),
            imageUrl = reply.writer.profileImage,
        )
        CommentMetaAndBody(
            nickname = reply.writer.name,
            nicknameColor = BookiiBookiiTheme.colors.uiMain,
            createdAt = reply.createdAt,
            secret = reply.secret,
            content = reply.content,
        )
    }
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

// 입력 필드
@Composable
private fun CommentInputField() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey300,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LockChip()
            Text(
                text = "텍스트 입력 전",
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey500,
            )
        }
        UploadChip()
    }
}

// 입력 필드 좌측 잠금 칩
@Composable
private fun LockChip() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(BookiiBookiiTheme.shape.round16)
            .background(BookiiBookiiTheme.colors.grey200),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = "비공개 토글",
            tint = BookiiBookiiTheme.colors.grey500,
            modifier = Modifier.size(24.dp),
        )
    }
}

// 입력 필드 우측 업로드 칩
@Composable
private fun UploadChip() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(BookiiBookiiTheme.shape.round50)
            .background(BookiiBookiiTheme.colors.grey400),
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

@Preview(widthDp = 412, showBackground = true, backgroundColor = 0xFFF6F6F6)
@Composable
private fun GroupCommentBottomSheetPeekPreview() {
    BookiiPreview {
        GroupCommentBottomSheetContent(
            comments = emptyList(),
            totalCount = 0,
        )
    }
}

@Preview(widthDp = 412, heightDp = 800, showBackground = true, backgroundColor = 0xFFF6F6F6)
@Composable
private fun GroupCommentBottomSheetExpandedPreview() {
    BookiiPreview {
        GroupCommentBottomSheetContent(
            comments = previewComments,
            totalCount = 30,
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
