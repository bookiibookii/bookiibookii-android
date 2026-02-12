package com.bookiibookii.bookiibookii

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.group.main.GroupFragment
import com.bookiibookii.bookiibookii.home.ExchangeRole
import com.bookiibookii.bookiibookii.home.HomeFragment
import com.bookiibookii.bookiibookii.lib.LibraryFragment
import com.bookiibookii.bookiibookii.myPage.MypageFragment
import com.bookiibookii.bookiibookii.trkGuest.GuestActivity
import com.bookiibookii.bookiibookii.trkHost.HostActivity
import com.bookiibookii.bookiibookii.trkHost.TrkHostMainFragment

class MainActivity : AppCompatActivity() {
    private enum class NavTab { HOME, GROUP, TRACKER, LIBRARY, MY }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: 추후 로그 삭제
        Log.d("ONB_FLOW", "MainActivity started")

//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        setContentView(R.layout.activity_main)


        // 최초 진입 시 홈 Fragment
        if (savedInstanceState == null) {
            setBottomNavSelected(NavTab.HOME)
            replaceFragment(HomeFragment())
        }

        initBottomNav()
    }

    private fun initBottomNav() {

        findViewById<View>(R.id.itemHome).setOnClickListener {
            selectTab(NavTab.HOME, HomeFragment())
        }

        findViewById<View>(R.id.itemGroup).setOnClickListener {
            selectTab(NavTab.GROUP, GroupFragment())
        }

        findViewById<View>(R.id.itemTracker).setOnClickListener {
            selectTab(NavTab.TRACKER, TrkHostMainFragment())
        }

        findViewById<View>(R.id.itemLibrary).setOnClickListener {
            selectTab(NavTab.LIBRARY, LibraryFragment())
        }

        findViewById<View>(R.id.itemMy).setOnClickListener {
            selectTab(NavTab.MY, MypageFragment())
        }
    }

    private fun selectTab(tab: NavTab, fragment: Fragment) {
        setBottomNavSelected(tab)
        replaceFragment(fragment)
    }

    fun moveToGroupTab() {
        selectTab(NavTab.GROUP, GroupFragment())
    }

    fun moveToTrackerDetail(groupId: Long, role: ExchangeRole) {

        val intent = when (role) {
            ExchangeRole.GUEST -> Intent(this, GuestActivity::class.java)
            ExchangeRole.HOST -> Intent(this, HostActivity::class.java)
        }

        intent.putExtra("group_id", groupId) // ✅ 여기 키가 핵심
        startActivity(intent)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun setBottomNavSelected(tab: NavTab) {

        fun setItem(
            ivId: Int,
            tvId: Int,
            selectedIcon: Int,
            unselectedIcon: Int,
            selected: Boolean
        ) {
            val iv = findViewById<ImageView>(ivId)
            val tv = findViewById<TextView>(tvId)

            iv.setImageResource(if (selected) selectedIcon else unselectedIcon)
            tv.setTextColor(
                if (selected) getColor(R.color.grey_900)
                else getColor(R.color.grey_400)
            )
        }

        setItem(
            R.id.ivHome, R.id.tvHome,
            R.drawable.ic_home_selected,
            R.drawable.ic_home_unselected,
            tab == NavTab.HOME
        )

        setItem(
            R.id.ivGroup, R.id.tvGroup,
            R.drawable.ic_group_selected,
            R.drawable.ic_group_unselected,
            tab == NavTab.GROUP
        )

        setItem(
            R.id.ivTracker, R.id.tvTracker,
            R.drawable.ic_tracker_selected,
            R.drawable.ic_tracker_unselected,
            tab == NavTab.TRACKER
        )

        setItem(
            R.id.ivLibrary, R.id.tvLibrary,
            R.drawable.ic_library_selected,
            R.drawable.ic_library_unselected,
            tab == NavTab.LIBRARY
        )

        setItem(
            R.id.ivMy, R.id.tvMy,
            R.drawable.ic_mypage_selected,
            R.drawable.ic_mypage_unselected,
            tab == NavTab.MY
        )
    }
}