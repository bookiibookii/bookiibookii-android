package com.bookiibookii.bookiibookii.myPage.profile

import android.app.AlertDialog
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
import com.bookiibookii.bookiibookii.data.model.UserUpdateRequest
import com.bookiibookii.bookiibookii.databinding.FragmentMypProfileEditBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MypProfileEditFragment : Fragment() {

    private var _binding: FragmentMypProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyPageViewModel by activityViewModels()

    private var selectedImageFile: File? = null
    private var cameraUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.profileData.value == null) {
            viewModel.fetchMypageData()
        }

        initUI()
        observeViewModel()
        initListeners()
        initResultListener()
    }

    private fun initUI() {
    }

    private fun observeViewModel() {
        // 1. 프로필 데이터 바인딩
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

                val imageUrl = data.userImage?.s3Key
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_profile_default)
                    .error(R.drawable.img_profile_default)
                    .fallback(R.drawable.img_profile_default)
                    .circleCrop()
                    .into(binding.mypEditProfileIv)
            }
        }

        // 2. 닉네임 중복 확인 상태 관찰
        viewModel.isNicknameChecked.observe(viewLifecycleOwner) { isChecked ->
            updateNicknameButtonState(isEnabled = !isChecked)
        }

        // 3. 이벤트 감지
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when(event) {
                    is MyPageViewModel.Event.ShowToast ->
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    is MyPageViewModel.Event.NavigateBack ->
                        parentFragmentManager.popBackStack()
                    is MyPageViewModel.Event.NicknameCheckResult -> {
                        // [수정] 서버에서 온 메시지(event.message)를 그대로 토스트로 출력
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initListeners() {
        binding.mypEditBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 닉네임 변경 감지
        binding.mypEditNickEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentInput = s.toString()
                if (currentInput == viewModel.confirmedNickname) {
                    viewModel.setNicknameChecked(true)
                } else {
                    viewModel.setNicknameChecked(false)
                }
            }
        })

        // 닉네임 중복 확인 버튼
        binding.mypEditNickCheckEt.setOnClickListener {
            val nickname = binding.mypEditNickEt.text.toString()
            if (nickname.isBlank()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.checkNickname(nickname)
        }

        // 카메라/갤러리 선택
        binding.mypEditProfileEditIv.setOnClickListener {
            showImagePickerOption()
        }

        // 주소 검색
        binding.mypEditPostCheckEt.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypPostcodeSearchFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.mypEditChangeInfoSearchEt.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypRegionSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // 수정 완료 버튼
        binding.mypEditEditBtn.setOnClickListener {
            val currentNick = binding.mypEditNickEt.text.toString()
            val originNick = viewModel.profileData.value?.nickname
            val isChecked = viewModel.isNicknameChecked.value ?: false

            if (currentNick != originNick && !isChecked) {
                Toast.makeText(context, "닉네임 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 전화번호 정규식 검사
            val phone = binding.mypEditNumEt.text.toString()
            val phonePattern = "^\\d{3}-\\d{3,4}-\\d{4}$"

            if (!phone.matches(Regex(phonePattern))) {
                Toast.makeText(context, "전화번호는 000-0000-0000 형식이어야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = UserUpdateRequest(
                nickname = currentNick,
                receiverName = binding.mypEditNameEt.text.toString(),
                phone = phone,
                zipCode = binding.mypEditPostEt.text.toString(),
                address = binding.mypEditAddressEt.text.toString(),
                addressDetail = binding.mypEditAddressDetailEt.text.toString(),
                region = binding.mypEditChangeInfoEt.text.toString(),
                meetPlace = binding.mypEditHopeAddressEt.text.toString()
            )
            viewModel.updateProfile(request, selectedImageFile)
        }
    }

    private fun showImagePickerOption() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_host_photo_selection_dialog, null)

        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val btnGallery = dialogView.findViewById<ConstraintLayout>(R.id.layout_pick_photo)
        val btnCamera = dialogView.findViewById<ConstraintLayout>(R.id.layout_take_camera)

        btnGallery.setOnClickListener {
            dialog.dismiss()
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnCamera.setOnClickListener {
            dialog.dismiss()
            cameraUri = createImageUri()
            if (cameraUri != null) {
                takePicture.launch(cameraUri)
            } else {
                Toast.makeText(context, "카메라 저장소를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess && cameraUri != null) {
            Glide.with(this).load(cameraUri).circleCrop().into(binding.mypEditProfileIv)
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

            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Glide.with(this).load(uri).circleCrop().into(binding.mypEditProfileIv)
            selectedImageFile = uriToFile(uri)
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

    private fun uriToFile(uri: Uri): File? {
        return try {
            val contentResolver = requireContext().contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input -> outputStream.use { output -> input.copyTo(output) } }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}