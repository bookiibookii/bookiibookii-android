package com.bookiibookii.bookiibookii.group

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

    // ViewBinding 설정
    private var _binding: FragmentGrpBinding? = null

    // binding 변수를 통해 XML의 ID에 접근합니다.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // XML을 코드로 변환(Inflate)
        _binding = FragmentGrpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 데이터 준비 (더미 데이터)
        val groupList = ArrayList<GroupData>()

        // 데이터 1 (HOT 태그 있는 경우)
        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/200",
                bookTitle = "괴테는 모든 것을 말했다",
                bookAuthor = "스즈키 유이", // '지음' 텍스트 제거 (XML 구조상 깔끔하게 보이기 위함)
                bookGenre = "(소설)",
                status = "모집 중",
                deadline = "7",
                memberCount = "5",
                isHot = true, // HOT 태그 표시
                profileImgUrl = "https://picsum.photos/100/100",
                nickname = "noshel",
                date = "2025. 12. 16.",
                tags = listOf("#메모환영", "#인사이트", "#깔끔", "#태그", "#태그")
            )
        )

        // 데이터 2 (HOT 태그 없는 경우)
        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/201",
                bookTitle = "클린 코드",
                bookAuthor = "로버트 C. 마틴",
                bookGenre = "(IT/개발)",
                status = "마감 임박",
                deadline = "1",
                memberCount = "3",
                isHot = false, // HOT 태그 숨김
                profileImgUrl = "https://picsum.photos/100/101",
                nickname = "dev_master",
                date = "2025. 12. 20.",
                tags = listOf("#개발", "#스터디", "#필독서")
            )
        )

        // 데이터 3
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

        // 데이터 4
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

        // 데이터 5
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

        // 2. 어댑터 생성
        val groupAdapter = GroupAdapter(groupList)

        // 3. 리사이클러뷰 설정
        binding.groupRecyclerview.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context)

            // 16dp를 픽셀로 정확히 변환하여 간격 적용
            val spaceInPx = dpToPx(16)
            // 기존에 추가된 데코레이션이 있다면 중복되지 않게 한 번만 추가하도록 주의해야 하지만,
            // Fragment가 재생성될 때마다 초기화되므로 여기선 괜찮습니다.
            if (itemDecorationCount == 0) {
                addItemDecoration(VerticalSpaceItemDecoration(spaceInPx))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // dp 단위를 현재 기기의 픽셀(px) 단위로 변환하는 함수
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    // 아이템 간격 데코레이션 클래스
    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            // 마지막 아이템이 아니면 아래쪽에 계산된 높이만큼 공간을 줌
            if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
                outRect.bottom = verticalSpaceHeight
            }
        }
    }
}