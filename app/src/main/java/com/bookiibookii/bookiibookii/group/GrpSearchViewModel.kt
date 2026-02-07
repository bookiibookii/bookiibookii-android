package com.bookiibookii.bookiibookii.group

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import kotlinx.coroutines.launch

data class PopularSearchItem(val rank: Int, val keyword: String)

class GrpSearchViewModel : ViewModel() {

    // 1. 인기 검색어 관련 변수
    private var popularFullList: List<PopularSearchItem> = emptyList()

    private val _displayList = MutableLiveData<List<PopularSearchItem>>()
    val displayList: LiveData<List<PopularSearchItem>> get() = _displayList

    private val _isExpanded = MutableLiveData(false)
    val isExpanded: LiveData<Boolean> get() = _isExpanded

    // 2. ★ 그룹 검색 결과 변수 (UI에서 쓸 GroupData 리스트)
    private val _searchResult = MutableLiveData<List<GroupData>>()
    val searchResult: LiveData<List<GroupData>> get() = _searchResult

    init {
        loadPopularKeywords()
    }

    // --- [기능 1] 인기 검색어 가져오기 ---
    private fun loadPopularKeywords() {
        viewModelScope.launch {
            try {
                // .api() 인지 .api 인지 RetrofitClient 설정에 따라 맞춰주세요
                val response = RetrofitClient.api().getPopularKeywords()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val rawResult = response.body()?.result ?: emptyList()

                    // String -> PopularSearchItem 변환
                    popularFullList = rawResult.mapIndexed { index, keyword ->
                        PopularSearchItem(rank = index + 1, keyword = keyword)
                    }
                    updatePopularList()
                } else {
                    Log.e("GrpSearchViewModel", "인기검색어 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GrpSearchViewModel", "인기검색어 에러", e)
            }
        }
    }

    fun toggleExpansion() {
        _isExpanded.value = !(_isExpanded.value ?: false)
        updatePopularList()
    }

    private fun updatePopularList() {
        if (_isExpanded.value == true) {
            _displayList.value = popularFullList
        } else {
            _displayList.value = popularFullList.take(3)
        }
    }


    // --- [기능 2] 그룹 검색하기 (NEW) ---
    fun searchGroups(query: String, sortType: String) {
        viewModelScope.launch {
            try {
                // API 호출
                val response = RetrofitClient.api().searchGroups(
                    keyword = query,
                    sort = sortType
                )

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val resultDto = response.body()?.result

                    // DTO 리스트 가져오기
                    val dtoList = resultDto?.groupList ?: emptyList()

                    // ★ 핵심: DTO -> UI Model(GroupData) 변환
                    val uiList = dtoList.map { dto ->
                        dto.toUiModel()
                    }

                    // LiveData 업데이트 -> Activity가 감지해서 화면 갱신
                    _searchResult.value = uiList

                    Log.d("GrpSearchViewModel", "검색 성공: ${uiList.size}건")
                } else {
                    Log.e("GrpSearchViewModel", "검색 실패: ${response.code()}")
                    _searchResult.value = emptyList() // 실패 시 빈 리스트
                }
            } catch (e: Exception) {
                Log.e("GrpSearchViewModel", "검색 네트워크 오류", e)
                _searchResult.value = emptyList()
            }
        }
    }
}