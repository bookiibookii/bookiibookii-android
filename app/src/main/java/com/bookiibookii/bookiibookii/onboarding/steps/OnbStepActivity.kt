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
import androidx.core.content.edit

class OnbStepActivity : AppCompatActivity() {

    // 온보딩 전체 상태를 관리하는 ViewModel
    private val vm: OnbViewModel by viewModels()

    // 상단 네비게이션/진행바/하단 버튼
    private lateinit var ivBack: ImageView
    private lateinit var progress1: View
    private lateinit var progress2: View
    private lateinit var progress3: View
    private lateinit var btnNext: MaterialButton

    // 현재 온보딩 단계(1~3)
    private var currentStep = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onb_step)

        // View 참조 및 클릭 이벤트 바인딩
        bindViews()
        bindActions()

        // 시스템 뒤로가기 동작을 온보딩 플로우에 맞게 커스텀
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })

        // 상태 변화 감지 → 현재 단계 기준으로 버튼 활성화 상태 자동 갱신
        vm.state.observe(this) {
            updateNextButtonState()
        }

        // 최초 진입 시에만 Step1 화면을 초기 세팅
        if (savedInstanceState == null) {
            currentStep = 1
            renderProgress(currentStep)
            updateNextButtonState()

            supportFragmentManager.beginTransaction()
                .replace(R.id.stepContainer, OnbStep1Fragment())
                .commit()
        }
    }

    // Activity 내부에서 사용하는 View 참조 바인딩
    private fun bindViews() {
        ivBack = findViewById(R.id.ivBack)
        progress1 = findViewById(R.id.progress1)
        progress2 = findViewById(R.id.progress2)
        progress3 = findViewById(R.id.progress3)
        btnNext = findViewById(R.id.btn_footer)
    }

    // 클릭 이벤트 바인딩
    private fun bindActions() {
        ivBack.setOnClickListener { handleBack() }
        btnNext.setOnClickListener { handleNext() }
    }

    // 뒤로가기 처리: 백스택이 없으면 종료, 있으면 이전 단계로 복귀
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

    // 다음 버튼 처리: 현재 단계에 따라 다음 화면으로 이동하거나 온보딩 종료
    private fun handleNext() {
        when (currentStep) {
            1 -> moveToStep2()
            2 -> moveToStep3()
            3 -> finishOnboarding()
        }
    }

    // Step2 화면으로 전환 + 백스택 추가
    private fun moveToStep2() {
        currentStep = 2
        renderProgress(currentStep)
        updateNextButtonState()

        supportFragmentManager.beginTransaction()
            .replace(R.id.stepContainer, OnbStep2Fragment())
            .addToBackStack("step2")
            .commit()
    }

    // Step3 화면으로 전환 + 백스택 추가
    private fun moveToStep3() {
        currentStep = 3
        renderProgress(currentStep)
        updateNextButtonState()

        supportFragmentManager.beginTransaction()
            .replace(R.id.stepContainer, OnbStep3Fragment())
            .addToBackStack("step3")
            .commit()
    }

    // 온보딩 완료 후 로딩 화면으로 이동
    private fun finishOnboarding() {
        val state = vm.state.value ?: return

        val genreValues = state.readingPreferences.map { it.serverValue }

        // PHOTO/FOCUS 둘 다 serverValue가 CLEAN이니까 중복 제거 필요
        val methodValues = state.recordMethods.map { it.serverValue }.distinct()

        val speedValue = state.readingPace?.serverValue ?: return

        // 프로필에서 넘어온 값 (onCreate에서 intent로 받아서 멤버로 들고있다고 가정)
        val name = intent.getStringExtra(EXTRA_NAME) ?: return
        val s3Key = intent.getStringExtra(EXTRA_S3_KEY) // null 가능

        startActivity(
            Intent(this, OnbStatusActivity::class.java)
                .putExtra(OnbStatusActivity.EXTRA_STATUS, "LOADING")
                .putExtra(OnbStatusActivity.EXTRA_NAME, name)
                .putExtra(OnbStatusActivity.EXTRA_S3_KEY, s3Key)
                .putStringArrayListExtra(OnbStatusActivity.EXTRA_GENRES, ArrayList(genreValues))
                .putStringArrayListExtra(OnbStatusActivity.EXTRA_METHODS, ArrayList(methodValues))
                .putExtra(OnbStatusActivity.EXTRA_SPEED, speedValue)
        )
        finish()
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_S3_KEY = "extra_s3_key"
    }

    // 현재 단계에 맞게 진행바 UI 갱신
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

        // Step3에서도 버튼 문구는 동일하게 유지
        btnNext.text = "다음"
    }

    private fun setOnboardingDone() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        prefs.edit { putBoolean("onboarding_done", true) }
    }

    // 현재 단계에서 "다음" 버튼을 활성화할 수 있는지 판단
    private fun updateNextButtonState() {
        btnNext.isEnabled = when (currentStep) {
            1 -> vm.canGoStep2Next()
            2 -> vm.canGoStep3Next()
            3 -> vm.canFinish()
            else -> false
        }
    }
}