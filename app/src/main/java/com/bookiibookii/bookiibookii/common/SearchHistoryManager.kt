package com.bookiibookii.bookiibookii.common

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class SearchHistoryManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
    private val KEY_HISTORY = "history_list"
    private val MAX_SIZE = 10

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

    // 중복 검색어는 맨 앞으로 옮겨 최근 검색 순서를 유지한다.
    fun addHistory(keyword: String) {
        val list = getHistoryList()

        // 이미 있으면 삭제 (맨 앞으로 보내기 위해)
        if (list.contains(keyword)) {
            list.remove(keyword)
        }

        list.add(0, keyword)

        if (list.size > MAX_SIZE) {
            list.removeAt(list.size - 1)
        }

        saveList(list)
    }

    fun removeHistory(keyword: String) {
        val list = getHistoryList()
        if (list.contains(keyword)) {
            list.remove(keyword)
            saveList(list)
        }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveList(list: ArrayList<String>) {
        val jsonArray = JSONArray()
        for (item in list) {
            jsonArray.put(item)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }
}