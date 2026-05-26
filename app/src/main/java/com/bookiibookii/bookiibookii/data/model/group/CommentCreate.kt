package com.bookiibookii.bookiibookii.data.model.group

data class CommentCreateRequest(
    val content: String,
    val parentId: Long?,
    val secret: Boolean = false,    // 대댓글일 때만 true
)

data class CommentCreateResponse(
    val commentId: Long,
    val groupId: Long,
    val parentId: Long?,
    val content: String,
    val createdAt: String,
    val writer: CommentWriter
)
