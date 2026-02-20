package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Context
import android.util.Base64
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.TokenRefreshRequest
import org.json.JSONObject

object TokenManager {

    private const val PREF = "auth_prefs"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_ONBOARDING = "onboarding_done"

    fun saveTokens(context: Context, access: String, refresh: String, userId: Int) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_ACCESS, access)
            putString(KEY_REFRESH, refresh)
            putInt(KEY_USER_ID, userId)
            apply()
        }
    }

    fun saveOnboardingDone(context: Context, onboardingDone: Boolean) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ONBOARDING, onboardingDone).apply()
    }

    fun hasAccessToken(context: Context): Boolean {
        return !getAccessToken(context).isNullOrEmpty()
    }

    fun isOnboardingDone(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING, false)
    }

    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACCESS, null)
    }

    fun getRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REFRESH, null)
    }

    /**
     * JWT payload의 exp 값을 디코딩하여 만료 여부를 판단합니다.
     * 파싱 실패 시 안전하게 "만료"로 처리합니다.
     */
    fun isAccessTokenExpired(accessToken: String): Boolean {
        return try {
            val parts = accessToken.split(".")
            if (parts.size < 2) return true

            val payloadBytes = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            val payloadJson = String(payloadBytes)

            val expSeconds = JSONObject(payloadJson).optLong("exp", 0L)
            if (expSeconds <= 0L) return true

            val nowSeconds = System.currentTimeMillis() / 1000L
            nowSeconds >= expSeconds
        } catch (e: Exception) {
            true
        }
    }

    /**
     * /api/auth/refresh 호출로 토큰을 재발급합니다.
     * 성공 시 SharedPreferences에 새 토큰 저장하고 true 반환합니다.
     */
    suspend fun refreshAccessToken(context: Context, refreshToken: String): Boolean {
        return try {
            val response = RetrofitClient.api().postRefresh(TokenRefreshRequest(refreshToken))

            if (!response.isSuccessful) return false
            val body = response.body() ?: return false
            if (!body.isSuccess) return false

            val result = body.result ?: return false
            saveTokens(context, result.accessToken, result.refreshToken, result.userId)

            true
        } catch (e: Exception) {
            false
        }
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}