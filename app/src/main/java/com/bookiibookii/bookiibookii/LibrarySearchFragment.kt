package com.bookiibookii.bookiibookii

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
import com.bookiibookii.bookiibookii.bookData.Data.LibBook
import com.bookiibookii.bookiibookii.bookData.Data.ReadStatus
import com.bookiibookii.bookiibookii.databinding.FragmentLibSearchBinding

class LibrarySearchFragment : Fragment() {

    private var _binding: FragmentLibSearchBinding? = null
    private val binding get() = _binding!!

    // 기존에 만든 LibraryAdapter 재사용
    private lateinit var libraryAdapter: LibraryBookAdapter

    // 검색 대상이 될 전체 데이터 리스트 (실제로는 DB나 ViewModel에서 가져와야 합니다)
    private var allBooks: List<LibBook> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 데이터 로드 (더미 데이터 혹은 DB 로드)
        loadBookData()

        // 2. 리사이클러뷰 설정
        initRecyclerView()

        // 3. 리스너 설정 (검색, 뒤로가기)
        initListeners()

        // 4. 화면 진입 시 키보드 바로 올리기 (선택 사항 - 사용자 경험 향상)
        showKeyboard()
    }

    private fun loadBookData() {
        // 테스트용 더미 데이터입니다.
        // 힌트에 '한 줄 평' 검색도 있다고 하셨으므로, 추후 UserBook에 review 필드가 있다면 검색 로직에 추가해야 합니다.
        allBooks = listOf(
            LibBook(title = "괴테는 모든 것을 말했다", author = "noshel", readStatus = ReadStatus.READING, progress = "50% 읽음"),
            LibBook(title = "자바의 정석", author = "남궁성", readStatus = ReadStatus.READING, progress = "p.120"),
            LibBook(title = "해리포터와 마법사의 돌", author = "J.K.롤링", readStatus = ReadStatus.DONE, rating = 5),
            LibBook(title = "클린 코드", author = "로버트 C", readStatus = ReadStatus.READING, progress = "독서 시작 전"),
            LibBook(title = "반지의 제왕", author = "톨킨", readStatus = ReadStatus.DONE, rating = 4),
            LibBook(title = "코틀린 인 액션", author = "드미트리", readStatus = ReadStatus.READING, progress = "80% 읽음")
        )
    }

    private fun initRecyclerView() {
        // 처음에는 빈 리스트로 초기화 (검색어가 없으므로)
        libraryAdapter = LibraryBookAdapter(emptyList())

        binding.libSearchResultRv.apply {
            // 서재와 동일하게 3열 그리드 적용
            layoutManager = GridLayoutManager(context, 3)
            adapter = libraryAdapter
        }
    }

    private fun initListeners() {
        // 뒤로가기 버튼
        binding.libSearchBackIv.setOnClickListener {
            // 키보드 내리고 프래그먼트 종료
            hideKeyboard()
            parentFragmentManager.popBackStack()
        }

        // 검색어 입력 감지 (TextWatcher)
        binding.searchInputEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                filterBooks(query)
            }
        })

        // 키보드의 '검색(돋보기)' 버튼 눌렀을 때 처리
        binding.searchInputEt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard() // 검색 버튼 누르면 키보드 내림
                true
            } else {
                false
            }
        }
    }

    // 검색 로직
    private fun filterBooks(query: String) {
        if (query.isEmpty()) {
            // 검색어가 없으면 리스트를 비웁니다. (또는 전체를 보여주거나, 최근 검색어를 보여줄 수 있음)
            libraryAdapter.submitList(emptyList())
            binding.libSearchCountTv.text = "0 권"
        } else {
            // 제목이나 작가 이름에 검색어가 포함된 책 필터링 (대소문자 무시)
            val filteredList = allBooks.filter { book ->
                book.title.contains(query, ignoreCase = true) ||
                        book.author.contains(query, ignoreCase = true)
                // 만약 '한 줄 평'도 검색하고 싶다면 UserBook에 필드를 추가하고 아래처럼 조건 추가
                // || book.review?.contains(query, ignoreCase = true) == true
            }

            // 어댑터 갱신
            libraryAdapter.submitList(filteredList)

            // 검색 결과 개수 갱신
            binding.libSearchCountTv.text = "${filteredList.size} 권"
        }
    }

    // 키보드 숨기기 유틸 함수
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchInputEt.windowToken, 0)
    }

    // 키보드 보이기 유틸 함수
    private fun showKeyboard() {
        binding.searchInputEt.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 딜레이를 주어야 프래그먼트 전환 후 키보드가 안정적으로 올라옴
        binding.searchInputEt.postDelayed({
            imm.showSoftInput(binding.searchInputEt, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}