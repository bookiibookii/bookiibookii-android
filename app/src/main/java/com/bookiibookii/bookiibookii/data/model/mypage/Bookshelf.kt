package com.bookiibookii.bookiibookii.data.model.mypage

// ── GET /api/mypage/bookshelf ─────────────────────────────────────────────────
data class BookshelfResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: BookshelfResult?,
)

data class BookshelfResult(
    val completedBooks: List<CompletedBook>?,
    val favoriteBooks: List<FavoriteBook>?,
    val representativeBooks: List<RepresentativeBook>?,
)

data class CompletedBook(
    val groupBookId: Long,
    val title: String,
    val author: String?,
    val image: String?,
    val category: String?,
    val rating: Double,
    val completedAt: String?,
)

data class FavoriteBook(
    val userBookId: Long,
    val title: String,
    val author: String?,
    val category: String?,
    val image: String?,
)

data class RepresentativeBook(
    val userBookId: Long,
    val title: String,
    val displayOrder: Int,
    val isFavorite: Boolean,
)

// ── POST /api/mypage/bookshelf/favorites ─────────────────────────────────────
data class AddFavoriteBookRequest(
    val isbn13: String,
)

// ── POST /api/mypage/bookshelf/representatives ───────────────────────────────
data class AddRepresentativeBookRequest(
    val userBookId: Long? = null,
    val groupBookId: Long? = null,
)

// ── PATCH /api/mypage/bookshelf/representatives/order ────────────────────────
data class UpdateRepresentativeOrderRequest(
    val userBookId: Long,
    val targetOrder: Int,
)
