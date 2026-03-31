package com.bookiibookii.bookiibookii.lib

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.LibBook
import com.bookiibookii.bookiibookii.data.model.ReadStatus
import com.bookiibookii.bookiibookii.data.viewModel.LibraryViewModel
import com.bookiibookii.bookiibookii.data.viewModel.SortType
import com.bookiibookii.bookiibookii.databinding.FragmentLibBinding
import com.bookiibookii.bookiibookii.group.generation.GroupGenerationActivity
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

    private lateinit var libraryAdapter: LibraryBookAdapter
    private var allMyBooks: List<LibBook> = emptyList()
    private var currentTabStatus: ReadStatus = ReadStatus.READING

    private val viewModel: LibraryViewModel by activityViewModels()
    private val myPageViewModel: MyPageViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        binding.loadingProgressBar.visibility = View.GONE

        initRecyclerView()
        initClickListeners()
        setupProfileAndFetchBooks()

        viewModel.sortType.observe(viewLifecycleOwner) { sortType -> applySort(sortType) }

        requireActivity().supportFragmentManager.setFragmentResultListener("REFRESH_LIBRARY", viewLifecycleOwner) { _, _ ->
            fetchBooks()
        }
    }

    private fun setupProfileAndFetchBooks() {
        myPageViewModel.profileData.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                if (allMyBooks.isEmpty()) {
                    fetchBooks()
                } else {
                    allMyBooks.forEach { it.isMine = (it.hostName == profile.nickname) }
                    libraryAdapter.notifyDataSetChanged()
                }
            }
        }

        if (myPageViewModel.profileData.value == null) {
            myPageViewModel.fetchMypageData()
        } else {
            fetchBooks()
        }
    }

    private fun fetchBooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            loadingDialog.show()
            binding.libBookListRv.visibility = View.INVISIBLE
            try {
                val response = RetrofitClient.api().getLibraryBooks()
                Log.d("LibraryAPI", "${response.body()}")

                val myNickname = myPageViewModel.profileData.value?.nickname ?: ""
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val resultList = response.body()?.result ?: emptyList()

                    val filteredList = resultList.filter {
                        it.groupStatus == "MATCHED" || it.groupStatus == "COMPLETED"
                    }

                    allMyBooks = filteredList.map { apiData ->
                        val state = apiData.groupStatus
                        val status = if (state == "COMPLETED") ReadStatus.DONE else ReadStatus.READING
                        val reviewWritten = apiData.rating > 0.0

                        LibBook(
                            id = apiData.userBookId,
                            groupId = apiData.groupId,
                            title = apiData.title,
                            author = apiData.author,
                            coverUrl = apiData.image,
                            hostName = apiData.hostNickName,
                            hostProfileUrl = apiData.hostProfileImageUrl,
                            startDate = apiData.startDate,
                            endDate = apiData.endDate,
                            isReviewed = reviewWritten,
                            readStatus = status,
                            progress = if (status == ReadStatus.READING) "${apiData.duration}일째" else "완독",
                            rating = apiData.rating,
                            groupType = apiData.groupType,
                            groupState = state ?: "",
                            isMine = (apiData.hostNickName == myNickname)
                        )
                    }
                    showBooksByStatus(currentTabStatus)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (loadingDialog.isShowing) loadingDialog.dismiss()
                binding.libBookListRv.visibility = View.VISIBLE
            }
        }
    }

    private fun applySort(sortType: SortType) {
        if (allMyBooks.isEmpty()) return
        allMyBooks = when (sortType) {
            SortType.TITLE -> allMyBooks.sortedBy { it.title }
            SortType.RATING_HIGH -> allMyBooks.sortedByDescending { it.rating }
            SortType.RATING_LOW -> allMyBooks.sortedBy { it.rating }
            SortType.RECENT -> allMyBooks.sortedByDescending { it.id }
            SortType.OLD -> allMyBooks.sortedBy { it.id }
        }
        showBooksByStatus(currentTabStatus)
    }

    private fun showBooksByStatus(status: ReadStatus) {
        val filteredList = allMyBooks.filter { it.readStatus == status }
        libraryAdapter.submitList(filteredList)
        binding.libTotalTv.text = "${filteredList.size}권"
        updateButtonStyles(status)

        if (filteredList.isEmpty()) {
            binding.layoutEmptyGroup.root.visibility = View.VISIBLE
            binding.libBookListRv.visibility = View.GONE
            binding.libGridIv.visibility = View.GONE
            binding.libTotalTv.visibility = View.GONE
            binding.libSortIv.visibility = View.GONE

            if (binding.layoutEmptyGroup.root is MaterialCardView) {
                (binding.layoutEmptyGroup.root as MaterialCardView).cardElevation = 0f
            } else {
                binding.layoutEmptyGroup.root.elevation = 0f
            }

            if (status == ReadStatus.READING) {
                binding.layoutEmptyGroup.tvEmptyTitle.text = "아직 진행 중인 독서가 없어요 \uD83D\uDE2D"
                binding.layoutEmptyGroup.tvEmptyDesc.text = "읽고 싶은 책을 골라 그룹을 만들어볼까요?"
                binding.layoutEmptyGroup.btnCreateGroup.visibility = View.VISIBLE
                binding.layoutEmptyGroup.root.setPadding(0, dpToPx(32), 0, dpToPx(24))
            } else {
                binding.layoutEmptyGroup.tvEmptyTitle.text = "완료한 독서가 없어요 \uD83D\uDE2D"
                binding.layoutEmptyGroup.tvEmptyDesc.text = "진행 중인 독서를 완료하고 기록을 남겨보세요!"
                binding.layoutEmptyGroup.btnCreateGroup.visibility = View.GONE
                binding.layoutEmptyGroup.root.setPadding(0, dpToPx(56), 0, dpToPx(56))
            }
        } else {
            binding.layoutEmptyGroup.root.visibility = View.GONE
            binding.libBookListRv.visibility = View.VISIBLE
            binding.libGridIv.visibility = View.VISIBLE
            binding.libTotalTv.visibility = View.VISIBLE
            binding.libSortIv.visibility = View.VISIBLE
        }
    }

    private fun initRecyclerView() {
        libraryAdapter = LibraryBookAdapter(emptyList()) { clickedBook ->
            Log.d("LibraryClick", "클릭: ${clickedBook.title}, Type: ${clickedBook.groupType}, Status: ${clickedBook.readStatus}, Reviewed: ${clickedBook.isReviewed}")

            // ★ 명확한 4가지 상황별 분기 처리
            val targetFragment: Fragment = if (clickedBook.groupType == "TOGETHER") {
                if (clickedBook.readStatus == ReadStatus.DONE || clickedBook.isReviewed) {
                    // 1. 함께읽기 + 종료 (완독/리뷰작성완료)
                    LibraryBookDetailTogetherFragment()
                } else {
                    // 2. 함께읽기 + 진행중
                    LibraryBookDetailIngFragment()
                }
            } else {
                if (clickedBook.readStatus == ReadStatus.DONE || clickedBook.isReviewed) {
                    // 3. 이어읽기 + 종료 (완독/리뷰작성완료)
                    LibraryBookDetailFragment()
                } else {
                    // 4. 이어읽기 + 진행중 (기존 트래커 대신 여기로 통합)
                    LibraryBookDetailFragment()
                }
            }

            navigateToFragment(targetFragment, clickedBook)

        }

        binding.libBookListRv.layoutManager = GridLayoutManager(context, 3)
        binding.libBookListRv.adapter = libraryAdapter
        setCoverModeLayout()
    }
    /*
    private fun checkTradeTypeAndNavigate(book: LibBook) {
        // ... 기존 트래커 액티비티 이동 로직 주석 처리됨 ...
    }
    */

    private fun navigateToFragment(targetFragment: Fragment, book: LibBook) {
        val bundle = Bundle().apply {
            putInt("userBookId", book.id)
            putInt("groupId", book.groupId)
            putString("bookTitle", book.title)
            putString("bookAuthor", book.author)
            putString("bookCover", book.coverUrl)
            putString("hostName", book.hostName)
            putString("hostProfileUrl", book.hostProfileUrl)
            putString("startDate", book.startDate)
            putString("endDate", book.endDate)
            putDouble("rating", book.rating)
            putBoolean("isMine", book.isMine)
        }
        targetFragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, targetFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun initClickListeners() {
        binding.libIngBtn.setOnClickListener {
            currentTabStatus = ReadStatus.READING
            showBooksByStatus(ReadStatus.READING)
        }
        binding.libEdBtn.setOnClickListener {
            currentTabStatus = ReadStatus.DONE
            showBooksByStatus(ReadStatus.DONE)
        }
        binding.libSortIv.setOnClickListener { LibrarySortBottomSheet().show(requireActivity().supportFragmentManager, "LibrarySortBottomSheet") }
        binding.libSearchIv.setOnClickListener {
            val searchFragment = LibrarySearchFragment().apply { arguments = Bundle().apply { putString("SOURCE", "LIBRARY") } }
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, searchFragment).addToBackStack(null).commit()
        }
        binding.libBookIv.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, LibraryBookmarkFragment()).addToBackStack(null).commit()
        }
        binding.libGridIv.setOnClickListener {
            libraryAdapter.toggleMode()
            if (libraryAdapter.isSpineMode) {
                setSpineModeLayout()
                binding.libGridIv.setImageResource(R.drawable.ic_grid_list)
            } else {
                setCoverModeLayout()
                binding.libGridIv.setImageResource(R.drawable.ic_grid)
            }
        }

        binding.layoutEmptyGroup.btnCreateGroup.setOnClickListener {
            val intent = Intent(requireContext(), GroupGenerationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setCoverModeLayout() {
        binding.libBookListRv.layoutManager = GridLayoutManager(context, 3)
        binding.libBookListRv.setPadding(0, 0, 0, dpToPx(80))
        binding.libBookListRv.clipToPadding = false
        removeAllItemDecorations()
        binding.libBookListRv.addItemDecoration(LibDetailGridDecoration(3, dpToPx(12), dpToPx(36), false))
    }

    private fun setSpineModeLayout() {
        val flexboxLayoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_END
        }
        binding.libBookListRv.layoutManager = flexboxLayoutManager
        binding.libBookListRv.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(80))
        binding.libBookListRv.clipToPadding = false
        removeAllItemDecorations()
        libraryAdapter.notifyDataSetChanged()
    }

    private fun removeAllItemDecorations() {
        while (binding.libBookListRv.itemDecorationCount > 0) binding.libBookListRv.removeItemDecorationAt(0)
    }

    private fun updateButtonStyles(status: ReadStatus) {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.white)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_900)

        if (status == ReadStatus.READING) {
            binding.libIngBtn.setBackgroundResource(R.drawable.bg_toggle_black10)
            binding.libIngBtn.setTextColor(activeColor)
            binding.libEdBtn.setBackgroundResource(R.drawable.bg_toggle_white10)
            binding.libEdBtn.setTextColor(inactiveColor)
        } else {
            binding.libIngBtn.setBackgroundResource(R.drawable.bg_toggle_white10)
            binding.libIngBtn.setTextColor(inactiveColor)
            binding.libEdBtn.setBackgroundResource(R.drawable.bg_toggle_black10)
            binding.libEdBtn.setTextColor(activeColor)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}