package com.bookiibookii.bookiibookii.group.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.group.main.GroupData
import kotlinx.coroutines.launch

data class PopularSearchItem(val rank: Int, val keyword: String)

class GrpSearchViewModel : ViewModel() {

    // Properties - Popular Search
    private var popularFullList: List<PopularSearchItem> = emptyList()

    private val _displayList = MutableLiveData<List<PopularSearchItem>>()
    val displayList: LiveData<List<PopularSearchItem>> get() = _displayList

    private val _isExpanded = MutableLiveData(false)
    val isExpanded: LiveData<Boolean> get() = _isExpanded


    // Properties - Search Result
    private val _searchResult = MutableLiveData<List<GroupData>>()
    val searchResult: LiveData<List<GroupData>> get() = _searchResult


    init {
        loadPopularKeywords()
    }

    // Popular Keywords Logic
    private fun loadPopularKeywords() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getPopularKeywords()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val rawResult = response.body()?.result ?: emptyList()

                    popularFullList = rawResult.mapIndexed { index, keyword ->
                        PopularSearchItem(rank = index + 1, keyword = keyword)
                    }
                    updatePopularList()
                } else {
                    Log.e("GrpSearchVM", "인기검색어 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GrpSearchVM", "인기검색어 에러", e)
            }
        }
    }

    fun toggleExpansion() {
        _isExpanded.value = !(_isExpanded.value ?: false)
        updatePopularList()
    }

    private fun updatePopularList() {
        _displayList.value = if (_isExpanded.value == true) popularFullList else popularFullList.take(3)
    }


   // Group Search Logic
    fun searchGroups(query: String, sortType: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().searchGroups(keyword = query, sort = sortType)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val dtoList = response.body()?.result?.groupList ?: emptyList()

                    // DTO -> UI Model 변환 후 post
                    _searchResult.value = dtoList.map { it.toUiModel() }
                    Log.d("GrpSearchVM", "검색 성공: ${dtoList.size}건")
                } else {
                    Log.e("GrpSearchVM", "검색 실패: ${response.code()}")
                    _searchResult.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("GrpSearchVM", "검색 에러", e)
                _searchResult.value = emptyList()
            }
        }
    }
}