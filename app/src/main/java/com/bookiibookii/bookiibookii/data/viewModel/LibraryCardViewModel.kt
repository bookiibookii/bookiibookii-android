package com.bookiibookii.bookiibookii.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.library.CardItem
import com.bookiibookii.bookiibookii.data.model.library.GroupCardResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class LibraryCardViewModel : ViewModel() {

    // 1. 상태 관리를 위한 LiveData (로딩, 에러)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // 2. 상세 화면 UI 업데이트를 위한 그룹 결과 (내 코멘트, 파트너 코멘트 등)
    private val _groupCardResult = MutableLiveData<GroupCardResult?>()
    val groupCardResult: LiveData<GroupCardResult?> get() = _groupCardResult

    // 3. 리사이클러뷰에 연결할 최종 카드 리스트
    private val _cardList = MutableLiveData<List<CardItem>>()
    val cardList: LiveData<List<CardItem>> get() = _cardList

    // 원본 리스트 (정렬용)
    private var originalCards: List<CardItem> = emptyList()

    /**
     * [상세 화면용] 그룹 카드를 가져오고, 각 카드의 프로필과 댓글 수를 조합합니다.
     */
    fun fetchGroupCards(groupId: Int) {
        if (groupId == -1) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitClient.libApi().getGroupCards(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    if (result != null) {
                        _groupCardResult.value = result

                        val rawCards = result.cards

                        // 각 카드의 추가 정보(프로필, 댓글수)를 병렬(async)로 조회
                        val mappedCards = coroutineScope {
                            rawCards.map { card ->
                                async {
                                    var profileUrl: String? = null
                                    var commentCount = 0

                                    try {
                                        val profileRes = RetrofitClient.userApi().getUserProfile(card.creatorName)
                                        if (profileRes.isSuccessful && profileRes.body()?.isSuccess == true) {
                                            profileUrl = profileRes.body()?.result?.profileImageUrl
                                        }
                                    } catch (e: Exception) { }

                                    try {
                                        val commentRes = RetrofitClient.libApi().getCardComments(card.cardId.toLong())
                                        if (commentRes.isSuccessful && commentRes.body()?.isSuccess == true) {
                                            commentCount = commentRes.body()?.result?.totalCount ?: 0
                                        }
                                    } catch (e: Exception) { }

                                    // 기존 필드 복사 + 새로운 필드 추가
                                    card.copy(
                                        profileImageUrl = profileUrl,
                                        commentCount = commentCount
                                    )
                                }
                            }.awaitAll()
                        }

                        // 최신순으로 기본 정렬
                        originalCards = mappedCards.sortedByDescending { it.createdAt }
                        _cardList.value = originalCards
                    }
                } else {
                    _errorMessage.value = "데이터를 불러오는 데 실패했습니다."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * [북마크 화면용] 북마크된 카드 목록을 가져오고, 각 카드의 프로필과 댓글 수를 조합합니다.
     */
    fun fetchBookmarkedCards() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = RetrofitClient.libApi().getBookmarkedCards()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val rawCards = response.body()?.result ?: emptyList()

                    val mappedCards = coroutineScope {
                        rawCards.map { card ->
                            async {
                                var profileUrl: String? = null
                                var commentCount = 0

                                try {
                                    val profileRes = RetrofitClient.userApi().getUserProfile(card.creatorName)
                                    if (profileRes.isSuccessful && profileRes.body()?.isSuccess == true) {
                                        profileUrl = profileRes.body()?.result?.profileImageUrl
                                    }
                                } catch (e: Exception) {}

                                try {
                                    val commentRes = RetrofitClient.libApi().getCardComments(card.cardId.toLong())
                                    if (commentRes.isSuccessful && commentRes.body()?.isSuccess == true) {
                                        commentCount = commentRes.body()?.result?.totalCount ?: 0
                                    }
                                } catch (e: Exception) {}

                                card.copy(
                                    profileImageUrl = profileUrl,
                                    commentCount = commentCount
                                )
                            }
                        }.awaitAll()
                    }

                    // 최신순으로 기본 정렬
                    originalCards = mappedCards.sortedByDescending { it.createdAt }
                    _cardList.value = originalCards
                } else {
                    _errorMessage.value = "북마크를 불러오는 데 실패했습니다."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 리스트 정렬
     */
    fun sortCards(isLately: Boolean) {
        val sortedList = if (isLately) {
            originalCards.sortedByDescending { it.createdAt }
        } else {
            originalCards.sortedBy { it.page }
        }
        _cardList.value = sortedList
    }
}