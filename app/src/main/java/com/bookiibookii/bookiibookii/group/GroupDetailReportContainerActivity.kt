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
            // 1. 프래그먼트 생성
            val fragment = MypReportWriteFragment()

            // 2. 인텐트로 받은 데이터(예: 그룹ID)를 프래그먼트에 전달 (선택사항)
            // val groupId = intent.getIntExtra("GROUP_ID", -1)
            // val bundle = Bundle()
            // bundle.putInt("GROUP_ID", groupId)
            // fragment.arguments = bundle

            // 3. 화면에 붙이기
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}