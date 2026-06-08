package com.bookiibookii.bookiibookii.library.feat

import android.content.Context

// 비로그인 공유 링크 진입 시 shareToken을 임시 저장.
// 로그인 체인이 CLEAR_TASK라 백스택 복귀가 불가 → 로그인 완료 후 MainActivity에서 consume해 뷰어로 복귀.
object PendingShareToken {

    private const val PREF = "share_pending"
    private const val KEY = "share_token"

    fun save(context: Context, token: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, token)
            .apply()
    }

    // 저장된 토큰을 반환하고 즉시 비운다(1회성).
    fun consume(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY, null)
        if (token != null) prefs.edit().remove(KEY).apply()
        return token
    }
}
