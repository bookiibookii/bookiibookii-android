package com.bookiibookii.bookiibookii.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.AuthInterceptor
import com.google.android.material.button.MaterialButton

class ComErrorActivity : AppCompatActivity() {

    private lateinit var ivError: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDes: TextView

    private lateinit var btnTop: MaterialButton
    private lateinit var btnBottom: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_com_error)

        bindViews()

        val type = intent.getIntExtra(EXTRA_COM_TYPE, TYPE_NETWORK_ERROR)

        applyContent(type)
        applyButtonLayout(type)
        bindActions(type)
    }

    private fun bindViews() {
        ivError = findViewById(R.id.iv_error_image)
        tvTitle = findViewById(R.id.tv_error_title)
        tvDes = findViewById(R.id.tv_error_des)

        // include_onb_button의 루트가 MaterialButton이라 include id로 바로 잡을 수 있음
        btnTop = findViewById(R.id.include_footer_button_1)
        btnBottom = findViewById(R.id.include_footer_button_2)
    }

    private fun applyContent(type: Int) {
        tvTitle.text = ""
        tvDes.text = ""
        tvTitle.visibility = View.VISIBLE
        tvDes.visibility = View.VISIBLE

        when (type) {
            TYPE_SYSTEM_ERROR -> {
                tvTitle.setText(R.string.type_system_error_title)
                tvDes.visibility = View.GONE
            }

            TYPE_NETWORK_ERROR -> {
                tvTitle.setText(R.string.type_network_error_title)
                tvDes.setText(R.string.type_network_error_desc)
            }

            TYPE_NO_PERMISSION -> {
                tvTitle.setText(R.string.type_permission_title)
                tvDes.visibility = View.GONE
            }

            TYPE_GROUP_DELETED -> {
                tvTitle.setText(R.string.type_group_delete_title)
            }
        }
    }

    private fun applyButtonLayout(type: Int) {
        when (type) {
            // 버튼 1개짜리 타입들 (12 계열)
            TYPE_NO_PERMISSION, TYPE_GROUP_DELETED -> {
                btnTop.visibility = View.GONE
                btnBottom.visibility = View.VISIBLE

                btnBottom.setText(R.string.com_btn_main)
                applyActiveLook(btnBottom)
            }

            // 버튼 2개짜리 타입들 (10/11 계열)
            else -> {
                btnTop.visibility = View.VISIBLE
                btnBottom.visibility = View.VISIBLE

                btnTop.setText(R.string.com_btn_retry)
                applyActiveLook(btnTop)

                btnBottom.setText(R.string.com_btn_back)
                applyInactiveLook(btnBottom)
            }
        }
    }

    private fun bindActions(type: Int) {
        // 다시 시도 (10/11에서만 노출)
        btnTop.setOnClickListener {
            AuthInterceptor.unlockRouting()
            ComRetryBus.emitRetry()
            setResult(RESULT_RETRY)
            finish()
        }

        // 하단 버튼: 이전으로(10/11) or 메인으로(12/13)
        btnBottom.setOnClickListener {
            AuthInterceptor.unlockRouting()
            when (type) {
                TYPE_NO_PERMISSION, TYPE_GROUP_DELETED -> setResult(RESULT_GO_MAIN)
                else -> setResult(Activity.RESULT_CANCELED)
            }
            finish()
        }
    }

    private fun applyActiveLook(button: MaterialButton) {
        // selector의 disabled 상태 안 타게 true 유지
        button.isEnabled = true
        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey_900)
        button.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun applyInactiveLook(button: MaterialButton) {
        // 실제 disabled가 아니라 "비활성처럼 보이기"만
        button.isEnabled = true
        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey_200)
        button.setTextColor(ContextCompat.getColor(this, R.color.grey_900))
    }

    companion object {
        private const val EXTRA_COM_TYPE = "extra_com_type"

        // 화면 타입
        const val TYPE_SYSTEM_ERROR = 10      // COM-010 (시스템 장애)
        const val TYPE_NETWORK_ERROR = 11     // COM-011 (네트워크 오류)
        const val TYPE_NO_PERMISSION = 12     // COM-012 (접근 권한 없음)
        const val TYPE_GROUP_DELETED = 13     // COM-012 변형 (그룹 삭제 안내)

        // 결과 코드
        const val RESULT_RETRY = 1001
        const val RESULT_GO_MAIN = 1002

        fun newIntent(context: Context, type: Int): Intent {
            return Intent(context, ComErrorActivity::class.java).apply {
                putExtra(EXTRA_COM_TYPE, type)
            }
        }
    }
}