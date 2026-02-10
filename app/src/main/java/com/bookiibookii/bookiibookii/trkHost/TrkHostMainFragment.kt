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
            val target = when (item.exchangeType) {
                ExchangeType.DELIVERY -> HostActivity::class.java
                ExchangeType.DIRECT -> DirectHostActivity::class.java
                ExchangeType.NONE -> TODO()
            }

            val intent = Intent(requireContext(), target).apply {
                putExtra("group_id", item.groupId)
            }
            startActivity(intent)
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

        // 화면 렌더링
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
//        vm.loadHostTrackersDummy()
    }

    private fun setupToggleLogic() {
        binding.myGroupBt.setOnClickListener {
            updateTabState(isMyGroup = true)
        }

        binding.joinedGroupBt.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragmentContainer, TrkGuestMainFragment())
                addToBackStack(null)
            }
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
