package com.bookiibookii.bookiibookii.mypage.feat.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.mypage.BaseMypageFragment
import com.bookiibookii.bookiibookii.mypage.ui.main.AddressManagementScreen
import com.bookiibookii.bookiibookii.mypage.vm.AddressViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.launch

class AddressManagementFragment : BaseMypageFragment() {

    private val viewModel: AddressViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                val deliveries by viewModel.deliveries.observeAsState(emptyList())
                val exchanges by viewModel.exchanges.observeAsState(emptyList())

                AddressManagementScreen(
                    deliveries = deliveries,
                    exchanges = exchanges,
                    initialTabIndex = arguments?.getInt(ARG_INITIAL_TAB) ?: 0,
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onFetchDeliveries = { viewModel.fetchDeliveries() },
                    onFetchExchanges = { viewModel.fetchExchanges() },
                    onAddDelivery = { req, onSuccess -> viewModel.addDelivery(req, onSuccess) },
                    onUpdateDelivery = { id, req, onSuccess -> viewModel.updateDelivery(id, req, onSuccess) },
                    onDeleteDelivery = { id -> viewModel.deleteDelivery(id) },
                    onAddExchange = { req, onSuccess -> viewModel.addExchange(req, onSuccess) },
                    onUpdateExchange = { id, req, onSuccess -> viewModel.updateExchange(id, req, onSuccess) },
                    onDeleteExchange = { id -> viewModel.deleteExchange(id) },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectEvents()
    }

    private fun collectEvents() {
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is AddressViewModel.Event.ShowToast ->
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        // 0 = 배송지, 1 = 희망 교환 장소
        private const val ARG_INITIAL_TAB = "initialTab"

        fun newInstance(initialTab: Int) = AddressManagementFragment().apply {
            arguments = Bundle().apply { putInt(ARG_INITIAL_TAB, initialTab) }
        }
    }
}
