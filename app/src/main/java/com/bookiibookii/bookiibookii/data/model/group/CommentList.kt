package com.bookiibookii.bookiibookii.data.model.group

data class CommentItem(
    val id: Long,
    val deleted: Boolean,
    val secret: Boolean,
    val content: String,
    val parentId: Long?,
    val writer: CommentWriter,
    val createdAt: String,
    val children: List<CommentItem>? = null
)
