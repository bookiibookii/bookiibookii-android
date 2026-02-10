package com.bookiibookii.bookiibookii.lib

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.data.model.LibBook
import com.bookiibookii.bookiibookii.data.model.ReadStatus
import com.bookiibookii.bookiibookii.data.viewModel.LibraryViewModel
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

    private val viewModel: LibraryViewModel by activityViewModels()

    // 모드 ("LIBRARY" 또는 "BOOKMARK")
    private var source: String = "LIBRARY"

    // 어댑터 (상황에 따라 초기화)
    private lateinit var libraryAdapter: LibraryBookAdapter       // 내 서재용
    private lateinit var bookmarkAdapter: LibraryBookmarkAdapter  // 북마크용
    private lateinit var recentSearchAdapter: RecentSearchAdapter // 최근 검색어용

    // 데이터 저장소
    private var allMyBooks: List<LibBook> = emptyList()
    private var allBookmarks: List<CardItem> = emptyList() // ★ CardItem 사용
    private val recentSearches = mutableListOf<String>()

    // SharedPreferences
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

        initData()         // 데이터 로드 (API or ViewModel)
        initRecentSearch() // 최근 검색어 로드
        initRecyclerView() // 어댑터 설정
        initListeners()    // 이벤트 리스너

        // 키보드 올리기
        showKeyboard()
        updateSearchState(isSearching = false)
    }

    // [1] 데이터 로드
    private fun initData() {
        if (source == "LIBRARY") {
            // 내 서재: ViewModel에 이미 로드된 데이터 사용
            viewModel.bookList.observe(viewLifecycleOwner) { books ->
                allMyBooks = books
            }
        } else {
            // 북마크: API 직접 호출하여 전체 목록 가져오기
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api().getBookmarkedCards()
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        allBookmarks = response.body()?.result ?: emptyList()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    // [2] RecyclerView 설정 (어댑터 분기)
    private fun initRecyclerView() {
        // A. 최근 검색어 어댑터
        recentSearchAdapter = RecentSearchAdapter(recentSearches,
            onDelete = { term ->
                recentSearches.remove(term)
                saveRecentSearchesToPref()
                recentSearchAdapter.notifyDataSetChanged()
            },
            onClick = { term ->
                binding.searchInputEt.setText(term)
                binding.searchInputEt.setSelection(term.length)
            }
        )

        // B. 검색 결과 어댑터 (모드에 따라 다름)
        if (source == "LIBRARY") {
            // 1. 내 서재 모드
            libraryAdapter = LibraryBookAdapter(emptyList()) { clickedBook ->
                // 상세 이동 로직
                val targetFragment = if (clickedBook.readStatus == ReadStatus.READING) {
                    LibraryBookDetailIngFragment()
                } else {
                    LibraryBookDetailFragment()
                }
                val bundle = Bundle().apply {
                    putInt("userBookId", clickedBook.id)
                    putInt("groupId", clickedBook.groupId)
                    putString("bookTitle", clickedBook.title)
                    putString("bookAuthor", clickedBook.author)
                    putString("bookCover", clickedBook.coverUrl)
                }
                targetFragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, targetFragment)
                    .addToBackStack(null)
                    .commit()
            }
            binding.libSearchResultRv.adapter = libraryAdapter
            binding.libSearchResultRv.layoutManager = GridLayoutManager(context, 3)

        } else {
            // 2. 북마크 모드
            bookmarkAdapter = LibraryBookmarkAdapter { clickedCard ->
                // 카드 상세 이동 로직
                val detailFragment = LibraryCardDetailFragment().apply {
                    arguments = Bundle().apply {
                        putLong("cardId", clickedCard.cardId.toLong())
                        putBoolean("isMine", false) // 북마크 목록에선 수정 불가
                        putString("writerName", clickedCard.creatorName)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
            binding.libSearchResultRv.adapter = bookmarkAdapter
            binding.libSearchResultRv.layoutManager = GridLayoutManager(context, 2)
            // 간격 데코레이션 추가
            binding.libSearchResultRv.addItemDecoration(LibDetailGridDecoration(2, dpToPx(10), dpToPx(12), false))
        }
    }

    private fun initListeners() {
        // 뒤로가기
        binding.libSearchBackIv.setOnClickListener {
            hideKeyboard()
            parentFragmentManager.popBackStack()
        }

        // 검색어 입력 감지
        binding.searchInputEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    updateSearchState(isSearching = false)
                } else {
                    updateSearchState(isSearching = true)
                    filterList(query)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 검색 버튼(키보드) 클릭 시 최근 검색어 저장
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

    // [3] 필터링 로직 (핵심)
    private fun filterList(query: String) {
        if (source == "LIBRARY") {
            // 내 서재 필터링 (제목, 저자)
            val filtered = allMyBooks.filter {
                it.title.contains(query, true) || it.author.contains(query, true)
            }
            libraryAdapter.submitList(filtered)
            binding.libSearchCountTv.text = "${filtered.size} 권"

        } else {
            // 북마크 필터링 (책제목, 메모, 작성자)
            val filtered = allBookmarks.filter {
                it.bookTitle.contains(query, true) ||
                        it.memo.contains(query, true) ||
                        it.creatorName.contains(query, true)
            }
            bookmarkAdapter.submitList(filtered)
            binding.libSearchCountTv.text = "${filtered.size} 개"
        }
    }

    private fun updateSearchState(isSearching: Boolean) {
        if (isSearching) {
            binding.libSearchCountTv.visibility = View.VISIBLE
            binding.libSearchResultRv.visibility = View.VISIBLE
            // 어댑터 재설정은 initRecyclerView에서 했으므로 LayoutManager 등은 그대로 둠
            if (source == "LIBRARY") {
                binding.libSearchResultRv.adapter = libraryAdapter
                binding.libSearchResultRv.layoutManager = GridLayoutManager(context, 3)
            } else {
                binding.libSearchResultRv.adapter = bookmarkAdapter
                binding.libSearchResultRv.layoutManager = GridLayoutManager(context, 2)
            }
        } else {
            // 최근 검색어 모드
            binding.libSearchCountTv.visibility = View.GONE
            val flexboxLayoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }
            binding.libSearchResultRv.layoutManager = flexboxLayoutManager
            binding.libSearchResultRv.adapter = recentSearchAdapter
        }
    }

    // --- 최근 검색어 관련 로직 (기존 동일) ---
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
        recentSearchAdapter.notifyDataSetChanged()
    }

    private fun saveRecentSearchesToPref() {
        val json = gson.toJson(recentSearches)
        sharedPreferences.edit().putString(KEY_RECENT_SEARCH, json).apply()
    }

    // --- 유틸 ---
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

    // --- 최근 검색어 어댑터 (Inner Class) ---
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