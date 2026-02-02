package com.bookiibookii.bookiibookii.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class PopularSearchItem(val rank: Int, val keyword: String)

class GrpSearchViewModel : ViewModel() {

    private val _fullList = listOf(
        PopularSearchItem(1, "한강"),
        PopularSearchItem(2, "자몽살구클럽"),
        PopularSearchItem(3, "최강록"),
        PopularSearchItem(4, "채사장"),
        PopularSearchItem(5, "지적대화를위한넓고얕은지식"),
        PopularSearchItem(6, "트렌드코리아 2026"),
        PopularSearchItem(7, "김영하"),
        PopularSearchItem(8, "천선란"),
        PopularSearchItem(9, "물고기는 존재하지 않는다"),
        PopularSearchItem(10, "소년이 온다")
    )

    private val _displayList = MutableLiveData<List<PopularSearchItem>>()
    val displayList: LiveData<List<PopularSearchItem>> get() = _displayList

    private val _isExpanded = MutableLiveData(false)
    val isExpanded: LiveData<Boolean> get() = _isExpanded

    init {
        updateList()
    }

    fun toggleExpansion() {
        _isExpanded.value = !(_isExpanded.value ?: false)
        updateList()
    }

    private fun updateList() {
        if (_isExpanded.value == true) {
            _displayList.value = _fullList
        } else {
            _displayList.value = _fullList.take(3)
        }
    }
}