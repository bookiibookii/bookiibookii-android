package com.bookiibookii.bookiibookii.common

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B : ViewBinding> : AppCompatActivity() {

    // 자식 액티비티에서 바인딩 객체에 접근할 때 사용할 변수
    protected lateinit var binding: B

    // 자식 액티비티에서 바인딩 인플레이터를 넘겨주도록 강제하는 추상 함수
    abstract fun getViewBinding(): B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 화면을 시스템 바 영역까지 확장 (Edge-to-Edge)
        enableEdgeToEdge()

        binding = getViewBinding()
        setContentView(binding.root)

        // 2. 상태바 영역을 흰색으로 (엣지투엣지라 상태바가 투명 → 흰색 View를 위에 덮음)
        applyWhiteStatusBar()

        // 3. 시스템 바 및 키보드(IME) 침범 방지 패딩 자동 적용
        setupWindowInsets(binding.root)

        // 4. 하단 시스템 네비게이션바 숨김 (몰입형 모드)
        hideNavigationBar()
    }

    /**
     * 하단 시스템 네비게이션바를 숨깁니다.
     * BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE: 가장자리에서 스와이프하면 잠깐 나타났다가
     * 다시 자동으로 사라지는 몰입형(immersive sticky) 모드입니다.
     */
    private fun hideNavigationBar() {
        val controller = WindowCompat.getInsetsController(window, binding.root)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    /**
     * 키보드를 닫거나 다른 앱에서 돌아오는 등 포커스가 다시 들어올 때
     * 네비게이션바가 도로 나타날 수 있어 다시 숨겨줍니다.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideNavigationBar()
    }

    /**
     * 상태바 영역에 흰색 배경 View를 깔아 상태바가 흰색으로 보이게 합니다.
     * 높이는 고정값이 아니라 시스템이 알려주는 실제 상태바 높이(statusBars().top)로 맞추므로
     * 기기·노치·회전 등에 따라 자동으로 변합니다.
     */
    private fun applyWhiteStatusBar() {
        val content = findViewById<FrameLayout>(android.R.id.content)
        val statusBarBg = View(this).apply {
            setBackgroundColor(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 0, Gravity.TOP
            )
        }
        content.addView(statusBarBg)

        ViewCompat.setOnApplyWindowInsetsListener(statusBarBg) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updateLayoutParams { height = top }
            insets
        }
    }

    /**
     * 기본적으로 루트 뷰에 상태바, 하단 네비게이션바, 그리고 '키보드' 높이만큼 패딩을 밀어 넣습니다.
     * 특정 액티비티에서 다르게 작동해야 한다면(예: 스크롤뷰에만 패딩을 주고 싶다면)
     * 자식 액티비티에서 이 함수를 override 해서 재정의하면 됩니다.
     */
    open fun setupWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            // systemBars()와 ime()를 'or'로 묶어서 두 영역을 모두 가져옵니다.
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )

            // 가져온 영역만큼 최상단 뷰의 패딩을 설정하여 UI가 밀려 올라가도록 처리
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)

            windowInsets
        }
    }
}