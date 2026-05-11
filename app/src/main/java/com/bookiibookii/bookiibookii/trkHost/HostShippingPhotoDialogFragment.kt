package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentHostShippingPhotoDialogBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.launch

class HostShippingPhotoDialogFragment : DialogFragment() {

    private var _binding: FragmentHostShippingPhotoDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostShippingPhotoDialogBinding.inflate(inflater, container, false)
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

        binding.btnConfirm.setOnClickListener { dismiss() }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val body = RetrofitClient.trkApi().getTrackerCheckShippingImage(groupId)

                if (!body.isSuccess || body.result == null) {
                    binding.tvEmpty.text = body.message ?: "이미지를 불러올 수 없어요."
                    return@launch
                }

                val url = body.result.presignedGetUrl
                showImage(url)

            } catch (e: Exception) {
                Log.e("HOST_PHOTO", "load shipping image failed", e)
                binding.tvEmpty.text = e.message ?: "네트워크 오류"
            }
        }
    }

    private fun showImage(url: String) {
        binding.tvEmpty.visibility = View.GONE
        binding.ivPhoto.visibility = View.VISIBLE

        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error {
                binding.ivPhoto.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvEmpty.text = "이미지를 불러올 수 없어요."
            }
            .into(binding.ivPhoto)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "HostShippingPhotoDialogFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = HostShippingPhotoDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }
}
