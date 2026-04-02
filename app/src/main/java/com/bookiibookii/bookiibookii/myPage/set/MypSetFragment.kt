package com.bookiibookii.bookiibookii.myPage.set

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.BaseDetailFragment
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypSetBinding
import com.bookiibookii.bookiibookii.myPage.notice.MypNoticeFragment
import com.bookiibookii.bookiibookii.myPage.question.MypQuestionFragment
import com.bookiibookii.bookiibookii.myPage.report.MypReportFragment
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import kotlinx.coroutines.launch

class MypSetFragment : BaseDetailFragment<FragmentMypSetBinding>() {

    private lateinit var sharedPreferences: SharedPreferences
    private var isPushEnabled = false // 현재 토글 상태

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setPushState(true, true)
            requireContext().showCustomToast("알림이 설정되었습니다.", true)
        } else {
            requireContext().showCustomToast("알림 권한이 거부되었습니다. 기기 설정에서 허용해주세요.", false)
            setPushState(false, true)
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMypSetBinding {
        return FragmentMypSetBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        initPushToggle()

        binding.mypSettingBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        binding.layoutNotice.setOnClickListener { navigateTo(MypNoticeFragment()) }
        binding.layoutQuestion.setOnClickListener { navigateTo(MypQuestionFragment()) }
        binding.layoutReport.setOnClickListener { navigateTo(MypReportFragment()) }
        binding.mypServiceTv.setOnClickListener { navigateTo(MypServiceFragment()) }
        binding.mypInformationTv.setOnClickListener { navigateTo(MypInformationFragment()) }

        binding.mypSetLogoutTv.setOnClickListener {
            CommonDialog(
                context = requireContext(),
                title = "로그아웃",
                subtitle = "",
                content = "로그아웃 하시겠습니까?",
                confirmBtnText = "로그아웃",
                confirmBtnColor = R.color.ui_point_red,
                onConfirmClick = { performLogout() }
            ).show()
        }

        binding.mypSetQuitTv.setOnClickListener {
            CommonDialog(
                context = requireContext(),
                title = "회원 탈퇴",
                subtitle = "",
                content = "회원 탈퇴 시 되돌릴 수 없습니다.\n그래도 하시겠습니까?",
                confirmBtnText = "회원 탈퇴",
                confirmBtnColor = R.color.ui_point_red,
                onConfirmClick = { performWithdraw() }
            ).show()
        }
    }

    private fun initPushToggle() {
        val isPushSavedEnabled = sharedPreferences.getBoolean("push_enabled", false)

        val hasOsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        // 초기 상태 설정 (애니메이션 없이)
        isPushEnabled = isPushSavedEnabled && hasOsPermission
        setPushState(isPushEnabled, false)

        // 토글 클릭 이벤트
        binding.mypSetPushToggleContainer.setOnClickListener {
            if (!isPushEnabled) {
                // 끄기 -> 켜기
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        setPushState(true, true)
                        requireContext().showCustomToast("알림이 설정되었습니다.", true)
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    setPushState(true, true)
                    requireContext().showCustomToast("알림이 설정되었습니다.", true)
                }
            } else {
                // 켜기 -> 끄기
                setPushState(false, true)
                requireContext().showCustomToast("알림이 해제되었습니다.", true)
            }
        }
    }

    private fun setPushState(enabled: Boolean, animate: Boolean) {
        isPushEnabled = enabled
        sharedPreferences.edit().putBoolean("push_enabled", enabled).apply()

        val container = binding.mypSetPushToggleContainer
        val thumb = binding.mypSetPushThumb

        // 배경색 변경
        val bgColor = if (enabled) R.color.pre_main else R.color.grey_400
        container.backgroundTintList = ContextCompat.getColorStateList(requireContext(), bgColor)

        val startMargin = dpToPx(2) // 왼쪽 여백 (꺼졌을 때 위치)

        val endMargin = dpToPx(22)

        val targetMargin = if (enabled) endMargin else startMargin

        val layoutParams = thumb.layoutParams as FrameLayout.LayoutParams

        if (animate) {
            val animator = android.animation.ValueAnimator.ofInt(layoutParams.marginStart, targetMargin)
            animator.duration = 200
            animator.addUpdateListener { animation ->
                val margin = animation.animatedValue as Int
                layoutParams.marginStart = margin
                thumb.layoutParams = layoutParams
            }
            animator.start()
        } else {
            layoutParams.marginStart = targetMargin
            thumb.layoutParams = layoutParams
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun performWithdraw() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().withdraw()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("Setting", "회원 탈퇴 성공")
                    clearLocalDataAndMoveToLogin()
                } else {
                    Log.e("Setting", "탈퇴 실패: ${response.code()}")
                    requireContext().showCustomToast("탈퇴 처리에 실패했습니다.", false)
                }
            } catch (e: Exception) {
                Log.e("Setting", "탈퇴 통신 오류", e)
                requireContext().showCustomToast("네트워크 오류가 발생했습니다.", false)
            }
        }
    }

    private fun clearLocalDataAndMoveToLogin() {
        val context = requireContext()
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun navigateTo(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun performLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().logout()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    handleLogoutSuccess()
                } else {
                    Log.e("Logout", "실패: ${response.code()} ${response.message()}")
                    requireContext().showCustomToast("로그아웃 실패했습니다.", false)
                    handleLogoutSuccess()
                }
            } catch (e: Exception) {
                Log.e("Logout", "네트워크 오류", e)
                requireContext().showCustomToast("네트워크 오류가 발생했습니다.", false)
            }
        }
    }

    private fun handleLogoutSuccess() {
        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        requireContext().showCustomToast("로그아웃 되었습니다.", true)
    }
}