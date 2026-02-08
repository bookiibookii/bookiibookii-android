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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.LibBook
import com.bookiibookii.bookiibookii.data.model.ReadStatus
import com.bookiibookii.bookiibookii.data.viewModel.LibraryViewModel
import com.bookiibookii.bookiibookii.databinding.FragmentLibSearchBinding
import com.bookiibookii.bookiibookii.databinding.ItemLibSearchLatelyBinding

class LibrarySearchFragment : Fragment() {

    private var _binding: FragmentLibSearchBinding? = null
    private val binding get() = _binding!!

    // ViewModel 공유 (LibraryFragment와 같은 데이터 사용)
    private val viewModel: LibraryViewModel by activityViewModels()

    // 어댑터
    private lateinit var libResultAdapter: LibraryBookAdapter // 검색 결과 (책)
    private lateinit var recentSearchAdapter: RecentSearchAdapter // 최근 검색어

    // 데이터
    private var allMyBooks: List<LibBook> = emptyList() // 전체 책 리스트
    private val recentSearches = mutableListOf<String>() // 최근 검색어 (임시 메모리 저장)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initRecyclerView()
        initListeners()

        // 처음엔 키보드 올리기
        showKeyboard()

        // 초기 상태: 최근 검색어 보여주기
        updateSearchState(isSearching = false)
    }

    private fun initData() {
        // 1. ViewModel에 저장된 책 리스트 가져오기
        viewModel.bookList.observe(viewLifecycleOwner) { books ->
            allMyBooks = books
        }

        // 2. 최근 검색어 로드 (실무에선 SharedPreference나 DB에서 불러옴)
        // 테스트용 더미 데이터
        if (recentSearches.isEmpty()) {
            recentSearches.add("해리포터")
            recentSearches.add("코틀린")
        }
    }

    private fun initRecyclerView() {
        // [1] 검색 결과 어댑터 (기존 LibraryBookAdapter 재사용)
        libResultAdapter = LibraryBookAdapter(emptyList()) { clickedBook ->
            // 검색 결과 클릭 시 상세 이동 (LibraryFragment와 동일 로직)
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
                recentSearchAdapter.notifyDataSetChanged()
            },
            onClick = { term ->
                binding.searchInputEt.setText(term)
                binding.searchInputEt.setSelection(term.length) // 커서 끝으로
            }
        )
    }

    private fun initListeners() {
        // 뒤로가기
        binding.libSearchBackIv.setOnClickListener {
            hideKeyboard()
            parentFragmentManager.popBackStack()
        }

        // 텍스트 입력 감지
        binding.searchInputEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    // 검색어 없으면 -> 최근 검색어 모드
                    updateSearchState(isSearching = false)
                } else {
                    // 검색어 있으면 -> 결과 필터링 모드
                    updateSearchState(isSearching = true)
                    filterBooks(query)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 키보드 엔터(검색) 버튼 클릭 시 -> 최근 검색어 저장
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

    // 모드 전환 (최근 검색어 vs 검색 결과)
    private fun updateSearchState(isSearching: Boolean) {
        if (isSearching) {
            // [검색 결과 모드]
            binding.libSearchCountTv.visibility = View.VISIBLE

            // Grid Layout (3열)
            binding.libSearchResultRv.layoutManager = GridLayoutManager(context, 3)
            binding.libSearchResultRv.adapter = libResultAdapter

            // 기존에 적용된 데코레이션이 있다면 초기화 후 다시 적용 추천
            // (여기선 생략)

        } else {
            // [최근 검색어 모드]
            binding.libSearchCountTv.visibility = View.GONE

            // Linear Layout (세로 리스트)
            binding.libSearchResultRv.layoutManager = LinearLayoutManager(context)
            binding.libSearchResultRv.adapter = recentSearchAdapter
        }
    }

    // 실제 검색 로직
    private fun filterBooks(query: String) {
        // 제목이나 저자에 검색어가 포함된 것 필터링
        val filteredList = allMyBooks.filter { book ->
            book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true)
        }

        libResultAdapter.submitList(filteredList)
        binding.libSearchCountTv.text = "${filteredList.size} 권"
    }

    private fun saveRecentSearch(query: String) {
        // 중복 제거 후 맨 앞에 추가
        if (recentSearches.contains(query)) {
            recentSearches.remove(query)
        }
        recentSearches.add(0, query)

        // 최대 개수 제한 (ex: 10개)
        if (recentSearches.size > 10) {
            recentSearches.removeAt(recentSearches.lastIndex)
        }
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

    // =========================================
    // [내부 클래스] 최근 검색어 어댑터
    // =========================================
    inner class RecentSearchAdapter(
        private val items: List<String>,
        private val onDelete: (String) -> Unit,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemLibSearchLatelyBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(text: String) {
                binding.itemSearchDetailTv.text = text

                // 클릭 시 검색어 입력
                binding.root.setOnClickListener { onClick(text) }

                // 삭제 버튼 (X)
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