package com.bookiibookii.bookiibookii.bookData.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bookiibookii.bookiibookii.bookData.Data.LibReview

class ReviewModel : ViewModel() {
    // 전체 리뷰 리스트
    private val _reviewList = MutableLiveData<MutableList<LibReview>>(mutableListOf())
    val reviewList: LiveData<MutableList<LibReview>> get() = _reviewList

    init {
        // 더미 데이터
        val dummy = mutableListOf(
            LibReview(1, "kanghunsim", "첫 번째 리뷰입니다.", 12, "2025.01.14", isMine = true),
            LibReview(2, "noshel", "타인이 쓴 리뷰입니다.", 150, "2025.01.12", isMine = false)
        )
        _reviewList.value = dummy
    }

    // ID로 리뷰 찾기 ( 핵심 )
    fun getReviewById(id: Long): LibReview? {
        return _reviewList.value?.find { it.id == id }
    }

    fun addReview(review: LibReview) {
        val currentList = _reviewList.value ?: mutableListOf()
        currentList.add(0, review)
        _reviewList.value = currentList // 갱신 트리거
    }

    fun updateReview(updatedReview: LibReview) {
        val currentList = _reviewList.value ?: return
        val index = currentList.indexOfFirst { it.id == updatedReview.id }
        if (index != -1) {
            currentList[index] = updatedReview
            _reviewList.value = currentList
        }
    }

    fun deleteReview(reviewId: Long) {
        val currentList = _reviewList.value ?: return
        currentList.removeAll { it.id == reviewId }
        _reviewList.value = currentList
    }
}