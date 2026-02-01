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
import com.bookiibookii.bookiibookii.trkHost.CreateGroupFooterAdapter
import com.bookiibookii.bookiibookii.trkHost.FooterMode
import com.bookiibookii.bookiibookii.trkHost.TrackerAdapter
import com.bookiibookii.bookiibookii.trkHost.TrackerData
import com.bookiibookii.bookiibookii.trkHost.TrackerStep


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
            // 클릭 로직 추가, 나중에 수정
            val intent = Intent(requireContext(), GuestActivity::class.java)
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
                withUserName = "noshel",
                coverImageUrl = null,
                currentStep = TrackerStep.DELIVERY
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}