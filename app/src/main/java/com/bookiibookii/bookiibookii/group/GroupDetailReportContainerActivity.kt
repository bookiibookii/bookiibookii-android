package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.myPage.report.MypReportWriteFragment

class GroupDetailReportContainerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_cantainer)

        if (savedInstanceState == null) {
            val groupId = intent.getLongExtra("GROUP_ID", -1L)
            val groupName = intent.getStringExtra("GROUP_NAME") ?: ""

            val fragment = MypReportWriteFragment().apply {
                arguments = Bundle().apply {
                    // "나 이제 그룹 고정 모드야"라고 신호를 보냄
                    putInt("FIXED_ID", groupId.toInt())
                    putString("FIXED_NAME", groupName)
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("REPORT_FRAGMENT")
                .commit()
        }

        // 프래그먼트가 popBackStack 되면 액티비티 종료
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) finish()
        }
    }
}