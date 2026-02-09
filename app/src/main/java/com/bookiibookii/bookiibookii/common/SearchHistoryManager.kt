package com.bookiibookii.bookiibookii.common

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class SearchHistoryManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    private val KEY_HISTORY = "history_list"
    private val MAX_SIZE = 10 // 최대 10개까지만 저장

    // 1. 저장된 검색어 리스트 가져오기
    fun getHistoryList(): ArrayList<String> {
        val jsonString = prefs.getString(KEY_HISTORY, null) ?: return arrayListOf()
        val list = ArrayList<String>()

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // 2. 검색어 추가하기 (중복 제거 & 최신순 정렬)
    fun addHistory(keyword: String) {
        val list = getHistoryList()

        // 이미 있으면 삭제 (맨 앞으로 보내기 위해)
        if (list.contains(keyword)) {
            list.remove(keyword)
        }

        // 맨 앞에 추가
        list.add(0, keyword)

        // 10개 넘으면 뒤에꺼 삭제
        if (list.size > MAX_SIZE) {
            list.removeAt(list.size - 1)
        }

        saveList(list)
    }

    // 3. 검색어 삭제하기 (X 버튼)
    fun removeHistory(keyword: String) {
        val list = getHistoryList()
        if (list.contains(keyword)) {
            list.remove(keyword)
            saveList(list)
        }
    }

    // 4. 전체 삭제
    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    // 내부 저장 로직 (List -> JSON String 변환)
    private fun saveList(list: ArrayList<String>) {
        val jsonArray = JSONArray()
        for (item in list) {
            jsonArray.put(item)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }
}