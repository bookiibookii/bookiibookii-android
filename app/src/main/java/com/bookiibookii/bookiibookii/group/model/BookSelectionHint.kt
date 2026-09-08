package com.bookiibookii.bookiibookii.group.model

/**
 * 도서 검색 필드 아래에 띄울 안내 문구. 안내할 게 없으면 null.
 *
 * 그룹 생성/참여 신청은 `isbn13`이 있어야 제출할 수 있는데, 이 값은 **검색 결과 목록에서
 * 책을 눌렀을 때만** 채워진다. 검색어만 입력하고 목록에서 고르지 않으면 텍스트는 채워져
 * 있는데 버튼이 비활성이라 사용자가 이유를 알 수 없다. 검색 결과가 0건일 때도 드롭다운이
 * 뜨지 않아 아무 일도 일어나지 않은 것처럼 보인다.
 *
 * 두 화면(그룹 생성/수정, 참여 신청)이 같은 문구를 쓰도록 한곳에 모아둔다.
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
    noResult -> "검색 결과가 없어요. 제목을 다시 확인해 주세요"
    hasResults -> "목록에서 책을 선택해 주세요"
    else -> null                 // 검색 중
}
