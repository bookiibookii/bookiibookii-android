package com.bookiibookii.bookiibookii.myPage

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.MypMyReviewFragment
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.LoadingDialog // ★ 로딩 다이얼로그 import
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.MypReview
import com.bookiibookii.bookiibookii.data.model.MypageResult
import com.bookiibookii.bookiibookii.databinding.FragmentMypBinding
import com.bookiibookii.bookiibookii.databinding.LayoutMypProfileCardBinding
import com.bookiibookii.bookiibookii.myPage.main.MypGroupAdapter
import com.bookiibookii.bookiibookii.myPage.main.MypLateBookAdapter
import com.bookiibookii.bookiibookii.myPage.main.MypReviewAdapter
import com.bookiibookii.bookiibookii.myPage.profile.MypProfileEditFragment
import com.bookiibookii.bookiibookii.myPage.set.MypSetFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class MypageFragment : Fragment() {

    private var _binding: FragmentMypBinding? = null
    private val binding get() = _binding!!

    // 프로필 카드 바인딩 (include 레이아웃)
    private var _profileBinding: LayoutMypProfileCardBinding? = null
    private val profileBinding get() = _profileBinding!!

    private lateinit var loadingDialog: LoadingDialog // ★ 로딩 선언

    private val viewModel: MyPageViewModel by activityViewModels()
    private var isGroupExpanded = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypBinding.inflate(inflater, container, false)
        _profileBinding = LayoutMypProfileCardBinding.bind(binding.layoutProfile.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext()) // ★ 로딩 초기화

        setupRecyclerViews()
        initNavigation()
        initGroupToggle()
        observeViewModel()
        fetchMypageData()
    }

    private fun initNavigation() {
        binding.mypSettingIv.setOnClickListener { navigateToFragment(MypSetFragment()) }
        binding.mypMyReviewIv.setOnClickListener { navigateToFragment(MypMyReviewFragment()) }
        profileBinding.mypEditIv.setOnClickListener { navigateToFragment(MypProfileEditFragment()) }
    }

    private fun initGroupToggle() {
        binding.mypGroupIv.setOnClickListener {
            isGroupExpanded = !isGroupExpanded
            if (isGroupExpanded) {
                binding.mypGroupsRv.visibility = View.VISIBLE
                binding.mypGroupIv.animate().rotation(0f).setDuration(200).start()
            } else {
                binding.mypGroupsRv.visibility = View.GONE
                binding.mypGroupIv.animate().rotation(180f).setDuration(200).start()
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.mypReviewsRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypGroupsRv.layoutManager = LinearLayoutManager(context)
        binding.mypGroupsRv.isNestedScrollingEnabled = false
        binding.rvBooks.layoutManager = LinearLayoutManager(context)
        binding.rvBooks.isNestedScrollingEnabled = false
    }

    private fun translateBadge(englishText: String): String {
        return when (englishText.uppercase()) {
            "PASSIONATE" -> "열정적인"
            "COMMUNICATOR" -> "소통왕"
            "FAST_READER" -> "스피드 리더"
            "KIND" -> "친절해요"
            "PUNCTUAL" -> "시간을 잘 지켜요"
            "GOOD_LISTENER" -> "경청을 잘해요"
            else -> englishText
        }
    }

    private fun updateUI(data: MypageResult) {
        with(profileBinding) {
            mypNameTv.text = data.nickname
            mypTempTv.text = "${data.manner}°C"
            mypAllBookTv.text = data.completeBook.toString()
            mypReadBookTv.text = data.relayGroup.toString()
            mypBookCardTv.text = data.togetherGroup.toString()

            val imageUrl = data.profileImageUrl

            // ★ Glide에 Listener를 달아서 이미지 로딩 완료 시점을 캐치합니다.
            Glide.with(root.context)
                .load(imageUrl)
                .placeholder(R.drawable.img_profile_default)
                .error(R.drawable.img_profile_default)
                .fallback(R.drawable.img_profile_default)
                .transform(CenterCrop(), RoundedCorners(dpToPx(60)))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        // 이미지 로딩 실패 (또는 URL이 null일 때) 로딩창 종료
                        if (loadingDialog.isShowing) loadingDialog.dismiss()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        // 이미지 로딩 성공 시 로딩창 종료
                        if (loadingDialog.isShowing) loadingDialog.dismiss()
                        return false
                    }
                })
                .into(mypProfileIv)

            mypTagsLayout.removeAllViews()
            data.topTags.forEach { tagText ->
                val textView = TextView(root.context).apply {
                    text = "#$tagText"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(ContextCompat.getColor(context, R.color.ui_main_sub))
                    setBackgroundResource(R.drawable.bg_round_8dp_gray300)
                    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.ui_main_sub_pale))

                    val pH = dpToPx(8)
                    val pV = dpToPx(4)
                    setPadding(pH, pV, pH, pV)

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = dpToPx(10) }
                }
                mypTagsLayout.addView(textView)
            }
        }

        val badgeList = data.userBadge?.map {
            MypReview(content = it.userBadge, count = it.count)
        } ?: emptyList()
        binding.mypReviewsRv.adapter = MypReviewAdapter(badgeList)

        binding.mypGroupsRv.adapter = MypGroupAdapter(data.groups ?: emptyList())
        binding.rvBooks.adapter = MypLateBookAdapter(data.books ?: emptyList())
    }

    private fun fetchMypageData() {
        lifecycleScope.launch {
            loadingDialog.show() // API 호출 전 로딩 시작

            // ★ API 통신 자체가 실패했을 때를 대비한 플래그 변수
            var isApiSuccess = false

            try {
                Log.d("MYPAGE_DEBUG", "fetchMypageData 호출 시작")
                val response = RetrofitClient.api().getMypage()
                Log.e("MYPAGE_DEBUG", "전체 응답: ${response.body()}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()!!.result
                    if (result != null) {
                        isApiSuccess = true
                        // 성공했다면 updateUI 내부의 Glide Listener가 로딩창을 꺼줍니다.
                        updateUI(result)
                    }
                } else {
                    Log.e("Mypage", "API Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("Mypage", "Network Error", e)
            } finally {
                // ★ API 호출에 실패해서 updateUI(Glide)까지 도달하지 못한 경우에만 여기서 안전하게 닫아줍니다.
                if (!isApiSuccess) {
                    if (loadingDialog.isShowing) loadingDialog.dismiss()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            profileBinding.mypNameTv.text = data.nickname
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.VISIBLE
        fetchMypageData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _profileBinding = null
    }
}