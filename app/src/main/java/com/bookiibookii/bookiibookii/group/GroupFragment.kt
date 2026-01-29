package com.bookiibookii.bookiibookii.group

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBinding

class GroupFragment : Fragment() {

    private var _binding: FragmentGrpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGrpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupList = ArrayList<GroupData>()

        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/200",
                bookTitle = "괴테는 모든 것을 말했다",
                bookAuthor = "스즈키 유이",
                bookGenre = "(소설)",
                status = "모집 중",
                deadline = "7",
                memberCount = "5",
                isHot = true,
                profileImgUrl = "https://picsum.photos/100/100",
                nickname = "noshel",
                date = "2025. 12. 16.",
                tags = listOf("#메모환영", "#인사이트", "#깔끔", "#태그", "#태그")
            )
        )

        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/201",
                bookTitle = "클린 코드",
                bookAuthor = "로버트 C. 마틴",
                bookGenre = "(IT/개발)",
                status = "마감 임박",
                deadline = "1",
                memberCount = "3",
                isHot = false,
                profileImgUrl = "https://picsum.photos/100/101",
                nickname = "dev_master",
                date = "2025. 12. 20.",
                tags = listOf("#개발", "#스터디", "#필독서")
            )
        )

        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/202",
                bookTitle = "돈의 속성",
                bookAuthor = "김승호",
                bookGenre = "(경제/경영)",
                status = "모집 중",
                deadline = "14",
                memberCount = "1",
                isHot = true,
                profileImgUrl = "https://picsum.photos/100/102",
                nickname = "rich_mind",
                date = "2025. 12. 22.",
                tags = listOf("#부자", "#투자", "#마인드")
            )
        )

        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/203",
                bookTitle = "미드나잇 라이브러리",
                bookAuthor = "매트 헤이그",
                bookGenre = "(소설)",
                status = "모집 완료",
                deadline = "0",
                memberCount = "10",
                isHot = false,
                profileImgUrl = "https://picsum.photos/100/103",
                nickname = "book_lover",
                date = "2025. 11. 30.",
                tags = listOf("#힐링", "#판타지", "#인생책")
            )
        )

        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/204",
                bookTitle = "사피엔스",
                bookAuthor = "유발 하라리",
                bookGenre = "(인문)",
                status = "모집 중",
                deadline = "3",
                memberCount = "2",
                isHot = true,
                profileImgUrl = "https://picsum.photos/100/104",
                nickname = "history_buff",
                date = "2025. 12. 10.",
                tags = listOf("#역사", "#인류", "#벽돌책")
            )
        )

        val groupAdapter = GroupAdapter(groupList) { groupData ->
            val intent = Intent(requireContext(), GroupDetailActivity::class.java)
            startActivity(intent)
        }

        binding.groupRecyclerview.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context)

            val spaceInPx = dpToPx(16)
            if (itemDecorationCount == 0) {
                addItemDecoration(VerticalSpaceItemDecoration(spaceInPx))
            }
        }

        binding.grpSearchIv.setOnClickListener {
            val intent = Intent(requireContext(), GrpSearchActivity::class.java)
            startActivity(intent)
        }

        binding.grpCategoryCp.setOnClickListener {
            val bottomSheet = FilterBottomSheetFragment { selectedCategory ->
                binding.grpCategoryCp.text = selectedCategory
                binding.grpCategoryCp.isChecked = true
            }
            bottomSheet.show(parentFragmentManager, "FilterBottomSheet")
        }

        binding.grpGroupListIv.setOnClickListener {
            val intent = Intent(requireContext(), GroupGenerationActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
                outRect.bottom = verticalSpaceHeight
            }
        }
    }
}