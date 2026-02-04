package com.bookiibookii.bookiibookii.onboarding.steps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.google.android.material.button.MaterialButton

class OnbStepActivity : AppCompatActivity() {

    private val vm: OnbViewModel by viewModels()

    private lateinit var ivBack: ImageView
    private lateinit var progress1: View
    private lateinit var progress2: View
    private lateinit var progress3: View
    private lateinit var btnNext: MaterialButton

    private var currentStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onb_step)

        bindViews()
        bindActions()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        // ✅ 상태 변화 관찰 → 버튼 자동 갱신
        vm.state.observe(this) {
            updateNextButtonState()
        }

        if (savedInstanceState == null) {
            currentStep = 1
            renderProgress(currentStep)
            updateNextButtonState()

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
        btnNext = findViewById(R.id.include_footer_button)
    }

    private fun bindActions() {
        ivBack.setOnClickListener { handleBack() }
        btnNext.setOnClickListener { handleNext() }
    }

    private fun handleBack() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
            return
        }

        supportFragmentManager.popBackStack()
        currentStep -= 1
        renderProgress(currentStep)
        updateNextButtonState()
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
        renderProgress(currentStep)
        updateNextButtonState()

        supportFragmentManager.beginTransaction()
            .replace(R.id.stepContainer, OnbStep2Fragment())
            .addToBackStack("step2")
            .commit()
    }

    private fun moveToStep3() {
        currentStep = 3
        renderProgress(currentStep)
        updateNextButtonState()

        supportFragmentManager.beginTransaction()
            .replace(R.id.stepContainer, OnbStep3Fragment())
            .addToBackStack("step3")
            .commit()

        btnNext.text = "완료"
    }

    private fun finishOnboarding() {
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

        if (step != 3) {
            btnNext.text = "다음"
        }
    }

    private fun updateNextButtonState() {
        btnNext.isEnabled = when (currentStep) {
            1 -> vm.canGoStep2Next()
            2 -> vm.canGoStep3Next()
            3 -> vm.canFinish()
            else -> false
        }
    }
}