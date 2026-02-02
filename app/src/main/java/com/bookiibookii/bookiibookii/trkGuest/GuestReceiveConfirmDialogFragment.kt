package com.bookiibookii.bookiibookii.trkGuest

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.bookiibookii.bookiibookii.databinding.FragmentGuestReceiveConfirmDialogBinding
import com.bookiibookii.bookiibookii.trkHost.HostPhotoSelectionDialogFragment
import java.io.File

class GuestReceiveConfirmDialogFragment : DialogFragment() {

    private var _binding: FragmentGuestReceiveConfirmDialogBinding? = null
    private val binding get() = _binding!!

    private var cameraImageUri: Uri? = null
    private var selectedPhotoUri: Uri? = null
    private var isChecked: Boolean = false

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            showPreview(uri)
        }

    private val takePicLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()){ success: Boolean ->
            if (!success) return@registerForActivityResult
            cameraImageUri?.let { uri ->
                showPreview(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuestReceiveConfirmDialogBinding.inflate(inflater, container, false)
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

        binding.btnFinish.isEnabled = false
        binding.btnFinish.alpha = 0.45f

        childFragmentManager.setFragmentResultListener(
            HostPhotoSelectionDialogFragment.REQ_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString(HostPhotoSelectionDialogFragment.ACTION_KEY)) {
                HostPhotoSelectionDialogFragment.ACTION_GALLERY -> {
                    pickImageLauncher.launch("image/*")
                }
                HostPhotoSelectionDialogFragment.ACTION_CAMERA -> {
                    cameraImageUri = createCameraImageUri()
                    takePicLauncher.launch(cameraImageUri)
                }
            }
        }

        binding.cardUpload.setOnClickListener{
            if (childFragmentManager.isStateSaved) return@setOnClickListener
            HostPhotoSelectionDialogFragment()
                .show(childFragmentManager, HostPhotoSelectionDialogFragment.TAG)
        }

        binding.cbCheck.setOnCheckedChangeListener { _, checked ->
            isChecked = checked
            updateFinishButtonState()
        }

        binding.btnFinish.setOnClickListener{
            val next = GuestStartBottomDialogFragment()
            val prevBottomSheet = parentFragmentManager.findFragmentByTag(
                GuestShippedBottomDialogFragment.TAG) as? DialogFragment
            dismiss()
            prevBottomSheet?.dismiss()
            next.show(parentFragmentManager, GuestStartBottomDialogFragment.TAG)
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun showPreview(uri: Uri) {
        selectedPhotoUri = uri

        binding.ivPreview.setImageURI(uri)
        binding.ivPreview.visibility = View.VISIBLE

        binding.ivUpload.visibility = View.GONE
        binding.tvUploadHint.visibility = View.GONE

        updateFinishButtonState()
    }

    private fun updateFinishButtonState() {
        val photoOk = selectedPhotoUri != null
        val enabled = photoOk && isChecked
        setFinishEnabled(enabled)
    }

    private fun setFinishEnabled(enabled: Boolean) {
        binding.btnFinish.isEnabled = enabled
        binding.btnFinish.alpha = if (enabled) 1.0f else 0.45f
    }

    private fun createCameraImageUri(): Uri {
        val dir = File(requireContext().cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "receive_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ReceiveConfirmFragment"
    }
}