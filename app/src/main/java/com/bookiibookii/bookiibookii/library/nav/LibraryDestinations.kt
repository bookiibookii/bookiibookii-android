package com.bookiibookii.bookiibookii.library.nav

import android.net.Uri
import com.bookiibookii.bookiibookii.library.ui.AddCardMode
import com.bookiibookii.bookiibookii.library.ui.ReadingCard
import com.bookiibookii.bookiibookii.library.ui.ReadingCardType

object LibraryDestinations {
    const val MAIN = "main"

    const val DETAIL_ARG_GROUP_ID = "groupId"
    const val DETAIL_ARG_MEMBER_BOOK_ID = "memberBookId"
    const val DETAIL_ARG_GROUP_NAME = "groupName"
    const val DETAIL_ARG_BOOK_TITLE = "bookTitle"
    const val DETAIL_ARG_AUTHOR = "author"
    const val DETAIL_ARG_GENRE = "genre"
    const val DETAIL_ARG_COVER_URL = "coverUrl"
    const val DETAIL_ARG_START_DATE = "startDate"
    const val DETAIL_ARG_END_DATE = "endDate"
    const val DETAIL_ARG_COMPLETED_AT = "completedAt"
    const val DETAIL_ARG_RATING = "rating"
    const val DETAIL_ARG_IS_DONE = "isDone"
    const val DETAIL_ARG_PROGRESS_RATE = "progressRate"
    const val DETAIL_ARG_TOTAL_PAGES = "totalPages"

    const val DETAIL_ROUTE = "detail/{$DETAIL_ARG_GROUP_ID}/{$DETAIL_ARG_MEMBER_BOOK_ID}" +
        "?$DETAIL_ARG_GROUP_NAME={$DETAIL_ARG_GROUP_NAME}" +
        "&$DETAIL_ARG_BOOK_TITLE={$DETAIL_ARG_BOOK_TITLE}" +
        "&$DETAIL_ARG_AUTHOR={$DETAIL_ARG_AUTHOR}" +
        "&$DETAIL_ARG_GENRE={$DETAIL_ARG_GENRE}" +
        "&$DETAIL_ARG_COVER_URL={$DETAIL_ARG_COVER_URL}" +
        "&$DETAIL_ARG_START_DATE={$DETAIL_ARG_START_DATE}" +
        "&$DETAIL_ARG_END_DATE={$DETAIL_ARG_END_DATE}" +
        "&$DETAIL_ARG_COMPLETED_AT={$DETAIL_ARG_COMPLETED_AT}" +
        "&$DETAIL_ARG_RATING={$DETAIL_ARG_RATING}" +
        "&$DETAIL_ARG_IS_DONE={$DETAIL_ARG_IS_DONE}" +
        "&$DETAIL_ARG_PROGRESS_RATE={$DETAIL_ARG_PROGRESS_RATE}" +
        "&$DETAIL_ARG_TOTAL_PAGES={$DETAIL_ARG_TOTAL_PAGES}"

    fun detail(
        groupId: Int,
        memberBookId: Int,
        groupName: String = "",
        bookTitle: String = "",
        author: String = "",
        genre: String = "",
        coverUrl: String = "",
        startDate: String = "",
        endDate: String = "",
        completedAt: String = "",
        rating: Double = 0.0,
        isDone: Boolean = false,
        progressRate: Int = 0,
        totalPages: Int = 0,
    ): String = "detail/$groupId/$memberBookId" +
        "?$DETAIL_ARG_GROUP_NAME=${Uri.encode(groupName)}" +
        "&$DETAIL_ARG_BOOK_TITLE=${Uri.encode(bookTitle)}" +
        "&$DETAIL_ARG_AUTHOR=${Uri.encode(author)}" +
        "&$DETAIL_ARG_GENRE=${Uri.encode(genre)}" +
        "&$DETAIL_ARG_COVER_URL=${Uri.encode(coverUrl)}" +
        "&$DETAIL_ARG_START_DATE=${Uri.encode(startDate)}" +
        "&$DETAIL_ARG_END_DATE=${Uri.encode(endDate)}" +
        "&$DETAIL_ARG_COMPLETED_AT=${Uri.encode(completedAt)}" +
        "&$DETAIL_ARG_RATING=$rating" +
        "&$DETAIL_ARG_IS_DONE=$isDone" +
        "&$DETAIL_ARG_PROGRESS_RATE=$progressRate" +
        "&$DETAIL_ARG_TOTAL_PAGES=$totalPages"

