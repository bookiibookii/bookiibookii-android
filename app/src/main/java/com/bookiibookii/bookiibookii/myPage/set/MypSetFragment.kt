package com.bookiibookii.bookiibookii.myPage.set

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.CommonDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentMypSetBinding
import com.bookiibookii.bookiibookii.myPage.Notice.MypNoticeFragment
import com.bookiibookii.bookiibookii.myPage.question.MypQuestionFragment
import com.bookiibookii.bookiibookii.myPage.report.MypReportFragment
import com.bookiibookii.bookiibookii.myPage.setting.MypInformationFragment
import com.bookiibookii.bookiibookii.myPage.setting.MypServiceFragment
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import kotlinx.coroutines.launch

class MypSetFragment : Fragment() {

    private var _binding: FragmentMypSetBinding? = null
    private val binding get() = _binding!!

    // ★ 알림 상태 저장을 위한 SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    // ★ 안드로이드 13 이상 알림 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 권한 허용됨 -> 스위치 ON 유지, 저장소 업데이트
            sharedPreferences.edit().putBoolean("push_enabled", true).apply()
            binding.mypSetPushIv.isChecked = true
            Toast.makeText(requireContext(), "알림이 설정되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 권한 거부됨 -> 스위치 강제 OFF, 안내 메시지
            Toast.makeText(requireContext(), "알림 권한이 거부되었습니다. 기기 설정에서 허용해주세요.", Toast.LENGTH_SHORT).show()
            sharedPreferences.edit().putBoolean("push_enabled", false).apply()

            // 리스너가 트리거되지 않게 잠시 해제 후 상태 변경
            binding.mypSetPushIv.setOnCheckedChangeListener(null)
            binding.mypSetPushIv.isChecked = false
            setPushToggleListener() // 리스너 다시 부착
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypSetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SharedPreferences 초기화
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        // ★ 알림 토글 버튼 초기화
        initPushToggle()

        binding.mypSettingBackIv.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        // 1. 공지사항 이동
        binding.layoutNotice.setOnClickListener { navigateTo(MypNoticeFragment()) }
        // 2. 문의하기 이동
        binding.layoutQuestion.setOnClickListener { navigateTo(MypQuestionFragment()) }
        // 3. 신고하기 이동
        binding.layoutReport.setOnClickListener { navigateTo(MypReportFragment()) }
        // 4. 이용약관 이동
        binding.mypServiceTv.setOnClickListener { navigateTo(MypServiceFragment()) }
        // 5. 개인정보 처리방침 이동
        binding.mypInformationTv.setOnClickListener { navigateTo(MypInformationFragment()) }

        // ★ 로그아웃 다이얼로그 연동
        binding.mypSetLogoutTv.setOnClickListener {
            CommonDialog(
                context = requireContext(),
                title = "로그아웃",
                subtitle = "",
                content = "로그아웃 하시겠습니까?",
                confirmBtnText = "로그아웃",
                confirmBtnColor = R.color.ui_point_red, // 앱에서 사용하는 빨간색 리소스
                onConfirmClick = { performLogout() }
            ).show()
        }

        // ★ 회원탈퇴 다이얼로그 연동
        binding.mypSetQuitTv.setOnClickListener {
            CommonDialog(
                context = requireContext(),
                title = "회원탈퇴",
                subtitle = "",
                content = "회원탈퇴 시 되돌릴 수 없습니다.\n그래도 하시겠습니까?",
                confirmBtnText = "회원탈퇴",
                confirmBtnColor = R.color.ui_point_red, // 앱에서 사용하는 빨간색 리소스
                onConfirmClick = { performWithdraw() }
            ).show()
        }
    }

    private fun initPushToggle() {
        // 1. 저장된 스위치 상태 불러오기
        val isPushSavedEnabled = sharedPreferences.getBoolean("push_enabled", false)

        // 2. 기기 자체의 알림 권한이 켜져있는지 확인 (Android 13 이상)
        val hasOsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 12 이하는 기본 허용
        }

        // 3. 앱 내 설정(저장소)과 기기 권한이 모두 켜져 있을 때만 스위치를 ON 상태로 둠
        binding.mypSetPushIv.setOnCheckedChangeListener(null)
        binding.mypSetPushIv.isChecked = isPushSavedEnabled && hasOsPermission

        // 4. 리스너 부착
        setPushToggleListener()
    }

    private fun setPushToggleListener() {
        binding.mypSetPushIv.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 스위치를 켤 때
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        // 이미 권한이 있으면 켜기 완료
                        sharedPreferences.edit().putBoolean("push_enabled", true).apply()
                        Toast.makeText(requireContext(), "알림이 설정되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        // 권한이 없으면 사용자에게 권한 요청 창 띄우기
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    // Android 12 이하일 때는 바로 저장
                    sharedPreferences.edit().putBoolean("push_enabled", true).apply()
                    Toast.makeText(requireContext(), "알림이 설정되었습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // 스위치를 끌 때
                sharedPreferences.edit().putBoolean("push_enabled", false).apply()
                Toast.makeText(requireContext(), "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performWithdraw() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().withdraw()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("Setting", "회원 탈퇴 성공")
                    clearLocalDataAndMoveToLogin()
                } else {
                    Log.e("Setting", "탈퇴 실패: ${response.code()}")
                    Toast.makeText(context, "탈퇴 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Setting", "탈퇴 통신 오류", e)
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
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
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().logout()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    handleLogoutSuccess()
                } else {
                    Log.e("Logout", "실패: ${response.code()} ${response.message()}")
                    Toast.makeText(requireContext(), "로그아웃 실패했습니다.", Toast.LENGTH_SHORT).show()
                    handleLogoutSuccess()
                }
            } catch (e: Exception) {
                Log.e("Logout", "네트워크 오류", e)
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLogoutSuccess() {
        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
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