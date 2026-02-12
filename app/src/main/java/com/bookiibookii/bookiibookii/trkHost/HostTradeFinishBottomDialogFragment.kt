package com.bookiibookii.bookiibookii.trkHost

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentHostTradeFinishBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class HostTradeFinishBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentHostTradeFinishBottomDialogBinding? = null
    private val binding get() = _binding!!

    private val groupId: Long by lazy {
        requireArguments().getLong(ARG_GROUP_ID)
    }

    interface Listener {
        fun onMoveToLibraryBookDetail(groupId: Long, userBookId: Int)

        fun onError(message: String)
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (parentFragment as? Listener) ?: (activity as? Listener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHostTradeFinishBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnWriteReview.setOnClickListener {
            fetchUserBookIdAndProceed()
        }
    }

    private fun fetchUserBookIdAndProceed() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getLibraryBooks()

                if (!response.isSuccessful) {
                    listener?.onError("HTTP 오류: ${response.code()}")
                    return@launch
                }

                val body = response.body()
                if (body == null) {
                    listener?.onError("응답이 비어있습니다.")
                    return@launch
                }

                if (!body.isSuccess) {
                    listener?.onError(body.message)
                    return@launch
                }

                val list = body.result.orEmpty()

                val matched = list.firstOrNull { it.groupId.toLong() == groupId }
                if (matched == null) {
                    listener?.onError("해당 groupId($groupId)에 해당하는 책이 없습니다.")
                    return@launch
                }

                val userBookId = matched.userBookId

                listener?.onMoveToLibraryBookDetail(groupId, userBookId)

                dismiss()

            } catch (e: Exception) {
                listener?.onError("네트워크 오류: ${e.message ?: "unknown"}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    companion object {
        const val TAG = "TradeFinishFragment"
        private const val ARG_GROUP_ID = "arg_group_id"

        fun newInstance(groupId: Long) =
            HostTradeFinishBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                }
            }
    }

    override fun getTheme(): Int = R.style.Theme_Bookii_BottomSheet_NoDim
}
