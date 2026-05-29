package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.data.model.group.GroupCreateRequest
import com.bookiibookii.bookiibookii.data.model.group.GroupModifyRequest
import com.bookiibookii.bookiibookii.data.model.group.GroupRule
import com.bookiibookii.bookiibookii.data.model.group.GroupRuleRequest
import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddress
import com.bookiibookii.bookiibookii.data.model.location.ExchangeAddress
import com.bookiibookii.bookiibookii.group.model.ExchangeType
import com.bookiibookii.bookiibookii.group.model.GroupEditorUiState
import com.bookiibookii.bookiibookii.group.model.ReadingStyle
import com.bookiibookii.bookiibookii.group.model.SelectablePlace
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupEditorViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // 수정 진입 시 라우트로 전달된 그룹 id (생성 진입이면 null)
    private val groupId: Long? =
        savedStateHandle.get<String?>(GroupDestinations.ARG_GROUP_ID)?.toLongOrNull()

    private val _state = MutableStateFlow(GroupEditorUiState(isEdit = groupId != null))
    val state: StateFlow<GroupEditorUiState> = _state

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    // 그룹 생성/수정 1회성 이벤트
    sealed class Event {
        data class Created(val groupId: Long?) : Event()   // 생성 성공
        data class Updated(val groupId: Long) : Event()    // 수정 성공
        data class ShowError(val message: String) : Event()
        // 수정 모드 진입 시 기존 그룹 데이터 프리필 실패 — 토스트 + 뒤로가기
        data class PrefillFailed(val message: String) : Event()
    }

    init {
        // 수정 모드면 기존 그룹 데이터를 폼에 프리필
        groupId?.let { loadGroupForEdit(it) }
    }

    // 수정 진입 시 기존 그룹 데이터를 폼에 채움
    private fun loadGroupForEdit(id: Long) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.grpApi().getGroupDetail(id)
                val detail = res.body()?.result
                if (res.isSuccessful && res.body()?.isSuccess == true && detail != null) {
                    // 수정 가능한 필드만 프리필 (도서/교환유형/주소는 수정 화면에서 숨김)
                    val periodIndex = GroupEditorUiState.PERIODS
                        .indexOf(detail.readingPeriod).coerceAtLeast(0)
                    val (ruleStyle, customRules) = splitRules(detail.rules)
                    _state.update {
                        it.copy(
                            groupName = detail.groupName,
                            readingPeriodIndex = periodIndex,
                            groupComment = detail.groupComment.orEmpty(),
                            ruleStyle = ruleStyle,
                            customRules = customRules,
                        )
                    }
                } else {
                    _eventFlow.emit(Event.PrefillFailed("그룹 정보를 불러오지 못했어요"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.PrefillFailed("네트워크 오류가 발생했어요"))
            }
        }
    }

    // apiTag "All_ROUNDER" 등 서버/로컬 케이스 차이를 대비해 대소문자 무시 매칭
    private fun splitRules(rules: List<GroupRule>): Pair<ReadingStyle?, List<String>> {
        val style = rules.firstNotNullOfOrNull { rule ->
            ReadingStyle.values().firstOrNull { it.apiTag.equals(rule.tag, ignoreCase = true) }
        }
        val customs = rules.filter { it.tag.equals("CUSTOM", ignoreCase = true) }.map { it.content }
        return style to customs
    }

    // 입력 텍스트 변경. 텍스트를 수정하면 기존 선택은 해제
    fun onBookSearchQueryChange(value: String) =
        _state.update { it.copy(bookSearchQuery = value, isbn13 = null) }

    // ic_search 클릭 또는 키보드 검색 액션으로 호출
    fun searchBooks() {
        val q = _state.value.bookSearchQuery
        if (q.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(bookSearchLoading = true, bookSearchError = null) }
            try {
                val res = RetrofitClient.grpApi().searchBooks(q, page = 1, size = 10)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    _state.update {
                        it.copy(
                            bookSearchResults = res.body()?.result?.books.orEmpty(),
                            bookSearchLoading = false,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(bookSearchError = "검색에 실패했어요", bookSearchLoading = false)
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(bookSearchError = "네트워크 오류가 발생했어요", bookSearchLoading = false)
                }
            }
        }
    }

    fun onBookSelect(book: BookItem) = _state.update {
        it.copy(
            isbn13 = book.isbn13,
            bookSearchQuery = book.title,
            bookSearchResults = emptyList(),
        )
    }

    // ic_x 클릭. 텍스트 모두 초기화
    fun onClearBookSearch() = _state.update {
        it.copy(
            bookSearchQuery = "",
            isbn13 = null,
            bookSearchResults = emptyList(),
            bookSearchError = null,
        )
    }

    fun onGroupNameChange(value: String) =
        _state.update { it.copy(groupName = value) }

    fun onTradeTypeSelect(type: ExchangeType) {
        // 교환 유형이 바뀌면 기존 주소 목록/선택을 비우고 새로 불러옴
        _state.update { it.copy(tradeType = type, places = emptyList(), selectedPlaceId = null) }
        loadPlaces(type)
    }

    // 교환 유형에 맞는 주소 목록을 불러오고 대표(isDefault) 주소를 기본 선택
    private fun loadPlaces(type: ExchangeType) {
        viewModelScope.launch {
            _state.update { it.copy(placesLoading = true) }
            try {
                val api = RetrofitClient.locationApi()
                val places = when (type) {
                    ExchangeType.DELIVERY -> {
                        val res = api.getDeliveries()
                        if (res.isSuccessful && res.body()?.isSuccess == true) {
                            res.body()?.result.orEmpty().map { it.toSelectablePlace() }
                        } else {
                            emptyList()
                        }
                    }
                    ExchangeType.DIRECT -> {
                        val res = api.getExchanges()
                        if (res.isSuccessful && res.body()?.isSuccess == true) {
                            res.body()?.result.orEmpty().map { it.toSelectablePlace() }
                        } else {
                            emptyList()
                        }
                    }
                }
                val defaultId = (places.firstOrNull { it.isDefault } ?: places.firstOrNull())?.id
                _state.update {
                    it.copy(places = places, selectedPlaceId = defaultId, placesLoading = false)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(places = emptyList(), selectedPlaceId = null, placesLoading = false)
                }
            }
        }
    }

    fun onPlaceSelect(id: Long) =
        _state.update { it.copy(selectedPlaceId = id) }

    fun onReadingPeriodSelect(index: Int) =
        _state.update { it.copy(readingPeriodIndex = index) }

    fun onRuleStyleSelect(style: ReadingStyle) =
        _state.update { it.copy(ruleStyle = style) }

    fun onGroupCommentChange(value: String) =
        _state.update { it.copy(groupComment = value) }

    fun onAddCustomRule() = _state.update {
        if (it.customRules.size >= GroupEditorUiState.MAX_CUSTOM_RULES) it
        else it.copy(customRules = it.customRules + "")
    }

    fun onCustomRuleChange(index: Int, value: String) = _state.update {
        it.copy(
            customRules = it.customRules.toMutableList()
                .apply { if (index in indices) this[index] = value },
        )
    }

    fun onRemoveCustomRule(index: Int) = _state.update {
        it.copy(customRules = it.customRules.filterIndexed { i, _ -> i != index })
    }

    // 생성/수정 공통 제출 진입점 — 수정 모드(groupId 있음)면 PATCH, 아니면 생성
    fun submit() {
        val id = groupId
        if (id != null) updateGroup(id) else createGroup()
    }

    // 입력값으로 그룹 생성 요청. 결과는 eventFlow로 전달
    private fun createGroup() {
        val s = _state.value
        val isbn13 = s.isbn13 ?: return
        val tradeType = s.tradeType ?: return
        val placeId = s.selectedPlaceId ?: return
        val ruleStyle = s.ruleStyle ?: return
        if (s.submitting) return
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            try {
                val rules = buildList {
                    add(GroupRuleRequest(tag = ruleStyle.apiTag, content = null))
                    s.customRules.filter { it.isNotBlank() }
                        .forEach { add(GroupRuleRequest(tag = "CUSTOM", content = it)) }
                }
                val request = GroupCreateRequest(
                    isbn13 = isbn13,
                    groupName = s.groupName,
                    tradeType = tradeType.name,
                    selectedPlaceId = placeId,
                    readingPeriod = s.readingPeriod,
                    groupComment = s.groupComment.ifBlank { null },
                    rules = rules,
                )
                val res = RetrofitClient.grpApi().createGroup(request)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.Created(res.body()?.result?.groupId))
                } else {
                    _eventFlow.emit(Event.ShowError(res.body()?.message ?: "그룹 생성에 실패했어요"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowError("네트워크 오류가 발생했어요"))
            } finally {
                _state.update { it.copy(submitting = false) }
            }
        }
    }

    // 입력값으로 그룹 수정 요청 — PATCH /api/groups/{groupId} (그룹명/독서기간/소개/규칙)
    private fun updateGroup(id: Long) {
        val s = _state.value
        val ruleStyle = s.ruleStyle ?: return
        if (s.submitting) return
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            try {
                val rules = buildList {
                    add(GroupRuleRequest(tag = ruleStyle.apiTag, content = null))
                    s.customRules.filter { it.isNotBlank() }
                        .forEach { add(GroupRuleRequest(tag = "CUSTOM", content = it)) }
                }
                val request = GroupModifyRequest(
                    readingPeriod = s.readingPeriod,
                    groupComment = s.groupComment.ifBlank { null },
                    groupName = s.groupName,
                    rules = rules,
                )
                val res = RetrofitClient.grpApi().modifyGroup(id, request)
                if (res.isSuccessful && res.body()?.isSuccess == true) {
                    // 성공 → 수정한 그룹 상세로 이동 (응답 id 없으면 진입 id)
                    _eventFlow.emit(Event.Updated(res.body()?.result?.groupId ?: id))
                } else {
                    _eventFlow.emit(Event.ShowError(res.body()?.message ?: "그룹 수정에 실패했어요"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(Event.ShowError("네트워크 오류가 발생했어요"))
            } finally {
                _state.update { it.copy(submitting = false) }
            }
        }
    }
}

private fun ExchangeAddress.toSelectablePlace() = SelectablePlace(
    id = id,
    placeName = placeName,
    address = joinAddress(address, addressDetail),
    isDefault = isDefault,
)

private fun DeliveryAddress.toSelectablePlace() = SelectablePlace(
    id = id,
    placeName = placeName,
    address = joinAddress(address, addressDetail),
    isDefault = isDefault,
)

// 주소 + 상세주소 한 줄로 합침
private fun joinAddress(address: String, detail: String?): String =
    listOfNotNull(address, detail?.takeIf { it.isNotBlank() }).joinToString(" ")
