package com.bookiibookii.bookiibookii.onboarding.steps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.google.android.material.button.MaterialButton

class OnbStatusActivity : AppCompatActivity() {

    // 로딩/완료 상태에서 상단에 노출되는 주황색 안내 텍스트
    private lateinit var tvTopOrange: TextView

    // 완료 상태에서 노출되는 검정색 타이틀 텍스트
    private lateinit var tvTopBlack: TextView

    // 완료 상태에서 타이틀 아래에 노출되는 주황색 설명 텍스트
    private lateinit var tvBottomOrange: TextView

    // include_onb_button.xml이 버튼 단일 레이아웃이므로 include 자체가 버튼
    private lateinit var btnFooter: MaterialButton

    // 현재 화면 상태 (로딩 / 완료)
    private var status: Status = Status.LOADING

    // LOADING 상태에서 일정 시간 후 COMPLETE 상태로 전환하기 위한 Runnable
    private val moveToCompleteRunnable = Runnable {
        status = Status.COMPLETE
        render(status)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onb_status)

        // View 참조 바인딩
        bindViews()

        // 뒤로가기 및 버튼 클릭 등 이벤트 바인딩
        bindActions()

        // Intent로 전달된 상태 값이 있으면 사용, 없으면 기본값 LOADING
        status = intent.getStringExtra(EXTRA_STATUS)
            ?.let { runCatching { Status.valueOf(it) }.getOrNull() }
            ?: Status.LOADING

        // 현재 상태에 맞는 UI 렌더링
        render(status)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Activity 종료 시 예약된 자동 전환 Runnable 제거 (메모리 누수 방지)
        tvTopOrange.removeCallbacks(moveToCompleteRunnable)
    }

    private fun bindViews() {
        // 상태별로 보여줄 텍스트 뷰들 바인딩
        tvTopOrange = findViewById(R.id.tv_top_orange)
        tvTopBlack = findViewById(R.id.tv_top_black)
        tvBottomOrange = findViewById(R.id.tv_bottom_orange)

        // include_onb_button의 id가 버튼 자체이므로 바로 버튼으로 캐스팅
        btnFooter = findViewById(R.id.include_footer_button)
    }

    private fun bindActions() {
        // 뒤로가기 동작 커스텀
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 완료 상태에서는 온보딩 재진입을 막기 위해 앱 종료
                if (status == Status.COMPLETE) {
                    finishAffinity()
                } else {
                    // 로딩 중에는 단순히 현재 화면 종료
                    finish()
                }
            }
        })

        // 완료 화면에서 하단 버튼 클릭 시 메인 화면으로 이동
        btnFooter.setOnClickListener {
            setOnboardingDone()

            // TODO: 온보딩까지 구현 후 삭제
            Log.d("ONB_FLOW", "saved onboarding_done=true")

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setOnboardingDone() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("onboarding_done", true)
            .apply()
    }

    // 현재 상태에 따라 로딩 화면 / 완료 화면 분기 처리
    private fun render(status: Status) {
        when (status) {
            Status.LOADING -> renderLoading()
            Status.COMPLETE -> renderComplete()
        }
    }

    private fun renderLoading() {
        // 로딩 상태 UI 노출
        tvTopOrange.visibility = View.VISIBLE
        tvTopBlack.visibility = View.GONE
        tvBottomOrange.visibility = View.GONE
        btnFooter.visibility = View.GONE

        // strings.xml에 정의된 로딩 안내 문구 설정
        tvTopOrange.setText(R.string.onb_status_loading)

        // 중복 예약 방지를 위해 기존 Runnable 제거 후 다시 예약
        tvTopOrange.removeCallbacks(moveToCompleteRunnable)
        tvTopOrange.postDelayed(moveToCompleteRunnable, 1800)
    }

    private fun renderComplete() {
        // 완료 상태 UI 노출
        tvTopOrange.visibility = View.GONE
        tvTopBlack.visibility = View.VISIBLE
        tvBottomOrange.visibility = View.VISIBLE
        btnFooter.visibility = View.VISIBLE

        // 완료 상태 문구 설정
        tvTopBlack.setText(R.string.onb_status_complete_title)
        tvBottomOrange.setText(R.string.onb_status_complete_desc)

        // 하단 버튼 활성화 및 문구 변경
        btnFooter.isEnabled = true
        btnFooter.setText(R.string.onb_status_complete_start)
    }

    // 상태 구분 enum
    enum class Status {
        LOADING, COMPLETE
    }

    companion object {
        // 외부에서 상태를 전달받기 위한 Intent key
        const val EXTRA_STATUS = "extra_status"
    }
}