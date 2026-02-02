package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.profile.OnbProfileActivity
import com.google.android.material.card.MaterialCardView
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {

    // 빠르게 연타했을 때 중복 이동 방지
    private var isNavigating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.e("APP_CHECK", "MyApplication onCreate called")


//        setupLoginButtons()

        findViewById<View>(R.id.btn_kakao_login).setOnClickListener {
            loginToKakao()
        }

    }

    // 로그인 버튼 UI 및 클릭 동작 설정
    private fun setupLoginButtons() {
        val kakaoButton = findViewById<View>(R.id.btn_kakao_login)
        setupLoginButton(
            root = kakaoButton,
            text = "카카오로 시작하기",
            iconRes = R.drawable.ic_kakao,
            backgroundColorRes = R.color.kakao,
            textColorRes = R.color.grey_900,
            toastMessage = "카카오 로그인 선택"
        )

        val googleButton = findViewById<View>(R.id.btn_google_login)
        setupLoginButton(
            root = googleButton,
            text = "구글로 시작하기",
            iconRes = R.drawable.ic_google,
            backgroundColorRes = R.color.grey_100,
            textColorRes = R.color.grey_900,
            toastMessage = "구글 로그인 선택"
        )
    }

    // 로그인 버튼 세팅
    private fun setupLoginButton(
        root: View,
        text: String,
        iconRes: Int,
        backgroundColorRes: Int,
        textColorRes: Int,
        toastMessage: String
    ) {
        val card = root as MaterialCardView
        val tv = root.findViewById<TextView>(R.id.tv_login_text)
        val iv = root.findViewById<ImageView>(R.id.iv_login_icon)

        tv.text = text
        tv.setTextColor(getColor(textColorRes))
        iv.setImageResource(iconRes)
        card.setCardBackgroundColor(getColor(backgroundColorRes))

        root.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            isNavigating = true
            showLoginCompleteAndMoveToMain(toastMessage)
        }
    }

    private fun showLoginCompleteAndMoveToMain(toastMessage: String) {
        // TODO: (카카오/구글) SDK 인증 시작
        // TODO: 인증 성공 시 서버 로그인/회원가입 요청
        // TODO: 유저 DB 조회 후 신규/기존 분기 처리
        //  - 신규: 프로필 설정 화면으로 이동
        //  - 기존: 메인 탭 화면으로 이동
        // TODO: 실패 시 "로그인에 실패하였습니다. 다시 시도해주세요." 토스트

        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()

        val buttonsGroup = findViewById<Group>(R.id.group_login_buttons)
        val completeText = findViewById<TextView>(R.id.tv_login_complete)

        // 로그인 버튼 영역 숨기고 성공 문구 표시
        buttonsGroup.visibility = View.GONE
        completeText.visibility = View.VISIBLE

        // 잠깐 보여준 뒤 메인으로 이동
        completeText.postDelayed({
            startActivity(Intent(this, OnbProfileActivity::class.java))
            finish()
        }, 1200L)
    }

    // 여기부터 ~~~~~

    private fun loginToKakao() {
        // 로그인 결과 콜백 (성공 시 처리)
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "로그인 실패", error)
            } else if (token != null) {
                Log.i("KakaoLogin", "로그인 성공")
                moveToMain()
            }
        }

        // 카카오톡 설치 여부 확인 후 로그인 시도
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e("KakaoLogin", "카카오톡 로그인 실패", error)

                    // 사용자가 뒤로가기 등으로 취소한 경우
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡 연결 실패 시, 웹(계정)으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Log.i("KakaoLogin", "카카오톡 로그인 성공")
                    moveToMain()
                }
            }
        } else {
            // 카카오톡이 없으면 웹으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    // 메인 화면으로 이동하는 함수
    private fun moveToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}