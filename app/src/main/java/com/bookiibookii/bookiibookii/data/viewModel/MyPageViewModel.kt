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

    // 2. 닉네임 중복 확인 상태
    private val _isNicknameChecked = MutableLiveData<Boolean>(true)
    val isNicknameChecked: LiveData<Boolean> get() = _isNicknameChecked

    // [추가] 검증 완료된 닉네임 저장소 (화면 이동 후 복귀 시 상태 유지용)
    var confirmedNickname: String? = null

    // 3. 이벤트 처리
    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class Event {
        object NavigateBack : Event()
        data class ShowToast(val message: String) : Event()
        data class NicknameCheckResult(val isAvailable: Boolean, val message: String) : Event()
    }

    // 상태 변경 함수
    fun setNicknameChecked(isChecked: Boolean) {
        _isNicknameChecked.value = isChecked
    }

    // --- API 호출 함수들 ---

    // 1. 마이페이지 정보 불러오기
    fun fetchMypageData() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getMypage()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()!!.result?.let {
                        _profileData.value = it
                        // 초기 데이터를 불러오면, 현재 닉네임은 '검증된 상태'임
                        confirmedNickname = it.nickname
                    }
                } else {
                    _eventFlow.emit(Event.ShowToast("정보를 불러오지 못했습니다."))
                }
            } catch (e: Exception) {
                Log.e("MyPageViewModel", "fetch error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    // 2. 닉네임 중복 확인
    fun checkNickname(nickname: String) {
        viewModelScope.launch {
            try {
                // API 명세에 따라 파라미터가 String인지 DTO인지 확인 필요 (여기선 String 가정)
                // 만약 DTO라면: checkNickname(NicknameCheckRequest(nickname)) 사용
                val response = RetrofitClient.api().postNicknameValidation(nickname)

                val serverMsg = response.body()?.message ?: "확인 불가"

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _isNicknameChecked.value = true
                    confirmedNickname = nickname
                    // [수정] 성공 메시지 전달
                    _eventFlow.emit(Event.NicknameCheckResult(true, serverMsg))
                } else {
                    _isNicknameChecked.value = false
                    // [수정] 실패 메시지 전달
                    _eventFlow.emit(Event.NicknameCheckResult(false, serverMsg))
                }
            } catch (e: Exception) {
                Log.e("MyPageViewModel", "CheckNick Error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류"))
            }
        }
    }

    // 3. 프로필 수정 (이미지 S3 업로드 -> 정보 수정 PATCH)
    fun updateProfile(
        request: UserUpdateRequest, // 텍스트 정보
        imageFile: File?            // 변경할 이미지 파일 (없으면 null)
    ) {
        viewModelScope.launch {
            try {
                var finalRequest = request

                // 3-1. 이미지가 있다면 S3 업로드 진행 (주석 해제 및 로직 연결)
//                if (imageFile != null) {
//                    Log.d("UpdateProfile", "1. Presigned URL 발급 요청 (POST)")
//
//                    val presignedRes = RetrofitClient.api().postPresignedUrl()
//
//                    if (presignedRes.isSuccessful && presignedRes.body()?.isSuccess == true) {
//                        val result = presignedRes.body()!!.result
//
//                        if (result != null) {
//                            val uploadUrl = result.presignedPutUrl
//                            val issuedS3Key = result.s3Key // 서버가 발급해준 키
//
//                            Log.d("UpdateProfile", "2. URL 획득 완료. S3 업로드 시작")
//
//                            // S3에 실제 이미지 업로드 (PUT)
//                            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
//                            val uploadRes = RetrofitClient.api().uploadImageToS3(uploadUrl, requestBody)
//
//                            if (!uploadRes.isSuccessful) {
//                                Log.e("UpdateProfile", "S3 업로드 실패: ${uploadRes.code()}")
//                                _eventFlow.emit(Event.ShowToast("이미지 업로드 실패"))
//                                return@launch
//                            }
//                            Log.d("UpdateProfile", "3. S3 업로드 성공. Key: $issuedS3Key")
//
//                            // ★★★ [핵심] 발급받은 키를 request에 담기 ★★★
//                            // UserUpdateRequest DTO에 userImage 필드가 존재해야 함
//                            finalRequest = request.copy(userImage = issuedS3Key)
//                        }
//                    } else {
//                        Log.e("UpdateProfile", "Presigned URL 발급 실패: ${presignedRes.code()}")
//                        _eventFlow.emit(Event.ShowToast("이미지 서버 연결 실패"))
//                        return@launch
//                    }
//                }

                // 3-2. 프로필 텍스트 정보 수정 요청 (PATCH)
                Log.d("UpdateProfile", "4. 최종 수정 요청 전송: $finalRequest")
                val updateRes = RetrofitClient.api().updateProfile(finalRequest)

                if (updateRes.isSuccessful && updateRes.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.ShowToast("프로필이 수정되었습니다."))
                    fetchMypageData() // 데이터 갱신
                    _eventFlow.emit(Event.NavigateBack)
                } else {
                    val errorBody = updateRes.errorBody()?.string()
                    val msg = updateRes.body()?.message ?: "수정 실패"
                    Log.e("UpdateProfile", "수정 실패: $msg / Body: $errorBody")
                    _eventFlow.emit(Event.ShowToast(msg))
                }

            } catch (e: Exception) {
                Log.e("UpdateProfile", "Exception", e)
                _eventFlow.emit(Event.ShowToast("오류가 발생했습니다."))
            }
        }
    }
}