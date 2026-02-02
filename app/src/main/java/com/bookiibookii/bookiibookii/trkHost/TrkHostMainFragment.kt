package com.bookiibookii.bookiibookii.trkHost

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookiibookii.bookiibookii.BuildConfig
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.bookData.API.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentTrkHostMainBinding
import com.bookiibookii.bookiibookii.trkDirectHost.DirectHostActivity
import com.bookiibookii.bookiibookii.trkGuest.TrkGuestMainFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrkHostMainFragment : Fragment() {
    private var _binding: FragmentTrkHostMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var trackerAdapter: TrackerAdapter
    private lateinit var footerAdapter: CreateGroupFooterAdapter
    private lateinit var concatAdapter: ConcatAdapter
    private var currentList: MutableList<TrackerData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrkHostMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToggleLogic()
        updateTabState(isMyGroup = true)

        trackerAdapter = TrackerAdapter { item ->
            val target = when (item.exchangeType) {
                ExchangeType.SHIPPING -> HostActivity::class.java
                ExchangeType.DIRECT -> DirectHostActivity::class.java
            }

            val intent = Intent(requireContext(), target).apply {
                putExtra("tracker_id", item.id)
                putExtra("exchange_type", item.exchangeType.name)
            }

            startActivity(intent)
        }

        footerAdapter = CreateGroupFooterAdapter(
            mode = FooterMode.HOST_CREATE,
            onActionClick = {
                // TODO: 그룹 만들기 화면 이동
            }
        )

        concatAdapter = ConcatAdapter(trackerAdapter, footerAdapter)

        binding.trkRecyclerview.apply {
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        currentList = createDummyTrackerList().toMutableList()
        trackerAdapter.submitList(currentList.toList()) {
            footerAdapter.setShowEmptyText(trackerAdapter.itemCount == 0)
        }

        updateAllBooksFromAladin()

    }

    private fun setupToggleLogic() {
        binding.myGroupBt.setOnClickListener {
            updateTabState(isMyGroup = true)
        }

        binding.joinedGroupBt.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragmentContainer, TrkGuestMainFragment())
                addToBackStack(null)
            }
        }
    }

    private fun updateTabState(isMyGroup: Boolean) {
        binding.myGroupBt.isSelected = isMyGroup
        binding.joinedGroupBt.isSelected = !isMyGroup
    }

    private fun updateAllBooksFromAladin() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            for (i in currentList.indices) {
                val title = currentList[i].bookTitle

                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.api.searchBooks(
                            ttbKey = BuildConfig.ALADIN_TTB_KEY,
                            query = title,
                            queryType = "Title",
                            maxResults = 10,
                            start = 1
                        )
                    }

                    val books = response.item.orEmpty()
                    val representative = pickRepresentativeBookLocal(books, title) ?: continue

                    currentList[i] = currentList[i].copy(
                        bookTitle = representative.title,
                        bookAuthor = representative.author,
                        coverImageUrl = representative.cover
                    )

                    trackerAdapter.submitList(currentList.toList()) {
                        footerAdapter.setShowEmptyText(trackerAdapter.itemCount == 0)
                    }

                } catch (e: Exception) {
                    android.util.Log.e("BOOK_API", "update fail: $title", e)
                }
            }
        }
    }

    private fun isSetBookLocal(title: String): Boolean {
        val setKeywords = listOf("세트", "전", "+")
        return setKeywords.any { title.contains(it) }
    }

    private fun pickRepresentativeBookLocal(
        books: List<com.bookiibookii.bookiibookii.bookData.Data.Book>,
        queryTitle: String
    ): com.bookiibookii.bookiibookii.bookData.Data.Book? {
        val singleBooks = books.filterNot { isSetBookLocal(it.title) }
        if (singleBooks.isEmpty()) return null
        return singleBooks.firstOrNull { it.title == queryTitle } ?: singleBooks.first()
    }


    // 더미 데이터
    private fun createDummyTrackerList(): List<TrackerData> {
        return listOf(
            TrackerData(
                id = 1L,
                bookTitle = "살인자의 기억법",
                bookAuthor = "김영하",
                withUserName = "noshel",
                coverImageUrl = null,
                currentStep = TrackerStep.DELIVERY,
                exchangeType = ExchangeType.SHIPPING
            ),
            TrackerData(
                id = 2L,
                bookTitle = "아몬드",
                bookAuthor = "손원평",
                withUserName = null,
                coverImageUrl = null,
                currentStep = TrackerStep.READING,
                exchangeType = ExchangeType.DIRECT
            ),
            TrackerData(
                id = 3L,
                bookTitle = "살인자의 기억법",
                bookAuthor = "김영하",
                withUserName = "noshel",
                coverImageUrl = null,
                currentStep = TrackerStep.DELIVERY,
                exchangeType = ExchangeType.SHIPPING
            ),
            TrackerData(
                id = 4L,
                bookTitle = "아몬드67",
                bookAuthor = "손원평",
                withUserName = null,
                coverImageUrl = null,
                currentStep = TrackerStep.READING,
                exchangeType = ExchangeType.SHIPPING
            ),
            TrackerData(
                id = 5L,
                bookTitle = "아몬드123",
                bookAuthor = "손원평",
                withUserName = null,
                coverImageUrl = null,
                currentStep = TrackerStep.READING,
                exchangeType = ExchangeType.SHIPPING
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}