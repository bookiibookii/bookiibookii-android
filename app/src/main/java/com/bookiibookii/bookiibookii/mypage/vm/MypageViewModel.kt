package com.bookiibookii.bookiibookii.mypage.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.mypage.MypageReqDTO
import com.bookiibookii.bookiibookii.data.model.mypage.UpdateIntroductionReqDTO
import com.bookiibookii.bookiibookii.data.model.mypage.UserProfileResDTO
import com.bookiibookii.bookiibookii.onboarding.steps.model.NicknameCheckState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MypageViewModel : ViewModel() {

    private val _profileData = MutableLiveData<UserProfileResDTO>()
    val profileData: LiveData<UserProfileResDTO> get() = _profileData

    private val _isNicknameChecked = MutableLiveData<Boolean>(true)
    val isNicknameChecked: LiveData<Boolean> get() = _isNicknameChecked

    private val _nicknameCheckState = MutableLiveData<NicknameCheckState>(NicknameCheckState.Idle)
    val nicknameCheckState: LiveData<NicknameCheckState> get() = _nicknameCheckState

    var confirmedNickname: String? = null

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class Event {
        object NavigateBack : Event()
        data class ShowToast(val message: String, val isSuccess: Boolean = false) : Event()
        data class NicknameCheckResult(val isAvailable: Boolean, val message: String) : Event()
    }

    fun setNicknameChecked(isChecked: Boolean) {
        _isNicknameChecked.value = isChecked
    }

    fun fetchMypageData() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().getMypage()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()!!.result?.let {
                        _profileData.value = it
                        confirmedNickname = it.nickname

                        // 후기 날짜 로그
                        Log.d("MypageReview", "=== recentBookReviews (${it.recentBookReviews?.size ?: 0}개) ===")
                        it.recentBookReviews?.forEachIndexed { i, r ->
                            Log.d("MypageReview", "  [$i] bookTitle=${r.bookTitle}, tradeType=${r.tradeType}, rating=${r.rating}, reviewDate=${r.reviewDate}, comment=${r.comment}")
                        }
                        Log.d("MypageReview", "=== recentReceivedReviews (${it.recentReceivedReviews?.size ?: 0}개) ===")
                        it.recentReceivedReviews?.forEachIndexed { i, r ->
                            Log.d("MypageReview", "  [$i] reviewerNickname=${r.reviewerNickname}, reaction=${r.reaction}, comment=${r.comment}, createdAt=${r.createdAt}")
                        }
                    }
                } else {
                    _eventFlow.emit(Event.ShowToast("정보를 불러오지 못했습니다.", false))
                }
            } catch (e: Exception) {
                Log.e("MypageViewModel", "fetch error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다.", false))
            }
        }
    }

    fun checkNickname(nickname: String) {
        viewModelScope.launch {
            _nicknameCheckState.value = NicknameCheckState.Loading
            try {
                val response = RetrofitClient.userApi().postNicknameValidation(nickname)
                val serverMsg = response.body()?.message ?: "확인 불가"

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val isAvailable = response.body()?.result?.isAvailable ?: false

                    if (isAvailable) {
                        _isNicknameChecked.value = true
                        confirmedNickname = nickname
                        _nicknameCheckState.value = NicknameCheckState.Available("사용 가능한 닉네임입니다.")
                        _eventFlow.emit(Event.NicknameCheckResult(true, "사용 가능한 닉네임입니다."))
                    } else {
                        _isNicknameChecked.value = false
                        _nicknameCheckState.value = NicknameCheckState.Duplicated("이미 사용 중인 닉네임입니다.")
                        _eventFlow.emit(Event.NicknameCheckResult(false, "이미 사용 중인 닉네임입니다."))
                    }
                } else {
                    _isNicknameChecked.value = false
                    _nicknameCheckState.value = NicknameCheckState.Error(serverMsg)
                    _eventFlow.emit(Event.NicknameCheckResult(false, serverMsg))
                }
            } catch (e: Exception) {
                Log.e("NickCheck", "오류 발생", e)
                _nicknameCheckState.value = NicknameCheckState.Error("네트워크 오류")
                _eventFlow.emit(Event.ShowToast("네트워크 오류", false))
            }
        }
    }

    fun resetNicknameCheckState() {
        _nicknameCheckState.value = NicknameCheckState.Idle
    }

    fun updateIntroduction(introduction: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi()
                    .updateIntroduction(UpdateIntroductionReqDTO(introduction.ifBlank { null }))
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _profileData.value = _profileData.value?.copy(introduction = introduction.ifBlank { null })
                }
            } catch (e: Exception) {
                Log.e("MypageViewModel", "updateIntroduction error", e)
            }
        }
    }

    fun updateProfile(
        request: MypageReqDTO,
        imageFile: File?,
    ) {
        viewModelScope.launch {
            try {
                var finalRequest = request

                if (imageFile != null) {
                    val presignedRes = RetrofitClient.userApi().postPresignedUrl()

                    if (presignedRes.isSuccessful && presignedRes.body()?.isSuccess == true) {
                        val result = presignedRes.body()?.result

                        if (result != null) {
                            val uploadUrl = result.presignedPutUrl
                            val issuedS3Key = result.s3Key

                            val mimeType = "image/jpeg"
                            val requestBody = imageFile.asRequestBody(mimeType.toMediaTypeOrNull())
                            val cleanClient = okhttp3.OkHttpClient()

                            val requestS3 = okhttp3.Request.Builder()
                                .url(uploadUrl)
                                .put(requestBody)
                                .addHeader("Content-Type", mimeType)
                                .build()

                            val uploadRes = withContext(Dispatchers.IO) {
                                cleanClient.newCall(requestS3).execute()
                            }

                            if (!uploadRes.isSuccessful) {
                                _eventFlow.emit(Event.ShowToast("이미지 업로드에 실패했습니다.", false))
                                return@launch
                            }
                            finalRequest = request.copy(s3Key = issuedS3Key)
                        }
                    } else {
                        _eventFlow.emit(Event.ShowToast("이미지 서버 연결에 실패했습니다.", false))
                        return@launch
                    }
                }

                val updateRes = RetrofitClient.mypApi().updateProfile(finalRequest)

                if (updateRes.isSuccessful && updateRes.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.ShowToast("프로필이 성공적으로 수정되었습니다.", true))
                    fetchMypageData()
                    _eventFlow.emit(Event.NavigateBack)
                } else {
                    val msg = updateRes.body()?.message ?: "수정 실패"
                    _eventFlow.emit(Event.ShowToast(msg, false))
                }

            } catch (e: Exception) {
                Log.e("UpdateProfile", "Exception", e)
                _eventFlow.emit(Event.ShowToast("요청 중 오류가 발생했습니다.", false))
            }
        }
    }
}
