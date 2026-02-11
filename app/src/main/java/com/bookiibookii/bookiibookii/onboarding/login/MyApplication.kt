package com.bookiibookii.bookiibookii.onboarding.login

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.bookiibookii.bookiibookii.BuildConfig
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
        KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY)

        val keyHash = Utility.getKeyHash(this)
        Log.e("KAKAO_KEYHASH", keyHash)

        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}