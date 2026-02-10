package com.bookiibookii.bookiibookii.group.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import kotlinx.coroutines.launch

class GroupDetailViewModel : ViewModel() {

    // 화면에 보여줄 데이터
    private val _groupDetail = MutableLiveData<GroupItemDto.GroupDetailResult?>()
    val groupDetail: LiveData<GroupItemDto.GroupDetailResult?> get() = _groupDetail

    // 로딩 상태나 에러 처리가 필요하면 추가 가능

    fun fetchGroupDetail(groupId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getGroupDetail(groupId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _groupDetail.value = response.body()!!.result
                    Log.d("GroupDetailVM", "데이터 로드 성공: ${response.body()!!.result.buttonStatus}")
                } else {
                    Log.e("GroupDetailVM", "데이터 로드 실패: ${response.code()}")
                    // 필요 시 에러 이벤트 처리
                }
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "네트워크 오류", e)
            }
        }
    }
}