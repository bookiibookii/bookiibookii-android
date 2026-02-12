package com.bookiibookii.bookiibookii.trkHost

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentTrkHostMainBinding
import com.bookiibookii.bookiibookii.trkDirectHost.DirectHostActivity
import com.bookiibookii.bookiibookii.trkGuest.TrkGuestMainFragment
import kotlinx.coroutines.launch

class TrkHostMainFragment : Fragment() {

    private var _binding: FragmentTrkHostMainBinding? = null
    private val binding get() = _binding!!

    private val vm: TrkHostMainViewModel by viewModels()

    private lateinit var trackerAdapter: TrackerAdapter
    private lateinit var footerAdapter: CreateGroupFooterAdapter
    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrkHostMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToggleLogic()
        updateTabState(isMyGroup = true)

        trackerAdapter = TrackerAdapter { item ->
            when (item.exchangeType) {
                ExchangeType.DELIVERY -> {
                    startActivity(Intent(requireContext(), HostActivity::class.java).apply {
                        putExtra("group_id", item.groupId)
                    })
                }

                ExchangeType.DIRECT -> {
                    startActivity(Intent(requireContext(), DirectHostActivity::class.java).apply {
                        putExtra("group_id", item.groupId)
                    })
                }

                ExchangeType.NONE -> {
                    // TODO: 나중에 연결
                    return@TrackerAdapter
                }
            }
        }

        footerAdapter = CreateGroupFooterAdapter(
            mode = FooterMode.HOST_CREATE,
            onActionClick = {
                // TODO: 그룹 만들기 화면 이동
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

        vm.loadHostTrackers()
    }

    private fun setupToggleLogic() {
        binding.myGroupBt.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            }
            updateTabState(isMyGroup = true)
        }

        binding.joinedGroupBt.setOnClickListener {
            val tag = "TrkGuestMainFragment"
            val current = parentFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (current is TrkGuestMainFragment) {
                updateTabState(isMyGroup = false)
                return@setOnClickListener
            }

            parentFragmentManager.commit {
                replace(R.id.fragmentContainer, TrkGuestMainFragment(), tag)
                addToBackStack(tag)
            }
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
