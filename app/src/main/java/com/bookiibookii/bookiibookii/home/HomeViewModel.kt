package com.bookiibookii.bookiibookii.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val _nickname = MutableLiveData<String>()
    val nickname: LiveData<String> = _nickname

    init {
        // 캐시된 닉네임 즉시 표시
        TokenManager.getNickname(app)?.let { _nickname.value = it }
        // 백그라운드에서 최신 닉네임 갱신
        fetchNickname()
    }

    private fun fetchNickname() {
        viewModelScope.launch {
            runCatching {
                RetrofitClient.mypApi().getMypage()
            }.onSuccess { response ->
                val nickname = response.body()?.result?.nickname ?: return@onSuccess
                _nickname.value = nickname
                TokenManager.saveNickname(getApplication(), nickname)
            }
        }
    }
}
