package com.bookiibookii.bookiibookii.library.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.common.DateUtils
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
                // 내 닉네임·프로필 조회
                val mypageResp = RetrofitClient.mypApi().getMypage()
                val myNickname = mypageResp.body()?.result?.nickname ?: ""
                val myProfileImageUrl = mypageResp.body()?.result?.profileImageUrl

                // 그룹 리뷰 조회 (신규 API)
                val reviewResp = RetrofitClient.libApi().getGroupReviews(groupId)
                if (reviewResp.isSuccessful && reviewResp.body()?.isSuccess == true) {
                    val result = reviewResp.body()?.result
                    val bookReviews   = result?.bookReviews ?: emptyList()
                    val memberReviews = result?.memberReviews ?: emptyList()

                    // 파트너 (멤버 후기 중 내가 아닌 작성자)
                    val partnerMember = memberReviews.firstOrNull { it.writerNickname != myNickname }
                    val partnerNickname = partnerMember?.writerNickname ?: ""
                    val partnerProfileImageUrl = partnerMember?.writerProfileImageUrl

                    // 대화 메시지 (파트너 후기)
                    val messages = memberReviews.mapNotNull { review ->
                        if (review.comment.isNullOrBlank()) null
                        else ExchangeMessage(
                            username        = review.writerNickname,
                            message         = review.comment,
                            reaction        = review.reaction.orEmpty(),   // null=반응 없음 → 배지 미표시
                            isMine          = review.writerNickname == myNickname,
                            profileImageUrl = review.writerProfileImageUrl,
                        )
                    }

                    val mappedReviews = bookReviews
                        .groupBy { it.bookId }
                        .map { (_, reviews) ->
                            // 작성자 구분: writerNickname 우선, 없으면 isEditable(내가 수정 가능 = 내 리뷰)
                            val mine = reviews.firstOrNull {
                                if (it.writerNickname != null) it.writerNickname == myNickname else it.isEditable == true
                            }
                            val partner = reviews.firstOrNull { it !== mine }
                            val anyOne  = mine ?: partner
                            BookReviewItem(
                                bookTitle     = anyOne?.bookTitle.orEmpty(),
                                bookAuthor    = anyOne?.bookAuthor.orEmpty(),
                                bookGenre     = "",
                                bookCoverUrl  = anyOne?.bookImageUrl,
                                myRating      = (mine?.rating ?: 0.0).toInt().coerceIn(0, 5),
                                myReview      = mine?.content.orEmpty(),
                                myDate        = DateUtils.formatDate(mine?.createdAt),
                                partnerRating = (partner?.rating ?: 0.0).toInt().coerceIn(0, 5),
                                partnerReview = partner?.content.orEmpty(),
                                partnerDate   = DateUtils.formatDate(partner?.createdAt),
                            )
                        }

                    // 기간 표기 "yyyy. MM. dd. ~ yyyy. MM. dd."
                    val dateRange = if (endDate.isNotBlank()) {
                        "${DateUtils.formatDate(startDate)} ~ ${DateUtils.formatDate(endDate)}"
                    } else {
                        DateUtils.formatDate(startDate)
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            myNickname = myNickname,
                            data = GroupReviewData(
                                groupName              = groupName,
                                dateRange              = dateRange,
                                myUsername             = myNickname,
                                partnerUsername        = partnerNickname,
                                myProfileImageUrl      = myProfileImageUrl,
                                partnerProfileImageUrl = partnerProfileImageUrl,
                                messages               = messages,
                                bookReviews            = mappedReviews,
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
