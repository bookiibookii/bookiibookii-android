package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

data class GroupDetailResponse(
    val groupId: Long,
    val title: String,
    val bookTitle: String,
    val bookImage: String?,
    val author: String,
    val category: String,
    val groupStatus: String,
    val buttonStatus: String,
    val isHost: Boolean,
    val readingPeriod: Int,
    val matchedCount: Int,
    val maxCapacity: Int,
    val waitingCount: Int,
    val isHot: Boolean,
    val createdAt: String,
    val startDate: String,
    val hostNickname: String,
    val hostProfileImageUrl: String?,
    val preferRegion: String?,
    val meetPlace: String?,
    val groupTags: List<String>?,
    val customTag: String?,
    val groupComment: String?,
    val participantSlots: List<ParticipantSlot>?
)

data class ParticipantSlot(
    val nickname: String?,
    @SerializedName("profileImageUrl")
    val profileImage: String?,
    val role: String,
    val isMe: Boolean
)
