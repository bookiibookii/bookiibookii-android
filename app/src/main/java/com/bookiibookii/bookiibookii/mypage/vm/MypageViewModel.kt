package com.bookiibookii.bookiibookii.mypage.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.api.S3Uploader
import com.bookiibookii.bookiibookii.data.model.mypage.MypageReqDTO
import com.bookiibookii.bookiibookii.data.model.mypage.ReceivedReviewItem
import com.bookiibookii.bookiibookii.data.model.mypage.UpdateIntroductionReqDTO
import com.bookiibookii.bookiibookii.data.model.mypage.UserProfileResDTO
import com.bookiibookii.bookiibookii.data.model.mypage.WrittenReviewItem
import com.bookiibookii.bookiibookii.onboarding.steps.model.NicknameCheckState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File

private const val PAGE_SIZE = 10

data class WrittenReviewUiState(
    val items: List<WrittenReviewItem> = emptyList(),
    val totalCount: Long = 0,
    val page: Int = 0,
    val hasNext: Boolean = false,
    val isLoading: Boolean = false,
)

data class ReceivedReviewUiState(
    val items: List<ReceivedReviewItem> = emptyList(),
    val positiveCount: Long = 0,
    val page: Int = 0,
    val hasNext: Boolean = false,
    val isLoading: Boolean = false,
)

class MypageViewModel : ViewModel() {

    private val _profileData = MutableLiveData<UserProfileResDTO>()
    val profileData: LiveData<UserProfileResDTO> get() = _profileData

    private val _writtenReviews = MutableLiveData(WrittenReviewUiState())
    val writtenReviews: LiveData<WrittenReviewUiState> get() = _writtenReviews

    private val _receivedReviews = MutableLiveData(ReceivedReviewUiState())
    val receivedReviews: LiveData<ReceivedReviewUiState> get() = _receivedReviews

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
                    }
                } else {
                    _eventFlow.emit(Event.ShowToast("정보를 불러오지 못했습니다.", false))
                }
            } catch (e: Exception) {
                Log.e("UpdateProfile", "fetch error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다.", false))
            }
        }
    }

    fun fetchWrittenReviews(reset: Boolean) {
        val state = _writtenReviews.value ?: WrittenReviewUiState()
        if (state.isLoading) return
        if (!reset && !state.hasNext) return
        val nextPage = if (reset) 0 else state.page + 1
        _writtenReviews.value = state.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().getWrittenReviews(page = nextPage, size = PAGE_SIZE)
                val result = response.body()?.result
                if (response.isSuccessful && response.body()?.isSuccess == true && result != null) {
                    val base = if (reset) emptyList() else (_writtenReviews.value?.items ?: emptyList())
                    _writtenReviews.value = WrittenReviewUiState(
                        items = base + result.content,
                        totalCount = result.totalCount,
                        page = result.pageInfo.page,
                        hasNext = result.pageInfo.hasNext,
                        isLoading = false,
                    )
                } else {
                    _writtenReviews.value = _writtenReviews.value?.copy(isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("UpdateProfile", "fetchWrittenReviews error", e)
                _writtenReviews.value = _writtenReviews.value?.copy(isLoading = false)
            }
        }
    }

    fun fetchReceivedReviews(reset: Boolean) {
        val state = _receivedReviews.value ?: ReceivedReviewUiState()
        if (state.isLoading) return
        if (!reset && !state.hasNext) return
        val nextPage = if (reset) 0 else state.page + 1
        _receivedReviews.value = state.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().getReceivedReviews(page = nextPage, size = PAGE_SIZE)
                val result = response.body()?.result
                if (response.isSuccessful && response.body()?.isSuccess == true && result != null) {
                    val base = if (reset) emptyList() else (_receivedReviews.value?.items ?: emptyList())
                    _receivedReviews.value = ReceivedReviewUiState(
                        items = base + result.content,
                        positiveCount = result.positiveCount,
                        page = result.pageInfo.page,
                        hasNext = result.pageInfo.hasNext,
                        isLoading = false,
                    )
                } else {
                    _receivedReviews.value = _receivedReviews.value?.copy(isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("UpdateProfile", "fetchReceivedReviews error", e)
                _receivedReviews.value = _receivedReviews.value?.copy(isLoading = false)
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

    // 소개/프로필 저장 진행 중 여부 — 더블탭으로 중복 저장/중복 네비를 차단
    private var profileMutating = false

    fun updateIntroduction(introduction: String) {
        if (profileMutating) return
        profileMutating = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi()
                    .updateIntroduction(UpdateIntroductionReqDTO(introduction.ifBlank { null }))
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _profileData.value = _profileData.value?.copy(introduction = introduction.ifBlank { null })
                }
            } catch (e: Exception) {
                Log.e("UpdateProfile", "updateIntroduction error", e)
            } finally {
                profileMutating = false
            }
        }
    }

    fun updateProfile(
        request: MypageReqDTO,
        imageFile: File?,
    ) {
        if (profileMutating) return
        profileMutating = true
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

                            val uploadResult = S3Uploader.uploadImage(imageFile, uploadUrl)
                            if (uploadResult.isFailure) {
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
            } finally {
                profileMutating = false
            }
        }
    }
}