    const val ADD_CARD_ARG_MODE = "mode"
    const val ADD_CARD_ARG_MEMBER_BOOK_ID = "memberBookId"
    const val ADD_CARD_ARG_CARD_ID = "cardId"
    const val ADD_CARD_ARG_QUOTE = "quote"
    const val ADD_CARD_ARG_PAGE = "page"
    const val ADD_CARD_ARG_MEMO = "memo"
    const val ADD_CARD_ARG_IMAGE_URL = "imageUrl"
    const val ADD_CARD_ARG_S3KEY = "s3Key"
    const val ADD_CARD_ARG_BOOK_TITLE = "bookTitle"
    const val ADD_CARD_ARG_TOTAL_PAGES = "totalPages"

    const val ADD_CARD_ROUTE = "addCard/{$ADD_CARD_ARG_MODE}/{$ADD_CARD_ARG_MEMBER_BOOK_ID}" +
        "?$ADD_CARD_ARG_CARD_ID={$ADD_CARD_ARG_CARD_ID}" +
        "&$ADD_CARD_ARG_QUOTE={$ADD_CARD_ARG_QUOTE}" +
        "&$ADD_CARD_ARG_PAGE={$ADD_CARD_ARG_PAGE}" +
        "&$ADD_CARD_ARG_MEMO={$ADD_CARD_ARG_MEMO}" +
        "&$ADD_CARD_ARG_IMAGE_URL={$ADD_CARD_ARG_IMAGE_URL}" +
        "&$ADD_CARD_ARG_S3KEY={$ADD_CARD_ARG_S3KEY}" +
        "&$ADD_CARD_ARG_BOOK_TITLE={$ADD_CARD_ARG_BOOK_TITLE}" +
        "&$ADD_CARD_ARG_TOTAL_PAGES={$ADD_CARD_ARG_TOTAL_PAGES}"

    fun addCard(
        mode: AddCardMode,
        memberBookId: Int,
        bookTitle: String = "",
        totalPages: Int = 0,
    ): String = "addCard/${mode.name}/$memberBookId" +
        "?$ADD_CARD_ARG_CARD_ID=-1" +
        "&$ADD_CARD_ARG_QUOTE=" +
        "&$ADD_CARD_ARG_PAGE=" +
        "&$ADD_CARD_ARG_MEMO=" +
        "&$ADD_CARD_ARG_IMAGE_URL=" +
        "&$ADD_CARD_ARG_S3KEY=" +
        "&$ADD_CARD_ARG_BOOK_TITLE=${Uri.encode(bookTitle)}" +
        "&$ADD_CARD_ARG_TOTAL_PAGES=$totalPages"

    fun addCardEdit(card: ReadingCard): String {
        val mode = if (card.type == ReadingCardType.PHOTO) AddCardMode.PHOTO else AddCardMode.TEXT
        return "addCard/${mode.name}/${card.memberBookId}" +
            "?$ADD_CARD_ARG_CARD_ID=${card.cardId}" +
            "&$ADD_CARD_ARG_QUOTE=${Uri.encode(card.quotation)}" +
            "&$ADD_CARD_ARG_PAGE=${Uri.encode(card.page)}" +
            "&$ADD_CARD_ARG_MEMO=${Uri.encode(card.content)}" +
            "&$ADD_CARD_ARG_IMAGE_URL=${Uri.encode(card.imageUrl.orEmpty())}" +
            "&$ADD_CARD_ARG_S3KEY=${Uri.encode(card.s3Key.orEmpty())}" +
            "&$ADD_CARD_ARG_BOOK_TITLE=${Uri.encode(card.bookTitle)}" +
            "&$ADD_CARD_ARG_TOTAL_PAGES=${card.totalPages ?: 0}"
    }

