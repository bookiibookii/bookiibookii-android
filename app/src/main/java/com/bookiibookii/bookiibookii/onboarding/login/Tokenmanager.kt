package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.auth.TokenRefreshRequest
import org.json.JSONObject

object TokenManager {

    // 평문 "auth_prefs" → 암호화 저장
    private const val PREF = "auth_prefs_enc"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_ONBOARDING = "onboarding_done"
    private const val KEY_NICKNAME = "nickname"

    @Volatile private var encryptedPrefs: SharedPreferences? = null

    private fun prefs(context: Context): SharedPreferences {
        encryptedPrefs?.let { return it }
        return synchronized(this) {
            encryptedPrefs ?: run {
                val appContext = context.applicationContext
                val masterKey = MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                val created = EncryptedSharedPreferences.create(
                    appContext,
                    PREF,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                encryptedPrefs = created
                created
            }
        }
    }

    fun saveTokens(context: Context, access: String, refresh: String, userId: Int) {
        prefs(context).edit().apply {
            putString(KEY_ACCESS, access)
            putString(KEY_REFRESH, refresh)
            putInt(KEY_USER_ID, userId)
            apply()
        }
    }

    fun saveOnboardingDone(context: Context, onboardingDone: Boolean) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING, onboardingDone).apply()
    }

    fun saveNickname(context: Context, nickname: String) {
        prefs(context).edit().putString(KEY_NICKNAME, nickname).apply()
    }

    fun getNickname(context: Context): String? {
        return prefs(context).getString(KEY_NICKNAME, null)
    }

    fun hasAccessToken(context: Context): Boolean {
        return !getAccessToken(context).isNullOrEmpty()
    }

    fun isOnboardingDone(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ONBOARDING, false)
    }

    fun getAccessToken(context: Context): String? {
        return prefs(context).getString(KEY_ACCESS, null)
    }

    fun getRefreshToken(context: Context): String? {
        return prefs(context).getString(KEY_REFRESH, null)
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
            if (refreshToken.isBlank()) return false

            val accessToken = getAccessToken(context)?.takeIf { it.isNotBlank() } ?: return false

            val response = RetrofitClient.authApiNoAuth().postRefresh(
                authorization = "Bearer $accessToken",
                request = TokenRefreshRequest(refreshToken)
            )

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
        prefs(context).edit().clear().apply()
    }
}
