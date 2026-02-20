package com.bookiibookii.bookiibookii

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.common.BaseActivity
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.ActivityMainBinding
import com.bookiibookii.bookiibookii.group.main.GroupFragment
import com.bookiibookii.bookiibookii.home.ExchangeRole
import com.bookiibookii.bookiibookii.home.HomeFragment
import com.bookiibookii.bookiibookii.home.OtherProfileFragment
import com.bookiibookii.bookiibookii.lib.LibraryAddCardFragment
import com.bookiibookii.bookiibookii.lib.LibraryBookDetailFragment
import com.bookiibookii.bookiibookii.lib.LibraryBookDetailIngFragment
import com.bookiibookii.bookiibookii.lib.LibraryBookDetailRelayWriteFragment
import com.bookiibookii.bookiibookii.lib.LibraryFragment
import com.bookiibookii.bookiibookii.myPage.MypageFragment
import com.bookiibookii.bookiibookii.trkGuest.GuestActivity
import com.bookiibookii.bookiibookii.trkHost.HostActivity
import com.bookiibookii.bookiibookii.trkHost.TrkHostMainFragment
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
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

      //  setContentView(R.layout.activity_main)


        // 최초 진입 시 홈 Fragment
        if (savedInstanceState == null) {
            setBottomNavSelected(NavTab.HOME)
            replaceFragment(HomeFragment())
        }

        initBottomNav()
        handleNavigationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // 새로운 Intent로 교체
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        when (intent?.getStringExtra("NAV_ACTION")) {
            "OPEN_RELAY_WRITE" -> {
                val groupId = intent.getLongExtra("target_group_id", -1L)
                val userBookId = intent.getIntExtra("target_user_book_id", -1)
                if (groupId != -1L && userBookId != -1) {
                    moveToRelayWriteFragment(groupId, userBookId)
                }
            }

            "OPEN_LIBRARY_ING" -> {
                val groupId = intent.getLongExtra("target_group_id", -1L)
                if (groupId != -1L) {
                    moveToLibraryIngFragment(groupId)
                }
            }

            "OPEN_ADD_CARD" -> {
                val groupId = intent.getLongExtra("target_group_id", -1L)
                if (groupId != -1L) {
                    moveToLibraryAddCardFragment(groupId)
                }
            }

            "OPEN_LIBRARY_DETAIL" -> {
                val groupId = intent.getLongExtra("target_group_id", -1L)
                if (groupId != -1L) {
                    moveToLibraryDetailFragment(groupId)
                }
            }

            "OPEN_MYP_REPORT" -> {
                moveToMypReportFragment()
            }
        }
    }

    private fun moveToMypReportFragment() {
        setBottomNavSelected(NavTab.MY)

        val fragment = com.bookiibookii.bookiibookii.myPage.report.MypReportFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()

        intent.removeExtra("NAV_ACTION")
    }


    private fun moveToLibraryDetailFragment(groupId: Long) {
        setBottomNavSelected(NavTab.LIBRARY)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getLibraryBooks()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = response.body()?.result ?: emptyList()

                    val target = list.find { it.groupId.toLong() == groupId } ?: return@launch

                    val fragment = LibraryBookDetailFragment().apply {
                        arguments = Bundle().apply {
                            putInt("groupId", target.groupId)
                            putInt("userBookId", target.userBookId)

                            putString("bookTitle", target.title)
                            putString("bookAuthor", target.author)
                            putString("bookCover", target.image ?: "")

                            putString("hostName", target.hostNickName ?: "")
                            putString("hostProfileUrl", target.hostProfileImageUrl ?: "")

                            putString("startDate", target.startDate)
                            putString("endDate", target.endDate ?: "")

                            putDouble("rating", target.rating)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()

                    intent.removeExtra("NAV_ACTION")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun moveToLibraryAddCardFragment(groupId: Long) {
        setBottomNavSelected(NavTab.LIBRARY)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getLibraryBooks()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = response.body()?.result ?: emptyList()

                    val target = list.find { it.groupId.toLong() == groupId }
                    if (target == null) {
                        return@launch
                    }

                    val fragment = LibraryAddCardFragment().apply {
                        arguments = Bundle().apply {
                            putBoolean("isEdit", false)
                            putInt("userBookId", target.userBookId)
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()

                    intent.removeExtra("NAV_ACTION")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun moveToRelayWriteFragment(groupId: Long, userBookId: Int) {
        setBottomNavSelected(NavTab.LIBRARY)

        val fragment = LibraryBookDetailRelayWriteFragment().apply {
            arguments = Bundle().apply {
                putInt("groupId", groupId.toInt())
                putInt("userBookId", userBookId)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()

        intent.removeExtra("NAV_ACTION")
    }

    private fun moveToLibraryIngFragment(groupId: Long) {
        setBottomNavSelected(NavTab.LIBRARY)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getLibraryBooks()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = response.body()?.result ?: emptyList()

                    val target = list.find { it.groupId.toLong() == groupId }
                    if (target == null) {
                        return@launch
                    }

                    val fragment = LibraryBookDetailIngFragment().apply {
                        arguments = Bundle().apply {
                            putInt("userBookId", target.userBookId)
                            putInt("groupId", target.groupId)

                            putString("bookTitle", target.title)
                            putString("bookAuthor", target.author)
                            putString("bookCover", target.image ?: "")

                            putString("hostName", target.hostNickName ?: "")
                            putString("hostProfileUrl", target.hostProfileImageUrl ?: "")

                            putString("startDate", target.startDate)
                            putString("endDate", target.endDate ?: "")
                        }
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()

                    intent.removeExtra("NAV_ACTION")

                } else {

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    fun moveToOtherProfile(nickname: String) {
        val fragment = OtherProfileFragment().apply {
            arguments = Bundle().apply {
                putString(OtherProfileFragment.ARG_NICKNAME, nickname)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment) // ✅ 여기 수정
            .addToBackStack(null) // 뒤로가기 가능
            .commit()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun moveToMyPageTab() {
        // 내부적으로 탭 아이콘을 바꾸고(setBottomNavSelected),
        // 프래그먼트를 마이페이지로 교체(replaceFragment)합니다.
        selectTab(NavTab.MY, MypageFragment())
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

    override fun setupWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 상단은 fragmentContainer에만 적용
            binding.fragmentContainer.updatePadding(
                top = systemBars.top
            )

            // 하단은 bottomNav에 padding ❌
            // margin으로 처리해야 constraint 안 깨짐

            val params = binding.bottomNav.root.layoutParams as ConstraintLayout.LayoutParams
            params.bottomMargin = systemBars.bottom
            binding.bottomNav.root.layoutParams = params

            insets
        }
    }

}