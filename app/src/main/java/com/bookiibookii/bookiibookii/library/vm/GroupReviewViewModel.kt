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

data class GroupReviewUiState(
    val isLoading: Boolean = false,
    val data: GroupReviewData? = null,
    val myNickname: String = "",
)

class GroupReviewViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GroupReviewUiState())
    val uiState: StateFlow<GroupReviewUiState> = _uiState.asStateFlow()

    fun loadReview(
        groupId: Int,
        groupName: String,
        startDate: String,
        endDate: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 내 닉네임 조회
                val mypageResp = RetrofitClient.mypApi().getMypage()
                val myNickname = mypageResp.body()?.result?.nickname ?: ""

                // 그룹 리뷰 조회 (신규 API)
                val reviewResp = RetrofitClient.libApi().getGroupReviews(groupId)
                if (reviewResp.isSuccessful && reviewResp.body()?.isSuccess == true) {
                    val result = reviewResp.body()?.result
                    val bookReviews   = result?.bookReviews ?: emptyList()
                    val memberReviews = result?.memberReviews ?: emptyList()

                    // 파트너 닉네임
                    val partnerNickname = memberReviews
                        .firstOrNull { it.writerNickname != myNickname }
                        ?.writerNickname ?: ""

                    // 대화 메시지 (파트너 후기)
                    val messages = memberReviews.mapNotNull { review ->
                        if (review.comment.isNullOrBlank()) null
                        else ExchangeMessage(
                            username = review.writerNickname,
                            message  = review.comment,
                            reaction = review.reaction.isNotBlank(),
                            isMine   = review.writerNickname == myNickname,
                        )
                    }

                    // 도서별 리뷰 (내 리뷰 + 파트너 리뷰 매칭)
                    val myBookReviews      = bookReviews.filter { it.writerNickname == myNickname }
                    val partnerBookReviews = bookReviews.filter { it.writerNickname != myNickname }

                    val mappedReviews = myBookReviews.map { my ->
                        val partner = partnerBookReviews.firstOrNull { it.bookId == my.bookId }
                        BookReviewItem(
                            bookTitle     = my.bookTitle,
                            bookAuthor    = my.bookAuthor.orEmpty(),
                            bookGenre     = "",
                            myRating      = my.star.toInt().coerceIn(0, 5),
                            myReview      = my.comment.orEmpty(),
                            myDate        = my.createdAt.take(10),
                            partnerRating = partner?.star?.toInt()?.coerceIn(0, 5) ?: 0,
                            partnerReview = partner?.comment.orEmpty(),
                            partnerDate   = partner?.createdAt?.take(10).orEmpty(),
                        )
                    }

                    val dateRange = if (endDate.isNotBlank()) "$startDate ~ $endDate" else startDate

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            myNickname = myNickname,
                            data = GroupReviewData(
                                groupName       = groupName,
                                dateRange       = dateRange,
                                myUsername      = myNickname,
                                partnerUsername = partnerNickname,
                                messages        = messages,
                                bookReviews     = mappedReviews,
                            )
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
