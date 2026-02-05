package com.bookiibookii.bookiibookii.group

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.ContextThemeWrapper
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ActivityGrpSearchBinding
import com.google.android.material.chip.Chip

class GrpSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpSearchBinding
    private val viewModel: GrpSearchViewModel by viewModels()
    private val popularAdapter by lazy {
        PopularSearchAdapter { keyword ->
            binding.actGrpSearchBar.setText(keyword)
            performSearch(keyword)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initViewModel()
        initListeners()
        setupChipGroup()
    }

    private fun initViews() {
        binding.actSearchHotGrpRv.adapter = popularAdapter
    }
    private fun initViewModel() {
        // 리스트 업데이트 (3개 <-> 10개)
        viewModel.displayList.observe(this) { list ->
            TransitionManager.beginDelayedTransition(binding.root as android.view.ViewGroup, AutoTransition())
            popularAdapter.submitList(list)
        }

        //화살표 회전
        viewModel.isExpanded.observe(this) { isExpanded ->
            val angle = if (isExpanded) 180f else 0f
            binding.actSearchHotGrpCardArrowDownIv.animate().rotation(angle).setDuration(300).start()
        }
    }

    private fun initListeners() {
        binding.actGrpSearchBackIv.setOnClickListener { finish() }

        // 화살표 클릭 시 뷰모델 토글
        binding.actSearchHotGrpCardArrowDownIv.setOnClickListener {
            viewModel.toggleExpansion()
        }

        binding.actGrpSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.actGrpSearchBar.text.toString())
                true
            } else false
        }

        binding.actGrpNextBtn.setOnClickListener {
            val intent = Intent(this, GroupGenerationActivity::class.java)
            intent.putExtra("IS_EDIT_MODE", false)
            startActivity(intent)
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) Toast.makeText(this, "검색어: $query", Toast.LENGTH_SHORT).show()
    }

    private fun setupChipGroup() {
        val popularKeywords = listOf("인사이트", "한강", "트렌드코리아", "천선란", "베스트셀러")
        popularKeywords.forEach { addChip(it) }
    }

    private fun addChip(text: String) {
        val chip = layoutInflater.inflate(R.layout.view_chip_entry, binding.chipGroupRecentSearch, false) as Chip

        // 텍스트 설정
        chip.text = text

        // 클릭 시 검색
        chip.setOnClickListener {
            binding.actGrpSearchBar.setText(text)
            performSearch(text)
        }

        // X 버튼 클릭 시 삭제 (Entry 스타일의 핵심)
        chip.setOnCloseIconClickListener {
            binding.chipGroupRecentSearch.removeView(chip)
        }

        // 칩 그룹에 추가
        binding.chipGroupRecentSearch.addView(chip)
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}