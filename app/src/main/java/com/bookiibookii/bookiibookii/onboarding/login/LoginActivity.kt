package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.WindowManager
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.bookiibookii.bookiibookii.data.model.auth.LoginRequest
import com.bookiibookii.bookiibookii.data.model.mypage.MypageResult
import com.bookiibookii.bookiibookii.onboarding.Intro.LoginIntroAnimActivity
import com.bookiibookii.bookiibookii.onboarding.profile.OnbProfileActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
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

        // TODO: 추후 로그 삭제 (온보딩/토큰 분기 디버깅용)
        Log.d(
            "ONB_FLOW",
            "token=${TokenManager.hasAccessToken(this)} done=${TokenManager.isOnboardingDone(this)}"
        )

        // 자동 로그인 분기
        if (routeAutoLoginIfPossible()) return

        credentialManager = CredentialManager.create(this)

        bindLoginButtons()
        setupTermsNotice()
        setupTagline()

        // TODO: 추후 로그 삭제 (카카오 키해시 확인용)
        Log.e("KeyHash_Check", "내 앱의 현재 키 해시: ${com.kakao.sdk.common.KakaoSdk.keyHash}")
    }

    private fun routeAutoLoginIfPossible(): Boolean {
        if (!TokenManager.hasAccessToken(this)) return false

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
        val webClientId = getString(R.string.web_client_id)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
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
                val response = RetrofitClient.authApiNoAuth().postLogin(request)

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
        if (isNavigating) return
        isNavigating = true

        showLoadingState(true)

        val tvComplete = findViewById<TextView>(R.id.tv_login_complete)
        tvComplete.visibility = View.VISIBLE

        tvComplete.postDelayed({
            val isNewUser = !TokenManager.isOnboardingDone(this)

            if (isNewUser) {
                moveToOnboarding()
            } else {
                moveToMain()
            }
        }, 800L)
    }

    private fun moveToIntroAnim() {
        val intent = Intent(this, LoginIntroAnimActivity::class.java).apply {
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
            if (TokenManager.isOnboardingDone(this)) {
                moveToMain()
            } else {
                moveToOnboarding()
            }
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
                showTermsDialog(
                    title = "서비스 약관",
                    rawResId = R.raw.terms_service
                )
            }
            override fun updateDrawState(ds: TextPaint) {
                ds.color = Color.WHITE
                ds.isUnderlineText = true
            }
        }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                showTermsDialog(
                    title = "개인정보 처리방침",
                    rawResId = R.raw.terms_privacy
                )
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

    private fun showTermsDialog(title: String, rawResId: Int) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_login_terms, null, false)

        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvContent = view.findViewById<TextView>(R.id.tv_content)
        val ivClose = view.findViewById<ImageView>(R.id.iv_close)

        tvTitle.text = title
        tvContent.text = readRawText(rawResId)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        ivClose.setOnClickListener { dialog.dismiss() }

        dialog.show()

        // 카드 크기: 380dp × 480dp
        val density = resources.displayMetrics.density
        dialog.window?.setLayout(
            (380 * density).toInt(),
            (480 * density).toInt()
        )
        // dim 배경 (rgba(0,0,0,0.45))
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setDimAmount(0.45f)
    }

    private fun readRawText(rawResId: Int): String {
        return resources.openRawResource(rawResId).bufferedReader().use { it.readText() }
    }
}