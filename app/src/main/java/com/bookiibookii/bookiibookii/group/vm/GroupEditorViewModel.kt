package com.bookiibookii.bookiibookii.group.vm

import androidx.lifecycle.ViewModel
import com.bookiibookii.bookiibookii.group.model.ExchangeType
import com.bookiibookii.bookiibookii.group.model.GroupEditorUiState
import com.bookiibookii.bookiibookii.group.model.ReadingStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class GroupEditorViewModel : ViewModel() {

    private val _state = MutableStateFlow(GroupEditorUiState())
    val state: StateFlow<GroupEditorUiState> = _state

    fun onGroupNameChange(value: String) =
        _state.update { it.copy(groupName = value) }

    fun onTradeTypeSelect(type: ExchangeType) =
        _state.update { it.copy(tradeType = type) }

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
}
