package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.openPrivacyPolicy
import com.bookiibookii.bookiibookii.common.openTermsOfService
import com.bookiibookii.bookiibookii.data.api.AuthInterceptor
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.auth.LoginRequest
import com.bookiibookii.bookiibookii.data.model.mypage.MypageResult
import com.bookiibookii.bookiibookii.onboarding.Intro.LoginIntroActivity
import com.bookiibookii.bookiibookii.onboarding.steps.OnbStepActivity
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private var isNavigating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // 이거 추가했음 -> BaseActivity 사용하기에는 findViewById 사용되서 어려움 ..
        super.onCreate(savedInstanceState)

        //상하좌우 패딩주기
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
        // 공통 에러/로그아웃 라우팅 잠금 해제
        AuthInterceptor.unlockRouting()

        setContentView(R.layout.activity_login)

        // 자동 로그인 분기
        if (routeAutoLoginIfPossible()) return

        bindLoginButtons()
        setupTermsNotice()
        setupTagline()

    }

    private fun routeAutoLoginIfPossible(): Boolean {
        if (!TokenManager.hasAccessToken(this)) return false
        if (!TokenManager.isOnboardingDone(this)) return false

        showLoginCompleteThenRoute()
        return true
    }

    private fun bindLoginButtons() {
        val kakaoButton = findViewById<ImageButton>(R.id.btn_kakao_circle)
        val googleButton = findViewById<ImageButton>(R.id.btn_google_circle)

        kakaoButton.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            showLoadingState(true)
            loginToKakao()
        }

        googleButton.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            showLoadingState(true)
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val option = GetSignInWithGoogleOption.Builder(getString(R.string.web_client_id)).build()
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
                    showLoadingState(false)
                }
            } catch (e: GetCredentialException) {
                showLoadingState(false)
            }
        }
    }

    private fun loginToKakao() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
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
                        showLoadingState(false)
                    }
                } else {
                    showLoadingState(false)
                }
            } catch (e: Exception) {
                showLoadingState(false)
            }
        }
    }

    private fun onLoginSuccess() {
        if (isNavigating) return
        isNavigating = true

        showLoadingState(true)

        val tvComplete = findViewById<TextView>(R.id.tv_login_complete)
        tvComplete.visibility = View.VISIBLE

        tvComplete.postDelayed({
            val isNewUser = !TokenManager.isOnboardingDone(this)
            if (isNewUser) moveToOnboarding() else moveToMain()
        }, 800L)
    }

    private fun moveToIntroAnim() {
        val intent = Intent(this, LoginIntroActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showLoadingState(isLoading: Boolean) {
        val snsSection = findViewById<LinearLayout>(R.id.layout_sns_section)
        val terms = findViewById<TextView>(R.id.tv_terms_notice)

        snsSection.visibility = if (isLoading) View.GONE else View.VISIBLE
        terms.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showLoginCompleteThenRoute() {
        if (isNavigating) return
        isNavigating = true

        showLoadingState(true)

        val tvComplete = findViewById<TextView>(R.id.tv_login_complete)
        tvComplete.visibility = View.VISIBLE

        tvComplete.postDelayed({
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
        // OnbStepActivity에서 뒤로 돌아왔을 때 토큰 제거 및 UI 초기화
        if (isNavigating) {
            isNavigating = false
            TokenManager.clear(this)
            val tvComplete = findViewById<TextView>(R.id.tv_login_complete)
            tvComplete.visibility = View.GONE
            showLoadingState(false)
        }
    }

    data class ProfileResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String,
        val result: MypageResult?
    )

    private fun setupTagline() {
        val tv = findViewById<TextView>(R.id.tv_tagline)
        val line1 = "읽고, 교환하고, 기록하다 –\n"
        val bold = "부키부키"
        val line2 = "에서 교환독서 파트너를 찾아보세요"
        val spannable = SpannableStringBuilder(line1 + bold + line2)
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            line1.length,
            line1.length + bold.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv.text = spannable
    }

    private fun setupTermsNotice() {
        val tv = findViewById<TextView>(R.id.tv_terms_notice)

        val prefix = "로그인하면 부키부키의 "
        val terms = "서비스 약관"
        val mid = " 및 "
        val privacy = "개인정보 처리방침"
        val suffix = "에 동의하게 됩니다."

        val full = prefix + terms + mid + privacy + suffix
        val spannable = SpannableString(full)

        val termsStart = prefix.length
        val termsEnd = termsStart + terms.length

        val privacyStart = termsEnd + mid.length
        val privacyEnd = privacyStart + privacy.length

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openTermsOfService()
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.WHITE
                ds.isUnderlineText = true
            }
        }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openPrivacyPolicy()
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.WHITE
                ds.isUnderlineText = true
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tv.text = spannable
        tv.movementMethod = LinkMovementMethod.getInstance()
        tv.highlightColor = Color.TRANSPARENT
    }
}
