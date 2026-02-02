package com.bookiibookii.bookiibookii.lib

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.Data.LibBook
import com.bookiibookii.bookiibookii.bookData.Data.LibBookmarkItem
import com.bookiibookii.bookiibookii.bookData.Data.ReadStatus
import com.bookiibookii.bookiibookii.databinding.FragmentLibSearchBinding
import com.bookiibookii.bookiibookii.databinding.ItemSearchLatelyBinding

class LibrarySearchFragment : Fragment() {

    private var _binding: FragmentLibSearchBinding? = null
    private val binding get() = _binding!!

    // 진입 경로 확인 변수 ("LIBRARY" or "BOOKMARK")
    private var source: String = "LIBRARY"

    // 어댑터들
    private lateinit var libAdapter: LibraryBookAdapter      // 서재용 (Grid)
    private lateinit var bookmarkAdapter: LibraryBookmarkAdapter // 북마크용 (List)
    private lateinit var recentSearchAdapter: RecentSearchAdapter // 최근 검색어용

    // 데이터 리스트
    private var allLibBooks: List<LibBook> = listOf()       // 서재 전체 데이터
    private var allBookmarks: List<LibBookmarkItem> = listOf() // 북마크 전체 데이터
    private val recentSearches = mutableListOf<String>()    // 최근 검색어 (메모리 저장)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // arguments에서 진입 경로 확인
        source = arguments?.getString("SOURCE") ?: "LIBRARY"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData()
        setupAdapters()
        initListeners()

        // 초기 상태: 입력창 비어있으면 최근 검색어 표시
        showRecentSearches()
        showKeyboard()
    }

    private fun loadData() {
        // 더미 데이터 로드 (실제로는 DB/API)
        if (source == "LIBRARY") {
            allLibBooks = listOf(
                LibBook(
                    title = "괴테는 모든 것을 말했다",
                    author = "noshel",
                    readStatus = ReadStatus.READING,
                    progress = "50% 읽음"
                ),
                LibBook(
                    title = "자바의 정석",
                    author = "남궁성",
                    readStatus = ReadStatus.READING,
                    progress = "p.120"
                ),
                LibBook(
                    title = "해리포터와 마법사의 돌",
                    author = "J.K.롤링",
                    readStatus = ReadStatus.DONE,
                    rating = 5
                ),
                LibBook(
                    title = "클린 코드",
                    author = "로버트 C",
                    readStatus = ReadStatus.READING,
                    progress = "독서 시작 전"
                ),
                LibBook(title = "반지의 제왕", author = "톨킨", readStatus = ReadStatus.DONE, rating = 4),
                LibBook(
                    title = "코틀린 인 액션",
                    author = "드미트리",
                    readStatus = ReadStatus.READING,
                    progress = "80% 읽음"
                )
            )

        } else {
            allBookmarks = listOf(
                LibBookmarkItem(1, "괴테는 모든 것을 말했다", 72, "내용...", "2026.01.20", "me", null, null),
                LibBookmarkItem(2, "어린왕자", 15, "중요한건...", "2026.02.01", "me", null, null)
            )
        }
    }

    private fun setupAdapters() {
        if (source == "LIBRARY") {
            libAdapter = LibraryBookAdapter(emptyList()) { /* 클릭 이동 */ }
            binding.libSearchResultRv.layoutManager = GridLayoutManager(context, 3)
            binding.libSearchResultRv.adapter = libAdapter
            // Grid Item Decoration 추가 필요시 여기서 추가
        } else {
            bookmarkAdapter = LibraryBookmarkAdapter(emptyList()) { /* 클릭 이동 */ }
            binding.libSearchResultRv.layoutManager = LinearLayoutManager(context)
            binding.libSearchResultRv.adapter = bookmarkAdapter
        }

        recentSearchAdapter = RecentSearchAdapter(recentSearches,
            onDelete = { term ->
                recentSearches.remove(term)
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
                    showRecentSearches()
                } else {
                    filterData(query)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 검색 완료(엔터) 시 최근 검색어 저장
        binding.searchInputEt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchInputEt.text.toString().trim()
                if (query.isNotEmpty() && !recentSearches.contains(query)) {
                    recentSearches.add(0, query) // 맨 앞에 추가
                }
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    // 최근 검색어 모드로 전환
    private fun showRecentSearches() {
        binding.libSearchCountTv.visibility = View.GONE
        binding.libSearchResultRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) // 가로 스크롤 혹은 세로

        binding.libSearchResultRv.layoutManager = com.google.android.flexbox.FlexboxLayoutManager(context).apply {
            flexDirection = com.google.android.flexbox.FlexDirection.ROW
            flexWrap = com.google.android.flexbox.FlexWrap.WRAP
        }
        binding.libSearchResultRv.adapter = recentSearchAdapter
    }

    // 검색 결과 모드로 전환 및 필터링
    private fun filterData(query: String) {
        binding.libSearchCountTv.visibility = View.VISIBLE

        if (source == "LIBRARY") {
            // 서재 모드 복구
            binding.libSearchResultRv.layoutManager = GridLayoutManager(context, 3)
            binding.libSearchResultRv.adapter = libAdapter

            val filtered = allLibBooks.filter { it.title.contains(query, true) || it.author.contains(query, true) }
            libAdapter.submitList(filtered)
            binding.libSearchCountTv.text = "${filtered.size} 권"
        } else {
            // 북마크 모드 복구
            binding.libSearchResultRv.layoutManager = LinearLayoutManager(context)
            binding.libSearchResultRv.adapter = bookmarkAdapter

            val filtered = allBookmarks.filter { it.title.contains(query, true) || it.content.contains(query, true) }
            bookmarkAdapter.submitList(filtered)
            binding.libSearchCountTv.text = "${filtered.size} 개"
        }
    }

    private fun hideKeyboard() { /* 기존 동일 */ }
    private fun showKeyboard() { /* 기존 동일 */ }

    // 최근 검색어 어댑터 (내부 클래스)
    inner class RecentSearchAdapter(
        private val items: List<String>,
        private val onDelete: (String) -> Unit,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemSearchLatelyBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(text: String) {
                binding.itemSearchDetailTv.text = text
                binding.root.setOnClickListener { onClick(text) }
                binding.itemSearchDeleteIv.setOnClickListener { onDelete(text) }
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSearchLatelyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
        override fun getItemCount() = items.size
    }
}