package com.bookiibookii.bookiibookii.myPage.profile

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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

    // Activity Scope ViewModel 사용 (데이터 공유)
    private val viewModel: MyPageViewModel by activityViewModels()

    private var selectedImageFile: File? = null // 업로드할 이미지 파일
    private var isNicknameChecked = true // 닉네임 중복 확인 완료 여부 (초기값 true)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 초기 UI 상태 설정
        initUI()

        // 2. ViewModel 관찰 (데이터 로드 및 이벤트 처리)
        observeViewModel()

        // 3. 버튼 리스너 및 기능 설정
        initListeners()

        // 4. 결과 수신 (주소 검색 등)
        initResultListener()
    }

    private fun initUI() {
        // 처음 들어왔을 때는 닉네임이 변경되지 않았으므로 버튼 비활성화 상태로 시작
        updateNicknameButtonState(isEnabled = false)
    }

    private fun observeViewModel() {
        // 프로필 데이터 로드
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            // EditText가 비어있을 때만 세팅 (사용자 입력 중 덮어쓰기 방지)
            if (binding.mypEditNickEt.text.isEmpty()) {

                // 1. 닉네임 (최상위)
                binding.mypEditNickEt.setText(data.nickname)

                // 2. 유저 상세 정보 꺼내기
                // JSON 구조: result -> userImage -> user 안에 정보가 있다고 가정
                val userDetail = data.userImage?.user

                // 3. 데이터 바인딩 (데이터가 없으면 "" 빈 문자열 처리)
                binding.mypEditNameEt.setText(userDetail?.name ?: "")
                binding.mypEditChangeInfoEt.setText(userDetail?.meetPlace ?: "")

                // ★ [최초 등록 로직]
                // 서버에서 값이 안 오면(null) -> 빈 칸으로 뜸 -> 사용자가 입력 -> 저장
                binding.mypEditNumEt.setText(userDetail?.phone ?: "")
                binding.mypEditPostEt.setText(userDetail?.zipCode ?: "")
                binding.mypEditAddressEt.setText(userDetail?.address ?: "")
                binding.mypEditAddressDetailEt.setText(userDetail?.addressDetail ?: "")

                // 4. 이미지 세팅
                if (!data.userImage?.s3Key.isNullOrEmpty()) {
                    Glide.with(this).load(data.userImage.s3Key).circleCrop().into(binding.mypEditProfileIv)
                }
            }
        }
        // 이벤트 관찰 (토스트 메시지, 뒤로가기 등)
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
                            // 사용 가능하면 버튼을 다시 비활성화하거나, '확인 완료' 상태로 둘 수 있음 (여기선 그대로 둠)
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
        // 뒤로가기
        binding.mypEditBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // [기능 추가] 닉네임 변경 감지 (TextWatcher)
        binding.mypEditNickEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentNick = s.toString()
                val originNick = viewModel.profileData.value?.nickname ?: ""

                if (currentNick == originNick) {
                    // 원래 닉네임과 같음 -> 중복확인 버튼 비활성화 (grey_300)
                    isNicknameChecked = true // 원래 닉네임이니 검증된 것으로 간주
                    updateNicknameButtonState(isEnabled = false)
                } else {
                    // 닉네임 변경됨 -> 중복확인 버튼 활성화 (grey_900)
                    isNicknameChecked = false // 검증 필요함
                    updateNicknameButtonState(isEnabled = true)
                }
            }
        })

        // 닉네임 중복 확인 버튼 클릭
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

        // 우편번호 검색 (화면 이동)
        binding.mypEditPostCheckEt.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypPostcodeSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // 지역 검색 (화면 이동)
        binding.mypEditChangeInfoSearchEt.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypRegionSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // 수정하기 버튼 (최종 저장)
        binding.mypEditEditBtn.setOnClickListener {
            // 닉네임 변경되었는데 중복확인 안했으면 막기
            val currentNick = binding.mypEditNickEt.text.toString()
            val originNick = viewModel.profileData.value?.nickname

            if (currentNick != originNick && !isNicknameChecked) {
                Toast.makeText(context, "닉네임 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 요청 데이터 생성
            val request = UserUpdateRequest(
                nickname = currentNick,
                receiverName = binding.mypEditNameEt.text.toString(),
                phone = binding.mypEditNumEt.text.toString(),
                zipCode = binding.mypEditPostEt.text.toString(),
                address = binding.mypEditAddressEt.text.toString(),
                addressDetail = binding.mypEditAddressDetailEt.text.toString(),
                meetPlace = binding.mypEditChangeInfoEt.text.toString(),
                userImage = viewModel.profileData.value?.userImage?.s3Key // 기존 이미지 키 (변경 시 VM에서 처리)
            )

            // ViewModel에 요청 (이미지 파일이 있으면 업로드 후 저장, 없으면 바로 저장)
            viewModel.updateProfile(request, selectedImageFile)
        }
    }

    // [핵심 기능] 닉네임 버튼 상태 변경 함수
    private fun updateNicknameButtonState(isEnabled: Boolean) {
        val context = requireContext()
        with(binding.mypEditNickCheckEt) {
            this.isEnabled = isEnabled
            if (isEnabled) {
                // 변경됨 -> 활성화 (Grey 900 / White)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_900))
                setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                // 변경안됨 -> 비활성화 (Grey 300 / Grey 100)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey_300))
                setTextColor(ContextCompat.getColor(context, R.color.grey_100))
            }
        }
    }

    // 갤러리 선택 결과 핸들러
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Glide.with(this).load(uri).circleCrop().into(binding.mypEditProfileIv)
            selectedImageFile = uriToFile(uri) // 파일로 변환
        }
    }

    // 결과 수신 리스너 (주소, 지역)
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

    // Uri -> File 변환 유틸
    private fun uriToFile(uri: Uri): File? {
        return try {
            val contentResolver = requireContext().contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onResume() {
        super.onResume()
        // 하단 탭바 숨기기
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}