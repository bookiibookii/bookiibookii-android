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

    // 문구 최종 위치(위로 올릴 정도)
    private val DESC_UP_DP = 180f

    private val cardPages = listOf(
        R.drawable.img_anim_01,
        R.drawable.img_anim_02,
        R.drawable.img_anim_03
    )

    private val descByPage = listOf(
        "우리들의 비밀스런 북클럽\n부키부키",
        "한번에 하는 비밀의 교환독서",
        "소중한 후기로 가꾸는\n나에게 꼭 맞는 독서 파트너와의 만남"
    )

    // 스크롤이 끝난(IDLE) 뒤에 문구/버튼을 처리하기 위한 보류 값
    private var pendingDesc: String? = null

    // 마지막 페이지 도착 후 버튼 노출 딜레이(원하면 200~600 사이로 조절)
    private val START_BTN_DELAY_MS = 350L

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
        btnStart.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        // 카드 세팅
        pager.adapter = IntroPagerAdapter(cardPages)
        pager.isUserInputEnabled = false
        pager.alpha = 0f

        // 캐러셀 세팅(가볍게)
        setupCarouselPager(pager)

        // 문구 초기(아래에서 올라오며 등장)
        desc.text = descByPage[0]
        desc.alpha = 0f
        desc.translationY = dpToPx(18f)

        // 페이지 콜백: 스크롤 중에는 UI 변화 최소, IDLE 이후 처리
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                // ✅ 스크롤 애니와 문구/버튼 애니를 겹치지 않게 보류
                pendingDesc = descByPage.getOrElse(position) { descByPage.last() }

                // 스크롤 중에는 버튼 숨김 유지
                if (position != cardPages.lastIndex) {
                    btnStart.visibility = View.GONE
                    btnStart.animate().cancel()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state != ViewPager2.SCROLL_STATE_IDLE) return

                // ✅ 스크롤이 완전히 끝난 뒤 문구 변경(끊김 감소)
                pendingDesc?.let {
                    animateDescChange(desc, it)
                    pendingDesc = null
                }

                // ✅ 마지막 페이지면 버튼도 IDLE 이후 + 딜레이 후 노출
                if (pager.currentItem == cardPages.lastIndex) {
                    btnStart.removeCallbacks(null)
                    btnStart.visibility = View.GONE
                    btnStart.animate().cancel()

                    btnStart.postDelayed({
                        btnStart.alpha = 0f
                        btnStart.translationY = dpToPx(8f)
                        btnStart.visibility = View.VISIBLE
                        btnStart.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(250L)
                            .start()
                    }, START_BTN_DELAY_MS)
                } else {
                    btnStart.visibility = View.GONE
                }
            }
        })

        // 1) 로고(중앙) → 위로 이동
        logo.post {
            val targetY = -(logo.top - dpToPx(50f))
            logo.animate()
                .translationY(targetY)
                .setDuration(700L)
                .start()
        }

        // 2) 로고가 올라간 자리에 문구 등장
        logo.postDelayed({
            desc.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(350L)
                .start()
        }, 650L)

        // 3) 문구 최종 위치로 위로 + 카드 등장(문구/카드 간격 유지)
        logo.postDelayed({
            val descFinalY = -dpToPx(DESC_UP_DP)

            val gapDp = 20f
            val pagerFinalY = descFinalY + dpToPx(gapDp)

            desc.animate()
                .translationY(descFinalY)
                .setDuration(220L)
                .start()

            pager.translationY = pagerFinalY + dpToPx(20f)
            pager.animate()
                .alpha(1f)
                .translationY(pagerFinalY)
                .setDuration(350L)
                .start()
        }, 1200L)

        // 4) 카드 자동 슬라이드 시작(첫 이동은 살짝 당겨서 덜 뜸들임)
        startAutoSlide(pager, intervalMs = 2000L)
    }

    private fun startAutoSlide(pager: ViewPager2, intervalMs: Long) {
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

        // 첫 슬라이드 시작을 조금 당김
        pager.postDelayed(autoSlideRunnable!!, 1400L)
    }

    // 문구 변경: 위치는 유지하고 페이드만(가볍게)
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

    // 캐러셀: padding + (가벼운) scale/alpha만
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
        super.onDestroy()
    }
}