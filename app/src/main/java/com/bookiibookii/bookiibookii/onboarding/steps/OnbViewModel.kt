package com.bookiibookii.bookiibookii.onboarding.steps

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.api.S3Uploader
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.data.model.user.OnboardingBook
import com.bookiibookii.bookiibookii.data.model.user.OnboardingRequest
import com.bookiibookii.bookiibookii.onboarding.steps.model.BookSearchState
import com.bookiibookii.bookiibookii.onboarding.steps.model.NicknameCheckState
import com.bookiibookii.bookiibookii.onboarding.steps.model.OnbState
import com.bookiibookii.bookiibookii.onboarding.steps.model.ProfileImageUploadState
import com.bookiibookii.bookiibookii.onboarding.steps.model.OnboardingSubmitState
import com.bookiibookii.bookiibookii.onboarding.steps.model.RecordMethod
import kotlinx.coroutines.launch

class OnbViewModel : ViewModel() {

    private val _state = MutableLiveData(OnbState())
    val state: LiveData<OnbState> = _state

    private val _nicknameCheckState =
        MutableLiveData<NicknameCheckState>(NicknameCheckState.Idle)
    val nicknameCheckState: LiveData<NicknameCheckState> = _nicknameCheckState

    private val _imageUploadState =
        MutableLiveData<ProfileImageUploadState>(ProfileImageUploadState.Idle)
    val imageUploadState: LiveData<ProfileImageUploadState> = _imageUploadState

    private val _bookSearchState =
        MutableLiveData<BookSearchState>(BookSearchState.Idle)
    val bookSearchState: LiveData<BookSearchState> = _bookSearchState

    private val _onboardingSubmitState =
        MutableLiveData<OnboardingSubmitState>(OnboardingSubmitState.Idle)
    val onboardingSubmitState: LiveData<OnboardingSubmitState> = _onboardingSubmitState

    private fun currentState(): OnbState = _state.value ?: OnbState()
    private fun updateState(newState: OnbState) { _state.value = newState }

    // ── Step 1: 프로필 ─────────────────────────────────────────────────────────

    fun setNickname(nickname: String) {
        updateState(currentState().copy(nickname = nickname))
        _nicknameCheckState.value = NicknameCheckState.Idle
    }

    fun setGender(gender: String) {
        updateState(currentState().copy(gender = gender))
    }

    fun setBirthdate(birthdate: String) {
        updateState(currentState().copy(birthdate = birthdate))
    }

    fun setProfileUri(uri: Uri) {
        updateState(currentState().copy(profileUri = uri))
    }

