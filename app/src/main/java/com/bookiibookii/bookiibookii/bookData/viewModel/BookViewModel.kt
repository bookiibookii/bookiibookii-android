package com.bookiibookii.bookiibookii.bookData.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.bookData.API.RetrofitClient
import com.bookiibookii.bookiibookii.bookData.Data.Book
import com.bookiibookii.bookiibookii.bookData.Data.BookCategory
import kotlinx.coroutines.launch

// 더미 데이터 (나중에 검색 입력과 연결)
private const val DUMMY_TITLE = "돈의 방정식"

class BookViewModel : ViewModel() {

    init {
        Log.d("BOOK_API", "ViewModel init")
        fetchBooks()
    }

    private fun fetchBooks() {
        Log.d("BOOK_API", "fetchBooks called")

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.searchBooks(
                    ttbKey = BuildConfig.ALADIN_TTB_KEY,
                    query = DUMMY_TITLE,
                    queryType = "Title"
                )

                Log.d("BOOK_API", "totalResults = ${response.totalResults}")

                val books = response.item.orEmpty()
                if (books.isEmpty()) {
                    Log.d("BOOK_API", "검색 결과 없음: $DUMMY_TITLE")
                    return@launch
                }

                // 대표 1권 선택 -> 지금은 세트/~전 제외 단일 상품으로 지정 -> UI구현 후 대표 서적 관리 변경 필요
                val representative = pickRepresentativeBook(
                    books = books,
                    queryTitle = DUMMY_TITLE
                ) ?: run {
                    Log.d("BOOK_API", "대표 도서 없음")
                    return@launch
                }

                // 최종 분야(카테고리) 결정
                val category = determineFinalCategory(representative)

                // 로그 출력
                Log.d("BOOK_API", "📘 대표 도서")
                Log.d("BOOK_API", "제목: ${representative.title}")
                Log.d("BOOK_API", "저자: ${representative.author}")
                Log.d("BOOK_API", "표지: ${representative.cover}")
                Log.d("BOOK_API", "링크: ${representative.link}")
                Log.d("BOOK_API", "분야: ${category.displayName}")

            } catch (e: Exception) {
                Log.e("BOOK_API", "API ERROR", e)
            }
        }
    }


    // 대표 1권 선택 로직
    private fun isSetBook(title: String): Boolean {
        val setKeywords = listOf("세트", "전", "+")
        return setKeywords.any { title.contains(it) }
    }

    private fun pickRepresentativeBook(
        books: List<Book>,
        queryTitle: String
    ): Book? {
        val singleBooks = books.filterNot { isSetBook(it.title) }
        if (singleBooks.isEmpty()) return null

        // 제목 완전 일치 우선
        return singleBooks.firstOrNull { it.title == queryTitle }
            ?: singleBooks.first()
    }

    // 1. 알라딘 카테고리 힌트 추출

    private fun extractAladinCategoryHint(categoryName: String?): String? {
        if (categoryName.isNullOrBlank()) return null

        return when {
            categoryName.contains("경제") || categoryName.contains("경영") -> "경제/경영"
            categoryName.contains("컴퓨터") || categoryName.contains("과학") -> "기술/과학"
            categoryName.contains("소설") -> "소설"
            categoryName.contains("시") -> "시"
            categoryName.contains("에세이") -> "에세이"
            categoryName.contains("예술") || categoryName.contains("문화") -> "예술/문화"
            categoryName.contains("인문") -> "인문학"
            categoryName.contains("자기계발") -> "자기계발"
            categoryName.contains("정치") || categoryName.contains("사회") -> "정치/사회"
            else -> null
        }
    }
    // 2. 추출 내용 바탕 카테고리 정리

    private fun mapToServiceCategory(aladinHint: String?): BookCategory? {
        return when (aladinHint) {
            "경제/경영" -> BookCategory.ECONOMY_MANAGEMENT
            "기술/과학" -> BookCategory.TECHNOLOGY_SCIENCE
            "소설" -> BookCategory.NOVEL
            "시" -> BookCategory.POETRY
            "에세이" -> BookCategory.ESSAY
            "예술/문화" -> BookCategory.ART_CULTURE
            "인문학" -> BookCategory.HUMANITIES
            "자기계발" -> BookCategory.SELF_HELP
            "정치/사회" -> BookCategory.POLITICS_SOCIETY
            else -> null
        }
    }

    // 3. 카테고리 분류 미흡 시 보정 로직 (제목 기반)

    private fun inferCategoryByTitle(title: String): BookCategory {
        return when {
            title.contains("소설") -> BookCategory.NOVEL
            title.contains("시집") -> BookCategory.POETRY
            title.contains("에세이") -> BookCategory.ESSAY
            title.contains("경제") || title.contains("투자") -> BookCategory.ECONOMY_MANAGEMENT
            title.contains("개발") || title.contains("프로그래밍") -> BookCategory.TECHNOLOGY_SCIENCE
            title.contains("자기") || title.contains("습관") -> BookCategory.SELF_HELP
            title.contains("정치") || title.contains("사회") -> BookCategory.POLITICS_SOCIETY
            else -> BookCategory.ETC
        }
    }
    // 카테고리 최종 결정 함수

    private fun determineFinalCategory(book: Book): BookCategory {

        // 알라딘 카테고리 힌트
        val aladinHint = extractAladinCategoryHint(book.categoryName)

        // 2알라딘 힌트 우선 적용
        mapToServiceCategory(aladinHint)?.let {
            return it
        }

        // 제목 기반 보정
        return inferCategoryByTitle(book.title)
    }
}