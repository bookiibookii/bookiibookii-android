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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
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

class LibrarySearchFragment : Fragment() {

    private var _binding: FragmentLibSearchBinding? = null
    private val binding get() = _binding!!

    // ViewModel 공유 (LibraryFragment와 같은 데이터 사용)
    private val viewModel: LibraryViewModel by activityViewModels()

    // 어댑터
    private lateinit var libResultAdapter: LibraryBookAdapter
    private lateinit var recentSearchAdapter: RecentSearchAdapter

    // 데이터
    private var allMyBooks: List<LibBook> = emptyList()
    private val recentSearches = mutableListOf<String>()

    // [추가] SharedPreferences 관련 상수
    private val PREF_NAME = "search_pref"
    private val KEY_RECENT_SEARCH = "recent_search_list"
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson() // 리스트 저장용 (build.gradle에 gson 추가 필요)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SharedPreferences 초기화
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        initData()
        initRecyclerView()
        initListeners()

        showKeyboard()
        updateSearchState(isSearching = false)
    }

    private fun initData() {
        // 1. 책 리스트 가져오기 (ViewModel 공유)
        viewModel.bookList.observe(viewLifecycleOwner) { books ->
            allMyBooks = books
        }

        // 2. [수정] 최근 검색어 불러오기 (SharedPreferences)
        loadRecentSearches()
    }

    // ★★★ [추가] 저장된 검색어 불러오기
    private fun loadRecentSearches() {
        val json = sharedPreferences.getString(KEY_RECENT_SEARCH, null)
        recentSearches.clear()
        if (json != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            val savedList: List<String> = gson.fromJson(json, type)
            recentSearches.addAll(savedList)
        }
    }

    // ★★★ [추가] 검색어 저장하기 (리스트 -> JSON 문자열 -> Pref 저장)
    private fun saveRecentSearchesToPref() {
        val json = gson.toJson(recentSearches)
        sharedPreferences.edit().putString(KEY_RECENT_SEARCH, json).apply()
    }

    private fun initRecyclerView() {
        // [1] 검색 결과 어댑터
        libResultAdapter = LibraryBookAdapter(emptyList()) { clickedBook ->
            val targetFragment = if (clickedBook.readStatus == ReadStatus.READING) {
                LibraryBookDetailIngFragment()
            } else {
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

        // [2] 최근 검색어 어댑터
        recentSearchAdapter = RecentSearchAdapter(recentSearches,
            onDelete = { term ->
                recentSearches.remove(term)
                saveRecentSearchesToPref() // [추가] 삭제 시에도 저장
                recentSearchAdapter.notifyDataSetChanged()
            },
            onClick = { term ->
                binding.searchInputEt.setText(term)
                binding.searchInputEt.setSelection(term.length)
            }
        )
    }

    private fun initListeners() {
        binding.libSearchBackIv.setOnClickListener {
            hideKeyboard()
            parentFragmentManager.popBackStack()
        }

        binding.searchInputEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    updateSearchState(isSearching = false)
                } else {
                    updateSearchState(isSearching = true)
                    filterBooks(query)
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

    private fun updateSearchState(isSearching: Boolean) {
        if (isSearching) {
            binding.libSearchCountTv.visibility = View.VISIBLE
            binding.libSearchResultRv.layoutManager = GridLayoutManager(context, 3)
            binding.libSearchResultRv.adapter = libResultAdapter
        } else {
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

    private fun filterBooks(query: String) {
        // [답변 2번 관련] API 호출 없이 로컬 리스트에서 필터링
        val filteredList = allMyBooks.filter { book ->
            book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true)
        }
        libResultAdapter.submitList(filteredList)
        binding.libSearchCountTv.text = "${filteredList.size} 권"
    }

    private fun saveRecentSearch(query: String) {
        if (recentSearches.contains(query)) {
            recentSearches.remove(query)
        }
        recentSearches.add(0, query)

        if (recentSearches.size > 10) {
            recentSearches.removeAt(recentSearches.lastIndex)
        }

        saveRecentSearchesToPref() // [추가] 변경사항 영구 저장
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 내부 어댑터 클래스 (기존 동일)
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