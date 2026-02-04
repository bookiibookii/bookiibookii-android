package com.bookiibookii.bookiibookii.onboarding.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.LoginRequest
import com.bookiibookii.bookiibookii.data.model.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> get() = _loginResult

    fun postLogin(kakaoAccessToken: String) {
        viewModelScope.launch {
            try {
                val request = LoginRequest(socialType = "KAKAO", token = kakaoAccessToken)

                val response = RetrofitClient.getInstance(getApplication()).postLogin(request)

                if (response.isSuccessful) {
                    _loginResult.value = response.body()
                    Log.d("SERVER_LOGIN", "성공: ${response.body()}")
                } else {
                    Log.e("SERVER_LOGIN", "실패 코드: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SERVER_LOGIN", "에러 발생: ", e)
            }
        }
    }
}