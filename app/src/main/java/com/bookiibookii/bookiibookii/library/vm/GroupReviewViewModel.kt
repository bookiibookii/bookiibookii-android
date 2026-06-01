package com.bookiibookii.bookiibookii.library.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.library.ui.BookReviewItem
import com.bookiibookii.bookiibookii.library.ui.ExchangeMessage
import com.bookiibookii.bookiibookii.library.ui.GroupReviewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class GroupReviewUiState(
    val isLoading: Boolean = false,
    val data: GroupReviewData? = null,
)

class GroupReviewViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GroupReviewUiState())
    val uiState: StateFlow<GroupReviewUiState> = _uiState.asStateFlow()

    /**
     * GET /api/mypage 에서 최근 리뷰 데이터를 가져와 bookTitle로 매칭.
     * 서버에 그룹별 리뷰 조회 API가 없어 마이페이지 recent 데이터를 활용.
     */
    fun loadReview(
        groupName: String,
        bookTitle: String,
        startDate: String,
        endDate: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val resp = RetrofitClient.mypApi().getMypage()
                if (resp.isSuccessful && resp.body()?.isSuccess == true) {
                    val result = resp.body()?.result
                    val myNickname = result?.nickname ?: ""

                    // 내 책 리뷰 중 해당 도서와 제목 매칭
                    val myBookReview = result?.recentBookReviews?.firstOrNull {
                        it.bookTitle == bookTitle
                    }

                    // 내가 받은 파트너 후기
                    val receivedReviews = result?.recentReceivedReviews ?: emptyList()
                    val partnerNickname = receivedReviews.firstOrNull()?.reviewerNickname ?: ""

                    // 대화 형태 메시지 구성
                    val messages = buildList {
                        // 파트너가 나에게 남긴 후기 (왼쪽 버블)
                        receivedReviews.forEach { received ->
                            if (!received.comment.isNullOrBlank()) {
                                add(
                                    ExchangeMessage(
                                        username = received.reviewerNickname,
                                        message  = received.comment,
                                        reaction = received.reaction.isNotBlank(),
                                        isMine   = false,
                                    )
                                )
                            }
                        }
                        // 내가 쓴 책 후기 (오른쪽 버블)
                        myBookReview?.let {
                            if (!it.comment.isNullOrBlank()) {
                                add(
                                    ExchangeMessage(
                                        username = myNickname,
                                        message  = it.comment,
                                        reaction = false,
                                        isMine   = true,
                                    )
                                )
                            }
                        }
                    }

                    // 도서별 리뷰 카드 구성
                    val bookReviews = buildList {
                        myBookReview?.let { review ->
                            val partnerReceived = receivedReviews.firstOrNull()
                            add(
                                BookReviewItem(
                                    bookTitle     = review.bookTitle,
                                    bookAuthor    = review.bookAuthor,
                                    bookGenre     = review.tradeType,
                                    myRating      = review.rating.roundToInt().coerceIn(0, 5),
                                    myReview      = review.comment ?: "",
                                    myDate        = review.reviewDate?.take(10) ?: "",
                                    partnerRating = 0, // 파트너의 책 별점은 API 미제공
                                    partnerReview = partnerReceived?.comment ?: "",
                                    partnerDate   = partnerReceived?.createdAt?.take(10) ?: "",
                                )
                            )
                        }
                    }

                    val dateRange = if (endDate.isNotBlank()) "$startDate ~ $endDate" else startDate

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            data = GroupReviewData(
                                groupName       = groupName,
                                dateRange       = dateRange,
                                myUsername      = myNickname,
                                partnerUsername = partnerNickname,
                                messages        = messages,
                                bookReviews     = bookReviews,
                            )
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
