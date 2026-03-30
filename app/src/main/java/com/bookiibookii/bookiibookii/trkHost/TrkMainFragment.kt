package com.bookiibookii.bookiibookii.trkHost

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
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.databinding.FragmentTrkHostMainBinding
import com.bookiibookii.bookiibookii.trkDirectGuest.DirectGuestActivity
import com.bookiibookii.bookiibookii.trkDirectHost.DirectHostActivity
import com.bookiibookii.bookiibookii.trkGuest.GuestActivity
import kotlinx.coroutines.launch

class TrkMainFragment : Fragment() {

    private var _binding: FragmentTrkHostMainBinding? = null
    private val binding get() = _binding!!

    private val vm: TrkMainViewModel by viewModels()

    private lateinit var trackerAdapter: TrackerAdapter
    private lateinit var footerAdapter: CreateGroupFooterAdapter
    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTrkHostMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackerAdapter = TrackerAdapter { item ->
            val tab = vm.currentTab.value
            when (item.exchangeType) {
                ExchangeType.DELIVERY -> {
                    val actClass = if (tab == TrackerTab.MY_GROUP) HostActivity::class.java else GuestActivity::class.java
                    startActivity(Intent(requireContext(), actClass).apply { putExtra("group_id", item.groupId) })
                }
                ExchangeType.DIRECT -> {
                    val actClass = if (tab == TrackerTab.MY_GROUP) DirectHostActivity::class.java else DirectGuestActivity::class.java
                    startActivity(Intent(requireContext(), actClass).apply { putExtra("group_id", item.groupId) })
                }
                ExchangeType.NONE -> {
                    startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("NAV_ACTION", "OPEN_LIBRARY_ING")
                        putExtra("target_group_id", item.groupId)
                    })
                }
            }
        }

        footerAdapter = CreateGroupFooterAdapter(
            mode = FooterMode.HOST_CREATE,
            onActionClick = { (requireActivity() as? MainActivity)?.moveToGroupTab() }
        )

        concatAdapter = ConcatAdapter(trackerAdapter, footerAdapter)
        binding.trkRecyclerview.apply {
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            itemAnimator = null
        }

        binding.myGroupBt.setOnClickListener { vm.selectTab(TrackerTab.MY_GROUP) }
        binding.joinedGroupBt.setOnClickListener { vm.selectTab(TrackerTab.JOINED_GROUP) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.currentTab.collect { tab ->
                        val isMyGroup = tab == TrackerTab.MY_GROUP
                        binding.myGroupBt.isSelected = isMyGroup
                        binding.joinedGroupBt.isSelected = !isMyGroup
                        footerAdapter.updateMode(if (isMyGroup) FooterMode.HOST_CREATE else FooterMode.GUEST_JOIN)
                    }
                }
                launch {
                    vm.trackers.collect { list ->
                        trackerAdapter.submitList(list) {
                            footerAdapter.setShowEmptyText(trackerAdapter.itemCount == 0)
                            binding.trkRecyclerview.scrollToPosition(0)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
