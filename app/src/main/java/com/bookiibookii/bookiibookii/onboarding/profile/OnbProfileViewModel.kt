package com.bookiibookii.bookiibookii.onboarding.profile

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.api.S3Uploader
import kotlinx.coroutines.launch

class OnbProfileViewModel : ViewModel() {

    // 닉네임 중복 검사 상태 관리
    private val _nicknameState = MutableLiveData<NicknameCheckState>(NicknameCheckState.Idle)
    val nicknameState: LiveData<NicknameCheckState> = _nicknameState

    // 프로필 이미지 업로드 상태 관리
    private val _imageUploadState =
        MutableLiveData<ProfileImageUploadState>(ProfileImageUploadState.Idle)
    val imageUploadState: LiveData<ProfileImageUploadState> = _imageUploadState

    // 온보딩 저장 API에 전달할 S3 Key 보관용
    private val _profileS3Key = MutableLiveData<String?>(null)
    val profileS3Key: LiveData<String?> = _profileS3Key

    // 닉네임 중복 검사 요청
    fun checkNickname(nickname: String) {
        viewModelScope.launch {
            _nicknameState.value = NicknameCheckState.Loading

            runCatching {
                RetrofitClient.api().postNicknameValidation(nickname)
            }.onSuccess { response ->
                if (!response.isSuccessful) {
                    _nicknameState.value =
                        NicknameCheckState.Error("서버 오류가 발생했습니다. (${response.code()})")
                    return@onSuccess
                }

                val body = response.body()
                if (body?.isSuccess != true) {
                    _nicknameState.value =
                        NicknameCheckState.Error(body?.message ?: "요청에 실패했습니다.")
                    return@onSuccess
                }

                val available = body.result?.isAvailable == true
                _nicknameState.value = if (available) {
                    NicknameCheckState.Available("사용 가능한 닉네임입니다.")
                } else {
                    NicknameCheckState.Duplicated("이미 존재하는 닉네임입니다.")
                }

            }.onFailure { e ->
                _nicknameState.value =
                    NicknameCheckState.Error(e.message ?: "네트워크 오류가 발생했습니다.")
            }
        }
    }

    // 프로필 이미지 업로드 (Presigned URL + S3 PUT)
    fun uploadProfileImage(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            _imageUploadState.value = ProfileImageUploadState.Loading

            // TODO: 추후 로그 삭제
            android.util.Log.d("IMG_UPLOAD", "🚀 uploadProfileImage start, uri=$uri")

            runCatching {
                // TODO: 추후 로그 삭제
                android.util.Log.d("IMG_UPLOAD", "📡 request presigned-url")

                val presignedRes = RetrofitClient.api().postPresignedUrl()
                if (!presignedRes.isSuccessful) {
                    error("Presigned URL 발급 실패 (HTTP ${presignedRes.code()})")
                }

                val body = presignedRes.body()
                if (body?.isSuccess != true || body.result == null) {
                    error(body?.message ?: "Presigned URL 발급 실패")
                }

                val s3Key = body.result.s3Key
                val putUrl = body.result.presignedPutUrl

                // TODO: 추후 로그 삭제
                android.util.Log.d("IMG_UPLOAD", "🔑 s3Key=$s3Key")

                // TODO: 추후 로그 삭제
                android.util.Log.d("IMG_UPLOAD", "☁️ S3 PUT start")

                S3Uploader.uploadImage(contentResolver, uri, putUrl).getOrThrow()

                // TODO: 추후 로그 삭제
                android.util.Log.d("IMG_UPLOAD", "✅ S3 PUT success")

                s3Key
            }.onSuccess { s3Key ->
                // TODO: 추후 로그 삭제
                android.util.Log.d("IMG_UPLOAD", "🎉 state=Success")

                _profileS3Key.value = s3Key
                _imageUploadState.value = ProfileImageUploadState.Success(s3Key)
            }.onFailure { e ->
                _imageUploadState.value =
                    ProfileImageUploadState.Error(e.message ?: "이미지 업로드에 실패했습니다.")

                // TODO: 추후 로그 삭제
                android.util.Log.e("IMG_UPLOAD", "💥 state=Error", e)
            }
        }
    }
}