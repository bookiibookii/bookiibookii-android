package com.bookiibookii.bookiibookii.myPage.profile

import android.app.AlertDialog
import android.content.Context
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
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.model.UserUpdateRequest
import com.bookiibookii.bookiibookii.databinding.FragmentMypProfileEditBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MypProfileEditFragment : Fragment() {

    private var _binding: FragmentMypProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyPageViewModel by activityViewModels()
    private lateinit var loadingDialog: LoadingDialog

    private var selectedImageFile: File? = null
    private var cameraUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypProfileEditBinding.inflate(inflater, container, false)
        return binding.root
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
    }

    private fun initUI() {
        // 초기 버튼 색상 및 상태 설정 (비활성화 색상)
        updateNicknameButtonState(false)
        updateEditButtonState(false)
    }

    private fun observeViewModel() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            // 처음 로드될 때만 EditText에 채워넣음
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

                validateAllFields() // 데이터가 채워진 후 버튼 상태 업데이트
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
                    loadingDialog.dismiss()
                }

                when(event) {
                    is MyPageViewModel.Event.ShowToast ->
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    is MyPageViewModel.Event.NavigateBack ->
                        requireActivity().supportFragmentManager.popBackStack()
                    is MyPageViewModel.Event.NicknameCheckResult -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initListeners() {
        binding.mypEditBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        // 모든 입력 필드 변화를 감지하기 위한 공통 와처
        val commonWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateAllFields()
            }
        }

        // 닉네임 입력 감지 (중복확인 버튼 상태 제어 포함)
        binding.mypEditNickEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) { validateAllFields() }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentInput = s.toString()
                val originalNick = viewModel.profileData.value?.nickname ?: ""

                // 닉네임이 기존과 다를 때만 중복확인 버튼 활성화
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

        // 다른 모든 필드에도 와처 등록
        binding.mypEditNameEt.addTextChangedListener(commonWatcher)
        binding.mypEditNumEt.addTextChangedListener(commonWatcher)
        binding.mypEditPostEt.addTextChangedListener(commonWatcher)
        binding.mypEditAddressEt.addTextChangedListener(commonWatcher)
        binding.mypEditAddressDetailEt.addTextChangedListener(commonWatcher)
        binding.mypEditChangeInfoEt.addTextChangedListener(commonWatcher)
        binding.mypEditHopeAddressEt.addTextChangedListener(commonWatcher)

        // 닉네임 중복 확인 클릭
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

        // 수정하기 버튼 클릭
        binding.mypEditEditBtn.setOnClickListener {
            if (!isAllFieldsFilled()) {
                Toast.makeText(context, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentNick = binding.mypEditNickEt.text.toString()
            val originNick = viewModel.profileData.value?.nickname
            val isChecked = viewModel.isNicknameChecked.value ?: false

            if (currentNick != originNick && !isChecked) {
                Toast.makeText(context, "닉네임 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val phone = binding.mypEditNumEt.text.toString()
            if (!phone.matches(Regex("^\\d{3}-\\d{3,4}-\\d{4}$"))) {
                Toast.makeText(context, "전화번호 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
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
        val dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_host_photo_selection_dialog, null)
        val dialog = AlertDialog.Builder(context).setView(dialogView).setCancelable(true).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialogView.findViewById<ConstraintLayout>(R.id.layout_pick_photo).setOnClickListener {
            dialog.dismiss()
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        dialogView.findViewById<ConstraintLayout>(R.id.layout_take_camera).setOnClickListener {
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

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}