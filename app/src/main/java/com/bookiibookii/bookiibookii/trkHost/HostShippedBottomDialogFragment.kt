package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentHostShippedBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class HostShippedBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentHostShippedBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val vm: HostViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostShippedBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loadTracker(groupId)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collectLatest { state ->
                    val info = state.data?.deliveryInfo

                    binding.tvCourier.text = info?.deliveryCompany ?: "-"
                    binding.tvTrackingNum.text = info?.trackingNumber ?: "-"
                }
            }
        }

        binding.btnViewShippingPhoto.setOnClickListener{
            HostShippingPhotoDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, HostShippingPhotoDialogFragment.TAG)
        }

        binding.btnDoReceiveConfirm.setOnClickListener{
            HostReceiveConfirmDialogFragment
                .newInstance(groupId)
                .show(parentFragmentManager, HostReceiveConfirmDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ShippedFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = HostShippedBottomDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Bookii_BottomSheet_NoDim
    }
}