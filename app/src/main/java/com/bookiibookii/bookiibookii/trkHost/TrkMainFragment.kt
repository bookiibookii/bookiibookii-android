package com.bookiibookii.bookiibookii.trkHost

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.databinding.FragmentTrkMainBinding

class TrkMainFragment : Fragment() {
    private var _binding: FragmentTrkMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var trackerAdapter: TrackerAdapter
    private lateinit var footerAdapter: CreateGroupFooterAdapter
    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrkMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrkMainBinding.bind(view)

        setupToggleLogic()
        updateTabState(isMyGroup = true)

        trackerAdapter = TrackerAdapter { item ->
            // 클릭 로직 추가, 나중에 수정
            val intent = Intent(requireContext(), HostActivity::class.java)
            startActivity(intent)
        }

        footerAdapter = CreateGroupFooterAdapter(
            onCreateGroupClick = {
                // TODO: 그룹 만들기 화면 이동
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
            updateTabState(isMyGroup = true)
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
            ),
            TrackerData(
                id = 2L,
                bookTitle = "아몬드",
                bookAuthor = "손원평",
                withUserName = null,
                coverImageUrl = null,
                currentStep = TrackerStep.READING
            ),
            TrackerData(
                id = 3L,
                bookTitle = "살인자의 기억법",
                bookAuthor = "김영하",
                withUserName = "noshel",
                coverImageUrl = null,
                currentStep = TrackerStep.DELIVERY
            ),
            TrackerData(
                id = 4L,
                bookTitle = "아몬드67",
                bookAuthor = "손원평",
                withUserName = null,
                coverImageUrl = null,
                currentStep = TrackerStep.READING
            ),
            TrackerData(
                id = 1L,
                bookTitle = "아몬드123",
                bookAuthor = "손원평",
                withUserName = null,
                coverImageUrl = null,
                currentStep = TrackerStep.READING
            )
        )
    }
}