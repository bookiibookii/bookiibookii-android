package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.common.openPrivacyPolicy
import com.bookiibookii.bookiibookii.common.openTermsOfService
import com.bookiibookii.bookiibookii.data.api.AuthInterceptor
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.auth.LoginRequest
import com.bookiibookii.bookiibookii.data.model.mypage.MypageResult
import com.bookiibookii.bookiibookii.onboarding.Intro.LoginIntroActivity
import com.bookiibookii.bookiibookii.onboarding.login.ui.LoginScreen
import com.bookiibookii.bookiibookii.onboarding.steps.OnbStepActivity
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private var isNavigating = false
    private var isLoading by mutableStateOf(false)
    private var showLoginComplete by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AuthInterceptor.unlockRouting()

        if (routeAutoLoginIfPossible()) return

        setContent {
            BookiiBookiiTheme {
                LoginScreen(
                    isLoading = isLoading,
                    showLoginComplete = showLoginComplete,
                    onKakaoClick = {
                        if (!isNavigating) {
                            isLoading = true
                            loginToKakao()
                        }
                    },
                    onGoogleClick = {
                        if (!isNavigating) {
                            isLoading = true
                            signInWithGoogle()
                        }
                    },
                    onTermsClick = { openTermsOfService() },
                    onPrivacyClick = { openPrivacyPolicy() },
                )
            }
        }
    }

    private fun routeAutoLoginIfPossible(): Boolean {
        if (!TokenManager.hasAccessToken(this)) return false
        if (!TokenManager.isOnboardingDone(this)) return false

        showLoginCompleteThenRoute()
        return true
    }

    private fun signInWithGoogle() {
        val option = GetSignInWithGoogleOption.Builder(getString(com.bookiibookii.bookiibookii.R.string.web_client_id)).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        lifecycleScope.launch {
            try {
                val result = CredentialManager.create(this@LoginActivity)
                    .getCredential(this@LoginActivity, request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    sendTokenToBackend("GOOGLE", idToken)
                } else {
                    isLoading = false
                }
            } catch (e: GetCredentialException) {
                isLoading = false
            }
        }
    }

    private fun loginToKakao() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                isLoading = false
            } else if (token != null) {
                sendTokenToBackend("KAKAO", token.accessToken)
            } else {
                isLoading = false
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        isLoading = false
                        return@loginWithKakaoTalk
                    }
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    return@loginWithKakaoTalk
                }
                if (token != null) {
                    sendTokenToBackend(socialType = "KAKAO", token = token.accessToken)
                } else {
                    isLoading = false
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun sendTokenToBackend(socialType: String, token: String) {
        lifecycleScope.launch {
            try {
                val request = LoginRequest(socialType = socialType, token = token)
                val response = RetrofitClient.authApiNoAuth().postLogin(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    if (result != null) {
                        getSharedPreferences("bookii_prefs", android.content.Context.MODE_PRIVATE)
                            .edit().putBoolean("push_notification_enabled", true).apply()
                        TokenManager.saveTokens(
                            this@LoginActivity,
                            result.accessToken,
                            result.refreshToken,
                            result.userId
                        )
                        TokenManager.saveOnboardingDone(
                            // TODO: 이거 확인해서 수정해야함
                            this@LoginActivity,
                            result.onboardingStatus == "COMPLETED" ||
                                result.onboardingStatus == "SPLASH_DONE",
                        )
                        onLoginSuccess()
                    } else {
                        isLoading = false
                    }
                } else {
                    isLoading = false
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    private fun onLoginSuccess() {
        if (isNavigating) return
        isNavigating = true

        isLoading = true
        showLoginComplete = true

        window.decorView.postDelayed({
            val isNewUser = !TokenManager.isOnboardingDone(this)
            if (isNewUser) moveToOnboarding() else moveToMain()
        }, 800L)
    }

    private fun showLoginCompleteThenRoute() {
        if (isNavigating) return
        isNavigating = true

        isLoading = true

        setContent {
            BookiiBookiiTheme {
                LoginScreen(
                    isLoading = true,
                    showLoginComplete = true,
                    onKakaoClick = {},
                    onGoogleClick = {},
                    onTermsClick = {},
                    onPrivacyClick = {},
                )
            }
        }

        window.decorView.postDelayed({
            if (TokenManager.isOnboardingDone(this)) moveToMain() else moveToOnboarding()
        }, 1500L)
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
        startActivity(Intent(this, OnbStepActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        if (isNavigating) {
            isNavigating = false
            TokenManager.clear(this)
            isLoading = false
            showLoginComplete = false
        }
    }

    data class ProfileResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: MypageResult?
    )
}
