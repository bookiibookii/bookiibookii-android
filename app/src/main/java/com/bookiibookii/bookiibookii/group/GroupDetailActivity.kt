package com.bookiibookii.bookiibookii.group

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.databinding.ActivityGrpHostBinding
import com.bookiibookii.bookiibookii.databinding.DialogGroupJoinBinding
import com.bumptech.glide.Glide // Glide 라이브러리 추가 필요 (없으면 주석 처리)
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpHostBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var isHost: Boolean = false
    private val currentUserId = "user123"
    private val groupHostId = "user123"

    // ★ 데이터를 저장할 변수들 (초기값은 빈 문자열)
    private var groupType: String = "RELAY"
    private var bookTitle: String = ""
    private var bookAuthor: String = ""
    private var bookGenre: String = ""
    private var startDate: String = ""
    private var coverImgUrl: String = ""
    private var profileImgUrl: String = ""
    private var nickname: String = ""
    private var memberCount: String = ""
    private var tagsList: ArrayList<String>? = null
    private var status: String = ""
    private var deadline: String = ""
    private var isHot: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentData()

        // ★ 화면 그리기 함수
        setTopCardUi()   // 상단 카드뷰
        setBottomMemberUi() // 하단 멤버 리스트

        checkUserAuthority()
        initView()
        initBottomSheet()

        val itemBinding = binding.actGrpHoIncludedItem

        if (isHost) {
            itemBinding.grpItemMgBtnTitleTv.text = "참여 요청 관리"
            itemBinding.grpItemMgBtnNumTv.visibility = View.VISIBLE
            itemBinding.grpItemMgBtnNumTv.text = "(5)"
            itemBinding.grpItemMgManageBtn.setOnClickListener {
                val intent = Intent(this, GroupJoinManagementActivity::class.java)
                startActivity(intent)
            }
        } else {
            itemBinding.grpItemMgBtnTitleTv.text = "참여 신청하기"
            itemBinding.grpItemMgBtnNumTv.visibility = View.GONE
            itemBinding.grpItemMgManageBtn.setOnClickListener {
                showJoinDialog()
            }
        }
    }

    // ★ Intent로 넘어온 데이터 추출
    private fun getIntentData() {
        groupType = intent.getStringExtra("GROUP_TYPE") ?: "RELAY"
        bookTitle = intent.getStringExtra("BOOK_TITLE") ?: "제목 없음"
        bookAuthor = intent.getStringExtra("BOOK_AUTHOR") ?: "저자 미상"
        bookGenre = intent.getStringExtra("BOOK_GENRE") ?: ""
        startDate = intent.getStringExtra("START_DATE") ?: "날짜 미정"
        coverImgUrl = intent.getStringExtra("COVER_IMG") ?: ""
        profileImgUrl = intent.getStringExtra("PROFILE_IMG") ?: ""
        tagsList = intent.getStringArrayListExtra("TAGS")
        status = intent.getStringExtra("STATUS") ?: "모집 중"
        deadline = intent.getStringExtra("READING_PERIOD") ?: "0"
        memberCount = intent.getStringExtra("MEMBER_COUNT") ?: "0"
        isHot = intent.getBooleanExtra("IS_HOT", false)
        nickname = intent.getStringExtra("USER_NICNAME") ?: "닉네임 없음"
    }

    // 화면에 데이터 바인딩
    private fun setTopCardUi() {
        //
        with(binding.actGrpHoIncludedItem) {
            grpItemMgBookTitleTv.text = bookTitle
            grpItemMgBookAuthorTv.text = bookAuthor
            grpItemMgBookSortTv.text = bookGenre
            grpItemMgDateTv.text = startDate
            grpItemMgNicknameTv.text = nickname
            grpItemMgStatusCp.text = status
            grpItemMgDeadlineNoTv.text = deadline
            grpItemMgMemStatusNoTv.text = memberCount

            // HOT 뱃지
            grpItemMgHotCp.visibility = if (isHot) View.VISIBLE else View.GONE

            //  책 표지
            Glide.with(this@GroupDetailActivity)
                .load(coverImgUrl)
                .centerCrop()
                .into(grpItemMgCoverIv)

            // 작성자 프로필
            Glide.with(this@GroupDetailActivity)
                .load(profileImgUrl)
                .placeholder(R.drawable.ic_profile)
                .circleCrop() // 동그랗게!
                .into(grpItemMgProfileIv)

            // 태그 처리
            val chipViews = listOf(
                grpItemMgHash1Cp, grpItemMgHash2Cp, grpItemMgHash3Cp,
                grpItemMgHash4Cp, grpItemMgHash5Cp
            )
            val currentTags = tagsList ?: arrayListOf()

            chipViews.forEachIndexed { index, chip ->
                if (index < currentTags.size) {
                    chip.text = currentTags[index]
                    chip.visibility = View.VISIBLE
                } else {
                    chip.visibility = View.GONE
                }
            }
        }
    }

    // 하단 멤버 리스트 (Main Activity Layout) 데이터 세팅

    private fun setBottomMemberUi() {
        with(binding) {
            // 메인 타이틀
            actGrpHoMainTitleTv.text = bookTitle

            //  하단 멤버 프로필
            Glide.with(this@GroupDetailActivity)
                .load(profileImgUrl)
                .placeholder(R.drawable.ic_profile)
                .circleCrop() // 왜 얘는 둥굴까
                .into(actGrpHoMemberProfile1Iv)

            // 닉네임
            actGrpHoMemberNickname1Tv.text = nickname

            // 호스트 태그 표시 로직
            val isHostUser = true // (테스트용)
            if (isHostUser) {
                actGrpHoHostCp.visibility = View.VISIBLE
            } else {
                actGrpHoHostCp.visibility = View.GONE
            }
        }
    }

    private fun initView() {
        binding.actGrpHoBackIv.setOnClickListener { handleBackPress() }

        binding.grpMgBottomSheetSendIv.setOnClickListener {
            val text = binding.grpMgBottomSheetInputEt.text.toString()
            if(text.isNotBlank()) {
                Toast.makeText(this, "전송: $text", Toast.LENGTH_SHORT).show()
                binding.grpMgBottomSheetInputEt.text.clear()
            }
        }

        binding.grpMgBottomSheetReloadIv.setOnClickListener {
            Toast.makeText(this, "새로고침 클릭", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkUserAuthority() {
        isHost = (currentUserId == groupHostId)
        if (isHost) setupHostMode() else setupGuestMode()
    }

    private fun setupHostMode() {
        binding.actGrpHoMoreIv.setOnClickListener {
            val bottomSheet = GroupMoreBottomSheet(isHost = true) { action ->
                when (action) {
                    "EDIT" -> {
                        val intent = Intent(this, GroupGenerationActivity::class.java)

                        //수정 모드 설정 및 현재 데이터 다시 전달
                        intent.putExtra("IS_EDIT_MODE", true)
                        intent.putExtra("GROUP_TYPE", groupType) // 함께/이어 타입 전달
                        intent.putExtra("BOOK_TITLE", bookTitle)
                        intent.putExtra("START_DATE", startDate)
                        intent.putExtra("DURATION", "7") // 기간 (더미 or 실제값)

                        // 소개글 등 추가 데이터가 있다면 여기서 putExtra

                        startActivity(intent)
                    }
                    "DELETE" -> showDeleteDialog()
                }
            }
            bottomSheet.show(supportFragmentManager, "GroupMoreBottomSheet")
        }
    }

    private fun setupGuestMode() {
        binding.actGrpHoMoreIv.setOnClickListener {
            val bottomSheet = GroupMoreBottomSheet(isHost = false) { action ->
                when (action) {
                    "REPORT" -> Toast.makeText(this, "신고하기 화면으로 이동", Toast.LENGTH_SHORT).show()
                }
            }
            bottomSheet.show(supportFragmentManager, "GroupMoreBottomSheet")
        }
    }

    private fun initBottomSheet() {
        val bottomSheetLayout = binding.persistentBottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.peekHeight = dpToPx(170)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.isHideable = false

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.grpMgBottomSheetReloadIv.alpha = slideOffset
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { handleBackPress() }
        })
    }

    private fun handleBackPress() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            finish()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun showJoinDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogGroupJoinBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

        dialogBinding.dialogJoinInputEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                dialogBinding.dialogJoinCountTv.text = "$length/200"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        dialogBinding.dialogJoinCloseIv.setOnClickListener { dialog.dismiss() }
        dialogBinding.dialogJoinCancelBtn.setOnClickListener { dialog.dismiss() }
        dialogBinding.dialogJoinEnterBtn.setOnClickListener {
            val message = dialogBinding.dialogJoinInputEt.text.toString()
            Toast.makeText(this, "신청 내용: $message", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDeleteDialog() {
        CommonDialog(
            context = this,
            title = "그룹 삭제",
            subtitle = bookTitle,
            content = "그룹을 정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.",
            confirmBtnText = "삭제",
            confirmBtnColor = R.color.ui_point_red,
            onConfirmClick = {
                Toast.makeText(this, "그룹이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                finish() // 삭제 후 액티비티 종료
            }
        ).show()
    }
}