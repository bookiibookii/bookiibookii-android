package com.bookiibookii.bookiibookii.trkHost

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.databinding.FragmentHostReceiveConfirmDialogBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class HostReceiveConfirmDialogFragment : DialogFragment() {

    private var _binding: FragmentHostReceiveConfirmDialogBinding? = null
    private val binding get() = _binding!!

    private var cameraImageUri: Uri? = null
    private var selectedPhotoUri: Uri? = null
    private var isChecked: Boolean = false

    private val vm: HostViewModel by activityViewModels()

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            showPreview(uri)
        }

    private val takePicLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (!success) return@registerForActivityResult
            cameraImageUri?.let { uri -> showPreview(uri) }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostReceiveConfirmDialogBinding.inflate(inflater, container, false)
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

        vm.resetReceiveState()

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

        binding.cardUpload.setOnClickListener {
            if (childFragmentManager.isStateSaved) return@setOnClickListener
            HostPhotoSelectionDialogFragment()
                .show(childFragmentManager, HostPhotoSelectionDialogFragment.TAG)
        }

        binding.cbCheck.setOnCheckedChangeListener { _, checked ->
            isChecked = checked
            updateFinishButtonState()
        }

        binding.btnFinish.setOnClickListener {
            val photoUri = selectedPhotoUri ?: return@setOnClickListener

            val bytes = requireContext().contentResolver
                .openInputStream(photoUri)
                ?.use { it.readBytes() }
                ?: return@setOnClickListener

            vm.patchTrackerReceiveWithImage(
                groupId = groupId,
                imageBytes = bytes,
                contentType = "image/jpeg"
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.receiveState.collectLatest { state ->
                    when (state) {
                        is UiState.Idle -> Unit

                        is UiState.Loading -> {
                            setFinishEnabled(false)
                        }

                        is UiState.Success -> {
                            parentFragmentManager.setFragmentResult(
                                RESULT_KEY,
                                Bundle().apply {
                                    putString(BUNDLE_ACTION, "FINISHED")
                                    putLong(ARG_GROUP_ID, groupId)
                                }
                            )

                            (parentFragmentManager
                                .findFragmentByTag(HostShippedBottomDialogFragment.TAG) as? DialogFragment)
                                ?.dismissAllowingStateLoss()

                            dismissAllowingStateLoss()

                            HostTradeFinishBottomDialogFragment
                                .newInstance(groupId)
                                .show(parentFragmentManager, HostTradeFinishBottomDialogFragment.TAG)
                        }

                        is UiState.Error -> {
                            updateFinishButtonState()
                        }
                    }
                }
            }
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
        const val TAG = "HostReceiveConfirmDialogFragment"
        const val RESULT_KEY = "host_action"
        const val BUNDLE_ACTION = "action"

        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = HostReceiveConfirmDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }
}
