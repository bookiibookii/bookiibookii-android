package com.bookiibookii.bookiibookii.lib

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.LibBook
import com.bookiibookii.bookiibookii.data.model.ReadStatus
import com.bookiibookii.bookiibookii.data.viewModel.LibraryViewModel
import com.bookiibookii.bookiibookii.data.viewModel.SortType
import com.bookiibookii.bookiibookii.databinding.FragmentLibBinding
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibBinding? = null
    private val binding get() = _binding!!

    private lateinit var libraryAdapter: LibraryBookAdapter

    private var allMyBooks : List<LibBook> = emptyList()

    private var currentTabStatus : ReadStatus = ReadStatus.READING

    private val viewModel : LibraryViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initClickListeners()
        fetchBooks()
        //showBooksByStatus(ReadStatus.READING)
        viewModel.sortType.observe(viewLifecycleOwner) { sortType ->
            // 정렬 타입이 바뀌면 리스트를 재정렬하고 화면 갱신
            applySort(sortType)
        }
    }

    private fun applySort(sortType: SortType) {
        if (allMyBooks.isEmpty()) return

        // 원본 리스트(allMyBooks)를 기준에 맞춰 정렬하여 다시 저장
        allMyBooks = when (sortType) {
            SortType.TITLE -> allMyBooks.sortedBy { it.title }
            SortType.RATING_HIGH -> allMyBooks.sortedByDescending { it.rating }
            SortType.RATING_LOW -> allMyBooks.sortedBy { it.rating }

            // API 데이터에 날짜 필드가 문자열(2026-02-05)이라면 비교가 어려울 수 있으므로
            // ID(userBookId)가 순차적으로 생성된다고 가정하고 ID로 정렬합니다.
            SortType.RECENT -> allMyBooks.sortedByDescending { it.id } // 최신순 (ID 큰 게 위로)
            SortType.OLD -> allMyBooks.sortedBy { it.id }             // 오래된순 (ID 작은 게 위로)
        }

        // 정렬된 리스트를 바탕으로 현재 탭(진행중/완독) 상태에 맞춰 화면 갱신
        showBooksByStatus(currentTabStatus)
    }

    private fun fetchBooks(){
        lifecycleScope.launch {
            try{
                val response = RetrofitClient.api().getLibraryBooks()
                if(response.isSuccessful && response.body()?.isSuccess == true){
                    val resultList = response.body()?.result ?: emptyList()

                    allMyBooks = resultList.map{ apiData->
                        val status = if (apiData.rating > 0.0) ReadStatus.DONE else ReadStatus.READING
                        val progressText = if (status == ReadStatus.READING) {
                            "${apiData.duration}일째 독서 중"
                        } else {
                            "완독"
                        }
                        LibBook(
                            id = apiData.userBookId,
                            title = apiData.title,
                            author = apiData.author,
                            coverUrl = apiData.image,               // API의 image 필드
                            hostProfileUrl = apiData.hostProfileImageUrl, // API의 호스트 프로필
                            readStatus = status,
                            progress = progressText,
                            rating = apiData.rating
                        )
                    }
                    showBooksByStatus(currentTabStatus)
                    viewModel.setBookList(allMyBooks)
                }else{
                    Log.e("LibraryFragment", "API Error: ${response.code()} ${response.message()}")
                }
            }catch (e: Exception){
                Log.e("LibraryFragment", "Network Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun initRecyclerView(){
        libraryAdapter = LibraryBookAdapter(emptyList()){clickedBook ->
            val targetFragment = if(clickedBook.readStatus == ReadStatus.READING){
                LibraryBookDetailIngFragment()
            }else{
                LibraryBookDetailFragment()
            }

            val bundle = Bundle().apply {
                putInt("book_id", clickedBook.id)
                putString("book_title", clickedBook.title)
                putString("book_author", clickedBook.author)
                putString("book_cover", clickedBook.coverUrl)
            }
            targetFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, targetFragment)
                .addToBackStack(null)
                .commit()
        }
        binding.libBookListRv.adapter = libraryAdapter
        setCoverModeLayout()
    }

    private fun initClickListeners() {
        // [진행 중] 버튼 클릭
        binding.libIngBtn.setOnClickListener {
            currentTabStatus = ReadStatus.READING
            showBooksByStatus(ReadStatus.READING)
        }

        // [종료] 버튼 클릭
        binding.libEdBtn.setOnClickListener {
            currentTabStatus = ReadStatus.DONE
            showBooksByStatus(ReadStatus.DONE)
        }

        // [정렬] 버튼 클릭
        binding.libSortIv.setOnClickListener {
            val bottomSheet = LibrarySortBottomSheet()
            bottomSheet.show(parentFragmentManager, "LibrarySortBottomSheet")
        }

        // [검색] 버튼 클릭
        binding.libSearchIv.setOnClickListener {
            val searchFragment = LibrarySearchFragment().apply {
                arguments = Bundle().apply { putString("SOURCE", "LIBRARY") }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, searchFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.libBookIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LibraryBookmarkFragment())
                .addToBackStack(null)
                .commit()
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
    }

    private fun setCoverModeLayout() {
        binding.libBookListRv.layoutManager = GridLayoutManager(context, 3)

        binding.libBookListRv.setPadding(0, 0, 0, dpToPx(80))
        binding.libBookListRv.clipToPadding = false

        removeAllItemDecorations(binding.libBookListRv)
        val spacingHorizontal = dpToPx(12)
        val spacingVertical = dpToPx(36)

        binding.libBookListRv.addItemDecoration(
            LibDetailGridDecoration(3, spacingHorizontal, spacingVertical, false)
        )
    }

    private fun setSpineModeLayout() {
        val spanCount = 8
        // 🔹 일반 GridLayoutManager로 변경하여 정렬 기준선을 일관되게 유지
        val layoutManager = GridLayoutManager(context, spanCount)
        binding.libBookListRv.layoutManager = layoutManager

        // 🔹 RecyclerView 자체는 상단에 밀착 (여백 제거)
        binding.libBookListRv.setPadding(dpToPx(8), 0, dpToPx(8), dpToPx(80))
        binding.libBookListRv.clipToPadding = false

        removeAllItemDecorations(binding.libBookListRv)

        binding.libBookListRv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.left = dpToPx(4)
                outRect.right = dpToPx(4)
                outRect.top = dpToPx(16) // 🔹 줄 사이의 간격
                outRect.bottom = 0
            }
        })
    }

    private fun showBooksByStatus(status: ReadStatus) {
        val filteredList = allMyBooks.filter { it.readStatus == status }
        libraryAdapter.submitList(filteredList)
        binding.libTotalTv.text = "${filteredList.size}권"
        updateButtonStyles(status)
    }

    private fun updateButtonStyles(currentStatus: ReadStatus) {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.white)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey_900)

        if (currentStatus == ReadStatus.READING) {
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

    private fun removeAllItemDecorations(recyclerView: RecyclerView) {
        while (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}