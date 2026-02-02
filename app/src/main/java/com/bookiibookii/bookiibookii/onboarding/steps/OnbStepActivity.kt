package com.bookiibookii.bookiibookii.onboarding.steps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.google.android.material.button.MaterialButton

class OnbStepActivity : AppCompatActivity(), OnbStepHost {

    // 상단 뒤로가기 버튼
    private lateinit var ivBack: ImageView

    // 단계 진행 표시 바
    private lateinit var progress1: View
    private lateinit var progress2: View
    private lateinit var progress3: View

    // 하단 다음 / 완료 버튼
    private lateinit var btnNext: MaterialButton

    // 현재 온보딩 단계
    private var currentStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onb_step)

        bindViews()
        bindActions()

        // 시스템 뒤로가기 제스처/버튼 처리
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        // 최초 진입 시 Step1 표시
        if (savedInstanceState == null) {
            currentStep = 1
            renderProgress(currentStep)
            setNextEnabled(false)

            supportFragmentManager.beginTransaction()
                .replace(R.id.stepContainer, OnbStep1Fragment())
                .commit()
        }
    }

    private fun bindViews() {
        ivBack = findViewById(R.id.ivBack)

        progress1 = findViewById(R.id.progress1)
        progress2 = findViewById(R.id.progress2)
        progress3 = findViewById(R.id.progress3)

        // include된 하단 버튼은 include id로 접근
        btnNext = findViewById(R.id.include_footer_button)
    }

    private fun bindActions() {
        ivBack.setOnClickListener { handleBack() }
        btnNext.setOnClickListener { handleNext() }
    }

    private fun handleBack() {
        // Step1에서는 이전 Activity로 종료
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
            return
        }

        // Step2 이상에서는 이전 Fragment로 이동
        supportFragmentManager.popBackStack()
        currentStep -= 1
        setNextEnabled(false)
        renderProgress(currentStep)
    }

    private fun handleNext() {
        when (currentStep) {
            1 -> moveToStep2()
            2 -> moveToStep3()
            3 -> finishOnboarding()
        }
    }

    private fun moveToStep2() {
        currentStep = 2
        setNextEnabled(false)
        renderProgress(currentStep)

        supportFragmentManager.beginTransaction()
            .replace(R.id.stepContainer, OnbStep2Fragment())
            .addToBackStack("step2")
            .commit()
    }

    private fun moveToStep3() {
        currentStep = 3
        setNextEnabled(false)
        renderProgress(currentStep)

        supportFragmentManager.beginTransaction()
            .replace(R.id.stepContainer, OnbStep3Fragment())
            .addToBackStack("step3")
            .commit()

        // 마지막 단계에서는 버튼 텍스트 변경
        btnNext.text = "완료"
    }

    private fun finishOnboarding() {
        // 온보딩 완료 후 메인 화면 이동
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun renderProgress(step: Int) {
        progress1.setBackgroundResource(
            if (step >= 1) R.drawable.bg_onb_progress_active
            else R.drawable.bg_onb_progress_inactive
        )
        progress2.setBackgroundResource(
            if (step >= 2) R.drawable.bg_onb_progress_active
            else R.drawable.bg_onb_progress_inactive
        )
        progress3.setBackgroundResource(
            if (step >= 3) R.drawable.bg_onb_progress_active
            else R.drawable.bg_onb_progress_inactive
        )

        // 마지막 단계가 아니면 버튼 텍스트 원복
        if (step != 3) {
            btnNext.text = "다음"
        }
    }

    // Fragment에서 하단 버튼 활성/비활성 제어
    override fun setNextEnabled(enabled: Boolean) {
        btnNext.isEnabled = enabled
    }
}