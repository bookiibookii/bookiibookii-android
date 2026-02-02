package com.bookiibookii.bookiibookii.bookData.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bookiibookii.bookiibookii.bookData.Data.MypNotice
import com.bookiibookii.bookiibookii.bookData.Data.MypProfileData
import com.bookiibookii.bookiibookii.bookData.Data.MypQuestion
import com.bookiibookii.bookiibookii.bookData.Data.MypReport

class MyPageViewModel : ViewModel() {
    private val _profileData = MutableLiveData(
        MypProfileData(
            nickname = "noshel",
            name = "김철수",
            phone = "010-1234-5678",
            address = "서울시 강남구",
            addressDetail = "101동 101호",
            regionInfo = "서울 강남구"
        )
    )

    // 1. 공지사항 리스트
    private val _noticeList = MutableLiveData<List<MypNotice>>()
    val noticeList: LiveData<List<MypNotice>> get() = _noticeList

    // 2. 문의 내역 리스트
    private val _questionList = MutableLiveData<List<MypQuestion>>()
    val questionList: LiveData<List<MypQuestion>> get() = _questionList

    // 3. 신고 내역 리스트
    private val _reportList = MutableLiveData<List<MypReport>>()
    val reportList: LiveData<List<MypReport>> get() = _reportList

    init {
        _noticeList.value = listOf(
            MypNotice("12월 업데이트 안내", "새로운 기능이 추가되었습니다...", "5분 전", true),
            MypNotice("서버 점검 안내", "새벽 2시부터 점검이...", "2024.11.20", false)
        )

        _questionList.value = listOf(
            MypQuestion(1, "책 배송 관련 문의", "배송 언제 되나요?", "2024.11.29", "택배로 진행됩니다.", "2024.11.30")
        )

        _reportList.value = listOf(
            MypReport(1, "noshel", "욕설/비방 신고", "욕설을 너무 많이 해요", "2024.11.29", "답변 대기 중", null)
        )
    }

    fun addQuestion(question: MypQuestion) {
        val currentList = _questionList.value.orEmpty().toMutableList()
        currentList.add(0, question) // 최신순 추가
        _questionList.value = currentList
    }

    fun addReport(report: MypReport) {
        val currentList = _reportList.value.orEmpty().toMutableList()
        currentList.add(0, report)
        _reportList.value = currentList
    }
    val profileData: LiveData<MypProfileData> get() = _profileData

    fun updateProfile(newData: MypProfileData) {
        _profileData.value = newData
    }
}