package com.bookiibookii.bookiibookii.library.vm

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.api.S3Uploader
import com.bookiibookii.bookiibookii.data.model.library.MemberCardCreateRequestDTO
import com.bookiibookii.bookiibookii.library.ui.AddCardMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryAddCardUiState(
    val isLoading: Boolean = false,
)

class LibraryAddCardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryAddCardUiState())
    val uiState: StateFlow<LibraryAddCardUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<AddCardEvent>()
    val event: SharedFlow<AddCardEvent> = _event.asSharedFlow()

    sealed interface AddCardEvent {
        object Success : AddCardEvent
        data class Error(val message: String) : AddCardEvent
    }

    fun createCard(
        memberBookId: Int,
        mode: AddCardMode,
        page: Int,
        quotation: String,
        memo: String,
        imageUri: Uri?,
        contentResolver: ContentResolver,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cardType = if (mode == AddCardMode.PHOTO) "IMAGE" else "TEXT"
                var s3Key = ""

                if (mode == AddCardMode.PHOTO && imageUri != null) {
                    val urlResponse = RetrofitClient.libApi().postPresignedUrl(memberBookId)
                    if (!urlResponse.isSuccessful || urlResponse.body()?.isSuccess != true) {
                        _uiState.update { it.copy(isLoading = false) }
                        _event.emit(AddCardEvent.Error("이미지 업로드 URL을 가져오지 못했습니다."))
                        return@launch
                    }
                    val presignedData = urlResponse.body()?.result!!
                    s3Key = presignedData.s3Key

                    val uploadResult = S3Uploader.uploadImage(contentResolver, imageUri, presignedData.presignedPutUrl)
                    if (uploadResult.isFailure) {
                        _uiState.update { it.copy(isLoading = false) }
                        _event.emit(AddCardEvent.Error("이미지 업로드에 실패했습니다."))
                        return@launch
                    }
                }

                val request = MemberCardCreateRequestDTO(
                    cardType              = cardType,
                    quotation             = quotation,
                    s3Key                 = s3Key,
                    page                  = page,
                    memo                  = memo,
                    quotationValidForText = mode == AddCardMode.TEXT && quotation.isNotBlank(),
                    s3KeyValidForImage    = mode == AddCardMode.PHOTO && s3Key.isNotBlank(),
                )

                val response = RetrofitClient.libApi().createCard(memberBookId, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(AddCardEvent.Success)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(AddCardEvent.Error("독서카드 등록에 실패했습니다."))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.emit(AddCardEvent.Error("네트워크 오류가 발생했습니다."))
            }
        }
    }
}
