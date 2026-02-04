package com.bookiibookii.bookiibookii.myPage.set

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypSetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mypSettingBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 1. 공지사항 이동
        binding.mypSetNoticeNextIv.setOnClickListener {
            navigateTo(MypNoticeFragment())
        }

        // 2. 문의하기 이동
        binding.mypSetQuestionNextIv.setOnClickListener {
            navigateTo(MypQuestionFragment())
        }

        // 3. 신고하기 이동
        binding.mypSetReportNextIv.setOnClickListener {
            navigateTo(MypReportFragment())
        }

        // 4. 이용약관 이동
        binding.mypServiceTv.setOnClickListener {
            navigateTo(MypServiceFragment())
        }

        // 5. 개인정보 처리방침 이동
        binding.mypInformationTv.setOnClickListener {
            navigateTo(MypInformationFragment())
        }

        binding.mypSetLogoutTv.setOnClickListener {
            performLogout()
        }

        binding.mypSetQuitTv.setOnClickListener {
            performWithdraw()
        }
    }

    private fun performWithdraw() {
        lifecycleScope.launch {
            try {
                // API 호출
                val response = RetrofitClient.getInstance(requireContext()).withdraw()

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

        // 1. SharedPreferences 토큰 삭제
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // 2. 로그인 화면으로 이동 (Activity 스택 초기화)
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        activity?.finish()
    }

    private fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment) // 메인 액티비티의 컨테이너 ID 확인 필요
            .addToBackStack(null)
            .commit()
    }
    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // 1. 서버에 로그아웃 요청 (토큰은 자동으로 헤더에 실려감)
                val response = RetrofitClient.getInstance(requireContext()).logout()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 2. 서버 응답 성공 시 -> 앱 내부 데이터 삭제 및 이동
                    handleLogoutSuccess()
                } else {
                    // 실패 시 메시지 (토큰 만료 등)
                    Log.e("Logout", "실패: ${response.code()} ${response.message()}")
                    Toast.makeText(requireContext(), "로그아웃 실패했습니다.", Toast.LENGTH_SHORT).show()

                    // (선택) 만약 서버 에러가 나더라도 앱에서는 강제로 내보내고 싶다면
                    // 여기서 handleLogoutSuccess()를 호출해버려도 됩니다.
                }
            } catch (e: Exception) {
                Log.e("Logout", "네트워크 오류", e)
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 로그아웃 성공 후 처리하는 함수 (따로 뺌)
    private fun handleLogoutSuccess() {
        // 1. 내부 저장소(SharedPreferences) 토큰 삭제
        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply() // 싹 지우기

        // 2. 로그인 화면으로 이동 & 백스택 비우기 (뒤로가기 방지)
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