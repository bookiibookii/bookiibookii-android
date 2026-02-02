package com.bookiibookii.bookiibookii.onboarding.login
import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.bookiibookii.bookiibookii.BuildConfig

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY)

        val keyHash = Utility.getKeyHash(this)
        Log.e("KAKAO_KEYHASH", keyHash)

    }
}