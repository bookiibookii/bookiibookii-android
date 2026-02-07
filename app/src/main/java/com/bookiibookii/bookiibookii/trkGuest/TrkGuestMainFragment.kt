package com.bookiibookii.bookiibookii.trkGuest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.databinding.FragmentTrkGuestMainBinding
import com.bookiibookii.bookiibookii.trkDirectGuest.DirectGuestActivity
import com.bookiibookii.bookiibookii.trkHost.CreateGroupFooterAdapter
import com.bookiibookii.bookiibookii.trkHost.ExchangeType
import com.bookiibookii.bookiibookii.trkHost.FooterMode
import com.bookiibookii.bookiibookii.trkHost.TrackerAdapter
import kotlinx.coroutines.launch


class TrkGuestMainFragment : Fragment() {

    private var _binding: FragmentTrkGuestMainBinding? = null
    private val binding get() = _binding!!

    private val vm: TrkGuestMainViewModel by viewModels()

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
                putExtra("tracker_status", item.currentStatus.name)
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.trackers.collect { list ->
                    trackerAdapter.submitList(list) {
                        footerAdapter.setShowEmptyText(trackerAdapter.itemCount == 0)
                    }
                }
            }
        }

        vm.loadGuestTrackers()
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}