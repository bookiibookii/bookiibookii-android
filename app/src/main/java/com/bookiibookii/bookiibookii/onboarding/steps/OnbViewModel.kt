package com.bookiibookii.bookiibookii.onboarding.steps

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.common.observeSearchQuery
import com.bookiibookii.bookiibookii.common.runCatchingCancellable
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

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

    // 실시간 도서 검색: UI 입력은 이 쿼리만 갱신하고, 실제 호출은 디바운스(공통 헬퍼)가 담당
    private val _bookSearchQuery = MutableStateFlow("")

    init {
        observeSearchQuery(
            queryFlow = _bookSearchQuery,
            onBelowMinLength = { _bookSearchState.value = BookSearchState.Idle },
            onSearch = { performBookSearch(it) },
        )
    }

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

    fun clearProfileUri() {
        updateState(currentState().copy(profileUri = null))
        _imageUploadState.value = ProfileImageUploadState.Idle
    }

    fun checkNickname(nickname: String) {
        viewModelScope.launch {
            _nicknameCheckState.value = NicknameCheckState.Loading
            runCatching {
                RetrofitClient.userApi().postNicknameValidation(nickname)
            }.onSuccess { response ->
                if (!response.isSuccessful) {
                    _nicknameCheckState.value = NicknameCheckState.SystemError
                    return@onSuccess
                }
                val body = response.body()
                if (body?.isSuccess != true || body.result == null) {
                    _nicknameCheckState.value = NicknameCheckState.SystemError
                    return@onSuccess
                }
                val result = body.result
                val msg = result.message.ifBlank { "요청에 실패했습니다." }
                _nicknameCheckState.value = when (result.code) {
                    "SUCCESS" -> NicknameCheckState.Available(msg)
                    "DUPLICATE", "BAD_WORD" -> NicknameCheckState.Duplicated(msg)
                    else -> NicknameCheckState.SystemError
                }
            }.onFailure {
                _nicknameCheckState.value = NicknameCheckState.NetworkError
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
                    ProfileImageUploadState.Error(e.toUserMessage("이미지 업로드에 실패했습니다."))
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

    // UI 입력 콜백 — 쿼리만 갱신하면 디바운스 후 performBookSearch가 호출된다
    fun onBookSearchQueryChange(query: String) {
        _bookSearchQuery.value = query
    }

    // ic_search 클릭/키보드 검색 — 디바운스 기다리지 않고 현재 쿼리로 바로 검색
    fun searchBooks() {
        val query = _bookSearchQuery.value.trim()
        if (query.isBlank()) return
        viewModelScope.launch { performBookSearch(query) }
    }

    private suspend fun performBookSearch(query: String) {
        _bookSearchState.value = BookSearchState.Loading
        // 취소(쿼리 변경·화면 이탈)는 오류가 아니므로 runCatching 대신 사용
        runCatchingCancellable {
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
                BookSearchState.Error(e.toUserMessage("네트워크 오류가 발생했습니다."))
        }
    }

    fun clearBookSearch() {
        _bookSearchQuery.value = ""
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
        // 더블탭 가드 — 이미 제출 중이면 중복 POST 차단
        if (_onboardingSubmitState.value is OnboardingSubmitState.Loading) return
        val state = currentState()
        viewModelScope.launch {
            _onboardingSubmitState.value = OnboardingSubmitState.Loading
            runCatching {
                RetrofitClient.userApi().postOnboarding(
                    OnboardingRequest(
                        name = state.nickname,
                        gender = state.gender,
                        birth = state.birthdate,
                        // "잘 모르겠어요"(isUnknownMethod)는 recordMethods가 비므로 서버 NO_IDEA로 매핑.
                        // 서버 tags는 required + minItems 1이라 빈 배열이면 400.
                        tags = if (state.isUnknownMethod) {
                            listOf("NO_IDEA")
                        } else {
                            state.recordMethods.map { it.serverValue }.distinct()
                        },
                        s3Key = state.profileS3Key,
                        userBooks = state.lifeBooks.filterNotNull().map { OnboardingBook(it.isbn13) },
                        introduction = state.selfIntro
                    )
                )
            }.onSuccess { response ->
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _onboardingSubmitState.value = OnboardingSubmitState.Success
                } else {
                    // 실패 응답(주로 400/404) — errorBody에서 사용자용 사유를 추출해 토스트로 노출
                    val msg = parseSubmitErrorMessage(response.errorBody()?.string())
                    _onboardingSubmitState.value = OnboardingSubmitState.Error(msg)
                }
            }.onFailure {
                // 네트워크 예외 → AuthInterceptor가 전역 에러화면을 이미 띄움. fallback 문구만.
                _onboardingSubmitState.value =
                    OnboardingSubmitState.Error("네트워크 연결을 확인한 뒤 다시 시도해주세요.")
            }
        }
    }
}

/**
 * 예외 메시지를 토스트에 표시하기 적합한 형태로 변환합니다.
 *
 * 내부에서 throw한 오류 메시지는 "한글 설명: 기술 상세" 형태이므로
 * 콜론(:) 앞의 한글 부분만 추출해서 보여줍니다.
 * - "S3 업로드 실패: HTTP 403" → "S3 업로드 실패"
 * - "Unable to resolve host ...": 한글 없음 → fallback 반환
 */
private fun Throwable.toUserMessage(fallback: String): String {
    val raw = message ?: return fallback
    val beforeColon = raw.substringBefore(":").trim()
    // 콜론 앞에 한글이 있을 때만 분할 결과 사용 (영문 기술 메시지는 fallback 처리)
    return if (beforeColon.any { it in '\uAC00'..'\uD7A3' }) beforeColon else fallback
}

/**
 * \uC628\uBCF4\uB529 \uC81C\uCD9C \uC2E4\uD328 \uC751\uB2F5 body\uC5D0\uC11C \uC0AC\uC6A9\uC790\uC5D0\uAC8C \uBCF4\uC5EC\uC904 \uC0AC\uC720\uB97C \uCD94\uCD9C\uD569\uB2C8\uB2E4.
 *
 * \uC11C\uBC84 \uC751\uB2F5 \uD615\uC2DD(ApiResponse)\uC5D0 \uB530\uB77C \uC0AC\uC720 \uC704\uCE58\uAC00 \uB2E4\uB985\uB2C8\uB2E4.
 * - \uBE44\uC988\uB2C8\uC2A4 \uC5D0\uB7EC(UserException \uB4F1): \uC0AC\uC720\uAC00 \uCD5C\uC0C1\uC704 "message"\uC5D0 \uB2F4\uAE40
 *   \uC608) {"isSuccess":false,"code":"USER400_2","message":"\uC774\uBBF8 \uC0AC\uC6A9 \uC911\uC778 \uB2C9\uB124\uC784\uC785\uB2C8\uB2E4.","result":null}
 * - @Valid \uAC80\uC99D \uC5D0\uB7EC(COMMON400_1): \uCD5C\uC0C1\uC704 "message"\uB294 \uC81C\uB124\uB9AD("\uC798\uBABB\uB41C \uC694\uCCAD\uC785\uB2C8\uB2E4.")\uC774\uACE0
 *   \uC2E4\uC81C \uD544\uB4DC \uC0AC\uC720\uAC00 "result" \uBC30\uC5F4\uC5D0 \uB2F4\uAE30\uBBC0\uB85C \uC774\uB97C \uC6B0\uC120 \uC0AC\uC6A9
 *   \uC608) {"isSuccess":false,"code":"COMMON400_1","message":"\uC798\uBABB\uB41C \uC694\uCCAD\uC785\uB2C8\uB2E4.","result":["\uC131\uBCC4\uC740 \uD544\uC218 \uC785\uB825 \uC0AC\uD56D\uC785\uB2C8\uB2E4."]}
 */
private fun parseSubmitErrorMessage(errorBody: String?): String {
    val fallback = "\uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694."
    if (errorBody.isNullOrBlank()) return fallback
    return try {
        val json = JSONObject(errorBody)
        val result = json.optJSONArray("result")
        val fromResult = result?.takeIf { it.length() > 0 }?.optString(0)?.takeIf { it.isNotBlank() }
        fromResult ?: json.optString("message").takeIf { it.isNotBlank() } ?: fallback
    } catch (_: Exception) {
        fallback
    }
}
