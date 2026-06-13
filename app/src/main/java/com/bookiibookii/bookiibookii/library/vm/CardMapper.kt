package com.bookiibookii.bookiibookii.library.vm

import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.data.model.library.MemberCardResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.PublicReadingCardResponseDTO
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.ui.ReadingCardType

/**
 * API DTO → UI 모델 변환. library.vm 내 모든 ViewModel에서 공유.
 */
internal fun MemberCardResponseDTO.toReadingCard() = ReadingCard(
    cardId                 = cardId.toLong(),
    memberBookId           = memberBookId,
    username               = creatorName.orEmpty(),
    content                = memo.orEmpty(),
    page                   = page.toString(),
    type                   = if (cardType == "IMAGE") ReadingCardType.PHOTO else ReadingCardType.QUOTE,
    isBookmarked           = isBookmarked,
    date                   = DateUtils.formatDate(createdAt),
    completedAt            = completedAt,
    genre                  = genre.orEmpty(),
    totalPages            = totalPages,
    bookTitle              = bookTitle.orEmpty(),
    quotation              = quotation.orEmpty(),
    imageUrl               = cardImage?.presignedGetUrl,
    s3Key                  = cardImage?.s3Key,
    myReactions            = myReactions,
    reactionCounts         = reactionCounts.associate { it.reaction to it.count },
    creatorProfileImageUrl = creatorProfileImageUrl,
    isMine                 = isMine,
)

/**
 * 공유 토큰 공개 조회 DTO → UI 모델 변환.
 * 공개 응답엔 cardId/북마크/리액션이 없어 기본값 사용. 작성자=creatorNickname.
 */
internal fun PublicReadingCardResponseDTO.toReadingCard() = ReadingCard(
    username  = creatorNickname.orEmpty(),
    content   = memo.orEmpty(),
    page      = page?.toString().orEmpty(),
    type      = if (cardType == "IMAGE") ReadingCardType.PHOTO else ReadingCardType.QUOTE,
    bookTitle = bookTitle.orEmpty(),
    quotation = quotation.orEmpty(),
    imageUrl  = imageUrl,
)
