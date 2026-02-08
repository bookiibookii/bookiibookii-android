package com.bookiibookii.bookiibookii.myPage.profile

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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

    // Activity Scope ViewModel (데이터 공유)
    private val viewModel: MyPageViewModel by activityViewModels()

    private var selectedImageFile: File? = null // 업로드할 이미지 파일
    private var isNicknameChecked = true // 닉네임 중복 확인 완료 여부

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        observeViewModel()
        initListeners()
        initResultListener()
    }

    private fun initUI() {
        updateNicknameButtonState(isEnabled = false)
    }

    private fun observeViewModel() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            // EditText가 비어있을 때만 값 채우기 (사용자 입력 유지)
            if (binding.mypEditNickEt.text.isEmpty()) {

                // 1. 기본 닉네임 (최상위)
                binding.mypEditNickEt.setText(data.nickname)

                // 2. 중첩된 User 데이터 꺼내기
                val userDetail = data.userImage?.user

                // 3. 있는 데이터 바인딩
                binding.mypEditChangeInfoEt.setText(userDetail?.meetPlace ?: "") // 교환 장소
                binding.mypEditHopeAddressEt.setText(userDetail?.region ?: "")   // 희망 장소

                // 4. 없는 데이터 빈값 처리 (서버 GET에 필드가 없으므로 빈 문자열)
                binding.mypEditNameEt.setText("")           // 이름
                binding.mypEditNumEt.setText("")            // 전화번호
                binding.mypEditPostEt.setText("")           // 우편번호
                binding.mypEditAddressEt.setText("")        // 주소
                binding.mypEditAddressDetailEt.setText("")  // 상세주소

                // 5. 이미지 세팅
                val imageUrl = data.userImage?.s3Key
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .into(binding.mypEditProfileIv)
                }
            }
        }

        // 이벤트 처리
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when(event) {
                    is MyPageViewModel.Event.ShowToast ->
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()

                    is MyPageViewModel.Event.NavigateBack ->
                        parentFragmentManager.popBackStack()

                    is MyPageViewModel.Event.NicknameCheckResult -> {
                        if (event.isAvailable) {
                            isNicknameChecked = true
                            Toast.makeText(context, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
                            updateNicknameButtonState(isEnabled = false)
                        } else {
                            isNicknameChecked = false
                            Toast.makeText(context, "이미 사용 중인 닉네임입니다.", Toast.LENGTH_SHORT).show()
                        }
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
                val currentNick = s.toString()
                val originNick = viewModel.profileData.value?.nickname ?: ""

                if (currentNick == originNick) {
                    isNicknameChecked = true
                    updateNicknameButtonState(isEnabled = false)
                } else {
                    isNicknameChecked = false
                    updateNicknameButtonState(isEnabled = true)
                }
            }
        })

        // 닉네임 중복 확인
        binding.mypEditNickCheckEt.setOnClickListener {
            val nickname = binding.mypEditNickEt.text.toString()
            if (nickname.isBlank()) {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.checkNickname(nickname)
        }

        // 프로필 이미지 변경 (갤러리)
        binding.mypEditProfileEditIv.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // 우편번호 검색 이동
        binding.mypEditPostCheckEt.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypPostcodeSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // 지역 검색 이동
        binding.mypEditChangeInfoSearchEt.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypRegionSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // 수정하기 버튼 (최종 저장)
        binding.mypEditEditBtn.setOnClickListener {
            val currentNick = binding.mypEditNickEt.text.toString()
            val originNick = viewModel.profileData.value?.nickname

            if (currentNick != originNick && !isNicknameChecked) {
                Toast.makeText(context, "닉네임 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = UserUpdateRequest(
                nickname = currentNick,
                receiverName = binding.mypEditNameEt.text.toString(),
                phone = binding.mypEditNumEt.text.toString(),
                zipCode = binding.mypEditPostEt.text.toString(),
                address = binding.mypEditAddressEt.text.toString(),
                addressDetail = binding.mypEditAddressDetailEt.text.toString(),
                meetPlace = binding.mypEditChangeInfoEt.text.toString(),
            )

            viewModel.updateProfile(request, selectedImageFile)
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

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Glide.with(this).load(uri).circleCrop().into(binding.mypEditProfileIv)
            selectedImageFile = uriToFile(uri)
        }
    }

    private fun initResultListener() {
        setFragmentResultListener("requestKeyRegion") { _, bundle ->
            val selectedRegion = bundle.getString("regionResult")
            binding.mypEditChangeInfoEt.setText(selectedRegion)
        }

        setFragmentResultListener("requestKeyPostcode") { _, bundle ->
            val zonecode = bundle.getString("zonecode")
            val address = bundle.getString("address")
            binding.mypEditPostEt.setText(zonecode)
            binding.mypEditAddressEt.setText(address)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}