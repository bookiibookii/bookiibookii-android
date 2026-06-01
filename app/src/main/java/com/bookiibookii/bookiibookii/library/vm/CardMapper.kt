package com.bookiibookii.bookiibookii.library.vm

import com.bookiibookii.bookiibookii.data.model.library.MemberCardResponseDTO
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.ui.ReadingCardType

/**
 * API DTO → UI 모델 변환. library.vm 내 모든 ViewModel에서 공유.
 */
internal fun MemberCardResponseDTO.toReadingCard() = ReadingCard(
    cardId                 = cardId.toLong(),
    username               = creatorName,
    content                = memo,
    page                   = page.toString(),
    type                   = if (cardType == "IMAGE") ReadingCardType.PHOTO else ReadingCardType.QUOTE,
    isBookmarked           = isBookmarked,
    date                   = createdAt.take(10),
    bookTitle              = bookTitle,
    quotation              = quotation,
    imageUrl               = cardImage?.presignedGetUrl,
    myReactions            = myReactions,
    creatorProfileImageUrl = creatorProfileImageUrl,
    isMine                 = isMine,
)
