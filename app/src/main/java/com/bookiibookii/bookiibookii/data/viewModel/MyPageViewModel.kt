package com.bookiibookii.bookiibookii.bookData.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.MypageResult
import com.bookiibookii.bookiibookii.data.model.NicknameCheckRequest
import com.bookiibookii.bookiibookii.data.model.UserUpdateRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MyPageViewModel : ViewModel() {

    // 1. 프로필 데이터
    private val _profileData = MutableLiveData<MypageResult>()
    val profileData: LiveData<MypageResult> get() = _profileData

    // 2. 이벤트 처리
    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class Event {
        object NavigateBack : Event()
        data class ShowToast(val message: String) : Event()
        data class NicknameCheckResult(val isAvailable: Boolean) : Event()
    }

    // --- API 호출 함수들 ---

    // 1. 마이페이지 정보 불러오기
    fun fetchMypageData() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getMypage()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _profileData.value = response.body()!!.result!!
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowToast("정보를 불러오지 못했습니다."))
            }
        }
    }

    // 2. 닉네임 중복 확인
    fun checkNickname(nickname: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().checkNickname(NicknameCheckRequest(nickname))
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.NicknameCheckResult(true))
                } else {
                    _eventFlow.emit(Event.NicknameCheckResult(false))
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowToast("네트워크 오류"))
            }
        }
    }

    // 3. 프로필 수정 (이미지 로직 수정됨)
    fun updateProfile(
        request: UserUpdateRequest, // 텍스트 정보
        imageFile: File?            // 변경할 이미지 파일 (없으면 null)
    ) {
        viewModelScope.launch {
            try {
                // 3-1. 이미지가 있다면 S3 업로드 진행
                if (imageFile != null) {
                    val presignedRes = RetrofitClient.api().getPresignedUrl()
                    if (presignedRes.isSuccessful && presignedRes.body()?.isSuccess == true) {
                        val result = presignedRes.body()!!.result
                        val uploadUrl = result.presignedPutUrl
                        // val newS3Key = result.s3Key  <- 명세에 없어서 전송 안 함 (백엔드 자동 처리 가정)

                        // S3 업로드
                        val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                        val uploadRes = RetrofitClient.api().uploadImageToS3(uploadUrl, requestBody)

                        if (!uploadRes.isSuccessful) {
                            _eventFlow.emit(Event.ShowToast("이미지 업로드 실패"))
                            return@launch
                        }
                        // 업로드 성공! (키 전송 없이 업로드만 완료)
                    }
                }

                // 3-2. 프로필 텍스트 정보 수정 요청 (이미지 키 제외)
                val updateRes = RetrofitClient.api().updateProfile(request)

                if (updateRes.isSuccessful && updateRes.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.ShowToast("프로필이 수정되었습니다."))
                    fetchMypageData() // 데이터 갱신
                    _eventFlow.emit(Event.NavigateBack)
                } else {
                    _eventFlow.emit(Event.ShowToast(updateRes.body()?.message ?: "수정 실패"))
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "Error", e)
                _eventFlow.emit(Event.ShowToast("오류가 발생했습니다."))
            }
        }
    }
}