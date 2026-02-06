package com.bookiibookii.bookiibookii.trkGuest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.databinding.FragmentTrkGuestMainBinding
import com.bookiibookii.bookiibookii.trkDirectGuest.DirectGuestActivity
import com.bookiibookii.bookiibookii.trkHost.CreateGroupFooterAdapter
import com.bookiibookii.bookiibookii.trkHost.ExchangeType
import com.bookiibookii.bookiibookii.trkHost.FooterMode
import com.bookiibookii.bookiibookii.trkHost.TrackerAdapter
import com.bookiibookii.bookiibookii.trkHost.TrackerData
import com.bookiibookii.bookiibookii.trkHost.TrackerStatus


class TrkGuestMainFragment : Fragment() {

    private var _binding: FragmentTrkGuestMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var trackerAdapter: TrackerAdapter
    private lateinit var footerAdapter: CreateGroupFooterAdapter
    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrkGuestMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrkGuestMainBinding.bind(view)

        setupToggleLogic()
        updateTabState(isMyGroup = false)

        trackerAdapter = TrackerAdapter { item ->
            val target = when (item.exchangeType) {
                ExchangeType.DELIVERY -> GuestActivity::class.java
                ExchangeType.DIRECT -> DirectGuestActivity::class.java
                ExchangeType.NONE -> TODO()
            }

            val intent = Intent(requireContext(), target).apply {
                putExtra("tracker_id", item.id)
                putExtra("exchange_type", item.exchangeType.name)
            }

            startActivity(intent)
        }

        footerAdapter = CreateGroupFooterAdapter(
            mode = FooterMode.GUEST_JOIN,
            onActionClick = {
                // TODO: 그룹 참여 화면 이동
            }
        )

        concatAdapter = ConcatAdapter(trackerAdapter, footerAdapter)

        binding.trkRecyclerview.apply {
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        trackerAdapter.submitList(createDummyTrackerList()) {
            footerAdapter.setShowEmptyText(trackerAdapter.itemCount == 0)
        }

    }

    private fun setupToggleLogic() {
        binding.myGroupBt.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.joinedGroupBt.setOnClickListener {
            updateTabState(isMyGroup = false)
        }
    }

    private fun updateTabState(isMyGroup: Boolean) {
        binding.myGroupBt.isSelected = isMyGroup
        binding.joinedGroupBt.isSelected = !isMyGroup
    }

    // 더미 데이터
    private fun createDummyTrackerList(): List<TrackerData> {
        return listOf(
            TrackerData(
                id = 1L,
                bookTitle = "살인자의 기억법",
                bookAuthor = "김영하",
                bookCategory = "소설",
                withUserName = "noshel",
                coverImageUrl = null,

                // 1. SHIPPING -> DELIVERY 변경
                exchangeType = ExchangeType.DELIVERY,

                stepDates = listOf("2024.01.01", null, null, null),

                // 2. currentStep(TrackerStep) -> currentStatus(TrackerStatus) 변경
                currentStatus = TrackerStatus.HOST_READING,

                hostProfileImageUrl = null,
                guestProfileImageUrl = null
            ),
            TrackerData(
                id = 2L,
                bookTitle = "아몬드",
                bookAuthor = "손원평",
                bookCategory = "청소년 문학",
                withUserName = null,
                coverImageUrl = null,
                exchangeType = ExchangeType.DIRECT,

                stepDates = listOf("2024.02.10", "2024.02.15", null, null),

                // 3. 변경된 Enum 사용
                currentStatus = TrackerStatus.GUEST_READING,

                hostProfileImageUrl = null,
                guestProfileImageUrl = null
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}