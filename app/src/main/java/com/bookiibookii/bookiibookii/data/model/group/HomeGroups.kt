package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

// GET /api/groups/home 응답 result
data class HomeGroupsResponse(
    @SerializedName("sections") val sections: List<HomeSection> = emptyList(),
)
data class HomeSection(
    @SerializedName("sectionType") val sectionType: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("layoutType") val layoutType: String,
    @SerializedName("items") val items: List<HomeSectionItem> = emptyList(),
)

data class HomeSectionItem(
    // 공통
    @SerializedName("author") val author: String? = null,
    @SerializedName("bookImage") val bookImage: String? = null,
    // 책 항목
    @SerializedName("isbn13") val isbn13: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("searchKeyword") val searchKeyword: String? = null,
    @SerializedName("rank") val rank: Int? = null,
    // 그룹 항목
    @SerializedName("groupId") val groupId: Long? = null,
    @SerializedName("groupName") val groupName: String? = null,
    @SerializedName("hostNickname") val hostNickname: String? = null,
    @SerializedName("hostProfileImageUrl") val hostProfileImageUrl: String? = null,
    @SerializedName("bookTitle") val bookTitle: String? = null,
    @SerializedName("readingPeriod") val readingPeriod: Int? = null,
)

object HomeLayoutType {
    const val GROUP_CARD_CAROUSEL = "GROUP_CARD_CAROUSEL"
    const val BOOK_THUMBNAIL_CAROUSEL = "BOOK_THUMBNAIL_CAROUSEL"
    const val BOOK_THUMBNAIL_GRID = "BOOK_THUMBNAIL_GRID"
}
