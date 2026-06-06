package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

// GET /api/groups/apply/me 응답 result
data class AppliedGroupsResponse(
    @SerializedName("applicationList") val applicationList: List<AppliedGroupItem> = emptyList(),
    @SerializedName("totalCount") val totalCount: Int = 0,
)

data class AppliedGroupItem(
    @SerializedName("groupId") val groupId: Long,
    @SerializedName("groupName") val groupName: String,
    @SerializedName("hostNickname") val hostNickname: String?,
    @SerializedName("hostProfileImageUrl") val hostProfileImageUrl: String?,
    @SerializedName("bookImage") val bookImage: String?,
    @SerializedName("bookTitle") val bookTitle: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("readingPeriod") val readingPeriod: Int,
    @SerializedName("applicationStatus") val applicationStatus: String?,
    @SerializedName("tradeType") val tradeType: String?,
    @SerializedName("pictureBadge") val pictureBadge: String?,
) {
    fun toGroupItem() = GroupItem(
        groupId = groupId,
        groupName = groupName,
        title = bookTitle,
        author = author,
        genre = null,
        bookImage = bookImage,
        hostNickname = hostNickname,
        hostProfileImageUrl = hostProfileImageUrl,
        groupStatus = "RECRUITING",
        currentCount = 0,
        maxCapacity = 0,
        waitingCount = 0,
        isHot = false,
        tradeType = tradeType,
        readingPeriod = readingPeriod,
        pictureBadge = pictureBadge,
    )
}
