package com.bookiibookii.bookiibookii.onboarding.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R

class OnbStep2Fragment : Fragment(R.layout.fragment_onb_step2) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 최초 진입 시 다음 버튼 비활성
        (activity as? OnbStepHost)?.setNextEnabled(false)

        // TODO
        // 여기서 Step2 선택 로직 구현
        // 선택 완료 시 아래 호출
        //
        // (activity as? OnbStepHost)?.setNextEnabled(true)
    }
}