package com.bookiibookii.bookiibookii.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.data.viewModel.LibraryViewModel
import com.bookiibookii.bookiibookii.data.viewModel.SortType
import com.bookiibookii.bookiibookii.databinding.FragmentLibSortDialogBinding
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LibrarySortBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentLibSortDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LibraryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibSortDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 각 정렬 구현
        binding.libDialogCriteriaTitleTv.setOnClickListener {
            viewModel.setSortType(SortType.TITLE)
            dismiss()
        }
        binding.libDialogCriteriaHighRateTv.setOnClickListener {
            viewModel.setSortType(SortType.RATING_HIGH)
            dismiss()
        }
        binding.libDialogCriteriaLowRateTv.setOnClickListener {
            viewModel.setSortType(SortType.RATING_LOW)
            dismiss()
        }
        binding.libDialogCriteriaLatelyReadTv.setOnClickListener {
            viewModel.setSortType(SortType.RECENT)
            dismiss()
        }
        binding.libDialogCriteriaOldestReadTv.setOnClickListener {
            viewModel.setSortType(SortType.OLD)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(R.id.design_bottom_sheet)

        bottomSheet?.let { sheet ->
            val metrics = resources.displayMetrics
            val screenHeight = metrics.heightPixels

            val layoutParams = sheet.layoutParams
            layoutParams.height = (screenHeight * 0.45).toInt()
            sheet.layoutParams = layoutParams

            val behavior = BottomSheetBehavior.from(sheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}