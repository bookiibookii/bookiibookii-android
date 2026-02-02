package com.bookiibookii.bookiibookii.myPage.set

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentMypSetBinding
import com.bookiibookii.bookiibookii.myPage.Notice.MypNoticeFragment
import com.bookiibookii.bookiibookii.myPage.question.MypQuestionFragment
import com.bookiibookii.bookiibookii.myPage.report.MypReportFragment
import com.bookiibookii.bookiibookii.myPage.setting.MypInformationFragment
import com.bookiibookii.bookiibookii.myPage.setting.MypServiceFragment

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
    }

    private fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment) // 메인 액티비티의 컨테이너 ID 확인 필요
            .addToBackStack(null)
            .commit()
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