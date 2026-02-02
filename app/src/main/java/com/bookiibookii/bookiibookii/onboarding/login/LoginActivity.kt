package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.data.model.LoginRequest
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.RetrofitClient
import com.bookiibookii.bookiibookii.onboarding.profile.OnbProfileActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
    private var isNavigating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. 자동 로그인 체크 (토큰이 이미 있으면 메인으로)
        if (hasAccessToken()) {
            moveToMain()
            return
        }

        // 2. Credential Manager 초기화
        credentialManager = CredentialManager.create(this)

        setupLoginButtons()
    }

    private fun setupLoginButtons() {
        val kakaoButton = findViewById<View>(R.id.btn_kakao_login)
        setupButtonUI(kakaoButton, "카카오로 시작하기", R.drawable.ic_kakao, R.color.kakao, R.color.grey_900) {
            Toast.makeText(this, "카카오 로그인은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }

        val googleButton = findViewById<View>(R.id.btn_google_login)
        setupButtonUI(googleButton, "구글로 시작하기", R.drawable.ic_google, R.color.grey_100, R.color.grey_900) {
            showLoadingState(true)
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e("Login", "로그인 실패 또는 취소: ${e.message}")
                if (!e.type.contains("Cancellation")) {
                    Log.e("Login", "로그인 실패", e)
                }
                showLoadingState(false)
            } catch (e: Exception) {
                Log.e("Login", "예상치 못한 오류", e)
                showLoadingState(false)
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential

        // 구글 인증 정보인지 확인
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                // 토큰 추출
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                Log.d("Login", "Google ID Token 획득 성공: $idToken")

                // 백엔드로 전송
                sendTokenToBackend(idToken)
            } catch (e: Exception) {
                Log.e("Login", "인증 정보 파싱 실패", e)
                showLoadingState(false)
            }
        } else {
            Log.e("Login", "알 수 없는 인증 타입: ${credential.type}")
            showLoadingState(false)
        }
    }

    private fun sendTokenToBackend(idToken: String) {
        lifecycleScope.launch {
            try {
                val request = LoginRequest(socialType = "GOOGLE", token = idToken)
                val response = RetrofitClient.apiService.postLogin(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    if (result != null) {
                        Log.d("Login", "백엔드 로그인 성공! UserID: ${result.userId}")

                        // 토큰 저장
                        saveTokens(result.accessToken, result.refreshToken, result.userId)

                        // 화면 이동
                        onLoginSuccess()
                    } else {
                        showLoadingState(false)
                    }
                } else {
                    Log.e("Login", "백엔드 에러: ${response.code()} ${response.errorBody()?.string()}")
                    showLoadingState(false)
                }
            } catch (e: Exception) {
                Log.e("Login", "네트워크 오류", e)
                showLoadingState(false)
            }
        }
    }

    // --- 유틸리티 함수들 ---

    private fun onLoginSuccess() {

        moveToMain()
    }

    private fun moveToMain() {
        val intent = Intent(this, OnbProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoadingState(isLoading: Boolean) {
        val buttonsGroup = findViewById<Group>(R.id.group_login_buttons)

        if (isLoading) {
            buttonsGroup.visibility = View.GONE
            // progressBar?.visibility = View.VISIBLE
        } else {
            buttonsGroup.visibility = View.VISIBLE
            // progressBar?.visibility = View.GONE
        }
    }

    private fun setupButtonUI(root: View, text: String, iconRes: Int, bgRes: Int, textRes: Int, onClick: () -> Unit) {
        val card = root as MaterialCardView
        val tv = root.findViewById<TextView>(R.id.tv_login_text)
        val iv = root.findViewById<ImageView>(R.id.iv_login_icon)

        tv.text = text
        tv.setTextColor(getColor(textRes))
        iv.setImageResource(iconRes)
        card.setCardBackgroundColor(getColor(bgRes))

        root.setOnClickListener {
            if (!isNavigating) onClick()
        }
    }

    private fun saveTokens(access: String, refresh: String, userId: Int) {
        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("access_token", access)
            putString("refresh_token", refresh)
            putInt("user_id", userId)
            apply()
        }
    }

    private fun hasAccessToken(): Boolean {
        val prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return !prefs.getString("access_token", null).isNullOrEmpty()
    }
}