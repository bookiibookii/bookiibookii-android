package com.bookiibookii.bookiibookii.myPage.profile

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.common.showCustomToast // ★ 커스텀 토스트 임포트
import com.bookiibookii.bookiibookii.data.model.UserUpdateRequest
import com.bookiibookii.bookiibookii.databinding.FragmentMypProfileEditBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MypProfileEditFragment : BaseDetailFragment<FragmentMypProfileEditBinding>() {

    private val viewModel: MyPageViewModel by activityViewModels()
    private lateinit var loadingDialog: LoadingDialog

    private var selectedImageFile: File? = null
    private var cameraUri: Uri? = null

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypProfileEditBinding {
        return FragmentMypProfileEditBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        if (viewModel.profileData.value == null) {
            viewModel.fetchMypageData()
        }

        initUI()
        observeViewModel()
        initListeners()
        initResultListener()
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeVisible = insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
            val navBarHeight = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()).bottom

            // 키보드가 올라오면 키보드 높이만큼, 안 보이면 기본 하단바 높이만큼 프래그먼트의 밑바닥을 위로 밀어 올립니다.
            v.setPadding(0, 0, 0, if (imeVisible) imeHeight else navBarHeight)
            insets
        }
    }


    private fun initUI() {
        updateNicknameButtonState(false)
        updateEditButtonState(false)
    }

    private fun observeViewModel() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            if (binding.mypEditNickEt.text.isEmpty()) {
                binding.mypEditNickEt.setText(data.nickname)
                binding.mypEditNameEt.setText(data.receiverName ?: "")
                binding.mypEditNumEt.setText(data.phone ?: "")
                binding.mypEditPostEt.setText(data.zipCode ?: "")
                binding.mypEditAddressEt.setText(data.address ?: "")
                binding.mypEditAddressDetailEt.setText(data.addressDetail ?: "")
                binding.mypEditChangeInfoEt.setText(data.region ?: "")
                binding.mypEditHopeAddressEt.setText(data.meetPlace ?: "")

                val imageUrl = data.profileImageUrl
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_profile_default)
                    .error(R.drawable.img_profile_default)
                    .transform(CenterCrop(), RoundedCorners(dpToPx(45)))
                    .into(binding.mypEditProfileIv)

                validateAllFields()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
                    loadingDialog.dismiss()
                }

                when(event) {
                    is MyPageViewModel.Event.ShowToast -> {
                        // ★ 프로필 업데이트 성공/실패 여부에 따라 아이콘 변경 (Event 객체의 메시지 내용으로 임시 판별하거나,
                        // ViewModel의 Event 구조에 isSuccess 플래그를 추가하는 것이 더 정확합니다.
                        // 여기서는 일반적인 Toast 노출용으로 false(info 아이콘)를 기본 적용했습니다.)
                        val isSuccessMsg = event.message.contains("성공", true) || event.message.contains("완료", true)
                        requireContext().showCustomToast(event.message, isSuccessMsg)
                    }
                    is MyPageViewModel.Event.NavigateBack -> {
                        // 성공 후 뒤로가기 전 커스텀 토스트 띄우기
                        requireContext().showCustomToast("프로필 수정이 완료되었습니다.", true)
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    is MyPageViewModel.Event.NicknameCheckResult -> {
                        // 닉네임 중복 확인 결과 (사용 가능 = 성공, 불가능 = 실패 아이콘)
                        val isAvailable = event.message.contains("사용 가능", true)
                        requireContext().showCustomToast(event.message, isAvailable)
                    }
                }
            }
        }
    }

    private fun initListeners() {
        binding.mypEditBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.mypEditNumEt.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val digits = s.toString().replace(Regex("\\D"), "")
                val formatted = StringBuilder()

                if (digits.length == 10) {
                    formatted.append(digits.substring(0, 3)).append("-")
                    formatted.append(digits.substring(3, 6)).append("-")
                    formatted.append(digits.substring(6))
                } else if (digits.length > 3) {
                    formatted.append(digits.substring(0, 3)).append("-")
                    if (digits.length > 7) {
                        formatted.append(digits.substring(3, 7)).append("-")
                        formatted.append(digits.substring(7, minOf(digits.length, 11)))
                    } else {
                        formatted.append(digits.substring(3))
                    }
                } else {
                    formatted.append(digits)
                }

                if (s.toString() != formatted.toString()) {
                    binding.mypEditNumEt.setText(formatted.toString())
                    binding.mypEditNumEt.setSelection(formatted.length)
                }

                isFormatting = false
            }
        })

        val commonWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateAllFields()
            }
        }

        binding.mypEditNickEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) { validateAllFields() }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentInput = s.toString()
                val originalNick = viewModel.profileData.value?.nickname ?: ""

                if (currentInput != originalNick && currentInput.isNotBlank()) {
                    updateNicknameButtonState(true)
                    if (currentInput != viewModel.confirmedNickname) {
                        viewModel.setNicknameChecked(false)
                    }
                } else {
                    updateNicknameButtonState(false)
                }
            }
        })

        binding.mypEditNameEt.addTextChangedListener(commonWatcher)
        binding.mypEditNumEt.addTextChangedListener(commonWatcher)
        binding.mypEditPostEt.addTextChangedListener(commonWatcher)
        binding.mypEditAddressEt.addTextChangedListener(commonWatcher)
        binding.mypEditAddressDetailEt.addTextChangedListener(commonWatcher)
        binding.mypEditChangeInfoEt.addTextChangedListener(commonWatcher)
        binding.mypEditHopeAddressEt.addTextChangedListener(commonWatcher)

        binding.mypEditNickCheckEt.setOnClickListener {
            val nickname = binding.mypEditNickEt.text.toString()
            if (nickname.isBlank()) return@setOnClickListener
            loadingDialog.show()
            viewModel.checkNickname(nickname)
        }

        binding.mypEditProfileEditIv.setOnClickListener { showImagePickerOption() }

        binding.mypEditPostCheckEt.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypPostcodeSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.mypEditChangeInfoSearchEt.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypRegionSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.mypEditEditBtn.setOnClickListener {
            if (!isAllFieldsFilled()) {
                // ★ 커스텀 토스트 (경고)
                requireContext().showCustomToast("모든 필드를 입력해주세요.", false)
                return@setOnClickListener
            }

            val currentNick = binding.mypEditNickEt.text.toString()
            val originNick = viewModel.profileData.value?.nickname
            val isChecked = viewModel.isNicknameChecked.value ?: false

            if (currentNick != originNick && !isChecked) {
                // ★ 커스텀 토스트 (경고)
                requireContext().showCustomToast("닉네임 중복 확인을 해주세요.", false)
                return@setOnClickListener
            }

            val phone = binding.mypEditNumEt.text.toString()
            if (!phone.matches(Regex("^\\d{3}-\\d{3,4}-\\d{4}$"))) {
                // ★ 커스텀 토스트 (경고)
                requireContext().showCustomToast("전화번호 형식이 올바르지 않습니다.", false)
                return@setOnClickListener
            }

            val request = UserUpdateRequest(
                nickname = currentNick,
                s3Key = "",
                receiverName = binding.mypEditNameEt.text.toString(),
                phone = phone,
                zipCode = binding.mypEditPostEt.text.toString(),
                address = binding.mypEditAddressEt.text.toString(),
                addressDetail = binding.mypEditAddressDetailEt.text.toString(),
                region = binding.mypEditChangeInfoEt.text.toString(),
                meetPlace = binding.mypEditHopeAddressEt.text.toString()
            )

            loadingDialog.show()
            viewModel.updateProfile(request, selectedImageFile)
        }
    }

    private fun validateAllFields() {
        updateEditButtonState(isAllFieldsFilled())
    }

    private fun isAllFieldsFilled(): Boolean {
        return with(binding) {
            mypEditNickEt.text.isNotBlank() &&
                    mypEditNameEt.text.isNotBlank() &&
                    mypEditNumEt.text.isNotBlank() &&
                    mypEditPostEt.text.isNotBlank() &&
                    mypEditAddressEt.text.isNotBlank() &&
                    mypEditAddressDetailEt.text.isNotBlank() &&
                    mypEditChangeInfoEt.text.isNotBlank() &&
                    mypEditHopeAddressEt.text.isNotBlank()
        }
    }

    private fun updateEditButtonState(isEnabled: Boolean) {
        val context = requireContext()
        with(binding.mypEditEditBtn) {
            if (isEnabled) {
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_900))
                setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_300))
                setTextColor(ContextCompat.getColor(context, R.color.grey_100))
            }
        }
    }

    private fun updateNicknameButtonState(isEnabled: Boolean) {
        val context = requireContext()
        with(binding.mypEditNickCheckEt) {
            this.isEnabled = isEnabled
            if (isEnabled) {
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_900))
                setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_300))
                setTextColor(ContextCompat.getColor(context, R.color.grey_100))
            }
        }
    }

    private fun showImagePickerOption() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.fragment_host_photo_selection_dialog)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<ConstraintLayout>(R.id.layout_pick_photo).setOnClickListener {
            dialog.dismiss()
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        dialog.findViewById<ConstraintLayout>(R.id.layout_take_camera).setOnClickListener {
            dialog.dismiss()
            cameraUri = createImageUri()
            if (cameraUri != null) takePicture.launch(cameraUri)
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess && cameraUri != null) {
            Glide.with(this).load(cameraUri).transform(CenterCrop(), RoundedCorners(dpToPx(45))).into(binding.mypEditProfileIv)
            selectedImageFile = File(requireContext().cacheDir, "camera/temp_profile.jpg")
        }
    }

    private fun createImageUri(): Uri? {
        return try {
            val storageDir = File(requireContext().cacheDir, "camera")
            if (!storageDir.exists()) storageDir.mkdirs()
            val file = File(storageDir, "temp_profile.jpg")
            if (file.exists()) file.delete()
            file.createNewFile()
            FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        } catch (e: Exception) { null }
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Glide.with(this).load(uri).transform(CenterCrop(), RoundedCorners(dpToPx(45))).into(binding.mypEditProfileIv)
            selectedImageFile = uriToFile(uri)
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input -> outputStream.use { output -> input.copyTo(output) } }
            tempFile
        } catch (e: Exception) { null }
    }

    private fun initResultListener() {
        setFragmentResultListener("requestKeyRegion") { _, bundle ->
            binding.mypEditChangeInfoEt.setText(bundle.getString("regionResult"))
        }
        setFragmentResultListener("requestKeyPostcode") { _, bundle ->
            binding.mypEditPostEt.setText(bundle.getString("zonecode"))
            binding.mypEditAddressEt.setText(bundle.getString("address"))
            binding.mypEditAddressDetailEt.setText("")
            binding.mypEditAddressDetailEt.requestFocus()
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}