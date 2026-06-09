package com.bookiibookii.bookiibookii.data.model.tracker

data class TrackerDetailResDTO(
    val groupId: Long,
    val groupName: String?,
    val tradeType: String?,
    val myRole: String?,
    val displayStatus: String?,
    val displayBookTitle: String?,
    val displayStatusLabel: String?,
    val dDay: Int?,
    val myBook: BookInfo?,
    val partnerBook: BookInfo?,
    val steps: List<TrackerStepDTO>?
)

data class TrackerStepDTO(
    val status: String?,
    val title: String?,
    val description: String?,
    val completed: Boolean?
)
