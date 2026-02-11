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
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentHomeBinding
import com.bookiibookii.bookiibookii.databinding.SectionHomeExchangeProgressBinding
import com.bookiibookii.bookiibookii.databinding.SectionHomeGroupBinding
import com.bookiibookii.bookiibookii.databinding.SectionHomeMateBinding
import com.bookiibookii.bookiibookii.group.GroupDetailActivity
import com.bookiibookii.bookiibookii.group.generation.GroupGenerationActivity
import com.bookiibookii.bookiibookii.home.notification.ui.NotificationActivity
import kotlinx.coroutines.launch
import kotlin.jvm.java

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var exchangeBinding: SectionHomeExchangeProgressBinding
    private lateinit var groupBinding: SectionHomeGroupBinding
    private lateinit var mateBinding: SectionHomeMateBinding

    private val api by lazy { RetrofitClient.api() }
    private val trkApi by lazy { RetrofitClient.trkApi() } // ✅ 여기로 받기

    private var notiBadge: View? = null

    private val exchangeAdapter = ExchangeProgressAdapter { item ->
        (activity as? MainActivity)?.moveToTrackerDetail(item.groupId, item.role)
    }

    private val groupAdapter = GroupRecommendAdapter { item ->
        android.util.Log.d("HOME_GROUP", "click groupId=${item.groupId}")

        val intent = Intent(requireContext(), GroupDetailActivity::class.java)
        intent.putExtra("GROUP_ID", item.groupId)
        startActivity(intent)
    }

    private val mateAdapter = MateRecommendAdapter { _ ->
        // TODO
    }

    private var exchangeTotal = 0

    override fun onResume() {
        super.onResume()
        refreshNotiBadge()
    }

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

        exchangeBinding = binding.sectionExchange
        groupBinding = binding.sectionGroup
        mateBinding = binding.sectionMate

        // 그룹/메이트 리사이클러
        groupBinding.rvGroupCard.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = groupAdapter
            val spacing = resources.getDimensionPixelSize(R.dimen.spacing_12)
            addItemDecoration(GridSpacingItemDecoration(3, spacing))
        }

        mateBinding.rvMate.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = mateAdapter
            val spacing = resources.getDimensionPixelSize(R.dimen.spacing_12)
            addItemDecoration(HorizontalSpacingItemDecoration(spacing))
        }

        // empty 카드 문구
        setExchangeEmptyTexts()
        setGroupEmptyTexts()
        setMateEmptyTexts()

        // 진행 중 교환: ViewPager2 세팅 + 로드
        setupExchangePager()
        loadExchangeProgress()

        bindHeaderActions()
        bindEmptyActions()

        loadRecommendedGroups(refresh = false)
        groupBinding.btnRefresh.setOnClickListener { loadRecommendedGroups(refresh = true) }

        loadRecommendedBookmates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindHeaderActions() {
        val headerRoot = binding.sectionHomeHeader.root
        val ivNoti = headerRoot.findViewById<ImageView>(R.id.iv_home_notification)
        notiBadge = headerRoot.findViewById(R.id.view_home_notification_badge)

        ivNoti.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
    }

    private fun setNotiBadgeVisible(visible: Boolean) {
        notiBadge?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun refreshNotiBadge() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val size = 20

                // SYSTEM 1페이지
                val systemRes = api.getNotifications(
                    category = "SYSTEM",
                    cursor = null,
                    size = size
                )

                // KEYWORD 1페이지
                val keywordRes = api.getNotifications(
                    category = "KEYWORD",
                    cursor = null,
                    size = size
                )

                val systemItems = if (systemRes.isSuccessful) systemRes.body()?.result?.items.orEmpty() else emptyList()
                val keywordItems = if (keywordRes.isSuccessful) keywordRes.body()?.result?.items.orEmpty() else emptyList()

                val hasUnreadSystem = systemItems.any { !it.isRead }
                val hasUnreadKeyword = keywordItems.any { !it.isRead }

                hasUnreadSystem || hasUnreadKeyword
            }.onSuccess { hasUnread ->
                setNotiBadgeVisible(hasUnread)
            }.onFailure {
                // 실패 시는 일단 숨김(원하면 "이전 상태 유지"로 바꿔도 됨)
                setNotiBadgeVisible(false)
            }
        }
    }

    private fun applyExchangeState(hasData: Boolean) {
        exchangeBinding.vpExchangeProgress.visibility = if (hasData) View.VISIBLE else View.GONE
        exchangeBinding.includeExchangeEmpty.root.visibility = if (hasData) View.GONE else View.VISIBLE
        exchangeBinding.layoutPageControl.visibility = if (hasData) View.VISIBLE else View.GONE
    }

    private fun applyGroupState(hasData: Boolean) {
        groupBinding.rvGroupCard.visibility = if (hasData) View.VISIBLE else View.GONE
        groupBinding.includeGroupEmpty.root.visibility = if (hasData) View.GONE else View.VISIBLE
        groupBinding.btnRefresh.visibility = if (hasData) View.VISIBLE else View.GONE
    }

    private fun applyMateState(hasData: Boolean) {
        mateBinding.rvMate.visibility = if (hasData) View.VISIBLE else View.GONE
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
            (activity as? MainActivity)?.moveToGroupTab()
        }

        groupBinding.includeGroupEmpty.btnHomeAction.setOnClickListener {
            startActivity(Intent(requireContext(), GroupGenerationActivity::class.java))
        }

        mateBinding.includeMateEmpty.btnHomeAction.setOnClickListener {
            (activity as? MainActivity)?.moveToGroupTab()
        }
    }

    private fun loadRecommendedGroups(refresh: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { api.getRecommendedGroups(refresh = refresh) }
                .onSuccess { response ->
                    val list = if (response.isSuccessful) response.body()?.result.orEmpty() else emptyList()
                    android.util.Log.d("HOME_API", "groups=$list")

                    groupAdapter.submitList(list)
                    applyGroupState(list.isNotEmpty())
                }
                .onFailure {
                    groupAdapter.submitList(emptyList())
                    applyGroupState(false)
                }
        }
    }

    private fun loadRecommendedBookmates() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { api.getRecommendedBookmates() }
                .onSuccess { response ->
                    if (!response.isSuccessful) {
                        mateAdapter.submitList(emptyList())
                        applyMateState(false)
                        return@onSuccess
                    }

                    val body = response.body()
                    if (body?.isSuccess == false && body.code == "USERTAG404") {
                        mateAdapter.submitList(emptyList())
                        applyMateState(false)
                        return@onSuccess
                    }

                    val list = body?.result.orEmpty().take(5)
                    mateAdapter.submitList(list)
                    applyMateState(list.isNotEmpty())
                }
                .onFailure {
                    mateAdapter.submitList(emptyList())
                    applyMateState(false)
                }
        }
    }

    // ----------------------------
    // 진행 중인 교환(ViewPager2)
    // ----------------------------
    private fun setupExchangePager() {
        exchangeBinding.vpExchangeProgress.adapter = exchangeAdapter
        exchangeBinding.vpExchangeProgress.offscreenPageLimit = 1
        exchangeBinding.vpExchangeProgress.orientation =
            androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL

        exchangeBinding.vpExchangeProgress.registerOnPageChangeCallback(
            object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateExchangeIndicator(position, exchangeTotal)
                    updateExchangeArrowState(position, exchangeTotal)
                }
            }
        )

        exchangeBinding.btnPrev.setOnClickListener {
            val cur = exchangeBinding.vpExchangeProgress.currentItem
            if (cur > 0) exchangeBinding.vpExchangeProgress.setCurrentItem(cur - 1, true)
        }

        exchangeBinding.btnNext.setOnClickListener {
            val cur = exchangeBinding.vpExchangeProgress.currentItem
            if (cur < exchangeTotal - 1) exchangeBinding.vpExchangeProgress.setCurrentItem(cur + 1, true)
        }
    }

    private fun loadExchangeProgress() {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val guestRes = trkApi.getGuestTrackers()
                val hostRes = trkApi.getHostTrackers()

                val guestList = if (guestRes.isSuccessful) guestRes.body()?.result.orEmpty() else emptyList()
                val hostList = if (hostRes.isSuccessful) hostRes.body()?.result.orEmpty() else emptyList()

                android.util.Log.d("EXC_API", "guest=${guestList.size} host=${hostList.size}")

                val guestUi = guestList.map { it.toHomeExchangeItem() }
                val hostUi = hostList.map { it.toHomeExchangeItem() }

                // ✅ 최종 리스트를 여기서 확정해서 반환
                hostUi + guestUi
            }.onSuccess { list ->
                exchangeTotal = list.size

                if (exchangeTotal > 0) {
                    exchangeAdapter.submitList(list)
                    applyExchangeState(true)

                    exchangeBinding.vpExchangeProgress.setCurrentItem(0, false)
                    updateExchangeIndicator(0, exchangeTotal)
                    updateExchangeArrowState(0, exchangeTotal)
                } else {
                    exchangeAdapter.submitList(emptyList())
                    applyExchangeState(false)
                    updateExchangeIndicator(0, 0)
                }
            }.onFailure { e ->
                android.util.Log.e("EXC_API", "fail", e)
                exchangeAdapter.submitList(emptyList())
                applyExchangeState(false)
                updateExchangeIndicator(0, 0)
            }
        }
    }

    private fun updateExchangeIndicator(position: Int, total: Int) {
        if (total <= 0) {
            exchangeBinding.tvPageIndicator.text = "0"
            exchangeBinding.tvPageIndicatorTotal.text = "/0"
            return
        }
        exchangeBinding.tvPageIndicator.text = (position + 1).toString()
        exchangeBinding.tvPageIndicatorTotal.text = "/$total"
    }

    private fun updateExchangeArrowState(position: Int, total: Int) {
        val hasPrev = position > 0
        val hasNext = position < total - 1

        exchangeBinding.btnPrev.isEnabled = hasPrev
        exchangeBinding.btnNext.isEnabled = hasNext

        exchangeBinding.btnPrev.alpha = if (hasPrev) 1f else 0.3f
        exchangeBinding.btnNext.alpha = if (hasNext) 1f else 0.3f
    }
}