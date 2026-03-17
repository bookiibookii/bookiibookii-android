package com.bookiibookii.bookiibookii.common

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B : ViewBinding> : AppCompatActivity() {

    // 자식 액티비티에서 바인딩 객체에 접근할 때 사용할 변수
    protected lateinit var binding: B

    // 자식 액티비티에서 바인딩 인플레이터를 넘겨주도록 강제하는 추상 함수
    abstract fun getViewBinding(): B

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. 화면을 시스템 바 영역까지 확장 (Edge-to-Edge)
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        binding = getViewBinding()
        setContentView(binding.root)

        // 2. 시스템 바 및 키보드(IME) 침범 방지 패딩 자동 적용
        setupWindowInsets(binding.root)
    }

    /**
     * 기본적으로 루트 뷰에 상태바, 하단 네비게이션바, 그리고 '키보드' 높이만큼 패딩을 밀어 넣습니다.
     * 특정 액티비티에서 다르게 작동해야 한다면(예: 스크롤뷰에만 패딩을 주고 싶다면)
     * 자식 액티비티에서 이 함수를 override 해서 재정의하면 됩니다.
     */
    open fun setupWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            // ★ 핵심 포인트: systemBars()와 ime()를 'or'로 묶어서 두 영역을 모두 가져옵니다.
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )

            // 가져온 영역만큼 최상단 뷰의 패딩을 설정하여 UI가 밀려 올라가도록 처리
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)

            windowInsets
        }
    }
}