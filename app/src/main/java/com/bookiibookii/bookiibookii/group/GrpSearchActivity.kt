package com.bookiibookii.bookiibookii.group

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ActivityGrpSearchBinding
import com.google.android.material.chip.Chip

class GrpSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 바인딩 연결
        binding = ActivityGrpSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. 뒤로가기 버튼
        binding.actGrpSearchBackIv.setOnClickListener {
            finish() // 액티비티 종료
        }

        // 3. 키보드 검색 버튼 눌렀을 때 처리
        binding.actGrpSearchBar.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.actGrpSearchBar.text.toString()
                performSearch(query)
                return@setOnEditorActionListener true
            }
            false
        }

        // 4. 인기 검색어 칩 추가하기
        val popularKeywords = listOf(
            "인사이트", "한강", "트렌드코리아", "천선란",
            "베스트셀러", "김영하", "아몬드", "자몽살구클럽",
            "지적대화를위한넓고얕은지식", "채사장", "최재천"
        )

        popularKeywords.forEach { keyword ->
            addChip(keyword)
        }

        // 그룹 만들기 버튼 클릭
        binding.actGrpNextBtn.setOnClickListener {
            val intent = Intent(this, GroupGenerationActivity::class.java)

            startActivity(intent)
        }
    }

    private fun addChip(text: String) {
        val chip = Chip(this).apply {
            this.text = text
            isCheckable = false
            isClickable = true
            isFocusable = true

            // 스타일 먼저 적용 (폰트 크기 등 기본 설정)
            setTextAppearance(R.style.Widget_App_Chip_Tag)

            // 배경 설정
            setChipBackgroundColorResource(android.R.color.white)

            // 테두리 설정
            setChipStrokeColorResource(android.R.color.darker_gray)
            chipStrokeWidth = dpToPx(1).toFloat()
            chipCornerRadius = dpToPx(30).toFloat()

            setTextColor(Color.parseColor("#858481"))

            // 칩 클릭 시 해당 단어로 검색
            setOnClickListener {
                binding.actGrpSearchBar.setText(text)
                binding.actGrpSearchBar.setSelection(text.length) // 커서 맨 뒤로
                performSearch(text)
            }
        }
        // 칩 그룹에 추가
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            Toast.makeText(this, "검색어: $query", Toast.LENGTH_SHORT).show()
            // TODO: 실제 검색 결과 화면으로 이동하거나 리스트 갱신
        }
    }

    // dp -> px 변환 함수
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}