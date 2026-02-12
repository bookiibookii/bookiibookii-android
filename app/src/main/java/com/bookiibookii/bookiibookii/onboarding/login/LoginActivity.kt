package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.AuthInterceptor
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.LoginRequest
import com.bookiibookii.bookiibookii.data.model.MypageResult
import com.bookiibookii.bookiibookii.onboarding.profile.OnbProfileActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.card.MaterialCardView
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
    private var isNavigating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 공통 에러/로그아웃 라우팅 잠금 해제
        AuthInterceptor.unlockRouting()

        setContentView(R.layout.activity_login)

        // TODO: 추후 로그 삭제 (온보딩/토큰 분기 디버깅용)
        Log.d(
            "ONB_FLOW",
            "token=${TokenManager.hasAccessToken(this)} done=${TokenManager.isOnboardingDone(this)}"
        )

        // 자동 로그인 분기
        if (routeAutoLoginIfPossible()) return

        credentialManager = CredentialManager.create(this)

        bindLoginButtons()

        // TODO: 추후 로그 삭제 (카카오 키해시 확인용)
        Log.e("KeyHash_Check", "내 앱의 현재 키 해시: ${com.kakao.sdk.common.KakaoSdk.keyHash}")
    }

    private fun routeAutoLoginIfPossible(): Boolean {
        if (!TokenManager.hasAccessToken(this)) return false

        if (TokenManager.isOnboardingDone(this)) {
            moveToMain()
        } else {
            moveToOnboarding()
        }
        return true
    }

    private fun bindLoginButtons() {
        val kakaoButton = findViewById<View>(R.id.btn_kakao_login)
        val googleButton = findViewById<View>(R.id.btn_google_login)

        setupButtonUI(
            root = kakaoButton,
            text = "카카오로 시작하기",
            iconRes = R.drawable.ic_kakao,
            bgRes = R.color.kakao,
            textRes = R.color.grey_900
        ) {
            if (isNavigating) return@setupButtonUI
            showLoadingState(true)
            loginToKakao()
        }

        setupButtonUI(
            root = googleButton,
            text = "구글로 시작하기",
            iconRes = R.drawable.ic_google,
            bgRes = R.color.grey_100,
            textRes = R.color.grey_900
        ) {
            if (isNavigating) return@setupButtonUI
            showLoadingState(true)
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val webClientId = getString(R.string.web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
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
                handleGoogleSignIn(result)
            } catch (e: GetCredentialException) {
                // TODO: 추후 로그 삭제 (Credential Manager 예외 확인용)
                Log.e("Login", "Credential Manager 에러: ${e.message}", e)
                showLoadingState(false)
            } catch (e: Exception) {
                // TODO: 추후 로그 삭제 (예상치 못한 예외 확인용)
                Log.e("Login", "예상치 못한 에러", e)
                showLoadingState(false)
            }
        }
    }

    private fun handleGoogleSignIn(result: GetCredentialResponse) {
        val credential = result.credential

        val isGoogleToken =
            credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL

        if (!isGoogleToken) {
            // TODO: 추후 로그 삭제 (인증 타입 분기 확인용)
            Log.e("Login", "알 수 없는 인증 타입: ${credential.type}")
            showLoadingState(false)
            return
        }

        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            sendTokenToBackend(socialType = "GOOGLE", token = idToken)
        } catch (e: Exception) {
            // TODO: 추후 로그 삭제 (구글 토큰 파싱 실패 확인용)
            Log.e("Login", "인증 정보 파싱 실패", e)
            showLoadingState(false)
        }
    }

    private fun loginToKakao() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                // TODO: 추후 로그 삭제 (카카오 로그인 실패 원인 확인용)
                Log.e("KakaoLogin", "카카오 로그인 실패", error)
                showLoadingState(false)
            } else if (token != null) {
                sendTokenToBackend("KAKAO", token.accessToken)
            } else {
                showLoadingState(false)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    // TODO: 추후 로그 삭제 (카카오톡 앱 로그인 실패 원인 확인용)
                    Log.e("KakaoLogin", "카카오톡 앱 로그인 실패", error)

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        showLoadingState(false)
                        return@loginWithKakaoTalk
                    }

                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    return@loginWithKakaoTalk
                }

                if (token != null) {
                    sendTokenToBackend(socialType = "KAKAO", token = token.accessToken)
                } else {
                    showLoadingState(false)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun sendTokenToBackend(socialType: String, token: String) {
        // TODO: 추후 로그 삭제 (서버 전송 파라미터 확인용)
        Log.d("CheckToken", "[보내는 타입]=$socialType, tokenLength=${token.length}")

        lifecycleScope.launch {
            try {
                val request = LoginRequest(socialType = socialType, token = token)
                val response = RetrofitClient.api().postLogin(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    if (result != null) {
                        TokenManager.saveTokens(
                            this@LoginActivity,
                            result.accessToken,
                            result.refreshToken,
                            result.userId
                        )
                        TokenManager.saveOnboardingDone(this@LoginActivity, result.onboardingDone)

                        onLoginSuccess()
                    } else {
                        showLoadingState(false)
                    }
                } else {
                    // TODO: 추후 로그 삭제 (서버 응답 실패 디버깅용)
                    Log.e("Login", "백엔드 에러: ${response.code()} ${response.errorBody()?.string()}")
                    showLoadingState(false)
                }
            } catch (e: Exception) {
                // TODO: 추후 로그 삭제 (네트워크/예외 디버깅용)
                Log.e("Login", "네트워크 오류", e)
                showLoadingState(false)
            }
        }
    }

    private fun onLoginSuccess() {
        isNavigating = true
        if (TokenManager.isOnboardingDone(this)) {
            moveToMain()
        } else {
            moveToOnboarding()
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        val buttonsGroup = findViewById<Group>(R.id.group_login_buttons)
        buttonsGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun setupButtonUI(
        root: View,
        text: String,
        iconRes: Int,
        bgRes: Int,
        textRes: Int,
        onClick: () -> Unit
    ) {
        val card = root as MaterialCardView
        val tv = root.findViewById<TextView>(R.id.tv_login_text)
        val iv = root.findViewById<ImageView>(R.id.iv_login_icon)

        tv.text = text
        tv.setTextColor(getColor(textRes))
        iv.setImageResource(iconRes)
        card.setCardBackgroundColor(getColor(bgRes))

        root.setOnClickListener { onClick() }
    }

    private fun moveToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun moveToOnboarding() {
        val intent = Intent(this, OnbProfileActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    data class ProfileResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: MypageResult?
    )
}