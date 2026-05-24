package com.bookiibookii.bookiibookii

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bookiibookii.bookiibookii.common.BaseActivity
import com.bookiibookii.bookiibookii.databinding.ActivityMainBinding
import com.bookiibookii.bookiibookii.group.GroupFragment
import com.bookiibookii.bookiibookii.home.HomeFragment
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager

private enum class NavTab { HOME, TRACKER, LIBRARY }


class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (!TokenManager.hasAccessToken(this)) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        if (savedInstanceState == null) {
            setBottomNavSelected(NavTab.HOME)
            replaceFragment(HomeFragment())
        }

        initBottomNav()
        observeFragmentChanges()
        handleNavigationIntent(intent)
    }

    // 컨테이너에 들어온 Fragment 종류에 따라 BottomNav 표시 여부 토글
    // 우선 GroupFragment만 추가
    private fun observeFragmentChanges() {
        supportFragmentManager.addOnBackStackChangedListener {
            val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            binding.bottomNav.root.visibility =
                if (current is GroupFragment) View.GONE else View.VISIBLE
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        when (intent?.getStringExtra("NAV_ACTION")) {
            "OPEN_GROUP" -> {
                moveToGroupTab()
                intent.removeExtra("NAV_ACTION")
            }
        }
    }

    private fun initBottomNav() {
        binding.bottomNav.itemGroup.setOnClickListener {
            selectTab(NavTab.HOME, HomeFragment())
        }
    }

    private fun selectTab(tab: NavTab, fragment: Fragment) {
        setBottomNavSelected(tab)
        replaceFragment(fragment)
    }

    fun moveToGroupTab() {
        selectTab(NavTab.HOME, HomeFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setBottomNavSelected(tab: NavTab) {
        fun setItem(item: LinearLayout, tv: TextView, selected: Boolean) {
            item.background = if (selected)
                ContextCompat.getDrawable(this, R.drawable.bg_grey100_circle)
            else null
            tv.visibility = if (selected) View.GONE else View.VISIBLE
        }

        setItem(binding.bottomNav.itemGroup, binding.bottomNav.tvGroup, tab == NavTab.HOME)
        setItem(binding.bottomNav.itemTracker, binding.bottomNav.tvTracker, tab == NavTab.TRACKER)
        setItem(binding.bottomNav.itemLibrary, binding.bottomNav.tvLibrary, tab == NavTab.LIBRARY)
    }

    override fun setupWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.fragmentContainer.updatePadding(
                top = systemBars.top
            )

            val params = binding.bottomNav.root.layoutParams as ConstraintLayout.LayoutParams
            params.bottomMargin = systemBars.bottom
            binding.bottomNav.root.layoutParams = params

            insets
        }
    }

}
