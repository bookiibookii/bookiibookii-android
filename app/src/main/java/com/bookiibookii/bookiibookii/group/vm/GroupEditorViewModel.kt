package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.data.model.group.GroupCreateRequest
import com.bookiibookii.bookiibookii.data.model.group.GroupRuleRequest
import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddress
import com.bookiibookii.bookiibookii.data.model.location.ExchangeAddress
import com.bookiibookii.bookiibookii.group.model.ExchangeType
import com.bookiibookii.bookiibookii.group.model.GroupEditorUiState
import com.bookiibookii.bookiibookii.group.model.ReadingStyle
import com.bookiibookii.bookiibookii.group.model.SelectablePlace
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupEditorViewModel : ViewModel() {

    private val _state = MutableStateFlow(GroupEditorUiState())
    val state: StateFlow<GroupEditorUiState> = _state

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    // 그룹 생성 1회성 이벤트
    sealed class Event {
        data class Created(val groupId: Long?) : Event()
        data class ShowError(val message: String) : Event()
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
        // 교환 유형이 바뀌면 기존 주소 목록/선택을 비우고 새로 불러온다
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

    // 입력값으로 그룹 생성 요청. 결과는 eventFlow로 전달 (canSubmit이 보장하지만 안전 가드)
    fun createGroup() {
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

// 주소 + 상세주소를 한 줄로 합친다 (빈 값은 제외)
private fun joinAddress(address: String, detail: String): String =
    listOf(address, detail).filter { it.isNotBlank() }.joinToString(" ")
