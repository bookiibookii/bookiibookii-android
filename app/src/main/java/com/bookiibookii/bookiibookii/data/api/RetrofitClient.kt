package com.bookiibookii.bookiibookii.data.api

import android.content.Context
import com.bookiibookii.bookiibookii.trkData.api.TrkApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://bookii.gyeonseo.com/"

    @Volatile private var apiService: ApiService? = null
    @Volatile private var apiServiceNoAuth: ApiService? = null
    @Volatile private var trkService: TrkApi? = null

    fun init(context: Context) {
        if (apiService != null && apiServiceNoAuth != null && trkService != null) return

        synchronized(this) {
            if (apiService != null && apiServiceNoAuth != null && trkService != null) return

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authedClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(AuthInterceptor(context.applicationContext))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(authedClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
            trkService = retrofit.create(TrkApi::class.java)

            val noAuthClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofitNoAuth = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(noAuthClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiServiceNoAuth = retrofitNoAuth.create(ApiService::class.java)
        }
    }

    fun api(): ApiService =
        apiService ?: error("RetrofitClient.init(context) 먼저 호출해야 함")

    fun apiNoAuth(): ApiService =
        apiServiceNoAuth ?: error("RetrofitClient.init(context) 먼저 호출해야 함")

    fun trkApi(): TrkApi =
        trkService ?: error("RetrofitClient.init(context) 먼저 호출해야 함")
}