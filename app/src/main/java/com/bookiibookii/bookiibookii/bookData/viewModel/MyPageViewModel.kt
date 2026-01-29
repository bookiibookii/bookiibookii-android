package com.bookiibookii.bookiibookii.bookData.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bookiibookii.bookiibookii.bookData.Data.MypProfileData

class MyPageViewModel : ViewModel() {
    // 프로필 데이터 (초기값 더미)
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
    val profileData: LiveData<MypProfileData> get() = _profileData

    fun updateProfile(newData: MypProfileData) {
        _profileData.value = newData
    }
}