    fun checkNickname(nickname: String) {
        viewModelScope.launch {
            _nicknameCheckState.value = NicknameCheckState.Loading
            runCatching {
                RetrofitClient.userApi().postNicknameValidation(nickname)
            }.onSuccess { response ->
                if (!response.isSuccessful) {
                    _nicknameCheckState.value =
                        NicknameCheckState.Error("서버 오류가 발생했습니다. (${response.code()})")
                    return@onSuccess
                }
                val body = response.body()
                if (body?.isSuccess != true || body.result == null) {
                    _nicknameCheckState.value =
                        NicknameCheckState.Error(body?.message ?: "요청에 실패했습니다.")
                    return@onSuccess
                }
                val result = body.result
                val msg = result.message.ifBlank { "요청에 실패했습니다." }
                _nicknameCheckState.value = when (result.code) {
                    "SUCCESS" -> NicknameCheckState.Available(msg)
                    "DUPLICATE", "BAD_WORD" -> NicknameCheckState.Duplicated(msg)
                    else -> NicknameCheckState.Error(msg)
                }
            }.onFailure { e ->
                _nicknameCheckState.value =
                    NicknameCheckState.Error(e.message ?: "네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun uploadProfileImage(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            _imageUploadState.value = ProfileImageUploadState.Loading
            runCatching {
                val presignedRes = RetrofitClient.userApi().postPresignedUrl()
                if (!presignedRes.isSuccessful) error("Presigned URL 발급 실패 (HTTP ${presignedRes.code()})")
                val body = presignedRes.body()
                if (body?.isSuccess != true || body.result == null) error(body?.message ?: "Presigned URL 발급 실패")
                val s3Key = body.result.s3Key
                val putUrl = body.result.presignedPutUrl
                S3Uploader.uploadImage(contentResolver, uri, putUrl).getOrThrow()
                s3Key
            }.onSuccess { s3Key ->
                updateState(currentState().copy(profileS3Key = s3Key))
                _imageUploadState.value = ProfileImageUploadState.Success(s3Key)
            }.onFailure { e ->
                _imageUploadState.value =
                    ProfileImageUploadState.Error(e.message ?: "이미지 업로드에 실패했습니다.")
            }
        }
    }

    // ── Step 2: 인생 책 ────────────────────────────────────────────────────────

    fun setLifeBook(slotIndex: Int, book: BookItem) {
        val books = currentState().lifeBooks.toMutableList()
        // 빈 슬롯에 추가하는 경우 클릭한 위치와 무관하게 항상 첫 번째 빈 칸을 채움
        val targetIndex = if (books[slotIndex] == null) {
            books.indexOfFirst { it == null }.takeIf { it >= 0 } ?: slotIndex
        } else {
            slotIndex
        }
        books[targetIndex] = book
        updateState(currentState().copy(lifeBooks = books))
    }

    fun removeLifeBook(slotIndex: Int) {
        val books = currentState().lifeBooks.toMutableList()
        books[slotIndex] = null
        // 중간을 삭제해도 남은 책을 앞으로 당김
        val packed = books.filterNotNull()
        val result = (packed + List(books.size - packed.size) { null })
        updateState(currentState().copy(lifeBooks = result))
    }

    fun searchBooks(query: String) {
        if (query.isBlank()) {
            _bookSearchState.value = BookSearchState.Idle
            return
        }
        viewModelScope.launch {
            _bookSearchState.value = BookSearchState.Loading
            runCatching {
                RetrofitClient.grpApi().searchBooks(query)
            }.onSuccess { response ->
                val body = response.body()
                if (body?.isSuccess == true && body.result != null) {
                    _bookSearchState.value = BookSearchState.Success(body.result.books)
                } else {
                    _bookSearchState.value =
                        BookSearchState.Error(body?.message ?: "검색에 실패했습니다.")
                }
            }.onFailure { e ->
                _bookSearchState.value =
                    BookSearchState.Error(e.message ?: "네트워크 오류가 발생했습니다.")
            }
        }
    }

    fun clearBookSearch() {
        _bookSearchState.value = BookSearchState.Idle
    }

    // ── Step 3: 기록 방식 ──────────────────────────────────────────────────────

    fun toggleRecordMethod(method: RecordMethod) {
        val cur = currentState()
        if (method == RecordMethod.ANY) {
            val wasSelected = RecordMethod.ANY in cur.recordMethods
            updateState(cur.copy(
                recordMethods = if (wasSelected) emptySet() else setOf(RecordMethod.ANY),
                isUnknownMethod = false
            ))
        } else {
            val next = cur.recordMethods.toMutableSet()
            next.remove(RecordMethod.ANY)
            if (!next.add(method)) next.remove(method)
            updateState(cur.copy(recordMethods = next, isUnknownMethod = false))
        }
    }

    fun toggleUnknownMethod() {
        val cur = currentState()
        val wasUnknown = cur.isUnknownMethod
        updateState(cur.copy(
            isUnknownMethod = !wasUnknown,
            recordMethods = if (!wasUnknown) emptySet() else cur.recordMethods
        ))
    }

    // ── Step 4: 한 문장 ────────────────────────────────────────────────────────

    fun setSelfIntro(text: String) {
        updateState(currentState().copy(selfIntro = text))
    }

    // ── 온보딩 제출 ────────────────────────────────────────────────────────────

    fun submitOnboarding() {
        val state = currentState()
        viewModelScope.launch {
            _onboardingSubmitState.value = OnboardingSubmitState.Loading
            runCatching {
                RetrofitClient.userApi().postOnboarding(
                    OnboardingRequest(
                        name = state.nickname,
                        gender = state.gender,
                        birth = state.birthdate,
                        tags = state.recordMethods.map { it.serverValue }.distinct(),
                        s3Key = state.profileS3Key,
                        userBooks = state.lifeBooks.filterNotNull().map { OnboardingBook(it.isbn13) },
                        introduction = state.selfIntro
                    )
                )
            }.onSuccess { response ->
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _onboardingSubmitState.value = OnboardingSubmitState.Success
                } else {
                    _onboardingSubmitState.value =
                        OnboardingSubmitState.Error(response.body()?.message ?: "오류가 발생했습니다.")
                }
            }.onFailure { e ->
                _onboardingSubmitState.value =
                    OnboardingSubmitState.Error(e.message ?: "네트워크 오류가 발생했습니다.")
            }
        }
    }
}
