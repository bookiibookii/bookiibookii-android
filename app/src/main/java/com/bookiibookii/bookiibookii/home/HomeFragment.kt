package com.bookiibookii.bookiibookii.home

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.FragmentHomeBinding
import com.bookiibookii.bookiibookii.group.GroupFragment
import com.bookiibookii.bookiibookii.group.nav.GroupDestinations

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGreeting(username = "sayo")
        setupTabs()
        setupClickListeners()
    }

    private fun setupGreeting(username: String) {
        val greetingText = "안녕하세요, $username"
        val spannable = SpannableString(greetingText)
        val usernameStart = greetingText.indexOf(username)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.ui_main)),
            usernameStart,
            usernameStart + username.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvGreeting.text = spannable
    }

    private fun setupTabs() {
        selectTab(binding.tabRecommend)

        binding.tabRecommend.setOnClickListener { selectTab(binding.tabRecommend) }
        binding.tabAll.setOnClickListener { selectTab(binding.tabAll) }
        binding.tabApplied.setOnClickListener { selectTab(binding.tabApplied) }
    }

    private fun selectTab(selectedTab: TextView) {
        val tabs = listOf(binding.tabRecommend, binding.tabAll, binding.tabApplied)
        val indicators = listOf(
            binding.tabIndicatorRecommend,
            binding.tabIndicatorAll,
            binding.tabIndicatorApplied
        )

        tabs.forEachIndexed { i, tab ->
            val isSelected = tab == selectedTab
            tab.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isSelected) R.color.ui_main else R.color.grey_400
                )
            )
            indicators[i].setBackgroundColor(
                if (isSelected) ContextCompat.getColor(requireContext(), R.color.ui_main)
                else Color.TRANSPARENT
            )
        }
    }

    private fun setupClickListeners() {
        binding.searchBar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    GroupFragment.newInstance(GroupDestinations.SEARCH)
                )
                .addToBackStack(null)
                .commit()
        }
        binding.btnCreateGroup.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    GroupFragment.newInstance(GroupDestinations.EDITOR)
                )
                .addToBackStack(null)
                .commit()
        }
        binding.btnAlert.setOnClickListener {
            // TODO: 알림 화면으로 이동
        }
        binding.btnProfile.setOnClickListener {
            // TODO: 프로필/마이페이지로 이동
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
