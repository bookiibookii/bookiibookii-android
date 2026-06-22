package com.bookiibookii.bookiibookii.data.model.library

data class MemberCardListResponseDTO(
    val groupId: Int,
    val cards: List<MemberCardResponseDTO>
)

data class MemberCardResponseDTO(
    val cardId: Int,
    val memberBookId: Int,
    val cardType: String,
    val page: Int,
    val totalPages: Int?,
    val memo: String?,
    val quotation: String?,
    val cardImage: MemberCardImageResponseDTO?,
    val createdAt: String,
    val completedAt: String?,
    val genre: String?,
    val bookTitle: String?,
    val isMine: Boolean,
    val isBookmarked: Boolean,
    val creatorName: String?,
    val creatorProfileImageUrl: String?,
    val reactionCounts: List<MemberCardReactionCountDTO>,
    val myReactions: List<String>,
)

data class MemberCardImageResponseDTO(
    val cardImageId: Int,
    val s3Key: String,
    val presignedGetUrl: String
)

data class MemberCardReactionCountDTO(
    val reaction: String,
    val count: Int
)

data class MemberCardCreateRequestDTO(
    val cardType: String,
    val quotation: String,
    val s3Key: String,
    val page: Int,
    val memo: String,
    val quotationValidForText: Boolean = false,
    val s3KeyValidForImage: Boolean = false,
)

data class MemberCardUpdateRequestDTO(
    val page: Int,
    val memo: String,
    val quotation: String,
    val s3Key: String? = null
)

data class MemberCardCreateResponseDTO(
    val cardId: Int,
    val cardType: String,
    val page: Int,
    val memo: String,
    val quotation: String,
    val cardImage: MemberCardImageResponseDTO?,
    val createdAt: String,
    val creatorName: String,
    val creatorProfileImageUrl: String?
)

data class MemberCardBookmarkResponseDTO(
    val bookmarked: Boolean
)

data class MemberCardReactionToggleRequestDTO(
    val reaction: String
)

data class MemberCardReactionToggleResponseDTO(
    val reaction: String,
    val active: Boolean
)

data class PresignedUrlResponseDTO(
    val s3Key: String,
    val presignedPutUrl: String
)

data class ShareTokenResponseDTO(
    val shareToken: String,
    val shareUrl: String
)

data class PublicReadingCardResponseDTO(
    val cardType: String?,
    val bookTitle: String?,
    val bookAuthor: String?,
    val bookImage: String?,
    val creatorNickname: String?,
    val page: Int?,
    val memo: String?,
    val quotation: String?,
    val imageUrl: String?,
)
