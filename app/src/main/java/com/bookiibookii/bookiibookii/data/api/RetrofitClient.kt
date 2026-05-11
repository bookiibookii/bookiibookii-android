package com.bookiibookii.bookiibookii.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // main 도메인 주소
//    private const val BASE_URL = "https://bookiibookii.gyeonseo.com/"

    // dev 도메인 주소
    private const val BASE_URL = "https://bookii.gyeonseo.com/"

    private lateinit var authedRetrofit: Retrofit
    private lateinit var noAuthRetrofit: Retrofit

    @Volatile private var initialized = false

    fun init(context: Context) {
        if (initialized) return

        synchronized(this) {
            if (initialized) return

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authedClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(AuthInterceptor(context.applicationContext))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            authedRetrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(authedClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val noAuthClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            noAuthRetrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(noAuthClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            authApi = authedRetrofit.create(AuthApi::class.java)
            authApiNoAuth = noAuthRetrofit.create(AuthApi::class.java)
            grpApi = authedRetrofit.create(GrpApi::class.java)
            trkApi = authedRetrofit.create(TrkApi::class.java)
            mypApi = authedRetrofit.create(MypApi::class.java)
            libApi = authedRetrofit.create(LibApi::class.java)
            notiApi = authedRetrofit.create(NotiApi::class.java)
            userApi = authedRetrofit.create(UserApi::class.java)
            recmApi = authedRetrofit.create(RecmApi::class.java)
            kwdApi = authedRetrofit.create(KwdApi::class.java)

            initialized = true
        }
    }

    private fun check() {
        if (!initialized) error("RetrofitClient.init(context) 먼저 호출해야 함")
    }

    private lateinit var authApi: AuthApi
    private lateinit var authApiNoAuth: AuthApi
    private lateinit var grpApi: GrpApi
    private lateinit var trkApi: TrkApi
    private lateinit var mypApi: MypApi
    private lateinit var libApi: LibApi
    private lateinit var notiApi: NotiApi
    private lateinit var userApi: UserApi
    private lateinit var recmApi: RecmApi
    private lateinit var kwdApi: KwdApi

    fun authApi(): AuthApi { check(); return authApi }
    fun authApiNoAuth(): AuthApi { check(); return authApiNoAuth }
    fun grpApi(): GrpApi { check(); return grpApi }
    fun trkApi(): TrkApi { check(); return trkApi }
    fun mypApi(): MypApi { check(); return mypApi }
    fun libApi(): LibApi { check(); return libApi }
    fun notiApi(): NotiApi { check(); return notiApi }
    fun userApi(): UserApi { check(); return userApi }
    fun recmApi(): RecmApi { check(); return recmApi }
    fun kwdApi(): KwdApi { check(); return kwdApi }
}