    const val CARD_DETAIL_ARG_INDEX = "initialIndex"
    const val CARD_DETAIL_ARG_SORT = "sortByLatest"
    const val CARD_DETAIL_ARG_CARDS = "cards"

    const val CARD_DETAIL_ROUTE = "cardDetail/{$CARD_DETAIL_ARG_INDEX}/{$CARD_DETAIL_ARG_SORT}" +
        "?$CARD_DETAIL_ARG_CARDS={$CARD_DETAIL_ARG_CARDS}"

    fun cardDetail(initialIndex: Int, sortByLatest: Boolean, cardsJson: String): String =
        "cardDetail/$initialIndex/$sortByLatest?$CARD_DETAIL_ARG_CARDS=${Uri.encode(cardsJson)}"

    const val GROUP_REVIEW_ARG_GROUP_ID = "groupId"
    const val GROUP_REVIEW_ARG_GROUP_NAME = "groupName"
    const val GROUP_REVIEW_ARG_BOOK_TITLE = "bookTitle"
    const val GROUP_REVIEW_ARG_START_DATE = "startDate"
    const val GROUP_REVIEW_ARG_END_DATE = "endDate"

    const val GROUP_REVIEW_ROUTE = "groupReview/{$GROUP_REVIEW_ARG_GROUP_ID}" +
        "?$GROUP_REVIEW_ARG_GROUP_NAME={$GROUP_REVIEW_ARG_GROUP_NAME}" +
        "&$GROUP_REVIEW_ARG_BOOK_TITLE={$GROUP_REVIEW_ARG_BOOK_TITLE}" +
        "&$GROUP_REVIEW_ARG_START_DATE={$GROUP_REVIEW_ARG_START_DATE}" +
        "&$GROUP_REVIEW_ARG_END_DATE={$GROUP_REVIEW_ARG_END_DATE}"

    fun groupReview(
        groupId: Int,
        groupName: String = "",
        bookTitle: String = "",
        startDate: String = "",
        endDate: String = "",
    ): String = "groupReview/$groupId" +
        "?$GROUP_REVIEW_ARG_GROUP_NAME=${Uri.encode(groupName)}" +
        "&$GROUP_REVIEW_ARG_BOOK_TITLE=${Uri.encode(bookTitle)}" +
        "&$GROUP_REVIEW_ARG_START_DATE=${Uri.encode(startDate)}" +
        "&$GROUP_REVIEW_ARG_END_DATE=${Uri.encode(endDate)}"

    const val REVIEW_EDIT_ARG_GROUP_ID = "groupId"
    const val REVIEW_EDIT_ARG_GROUP_NAME = "groupName"
    const val REVIEW_EDIT_ARG_DATE_RANGE = "dateRange"
    const val REVIEW_EDIT_ARG_PARTNER_NAME = "partnerName"

    const val REVIEW_EDIT_ROUTE = "reviewEdit/{$REVIEW_EDIT_ARG_GROUP_ID}" +
        "?$REVIEW_EDIT_ARG_GROUP_NAME={$REVIEW_EDIT_ARG_GROUP_NAME}" +
        "&$REVIEW_EDIT_ARG_DATE_RANGE={$REVIEW_EDIT_ARG_DATE_RANGE}" +
        "&$REVIEW_EDIT_ARG_PARTNER_NAME={$REVIEW_EDIT_ARG_PARTNER_NAME}"

    fun reviewEdit(
        groupId: Int,
        groupName: String = "",
        dateRange: String = "",
        partnerName: String = "",
    ): String = "reviewEdit/$groupId" +
        "?$REVIEW_EDIT_ARG_GROUP_NAME=${Uri.encode(groupName)}" +
        "&$REVIEW_EDIT_ARG_DATE_RANGE=${Uri.encode(dateRange)}" +
        "&$REVIEW_EDIT_ARG_PARTNER_NAME=${Uri.encode(partnerName)}"

    const val BOOKMARK = "bookmark"
}
