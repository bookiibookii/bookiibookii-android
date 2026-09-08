package com.bookiibookii.bookiibookii.common

/**
 * 도서 검색 화면들이 공유하는 문구.
 *
 * 그룹 생성/수정, 그룹 참여 신청, 마이페이지 인생 책, 온보딩 인생 책이 모두 같은 검색 API를
 * 쓰지만 화면마다 문구가 제각각이면 같은 상황에서 다른 말을 하게 된다. 한곳에 모아둔다.
 */
const val BOOK_SEARCH_NO_RESULT = "검색 결과가 없어요. 제목을 다시 확인해 주세요"
const val BOOK_SEARCH_SELECT_PROMPT = "목록에서 책을 선택해 주세요"
const val BOOK_SEARCH_FAILED = "검색에 실패했어요"
const val BOOK_SEARCH_NETWORK_ERROR = "네트워크 오류가 발생했어요"

/**
 * 도서 검색 필드 아래에 띄울 안내 문구. 안내할 게 없으면 null.
 *
 * 그룹 생성/참여 신청은 `isbn13`이 있어야 제출할 수 있는데, 이 값은 **검색 결과 목록에서
 * 책을 눌렀을 때만** 채워진다. 검색어만 입력하고 목록에서 고르지 않으면 텍스트는 채워져
 * 있는데 버튼이 비활성이라 사용자가 이유를 알 수 없다. 검색 결과가 0건일 때도 드롭다운이
 * 뜨지 않아 아무 일도 일어나지 않은 것처럼 보인다.
 */
fun bookSelectionHint(
    query: String,
    isbn13: String?,
    hasResults: Boolean,
    noResult: Boolean,
    error: String?,
): String? = when {
    isbn13 != null -> null       // 선택 완료
    query.isBlank() -> null      // 입력 전
    error != null -> null        // 오류 메시지가 이미 표시됨
    noResult -> BOOK_SEARCH_NO_RESULT
    hasResults -> BOOK_SEARCH_SELECT_PROMPT
    else -> null                 // 검색 중
}
