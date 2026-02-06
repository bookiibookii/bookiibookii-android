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

    // 서비스 금칙어 (임시)
    // TODO: API 연동 후 삭제
    private val bannedWords = listOf(
        "관리자",
        "admin",
        "운영자",
        "bookii",
        "시발",
        "병신",
        "fuck",
        "shit"
    )

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

            // 금칙어 검증
            if (containsBannedWord(nickname)) {
                showError("금칙어가 포함되어 있습니다.")
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

    // 금칙어 포함 여부 확인
    private fun containsBannedWord(nickname: String): Boolean {
        val lower = nickname.lowercase()
        return bannedWords.any { banned -> lower.contains(banned.lowercase()) }
    }

    // 닉네임 형식 검증
    private fun validateNicknameInput(nickname: String): NicknameInputState {
        if (nickname.isBlank()) return NicknameInputState.EMPTY
        if (nickname.length > 10) return NicknameInputState.INVALID
        if (nickname.any { it.isWhitespace() }) return NicknameInputState.INVALID

        // 허용 문자 정규식
        val allowedRegex = Regex("^[가-힣A-Za-z0-9._\\-\\/\\(\\)\\[\\]:!?]+$")

        // 이모지(서로게이트 문자) 차단
        if (nickname.any { Character.isSurrogate(it) }) return NicknameInputState.INVALID

        return if (allowedRegex.matches(nickname))
            NicknameInputState.VALID
        else
            NicknameInputState.INVALID
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