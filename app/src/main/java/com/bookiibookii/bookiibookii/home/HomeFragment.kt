package com.bookiibookii.bookiibookii.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentHomeBinding
import com.bookiibookii.bookiibookii.databinding.SectionHomeExchangeProgressBinding
import com.bookiibookii.bookiibookii.databinding.SectionHomeGroupBinding
import com.bookiibookii.bookiibookii.databinding.SectionHomeMateBinding
import com.bookiibookii.bookiibookii.home.notification.ui.NotificationActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // section Binding
    private lateinit var exchangeBinding: SectionHomeExchangeProgressBinding
    private lateinit var groupBinding: SectionHomeGroupBinding
    private lateinit var mateBinding: SectionHomeMateBinding

    // TODO: 나중에 서버 데이터 연결 시 어댑터에 리스트 주입 로직 추가 필요
    private val exchangeAdapter = ExchangeProgressAdapter()

    private val groupAdapter = GroupRecommendAdapter { item ->
        // TODO: groupId로 그룹 상세 이동
        // val intent = Intent(requireContext(), GroupDetailActivity::class.java)
        // intent.putExtra("groupId", item.groupId)
        // startActivity(intent)
    }

    private val api by lazy { RetrofitClient.api() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // section binding
        exchangeBinding = binding.sectionExchange
        groupBinding = binding.sectionGroup
        mateBinding = binding.sectionMate

        // RecyclerView 세팅
        exchangeBinding.rvExchangeProgress.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exchangeAdapter
        }

        groupBinding.rvGroupCard.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = groupAdapter

            val spacing = resources.getDimensionPixelSize(R.dimen.spacing_12)
            addItemDecoration(GridSpacingItemDecoration(3, spacing))
        }
        // empty 카드 문구 세팅
        setExchangeEmptyTexts()
        setGroupEmptyTexts()
        setMateEmptyTexts()

        bindHeaderActions()
        bindEmptyActions()

        // 그룹 추천 최초 로드
        loadRecommendedGroups(refresh = false)

        // 그룹 새로고침 버튼
        groupBinding.btnRefresh.setOnClickListener {
            loadRecommendedGroups(refresh = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindHeaderActions() {

        // include된 헤더(root)에서 알림 아이콘 찾기
        val headerRoot = binding.sectionHomeHeader.root
        val ivNoti = headerRoot.findViewById<ImageView>(R.id.iv_home_notification)

        ivNoti.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setNotificationBadgeVisible(visible: Boolean) {
        val headerRoot = binding.sectionHomeHeader.root
        val badge = headerRoot.findViewById<View>(R.id.view_home_notification_badge)
        badge.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun applyExchangeState(hasData: Boolean) {
        exchangeBinding.rvExchangeProgress.visibility = if (hasData) View.VISIBLE else View.GONE
        exchangeBinding.includeExchangeEmpty.root.visibility = if (hasData) View.GONE else View.VISIBLE
        exchangeBinding.layoutPageControl.visibility = if (hasData) View.VISIBLE else View.GONE
    }

    private fun applyGroupState(hasData: Boolean) {
        groupBinding.rvGroupCard.visibility = if (hasData) View.VISIBLE else View.GONE
        groupBinding.includeGroupEmpty.root.visibility = if (hasData) View.GONE else View.VISIBLE
        groupBinding.btnRefresh.visibility = if (hasData) View.VISIBLE else View.GONE
    }

    private fun applyMateState(hasData: Boolean) {
        mateBinding.includeMateCard.root.visibility = if (hasData) View.VISIBLE else View.GONE
        mateBinding.includeMateEmpty.root.visibility = if (hasData) View.GONE else View.VISIBLE
    }

    private fun setExchangeEmptyTexts() {
        val empty = exchangeBinding.includeExchangeEmpty
        empty.tvEmptyTitle.text = "아직 진행 중인 교환이 없어요"
        empty.tvEmptyDesc.text = "그룹에 참여하고 새로운 책을 만나보세요."
        empty.btnHomeAction.text = "그룹 둘러보기"
    }

    private fun setGroupEmptyTexts() {
        val empty = groupBinding.includeGroupEmpty
        empty.tvEmptyTitle.text = "아직 추천할 그룹이 없어요"
        empty.tvEmptyDesc.text = "읽고 싶은 책으로 그룹을 직접 만들어보세요."
        empty.btnHomeAction.text = "그룹 만들기"
    }

    private fun setMateEmptyTexts() {
        val empty = mateBinding.includeMateEmpty
        empty.tvEmptyTitle.text = "추천할 부키메이트가 없어요"
        empty.tvEmptyDesc.text = "책을 더 많이 읽고 활동하면 취향이 비슷한 메이트를 추천해드려요."
        empty.btnHomeAction.text = "서재 채우기"
    }

    private fun bindEmptyActions() {
        exchangeBinding.includeExchangeEmpty.btnHomeAction.setOnClickListener {
            // TODO: GRP-001 이동
        }

        groupBinding.includeGroupEmpty.btnHomeAction.setOnClickListener {
            // TODO: GRP-020 이동
        }

        mateBinding.includeMateEmpty.btnHomeAction.setOnClickListener {
            // TODO: GRP-001 이동
        }
    }

    private fun loadRecommendedGroups(refresh: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                api.getRecommendedGroups(refresh = refresh)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val body = response.body()

                    //TODO: 추후 로그 삭제
                    android.util.Log.d("GROUP_API", "body=$body")
                    android.util.Log.d("GROUP_API", "first=${body?.result?.firstOrNull()}")

                    val list = body?.result.orEmpty()

                    if (list.isNotEmpty()) {
                        groupAdapter.submitList(list)
                        applyGroupState(true)
                    } else {
                        groupAdapter.submitList(emptyList())
                        applyGroupState(false)
                    }
                } else {
                    groupAdapter.submitList(emptyList())
                    applyGroupState(false)
                }
            }.onFailure {
                groupAdapter.submitList(emptyList())
                applyGroupState(false)
            }
        }
    }
}