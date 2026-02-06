package com.bookiibookii.bookiibookii.onboarding.steps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.OnboardingRequest
import com.bookiibookii.bookiibookii.data.model.OnboardingTag
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import androidx.core.content.edit

// 온보딩 완료/로딩 상태를 표시하는 액티비티
class OnbStatusActivity : AppCompatActivity() {

    // 상단/하단 텍스트 및 하단 버튼
    private lateinit var tvTopOrange: TextView
    private lateinit var tvTopBlack: TextView
    private lateinit var tvBottomOrange: TextView
    private lateinit var btnFooter: MaterialButton

    // 현재 화면 상태 (로딩 / 완료)
    private var status: Status = Status.LOADING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onb_status)

        // 뷰 바인딩
        bindViews()
        // 클릭/뒤로가기 액션 바인딩
        bindActions()

        // 전달받은 상태 값 파싱 (기본값: LOADING)
        status = intent.getStringExtra(EXTRA_STATUS)
            ?.let { runCatching { Status.valueOf(it) }.getOrNull() }
            ?: Status.LOADING

        // 상태에 따른 UI 렌더링
        render(status)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // 뷰 초기화
    private fun bindViews() {
        tvTopOrange = findViewById(R.id.tv_top_orange)
        tvTopBlack = findViewById(R.id.tv_top_black)
        tvBottomOrange = findViewById(R.id.tv_bottom_orange)
        btnFooter = findViewById(R.id.include_footer_button)
    }

    // 버튼 클릭 및 뒤로가기 처리
    private fun bindActions() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 완료 상태에서는 앱 전체 종료, 그 외에는 현재 화면 종료
                if (status == Status.COMPLETE) finishAffinity()
                else finish()
            }
        })

        // 완료 버튼 클릭 시 온보딩 완료 처리 후 메인으로 이동
        btnFooter.setOnClickListener {
            setOnboardingDone()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // 상태에 따른 분기 처리
    private fun render(status: Status) {
        when (status) {
            Status.LOADING -> renderLoading()
            Status.COMPLETE -> renderComplete()
        }
    }

    // 로딩 화면 UI 처리
    private fun renderLoading() {
        tvTopOrange.visibility = View.VISIBLE
        tvTopBlack.visibility = View.GONE
        tvBottomOrange.visibility = View.GONE
        btnFooter.visibility = View.GONE

        tvTopOrange.setText(R.string.onb_status_loading)

        // 온보딩 API 요청
        requestOnboarding()
    }

    // 온보딩 데이터 서버 전송
    private fun requestOnboarding() {
        val name = intent.getStringExtra(EXTRA_NAME) ?: run {
            // TODO: 추후 로그 삭제
            Log.e("ONB_API", "name is null")
            finish()
            return
        }

        val s3Key = intent.getStringExtra(EXTRA_S3_KEY)

        val genres = intent.getStringArrayListExtra(EXTRA_GENRES).orEmpty()
        val methods = intent.getStringArrayListExtra(EXTRA_METHODS).orEmpty()
        val speed = intent.getStringExtra(EXTRA_SPEED) ?: run {
            // TODO: 추후 로그 삭제
            Log.e("ONB_API", "speed is null")
            finish()
            return
        }

        // 서버 전송용 태그 리스트 구성
        val tags = listOf(
            OnboardingTag(type = "GENRE", value = genres),
            OnboardingTag(type = "METHOD", value = methods),
            OnboardingTag(type = "SPEED", value = listOf(speed))
        )

        val req = OnboardingRequest(
            name = name,
            tags = tags,
            s3Key = s3Key
        )

        // TODO: 추후 로그 삭제
        Log.d("ONB_API", "request=$req")
        // TODO: 추후 로그 삭제
        Log.d("ONB_API", "name=$name s3Key=$s3Key genres=$genres methods=$methods speed=$speed")

        lifecycleScope.launch {
            runCatching {
                RetrofitClient.api().postOnboarding(req)
            }.onSuccess { res ->
                val body = res.body()

                // TODO: 추후 로그 삭제
                Log.d("ONB_API", "http=${res.code()} success=${res.isSuccessful} body=$body")

                // 성공 시 완료 상태로 전환
                if (res.isSuccessful && body?.isSuccess == true) {
                    status = Status.COMPLETE
                    render(status)
                } else {
                    // TODO: 추후 로그 삭제
                    Log.e("ONB_API", "failed: http=${res.code()} msg=${body?.message}")
                    finish()
                }
            }.onFailure { e ->
                // TODO: 추후 로그 삭제
                Log.e("ONB_API", "network error", e)
                finish()
            }
        }
    }

    // 완료 화면 UI 처리
    private fun renderComplete() {
        tvTopOrange.visibility = View.GONE
        tvTopBlack.visibility = View.VISIBLE
        tvBottomOrange.visibility = View.VISIBLE
        btnFooter.visibility = View.VISIBLE

        tvTopBlack.setText(R.string.onb_status_complete_title)
        tvBottomOrange.setText(R.string.onb_status_complete_desc)

        btnFooter.isEnabled = true
        btnFooter.setText(R.string.onb_status_complete_start)
    }

    // 온보딩 완료 여부 로컬 저장
    private fun setOnboardingDone() {
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        prefs.edit { putBoolean("onboarding_done", true) }
    }

    // 화면 상태 enum
    enum class Status { LOADING, COMPLETE }

    companion object {
        const val EXTRA_STATUS = "extra_status"

        const val EXTRA_NAME = "extra_name"
        const val EXTRA_S3_KEY = "extra_s3_key"
        const val EXTRA_GENRES = "extra_genres"
        const val EXTRA_METHODS = "extra_methods"
        const val EXTRA_SPEED = "extra_speed"
    }
}