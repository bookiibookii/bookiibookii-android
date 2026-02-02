package com.bookiibookii.bookiibookii.onboarding.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.widget.addTextChangedListener
import com.bookiibookii.bookiibookii.MainActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ActivityOnbProfileBinding
import com.bookiibookii.bookiibookii.onboarding.steps.OnbStepActivity
import java.io.File

class OnbProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnbProfileBinding

    // 중복 확인 완료 여부
    private var isNicknameChecked = false

    // 카메라 촬영용 임시 Uri
    private var cameraImageUri: Uri? = null

    // 서비스 금칙어
    // TODO: 임시 (API 연동 시 삭제)
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

    // 갤러리(포토피커) 런처: 권한 없이 동작
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.ivProfile.setImageURI(uri)
            }
        }

    // 카메라 촬영 런처: 촬영 성공 시 Uri 반영
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraImageUri?.let { binding.ivProfile.setImageURI(it) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnbProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initListeners()
    }

    private fun initView() {
        binding.btnCheck.isEnabled = false
        binding.includeFooterButton.btnFooter.isEnabled = false
        binding.layoutValidation.visibility = View.GONE
    }

    private fun initListeners() {

        // 닉네임 입력 변경: 실시간은 "버튼 활성/비활성" 중심으로만 처리
        binding.etNickname.addTextChangedListener { text ->
            val nickname = text?.toString().orEmpty()

            // 입력이 바뀌면 이전 중복확인 결과는 무효
            isNicknameChecked = false
            binding.includeFooterButton.btnFooter.isEnabled = false

            // 입력 중에는 하단 메시지 숨김(다시 검증 필요)
            binding.layoutValidation.visibility = View.GONE

            // 형식이 유효할 때만 중복확인 버튼 활성화
            binding.btnCheck.isEnabled = validateNicknameInput(nickname) == NicknameInputState.VALID
        }

        // 중복확인 버튼 클릭: 형식 -> 금칙어 -> 중복 순서로 검증
        binding.btnCheck.setOnClickListener {
            val nickname = binding.etNickname.text.toString()

            // 1) 형식 검증
            if (validateNicknameInput(nickname) != NicknameInputState.VALID) {
                showError("허용되지 않은 문자가 포함되어 있습니다.")
                return@setOnClickListener
            }

            // 2) 금칙어 검증
            if (containsBannedWord(nickname)) {
                showError("금칙어가 포함되어 있습니다.")
                return@setOnClickListener
            }

            // 3) 중복 확인 (TODO: API로 교체)
            val isAvailable = checkNicknameDummy(nickname)

            if (isAvailable) {
                showSuccess("사용 가능한 닉네임입니다.")
            } else {
                showError("이미 존재하는 닉네임입니다.")
            }
        }

        // 카메라 버튼 클릭(프로필 수정) -> BottomSheet 노출
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

            startActivity(Intent(this, OnbStepActivity::class.java))
        }
    }

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

    // TODO: 실제 API 연결 시 제거 예정
    private fun checkNicknameDummy(nickname: String): Boolean {
        val duplicated = listOf("admin", "test", "bookii")
        return !duplicated.contains(nickname.lowercase())
    }

    // 금칙어 포함 여부: 부분 포함도 차단
    private fun containsBannedWord(nickname: String): Boolean {
        val lower = nickname.lowercase()
        return bannedWords.any { banned -> lower.contains(banned.lowercase()) }
    }

    // 요구사항: 한글/영문/숫자 + 지정 특수문자만 허용, 공백/이모지 제한, 10자 제한
    private fun validateNicknameInput(nickname: String): NicknameInputState {
        if (nickname.isBlank()) return NicknameInputState.EMPTY
        if (nickname.length > 10) return NicknameInputState.INVALID
        if (nickname.any { it.isWhitespace() }) return NicknameInputState.INVALID

        // 허용 특수문자: . - _ / ( ) [ ] : ! ?
        val allowedRegex = Regex("^[가-힣A-Za-z0-9._\\-\\/\\(\\)\\[\\]:!?]+$")

        // 이모지/서로게이트(대부분의 이모지) 차단
        if (nickname.any { Character.isSurrogate(it) }) return NicknameInputState.INVALID

        return if (allowedRegex.matches(nickname)) NicknameInputState.VALID else NicknameInputState.INVALID
    }

    private fun openGallery() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun openCamera() {
        val uri = createCameraImageUri() ?: return
        cameraImageUri = uri
        takePictureLauncher.launch(uri)
    }

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

    private enum class NicknameInputState {
        EMPTY, INVALID, VALID
    }
}