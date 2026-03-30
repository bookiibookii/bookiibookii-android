package com.bookiibookii.bookiibookii.onboarding.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bookiibookii.bookiibookii.R
import com.google.android.material.button.MaterialButton

class LoginIntroAnimActivity : AppCompatActivity() {

    private var autoSlideRunnable: Runnable? = null
    private var showStartBtnRunnable: Runnable? = null
    private var hasNavigatedToLogin = false

    // 문구 최종 위치(위로 올릴 정도)
    private val DESC_UP_DP = 180f

    // 타이밍 튜닝
    private val LOGO_UP_MS = 700L
    private val DESC_IN_MS = 350L
    private val DESC_UP_MS = 400L
    private val PAGER_IN_MS = 400L

    private var isPagerShown = false

    // 첫 카드가 너무 빨리 넘어가는 문제 방지
    private val FIRST_SLIDE_DELAY_MS = 2200L
    private val SLIDE_INTERVAL_MS = 2000L

    // 마지막 페이지 도착 후 버튼 노출 딜레이
    private val START_BTN_DELAY_MS = 350L

    private val cardPages = listOf(
        R.drawable.img_anim_01,
        R.drawable.img_anim_02,
        R.drawable.img_anim_03
    )

    private val descByPage = listOf(
        "우리들의 비밀스런 북클럽\n부키부키",
        "안심할 수 있는 비대면 교환독서 \n",
        "서로의 문장을\n공유하며 넓어지는 우리만의 서재",
        "소중한 후기로 가꾸는\n나에게 꼭 맞는 독서 파트너와의 만남"
    )

