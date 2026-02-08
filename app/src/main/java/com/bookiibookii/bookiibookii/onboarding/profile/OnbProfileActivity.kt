package com.bookiibookii.bookiibookii.onboarding.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.widget.addTextChangedListener
import android.text.InputFilter
import android.text.Spannable
import android.view.inputmethod.BaseInputConnection
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ActivityOnbProfileBinding
import com.bookiibookii.bookiibookii.onboarding.steps.OnbStepActivity
import java.io.File

class OnbProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnbProfileBinding

    private val viewModel: OnbProfileViewModel by viewModels()

    // 닉네임 중복 확인 완료 여부
    private var isNicknameChecked = false

    // 카메라 촬영 결과 Uri
    private var cameraImageUri: Uri? = null

    // 업로드 완료된 프로필 이미지 S3 Key
    private var uploadedS3Key: String? = null

    // 문자 1개 허용 여부(필터에서 사용)
    private val nicknameAllowedCharRegex =
        Regex("[가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9._\\-_/()\\[\\]:!?]")

    // 전체 문자열 허용 여부(최종 검증에서 사용)
    private val nicknameAllowedRegex =
        Regex("^[가-힣A-Za-z0-9._\\-_/()\\[\\]:!?]+$")

    // 갤러리 이미지 선택 런처 (권한 불필요)
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.ivProfile.setImageURI(uri)
                viewModel.uploadProfileImage(contentResolver, uri)
            }
        }

    // 카메라 촬영 런처
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            // TODO: 디버그 로그 제거
            android.util.Log.d("IMG_UI", "takePicture success=$success, uri=$cameraImageUri")

            if (success) {
                cameraImageUri?.let {
                    binding.ivProfile.setImageURI(it)
                    // TODO: 디버그 로그 제거
                    android.util.Log.d("IMG_UI", "call uploadProfileImage uri=$it")
                    viewModel.uploadProfileImage(contentResolver, it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnbProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNicknameFilters()
        initView()
        initListeners()
        observeViewModel()
    }

    // 초기 UI 상태 설정
    private fun initView() {
        binding.btnCheck.isEnabled = false
        binding.includeFooterButton.btnFooter.isEnabled = false
        binding.layoutValidation.visibility = View.GONE
    }

    private fun initListeners() {

        // 닉네임 입력 변경 시 버튼 상태 초기화
        binding.etNickname.addTextChangedListener { text ->
            val nickname = text?.toString().orEmpty()

            // 입력 변경 시 중복 확인 상태 초기화
            isNicknameChecked = false
            binding.includeFooterButton.btnFooter.isEnabled = false
            binding.layoutValidation.visibility = View.GONE

            // 형식이 유효한 경우에만 중복 확인 버튼 활성화
            binding.btnCheck.isEnabled =
                validateNicknameInput(nickname) == NicknameInputState.VALID
        }

        // 닉네임 중복 확인 버튼 클릭
        binding.btnCheck.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()

            // 형식 검증
            if (validateNicknameInput(nickname) != NicknameInputState.VALID) {
                showError("허용되지 않은 문자가 포함되어 있습니다.")
                return@setOnClickListener
            }

            // 서버 중복 확인 요청
            viewModel.checkNickname(nickname)
        }

        // 프로필 이미지 수정 버튼 클릭
        binding.ivProfileEdit.setOnClickListener {
            OnbProfileBottomSheet(object : OnbProfileBottomSheet.Listener {
                override fun onClickCamera() {
                    openCamera()
                }

                override fun onClickGallery() {
                    openGallery()
                }
            }).show(supportFragmentManager, "OnbProfileBottomSheet")
        }

        // 다음 버튼 클릭
        binding.includeFooterButton.btnFooter.setOnClickListener {
            if (!isNicknameChecked) return@setOnClickListener

            val nickname = binding.etNickname.text.toString().trim()

            startActivity(
                Intent(this, OnbStepActivity::class.java)
                    .putExtra(OnbStepActivity.EXTRA_NAME, nickname)
                    .putExtra(OnbStepActivity.EXTRA_S3_KEY, uploadedS3Key)
            )
        }
    }

    // ViewModel 상태 관찰
    private fun observeViewModel() {
        viewModel.nicknameState.observe(this) { state ->
            when (state) {
                NicknameCheckState.Idle -> Unit

                NicknameCheckState.Loading -> {
                    binding.btnCheck.isEnabled = false
                    binding.includeFooterButton.btnFooter.isEnabled = false
                    binding.layoutValidation.visibility = View.GONE
                }

                is NicknameCheckState.Available -> {
                    enableCheckButtonIfValid()
                    showSuccess(state.message)
                }

                is NicknameCheckState.Duplicated -> {
                    enableCheckButtonIfValid()
                    showError(state.message)
                }

                is NicknameCheckState.Error -> {
                    enableCheckButtonIfValid()
                    showError(state.message)
                }
            }
        }

        viewModel.imageUploadState.observe(this) { state ->
            when (state) {
                ProfileImageUploadState.Idle -> Unit

                ProfileImageUploadState.Loading -> {
                    binding.ivProfileEdit.isEnabled = false
                    binding.includeFooterButton.btnFooter.isEnabled = false
                }

                is ProfileImageUploadState.Success -> {
                    uploadedS3Key = state.s3Key
                    binding.ivProfileEdit.isEnabled = true
                    binding.includeFooterButton.btnFooter.isEnabled = isNicknameChecked
                    showCustomToast("프로필 이미지가 업로드되었습니다.")
                }

                is ProfileImageUploadState.Error -> {
                    binding.ivProfileEdit.isEnabled = true
                    binding.includeFooterButton.btnFooter.isEnabled = isNicknameChecked
                    showCustomToast(message = state.message)
                }
            }
        }
    }

    // 닉네임이 유효한 경우 중복 확인 버튼 활성화
    private fun enableCheckButtonIfValid() {
        val nickname = binding.etNickname.text?.toString()?.trim().orEmpty()
        binding.btnCheck.isEnabled =
            validateNicknameInput(nickname) == NicknameInputState.VALID
    }

    // 닉네임 사용 가능 UI 처리
    private fun showSuccess(message: String) {
        isNicknameChecked = true

        binding.layoutValidation.apply {
            visibility = View.VISIBLE
            setBackgroundResource(R.drawable.bg_validation_success)
        }

        binding.ivValidation.setImageResource(R.drawable.ic_success)
        binding.tvValidation.text = message
        binding.tvValidation.setTextColor("#00C317".toColorInt())

        binding.includeFooterButton.btnFooter.isEnabled = true
    }

    // 닉네임 오류 UI 처리
    private fun showError(message: String) {
        isNicknameChecked = false

        binding.layoutValidation.apply {
            visibility = View.VISIBLE
            setBackgroundResource(R.drawable.bg_validation_error)
        }

        binding.ivValidation.setImageResource(R.drawable.ic_error)
        binding.tvValidation.text = message
        binding.tvValidation.setTextColor(getColor(R.color.ui_point_red))

        binding.includeFooterButton.btnFooter.isEnabled = false
    }

    // 닉네임 형식 검증
    private fun validateNicknameInput(nickname: String): NicknameInputState {
        if (nickname.isBlank()) return NicknameInputState.EMPTY
        if (nickname.length > 10) return NicknameInputState.INVALID
        if (nickname.any { it.isWhitespace() || Character.isSurrogate(it) }) return NicknameInputState.INVALID

        return if (nicknameAllowedRegex.matches(nickname))
            NicknameInputState.VALID
        else
            NicknameInputState.INVALID
    }

    // 닉네임 입력 단계에서 허용되지 않은 문자/공백/이모지 입력 방지 + 10자 제한
    private fun setupNicknameFilters() {
        val lengthFilter = InputFilter.LengthFilter(10)

        val blockInvalidInputFilter = InputFilter { source, start, end, _, _, _ ->
            if (source.isEmpty()) return@InputFilter null // 삭제 허용

            for (i in start until end) {
                val ch = source[i]

                if (ch.isWhitespace()) return@InputFilter ""
                if (Character.isSurrogate(ch)) return@InputFilter ""
                if (!nicknameAllowedCharRegex.matches(ch.toString())) return@InputFilter ""
            }

            null
        }

        binding.etNickname.filters = arrayOf(lengthFilter, blockInvalidInputFilter)
    }

    // 갤러리 열기
    private fun openGallery() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // 카메라 열기
    private fun openCamera() {
        val uri = createCameraImageUri() ?: return
        cameraImageUri = uri
        takePictureLauncher.launch(uri)
    }

    // 카메라 촬영용 임시 Uri 생성
    private fun createCameraImageUri(): Uri? {
        val dir = File(cacheDir, "camera")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "profile_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
    }

    // 커스텀 토스트 표시
    private fun showCustomToast(
        message: String,
        iconRes: Int = R.drawable.ic_check
    ) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast_custom, null)

        val iconIv = layout.findViewById<ImageView>(R.id.toast_icon_iv)
        val messageTv = layout.findViewById<TextView>(R.id.toast_message_tv)

        iconIv.setImageResource(iconRes)
        messageTv.text = message

        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 120)
        }.show()
    }

    // 닉네임 입력 상태
    private enum class NicknameInputState {
        EMPTY, INVALID, VALID
    }
}