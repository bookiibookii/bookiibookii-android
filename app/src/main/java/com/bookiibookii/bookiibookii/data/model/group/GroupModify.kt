package com.bookiibookii.bookiibookii.data.model.group

data class GroupModifyRequest(
    val startDate: String,
    val readingPeriod: Int,
    val groupComment: String,
    val customTag: String?,
    val tags: List<GroupTagRequest>
)
