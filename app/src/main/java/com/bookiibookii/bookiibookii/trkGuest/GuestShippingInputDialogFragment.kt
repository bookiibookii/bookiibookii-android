package com.bookiibookii.bookiibookii.trkGuest

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.databinding.FragmentGuestShippingInputDialogBinding
import com.bookiibookii.bookiibookii.trkHost.HostPhotoSelectionDialogFragment
import com.bookiibookii.bookiibookii.trkHost.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class GuestShippingInputDialogFragment : DialogFragment() {

    private var _binding: FragmentGuestShippingInputDialogBinding? = null
    private val binding get() = _binding!!

    private var cameraImageUri: Uri? = null
    private var selectedCourier: String? = null
    private var selectedPhotoUri: Uri? = null

    private val vm: GuestViewModel by activityViewModels()

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
        _binding = FragmentGuestShippingInputDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.resetShippingStartState()

        binding.btnRegister.isEnabled = false
        binding.btnRegister.alpha = 0.45f
        binding.actvCourier.setDropDownBackgroundResource(android.R.color.white)
        setupCourierDropdown()

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

        binding.cardPhotoUpload.setOnClickListener {
            if (childFragmentManager.isStateSaved) return@setOnClickListener
            HostPhotoSelectionDialogFragment()
                .show(childFragmentManager, HostPhotoSelectionDialogFragment.TAG)
        }

        binding.btnRegister.setOnClickListener {
            val courier = selectedCourier ?: run {
                binding.tilCourier.error = "택배사를 선택해주세요."
                return@setOnClickListener
            }

            val tracking = binding.etTrackingNum.text?.toString()?.trim().orEmpty()
            if (tracking.isEmpty()) return@setOnClickListener

            val photoUri = selectedPhotoUri ?: return@setOnClickListener

            val bytes = requireContext().contentResolver
                .openInputStream(photoUri)
                ?.use { it.readBytes() }
                ?: run {
                    Toast.makeText(requireContext(), "이미지를 읽을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            vm.startShipping(
                groupId = groupId,
                deliveryCompany = courier,
                trackingNumber = tracking,
                imageBytes = bytes,
                contentType = "image/jpeg"
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.shippingStartState.collectLatest { state ->
                    when (state) {
                        is UiState.Idle -> Unit

                        is UiState.Loading -> {
                            binding.btnRegister.isEnabled = false
                            binding.btnRegister.alpha = 0.45f
                        }

                        is UiState.Success -> {
                            (parentFragmentManager.findFragmentByTag(GuestShippingBottomDialogFragment.TAG) as? DialogFragment)
                                ?.dismissAllowingStateLoss()

                            dismissAllowingStateLoss()

                            GuestShippingStatusBottomDialogFragment
                                .newInstance(groupId)
                                .show(parentFragmentManager, GuestShippingStatusBottomDialogFragment.TAG)
                        }

                        is UiState.Error -> {
                            updateRegisterButtonState()
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.etTrackingNum.doAfterTextChanged {
            updateRegisterButtonState()
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun showPreview(uri: Uri) {
        selectedPhotoUri = uri

        binding.ivPreview.setImageURI(uri)
        binding.ivPreview.visibility = View.VISIBLE

        binding.ivUploadIcon.visibility = View.GONE
        binding.tvUploadHint.visibility = View.GONE

        updateRegisterButtonState()
    }

    private fun createCameraImageUri(): Uri {
        val dir = File(requireContext().cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "shipping_${System.currentTimeMillis()}.jpg")

        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GuestShippingInputDialogFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) = GuestShippingInputDialogFragment().apply {
            arguments = Bundle().apply { putLong(ARG_GROUP_ID, groupId) }
        }
    }

    private val courierList = listOf(
        "CJ대한통운", "한진택배", "롯데택배", "우체국택배", "로젠택배",
        "경동택배", "대신택배", "일양로지스", "천일택배", "건영택배",
        "GS25 편의점택배", "CU 편의점택배", "홈픽", "SLX택배", "우리한방택배"
    )

    private fun setupCourierDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            courierList
        )

        binding.actvCourier.setAdapter(adapter)
        binding.actvCourier.setOnClickListener { binding.actvCourier.showDropDown() }

        binding.actvCourier.setOnItemClickListener { _, _, position, _ ->
            selectedCourier = courierList[position]
            binding.tilCourier.error = null
            updateRegisterButtonState()
        }
    }

    private fun updateRegisterButtonState() {
        val courierOk = !selectedCourier.isNullOrBlank()
        val trackingOk = binding.etTrackingNum.text?.toString()?.trim()?.isNotEmpty() == true
        val photoOk = selectedPhotoUri != null

        val enabled = courierOk && trackingOk && photoOk
        binding.btnRegister.isEnabled = enabled
        binding.btnRegister.alpha = if (enabled) 1.0f else 0.45f
    }
}