    // 스크롤이 끝난(IDLE) 뒤에 문구/버튼을 처리하기 위한 보류 값
    private var pendingDesc: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_intro_anim)

        // 뒤로가기 막기
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { }
        })

        val logo = findViewById<ImageView>(R.id.imgTitle)
        val desc = findViewById<TextView>(R.id.tv_intro_desc)
        val pager = findViewById<ViewPager2>(R.id.pager_intro)
        val btnStart = findViewById<MaterialButton>(R.id.btn_start)

        // 시작하기 버튼: 기본 숨김
        btnStart.visibility = View.GONE
        btnStart.setOnClickListener { navigateToLogin() }

        // 카드 세팅
        pager.adapter = IntroPagerAdapter(cardPages)
        pager.isUserInputEnabled = false
        pager.alpha = 0f
        setupCarouselPager(pager)
        pager.setCurrentItem(0, false)

        // 초기 문구: 로고 자리에서 등장할 "소개 문구"
        // (첫 카드 문구로 바꾸는 애니메이션은 "카드 등장 순간"에 따로 실행)
        desc.text = "독서 취향 기반 교환독서\n부키부키"
        desc.alpha = 0f
        desc.translationY = dpToPx(18f)

        // 페이지 콜백: 스크롤 중에는 UI 변화 최소, IDLE 이후 처리
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                val newText = descByPage.getOrElse(position + 1) { descByPage.last() }

                // ✅ 자동 슬라이드에서 IDLE이 누락되는 경우 대비: 페이지 선택 시 즉시 반영
                if (isPagerShown) {
                    if (desc.text.toString() != newText) {
                        animateDescChange(desc, newText)
                    }
                    pendingDesc = null
                } else {
                    // (카드 등장 전에는 IDLE 이후로 처리하는 기존 방식 유지)
                    pendingDesc = newText
                }

                if (position != cardPages.lastIndex) {
                    hideStartButton(btnStart)
                } else {
                    scheduleShowStartButton(btnStart)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state != ViewPager2.SCROLL_STATE_IDLE) return

                // 스크롤이 완전히 끝난 뒤 문구 변경
                pendingDesc?.let {
                    animateDescChange(desc, it)
                    pendingDesc = null
                }

                // 마지막 페이지면 버튼도 IDLE 이후 + 딜레이 후 노출
                if (pager.currentItem == cardPages.lastIndex) {
                    scheduleShowStartButton(btnStart)
                } else {
                    hideStartButton(btnStart)
                }
            }
        })

        // 1) 로고(중앙) → 위로 이동
        logo.post {
            val targetY = -(logo.top - dpToPx(50f))
            logo.animate()
                .translationY(targetY)
                .setDuration(LOGO_UP_MS)
                .start()
        }

        // 2) 로고가 올라간 자리에 문구 등장
        logo.postDelayed({
            desc.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(DESC_IN_MS)
                .start()
        }, 650L)

        // 3) 문구만 먼저 위로 올림 (카드는 아직 등장 X)
        val descUpStart = 1200L
        logo.postDelayed({
            val descFinalY = -dpToPx(DESC_UP_DP)
            desc.animate()
                .translationY(descFinalY)
                .setDuration(DESC_UP_MS)
                .start()
        }, descUpStart)

        // 4) 문구가 올라간 뒤에 카드 등장 + "첫 카드 문구"로 변경 + 오토슬라이드 시작
        logo.postDelayed({
            val descFinalY = -dpToPx(DESC_UP_DP)
            val gapDp = 20f
            val pagerFinalY = descFinalY + dpToPx(gapDp)

            isPagerShown = true

            // 첫 카드 문구로 변경(이 순간에 '바뀌는' 연출)
            animateDescChange(desc, descByPage[1])

            pager.translationY = pagerFinalY + dpToPx(20f)
            pager.animate()
                .alpha(1f)
                .translationY(pagerFinalY)
                .setDuration(PAGER_IN_MS)
                .start()

            // 카드 등장 이후에 슬라이드 시작 (첫 카드는 오래 보여주기)
            startAutoSlide(pager, intervalMs = SLIDE_INTERVAL_MS, firstDelayMs = FIRST_SLIDE_DELAY_MS)
        }, descUpStart + DESC_UP_MS)
    }

    private fun startAutoSlide(pager: ViewPager2, intervalMs: Long, firstDelayMs: Long) {
        autoSlideRunnable?.let { pager.removeCallbacks(it) }

        autoSlideRunnable = object : Runnable {
            override fun run() {
                val next = pager.currentItem + 1
                if (next <= cardPages.lastIndex) {
                    pager.setCurrentItem(next, true)
                    pager.postDelayed(this, intervalMs)
                }
            }
        }

        pager.postDelayed(autoSlideRunnable!!, firstDelayMs)
    }

    private fun scheduleShowStartButton(btnStart: MaterialButton) {
        showStartBtnRunnable?.let { btnStart.removeCallbacks(it) }
        btnStart.animate().cancel()
        btnStart.visibility = View.GONE

        showStartBtnRunnable = Runnable {
            btnStart.alpha = 0f
            btnStart.translationY = dpToPx(8f)
            btnStart.visibility = View.VISIBLE
            btnStart.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250L)
                .start()
        }

        btnStart.postDelayed(showStartBtnRunnable!!, START_BTN_DELAY_MS)
    }

    private fun hideStartButton(btnStart: MaterialButton) {
        showStartBtnRunnable?.let { btnStart.removeCallbacks(it) }
        btnStart.animate().cancel()
        btnStart.visibility = View.GONE
    }

    private fun navigateToLogin() {
        if (hasNavigatedToLogin) return
        hasNavigatedToLogin = true

        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    // 문구 변경: 위치는 유지하고 페이드만
    private fun animateDescChange(desc: TextView, newText: String) {
        val keepY = desc.translationY
        desc.animate().cancel()

        desc.animate()
            .alpha(0f)
            .setDuration(120L)
            .withEndAction {
                desc.text = newText
                desc.translationY = keepY
                desc.animate()
                    .alpha(1f)
                    .setDuration(180L)
                    .start()
            }
            .start()
    }

    // 캐러셀: padding + scale/alpha만
    private fun setupCarouselPager(pager: ViewPager2) {
        pager.clipToPadding = false
        pager.clipChildren = false
        pager.offscreenPageLimit = 3

        (pager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView)?.overScrollMode =
            View.OVER_SCROLL_NEVER

        val sidePadding = dpToPx(40f).toInt()
        pager.setPadding(sidePadding, 0, sidePadding, 0)

        pager.setPageTransformer { page, position ->
            val absPos = kotlin.math.abs(position)

            val scale = 0.94f + (1f - absPos) * 0.06f
            page.scaleX = scale
            page.scaleY = scale

            val alpha = 0.4f + (1f - absPos) * 0.6f
            page.alpha = alpha
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    override fun onDestroy() {
        val pager = findViewById<ViewPager2?>(R.id.pager_intro)
        autoSlideRunnable?.let { pager?.removeCallbacks(it) }
        autoSlideRunnable = null

        val btnStart = findViewById<MaterialButton?>(R.id.btn_start)
        showStartBtnRunnable?.let { btnStart?.removeCallbacks(it) }
        showStartBtnRunnable = null

        super.onDestroy()
    }
}
