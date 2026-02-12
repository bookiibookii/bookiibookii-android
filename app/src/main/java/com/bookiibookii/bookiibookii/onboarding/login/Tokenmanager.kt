package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Context

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
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return !prefs.getString(KEY_ACCESS, null).isNullOrEmpty()
    }

    fun isOnboardingDone(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING, false)
    }

    fun getRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REFRESH, null)
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}