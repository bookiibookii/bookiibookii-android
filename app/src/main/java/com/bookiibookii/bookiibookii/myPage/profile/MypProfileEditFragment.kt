package com.bookiibookii.bookiibookii.myPage.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.MypProfileData
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.databinding.FragmentMypProfileEditBinding

class MypProfileEditFragment : Fragment() {

    private var _binding: FragmentMypProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyPageViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 초기 데이터 바인딩 (ViewModel 데이터가 있다면 채우기)
        viewModel.profileData.value?.let { data ->
            binding.mypEditNickEt.setText(data.nickname)
            binding.mypEditNameEt.setText(data.name)
            binding.mypEditNumEt.setText(data.phone)
            binding.mypEditAddressEt.setText(data.address)
            binding.mypEditAddressDetailEt.setText(data.addressDetail)
            binding.mypEditChangeInfoEt.setText(data.regionInfo)
        }

        initListeners()
        initResultListener() // 지역 검색 결과 수신 대기
    }

    private fun initListeners() {
        binding.mypEditBackIv.setOnClickListener { parentFragmentManager.popBackStack() }

        // 중복 확인, 우편번호 검색 (더미 로직)
        binding.mypEditNickCheckEt.setOnClickListener { Toast.makeText(context, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show() }

        binding.mypEditPostCheckEt.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypPostcodeSearchFragment()) // 검색 화면으로 이동
                .addToBackStack(null)
                .commit()
        }

        // [중요] 지역 검색 화면 이동
        binding.mypEditChangeInfoSearchEt.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MypRegionSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // 수정하기 버튼 클릭
        binding.mypEditEditBtn.setOnClickListener {
            // 입력된 정보 가져오기
            val newData = MypProfileData(
                nickname = binding.mypEditNickEt.text.toString(),
                name = binding.mypEditNameEt.text.toString(),
                phone = binding.mypEditNumEt.text.toString(),
                address = binding.mypEditAddressEt.text.toString(),
                addressDetail = binding.mypEditAddressDetailEt.text.toString(),
                regionInfo = binding.mypEditChangeInfoEt.text.toString()
            )

            // ViewModel 업데이트 (마이페이지에 즉시 반영됨)
            viewModel.updateProfile(newData)
            Toast.makeText(context, "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    // [중요] 지역 검색 프래그먼트로부터 결과 수신
    private fun initResultListener() {
        setFragmentResultListener("requestKeyRegion") { _, bundle ->
            val selectedRegion = bundle.getString("regionResult")
            binding.mypEditChangeInfoEt.setText(selectedRegion)
        }

        setFragmentResultListener("requestKeyPostcode") { _, bundle ->
            val zonecode = bundle.getString("zonecode") // 우편번호
            val address = bundle.getString("address")   // 기본 주소

            // 받아온 데이터를 EditText에 반영
            binding.mypEditPostEt.setText(zonecode)
            binding.mypEditAddressEt.setText(address)

            // 상세주소 입력창을 비우고 포커스 주기 (편의성)
            binding.mypEditAddressDetailEt.setText("")
            binding.mypEditAddressDetailEt.requestFocus()
        }
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