package com.bookiibookii.bookiibookii.library.vm

import com.bookiibookii.bookiibookii.common.DateUtils
import com.bookiibookii.bookiibookii.data.model.library.MemberCardResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.PublicReadingCardResponseDTO
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.ui.ReadingCardType

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

internal fun PublicReadingCardResponseDTO.toReadingCard() = ReadingCard(
    username  = creatorNickname.orEmpty(),
    content   = memo.orEmpty(),
    page      = page?.toString().orEmpty(),
    type      = if (cardType == "IMAGE") ReadingCardType.PHOTO else ReadingCardType.QUOTE,
    bookTitle = bookTitle.orEmpty(),
    quotation = quotation.orEmpty(),
    imageUrl  = imageUrl,
)
