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
    private const val KEY_READING_CARD_COACH_MARK = "reading_card_coach_mark_done"
    private const val KEY_TRACKER_COMMENT_COACH_MARK = "tracker_comment_coach_mark_done"

    @Volatile private var encryptedPrefs: SharedPreferences? = null

    private fun prefs(context: Context): SharedPreferences {
        encryptedPrefs?.let { return it }
        return synchronized(this) {
            encryptedPrefs ?: run {
                val appContext = context.applicationContext
                val created = try {
                    buildEncryptedPrefs(appContext)
                } catch (e: Exception) {
                    // 백업 복원 등으로 마스터키-keyset 불일치(AEADBadTagException) → 복호화 불가
                    // 깨진 prefs 파일을 삭제하고 재생성하여 자가복구
                    // (복원된 토큰은 어차피 못 쓰는 값이라 버리고 로그인 화면으로 정상 진입)
                    appContext.deleteSharedPreferences(PREF)
                    buildEncryptedPrefs(appContext)
                }
                encryptedPrefs = created
                created
            }
        }
    }

    private fun buildEncryptedPrefs(appContext: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            appContext,
            PREF,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveTokens(context: Context, access: String, refresh: String, userId: Long) {
        prefs(context).edit().apply {
            putString(KEY_ACCESS, access)
            putString(KEY_REFRESH, refresh)
            putLong(KEY_USER_ID, userId)
            apply()
        }
    }

    fun saveOnboardingDone(context: Context, onboardingDone: Boolean) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING, onboardingDone).apply()
    }

    fun isReadingCardCoachMarkDone(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_READING_CARD_COACH_MARK, false)
    }

    fun saveReadingCardCoachMarkDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_READING_CARD_COACH_MARK, true).apply()
    }

    fun isTrackerCommentCoachMarkDone(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_TRACKER_COMMENT_COACH_MARK, false)
    }

    fun saveTrackerCommentCoachMarkDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_TRACKER_COMMENT_COACH_MARK, true).apply()
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

    // 로그인된 사용자 id. 저장 안 됐으면 null.
    // (임시) 댓글 본인 여부 비교용. 백엔드가 isMe 필드 추가하면 제거 예정
    fun getUserId(context: Context): Long? {
        val id = prefs(context).getLong(KEY_USER_ID, -1L)
        return if (id == -1L) null else id
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
