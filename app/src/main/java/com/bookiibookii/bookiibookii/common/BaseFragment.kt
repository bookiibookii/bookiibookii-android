package com.bookiibookii.bookiibookii.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B : ViewBinding> : Fragment() {

    // 내부에서 사용할 nullable 바인딩 객체
    private var _binding: B? = null

    // 자식 클래스에서 편하게 쓸 수 있는 non-null 바인딩 객체 (get()을 통해 호출 시점에 _binding 반환)
    protected val binding get() = _binding!!

    // 자식 프래그먼트에서 인플레이터를 넘겨주도록 강제하는 추상 함수
    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): B

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = getFragmentBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 프래그먼트 루트 뷰에 Insets 적용이 필요하다면 여기서 호출
        // setupWindowInsets(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ★ 프래그먼트 뷰가 파괴될 때 바인딩 객체 해제 (메모리 누수 방지)
        _binding = null
    }

    /**
     * 프래그먼트 전용 Insets 설정 함수 (필요 시 자식에서 override)
     */
    open fun setupWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
    }
}