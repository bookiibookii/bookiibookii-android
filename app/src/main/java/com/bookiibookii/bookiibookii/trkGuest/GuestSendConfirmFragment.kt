package com.bookiibookii.bookiibookii.trkGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.databinding.FragmentGuestSendConfirmBinding
import com.bookiibookii.bookiibookii.trkHost.UiState
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GuestSendConfirmFragment : DialogFragment() {

    private var _binding: FragmentGuestSendConfirmBinding? = null
    private val binding get() = _binding!!

    private val vm: GuestViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestSendConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.isEnabled = false
        binding.btnConfirm.alpha = 0.55f

        var imageReady = false
        var checked = false

        fun updateButton() {
            val enabled = imageReady && checked
            binding.btnConfirm.isEnabled = enabled
            binding.btnConfirm.alpha = if (enabled) 1f else 0.55f
        }

        binding.cbCheck.setOnCheckedChangeListener { _, isChecked ->
            checked = isChecked
            updateButton()
        }

        vm.loadCheckImage(groupId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.checkImageState.collectLatest { state ->
                    when (state) {
                        UiState.Idle -> Unit
                        UiState.Loading -> {
                            binding.tvImageHint.visibility = View.VISIBLE
                            binding.tvImageHint.text = "사진을 불러오는 중…"
                            imageReady = false
                            updateButton()
                        }
                        is UiState.Success -> {
                            val url = state.data.presignedGetUrl
                            binding.tvImageHint.visibility = View.GONE

                            Glide.with(binding.ivProof)
                                .load(url)
                                .into(binding.ivProof)

                            imageReady = true
                            updateButton()
                        }
                        is UiState.Error -> {
                            binding.tvImageHint.visibility = View.VISIBLE
                            binding.tvImageHint.text = "사진을 불러오지 못했어요"
                            imageReady = false
                            updateButton()
                        }
                    }
                }
            }
        }

        binding.btnConfirm.setOnClickListener {
            if (!binding.btnConfirm.isEnabled) return@setOnClickListener

            vm.patchConfirmReception(groupId)

            (parentFragmentManager
                .findFragmentByTag(GuestShippingStatusBottomDialogFragment.TAG) as? DialogFragment)
                ?.dismissAllowingStateLoss()

            dismissAllowingStateLoss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GuestSendConfirmFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = GuestSendConfirmFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }
}