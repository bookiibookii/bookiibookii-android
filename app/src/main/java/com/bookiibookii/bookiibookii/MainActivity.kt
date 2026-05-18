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
import com.bookiibookii.bookiibookii.common.BaseActivity
import com.bookiibookii.bookiibookii.databinding.ActivityMainBinding
import com.bookiibookii.bookiibookii.home.HomeFragment

private enum class NavTab { HOME, TRACKER, LIBRARY }


class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            setBottomNavSelected(NavTab.HOME)
            replaceFragment(HomeFragment())
        }

        initBottomNav()
        handleNavigationIntent(intent)
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
