package com.bookiibookii.bookiibookii.group

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.databinding.ActivityGrpHostBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class GroupHostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpHostBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGrpHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initBottomSheet()
    }

    private fun initView() {
        // 뒤로가기 버튼
        binding.actGrpHoBackIv.setOnClickListener {
            // 바텀시트가 열려있으면 닫고, 아니면 액티비티 종료
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                finish()
            }
        }

        // (참고) 리사이클러뷰 설정이 필요하다면 여기서 연결
        // binding.grpMgBottomSheetInfoRv.layoutManager = ...
    }

    private fun initBottomSheet() {
        val bottomSheetLayout = binding.persistentBottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)

        // [중요] 접혔을 때 높이 계산: (상단 핸들+타이틀 영역 높이) + (하단 입력창 영역 높이)
        // 대략 130dp ~ 140dp 정도로 설정하면
        // 화면 하단에 입력창이 있고, 그 바로 위에 '댓글' 타이틀이 빼꼼 나와있는 형태가 됩니다.
        bottomSheetBehavior.peekHeight = dpToPx(135)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isHideable = false
    }

    // dp -> px 변환 유틸
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}