package com.bookiibookii.bookiibookii.data.model.group

import com.bookiibookii.bookiibookii.group.main.GroupData
import com.google.gson.annotations.SerializedName

data class GroupItem(
    @SerializedName("groupId") val groupId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("bookImage") val bookImage: String?,
    @SerializedName("hostProfileImageUrl") val hostProfileImageUrl: String?,
    @SerializedName("hostNickname") val hostNickname: String?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("groupStatus") val groupStatus: String,
    @SerializedName("currentCount") val currentCount: Int,
    @SerializedName("maxCapacity") val maxCapacity: Int,
    @SerializedName("readingPeriod") val readingPeriod: Int,
    @SerializedName("customTag") val customTag: String?,
    val groupType: String,
    val tradeType: String?,
    val startDate: String?,
    val isHot: Boolean,
    @SerializedName("pictureBadge") val pictureBadge: String?
) {
    fun toUiModel(): GroupData {
        val safeTags = tags ?: emptyList()

        val uiStatus = when (groupStatus) {
            "RECRUITING" -> "모집 중"
            "MATCHED" -> "진행 중"
            "COMPLETED" -> "종료"
            else -> "마감"
        }

        val badgeText = pictureBadge ?: "모집"

        return GroupData(
            groupId = groupId.toInt(),
            coverImgUrl = bookImage ?: "",
            bookTitle = title,
            bookAuthor = author ?: "저자 미상",
            genre = genre ?: "장르",
            status = uiStatus,
            readingPeriod = readingPeriod.toString(),
            memberCount = "$currentCount",
            isHot = isHot,
            profileImgUrl = hostProfileImageUrl,
            nickname = hostNickname ?: "알 수 없음",
            date = startDate?.replace("-", ".") ?: "날짜 미정",
            tags = safeTags,
            customTag = customTag ?: "",
            groupType = groupType,
            tradeType = tradeType,
            maxMemberCount = maxCapacity,
            badgeContent = badgeText
        )
    }
}
