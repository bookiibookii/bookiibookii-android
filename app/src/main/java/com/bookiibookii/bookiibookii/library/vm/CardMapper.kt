package com.bookiibookii.bookiibookii.library.vm

import com.bookiibookii.bookiibookii.data.model.library.MemberCardResponseDTO
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.ui.ReadingCardType

/**
 * API DTO → UI 모델 변환. library.vm 내 모든 ViewModel에서 공유.
 */
internal fun MemberCardResponseDTO.toReadingCard() = ReadingCard(
    cardId                 = cardId.toLong(),
    username               = creatorName.orEmpty(),
    content                = memo.orEmpty(),
    page                   = page.toString(),
    type                   = if (cardType == "IMAGE") ReadingCardType.PHOTO else ReadingCardType.QUOTE,
    isBookmarked           = isBookmarked,
    date                   = createdAt.take(10),
    bookTitle              = bookTitle.orEmpty(),
    quotation              = quotation.orEmpty(),
    imageUrl               = cardImage?.presignedGetUrl,
    myReactions            = myReactions,
    reactionCounts         = reactionCounts.associate { it.reaction to it.count },
    creatorProfileImageUrl = creatorProfileImageUrl,
    isMine                 = isMine,
)
