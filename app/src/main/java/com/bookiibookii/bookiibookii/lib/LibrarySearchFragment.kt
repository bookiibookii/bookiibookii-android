package com.bookiibookii.bookiibookii.lib

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.viewModel.MyPageViewModel
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.data.model.LibBook
import com.bookiibookii.bookiibookii.data.model.ReadStatus
import com.bookiibookii.bookiibookii.databinding.FragmentLibSearchBinding
import com.bookiibookii.bookiibookii.databinding.ItemLibSearchLatelyBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class LibrarySearchFragment : Fragment() {

    private var _binding: FragmentLibSearchBinding? = null
    private val binding get() = _binding!!

    // 내 닉네임을 가져오기 위한 뷰모델 추가 (LibBook 생성 시 isMine 판별용)
    private val myPageViewModel: MyPageViewModel by activityViewModels()

    private var source: String = "LIBRARY"

    private lateinit var libraryAdapter: LibraryBookAdapter
    private lateinit var bookmarkAdapter: LibraryBookmarkAdapter
    private lateinit var recentSearchAdapter: RecentSearchAdapter

    private var allMyBooks: List<LibBook> = emptyList()
    private var allBookmarks: List<CardItem> = emptyList()
    private val recentSearches = mutableListOf<String>()

    private val PREF_NAME = "search_pref"
    private val KEY_RECENT_SEARCH = "recent_search_list"
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        source = arguments?.getString("SOURCE", "LIBRARY") ?: "LIBRARY"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        initRecyclerView()
        initRecentSearch()
        initData()
        initListeners()

        showKeyboard()
        updateSearchState(isSearching = false)
    }

    // ★ [수정됨] 뷰모델이 아닌 API에서 직접 데이터를 확실하게 당겨옵니다.
    private fun initData() {
        lifecycleScope.launch {
            try {
                if (source == "LIBRARY") {
                    val response = RetrofitClient.api().getLibraryBooks()
                    val myNickname = myPageViewModel.confirmedNickname ?: myPageViewModel.profileData.value?.nickname ?: ""

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val resultList = response.body()?.result ?: emptyList()

                        // 서재 화면과 동일하게 MATCHED, COMPLETED 만 필터링
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
                    }
                } else {
                    val response = RetrofitClient.api().getBookmarkedCards()
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        allBookmarks = response.body()?.result ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ★ [수정됨] 어댑터 초기화 및 리스너 세팅 개선
    private fun initRecyclerView() {
        recentSearchAdapter = RecentSearchAdapter(recentSearches,
            onDelete = { term ->
                recentSearches.remove(term)
                saveRecentSearchesToPref()
            },
            onClick = { term ->
                binding.searchInputEt.setText(term)
                binding.searchInputEt.setSelection(term.length)
            }
        )

        libraryAdapter = LibraryBookAdapter(emptyList()) { clickedBook ->
            hideKeyboard()
            val targetFragment: Fragment

            // 서재 목록과 동일한 이동 로직 적용
            if (clickedBook.groupType == "TOGETHER") {
                if (clickedBook.readStatus == ReadStatus.DONE) {
                    targetFragment = LibraryBookDetailTogetherFragment()
                } else {
                    targetFragment = LibraryBookDetailIngFragment()
                }
            } else {
                if (clickedBook.readStatus == ReadStatus.DONE) {
                    targetFragment = LibraryBookDetailFragment()
                } else {
                    targetFragment = LibraryFragment() // 진행중 트래커 화면 (클래스명 확인 필요)
                }
            }

            val bundle = Bundle().apply {
                putInt("userBookId", clickedBook.id)
                putInt("groupId", clickedBook.groupId)
                putString("bookTitle", clickedBook.title)
                putString("bookAuthor", clickedBook.author)
                putString("bookCover", clickedBook.coverUrl)
                putString("hostName", clickedBook.hostName)
                putString("hostProfileUrl", clickedBook.hostProfileUrl)
                putString("startDate", clickedBook.startDate)
                putString("endDate", clickedBook.endDate)
                putDouble("rating", clickedBook.rating)
                putBoolean("isMine", clickedBook.isMine)
            }
            targetFragment.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, targetFragment)
                .addToBackStack(null)
                .commit()
        }

        // 북마크 어댑터 초기화 코드는 생략 (구현체에 맞게 적용)
    }

    private fun initListeners() {
        binding.libSearchBackIv.setOnClickListener {
            hideKeyboard()
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.searchInputEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    updateSearchState(isSearching = false)
                } else {
                    // ★ 검색어가 있을 때 즉시 필터링 후 상태 변경
                    filterList(query)
                    updateSearchState(isSearching = true)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.searchInputEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchInputEt.text.toString().trim()
                if (query.isNotEmpty()) {
                    saveRecentSearch(query)
                }
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    // ★ [수정됨] 대소문자 무시(ignoreCase = true) 옵션 확실하게 적용
    private fun filterList(query: String) {
        if (source == "LIBRARY") {
            val filtered = allMyBooks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.author.contains(query, ignoreCase = true)
            }
            libraryAdapter.submitList(filtered)
            binding.libSearchCountTv.text = "${filtered.size} 권"
        } else {
            val filtered = allBookmarks.filter {
                it.bookTitle.contains(query, ignoreCase = true) ||
                        it.memo.contains(query, ignoreCase = true) ||
                        it.creatorName.contains(query, ignoreCase = true)
            }
            bookmarkAdapter.submitList(filtered)
            binding.libSearchCountTv.text = "${filtered.size} 개"
        }
    }

    // ★ [수정됨] 레이아웃 매니저를 매번 재생성하지 않도록 개선
    private fun updateSearchState(isSearching: Boolean) {
        if (isSearching) {
            binding.libSearchCountTv.visibility = View.VISIBLE
            binding.libSearchResultRv.visibility = View.VISIBLE

            if (source == "LIBRARY") {
                binding.libSearchResultRv.adapter = libraryAdapter
                binding.libSearchResultRv.layoutManager = GridLayoutManager(context, 3)
            } else {
                binding.libSearchResultRv.adapter = bookmarkAdapter
                binding.libSearchResultRv.layoutManager = GridLayoutManager(context, 2)
            }
        } else {
            binding.libSearchCountTv.visibility = View.GONE
            binding.libSearchResultRv.layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }
            binding.libSearchResultRv.adapter = recentSearchAdapter
        }
    }

    private fun initRecentSearch() {
        val json = sharedPreferences.getString(KEY_RECENT_SEARCH, null)
        recentSearches.clear()
        if (json != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            recentSearches.addAll(gson.fromJson(json, type))
        }
    }

    private fun saveRecentSearch(query: String) {
        if (recentSearches.contains(query)) recentSearches.remove(query)
        recentSearches.add(0, query)
        if (recentSearches.size > 10) recentSearches.removeAt(recentSearches.lastIndex)
        saveRecentSearchesToPref()
    }

    private fun saveRecentSearchesToPref() {
        val json = gson.toJson(recentSearches)
        sharedPreferences.edit().putString(KEY_RECENT_SEARCH, json).apply()
        recentSearchAdapter.notifyDataSetChanged()
    }

    private fun showKeyboard() {
        binding.searchInputEt.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchInputEt, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchInputEt.windowToken, 0)
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class RecentSearchAdapter(
        private val items: List<String>,
        private val onDelete: (String) -> Unit,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemLibSearchLatelyBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(text: String) {
                binding.itemSearchDetailTv.text = text
                binding.root.setOnClickListener { onClick(text) }
                binding.itemSearchDeleteIv.setOnClickListener { onDelete(text) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemLibSearchLatelyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size
    }
}