package com.bookiibookii.bookiibookii

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.databinding.FragmentGrpBinding // 패키지명 확인!

class GroupFragment : Fragment() {

    // ViewBinding 설정
    private var _binding: FragmentGrpBinding? = null

    // binding 변수를 통해 XML의 ID에 접근합니다.
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

        // 데이터 준비 (더미 데이터)
        val groupList = ArrayList<GroupData>()

        // 예시 데이터 추가 (원하는 만큼 추가하세요)
        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/200",
                date = "2025. 12. 16.",
                status = "모집 중",
                profileImgUrl = "https://picsum.photos/100/100",
                nickname = "noshel",
                bookTitle = "괴테는 모든 것을 말했다",
                bookAuthor = "스즈키 유이 지음",
                memberCount = "1/2명",
                deadline = "7일",
                tags = listOf("#메모환영", "#인사이트", "#깔끔")
            )
        )

        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/200",
                date = "2025. 12. 16.",
                status = "모집 중",
                profileImgUrl = "https://picsum.photos/100/100",
                nickname = "noshel",
                bookTitle = "괴테는 모든 것을 말했다",
                bookAuthor = "스즈키 유이 지음",
                memberCount = "1/2명",
                deadline = "7일",
                tags = listOf("#메모환영", "#인사이트", "#깔끔")
            )
        )
        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/200",
                date = "2025. 12. 16.",
                status = "모집 중",
                profileImgUrl = "https://picsum.photos/100/100",
                nickname = "noshel",
                bookTitle = "괴테는 모든 것을 말했다",
                bookAuthor = "스즈키 유이 지음",
                memberCount = "1/2명",
                deadline = "7일",
                tags = listOf("#메모환영", "#인사이트", "#깔끔")
            )
        )
        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/200",
                date = "2025. 12. 16.",
                status = "모집 중",
                profileImgUrl = "https://picsum.photos/100/100",
                nickname = "noshel",
                bookTitle = "괴테는 모든 것을 말했다",
                bookAuthor = "스즈키 유이 지음",
                memberCount = "1/2명",
                deadline = "7일",
                tags = listOf("#메모환영", "#인사이트", "#깔끔")
            )
        )
        groupList.add(
            GroupData(
                coverImgUrl = "https://picsum.photos/300/200",
                date = "2025. 12. 16.",
                status = "모집 중",
                profileImgUrl = "https://picsum.photos/100/100",
                nickname = "noshel",
                bookTitle = "괴테는 모든 것을 말했다",
                bookAuthor = "스즈키 유이 지음",
                memberCount = "1/2명",
                deadline = "7일",
                tags = listOf("#메모환영", "#인사이트", "#깔끔")
            )
        )


        // 2. 어댑터 생성 및 연결
        // (이전에 만든 GroupAdapter 클래스가 있어야 합니다)
        val groupAdapter = GroupAdapter(groupList)


        // 3. 리사이클러뷰 설정
        binding.groupRecyclerview.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context) // Fragment에서는 context 사용
            // [추가] 아이템 간격 데코레이션 적용
            addItemDecoration(VerticalSpaceItemDecoration(48)) // 48px (약 16dp) 정도
        }

    }

    // Fragment가 화면에서 사라질 때 메모리 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            // 마지막 아이템이 아니면 아래쪽에 공간을 줌
            if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1) {
                outRect.bottom = verticalSpaceHeight
            }
        }
    }
}
