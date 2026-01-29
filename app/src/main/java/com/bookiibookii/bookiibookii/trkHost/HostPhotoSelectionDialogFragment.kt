package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.bookiibookii.bookiibookii.databinding.FragmentHostPhotoSelectionDialogBinding

class HostPhotoSelectionDialogFragment : DialogFragment() {

    private var _binding: FragmentHostPhotoSelectionDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostPhotoSelectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutPickPhoto.setOnClickListener{
            setFragmentResult(REQ_KEY, Bundle().apply {
                putString(ACTION_KEY, ACTION_GALLERY)
            })
            dismiss()
        }

        binding.layoutTakeCamera.setOnClickListener{
            setFragmentResult(REQ_KEY, Bundle().apply {
                putString(ACTION_KEY, ACTION_CAMERA)
            })
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PhotoSelectionDialogFragment"

        const val REQ_KEY = "photo_selection_request"
        const val ACTION_KEY = "photo_selection_action"

        const val ACTION_GALLERY = "gallery"
        const val ACTION_CAMERA = "camera"
    }
}