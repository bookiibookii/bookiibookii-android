package com.bookiibookii.bookiibookii.data.model.library

// ==========================================
// [4] 댓글 목록 조회
// ==========================================
data class CommentListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: CommentListResult?
)

data class CommentListResult(
    val totalCount: Int,
    val comments: List<CommentItem>
)

data class CommentItem(
    val id: Int,
    val content: String,
    val writer: CommentWriter,
    val createdAt: String
)

data class CommentWriter(
    val userId: Int,
    val name: String,
    val profileImageUrl: String?
)

data class PostCommentRequest(
    val content: String
)

// 댓글 작성 응답 (Result가 content만 오는 경우)
data class PostCommentResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: PostCommentResult?
)

data class PostCommentResult(
    val content: String
)
