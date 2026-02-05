package com.bookiibookii.bookiibookii.trkHost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ActivityHostBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHostBinding

    private val vm: HostViewModel by viewModels()

    private val detailVm: HostTrackerDetailViewModel by viewModels()

    private val groupId: Long by lazy {
        intent.getLongExtra("tracker_id", -1L)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.cardWidget.setOnClickListener {
            if (groupId != -1L) {
                detailVm.load(groupId)
                showTrackerBottomSheetIfNotShown()
            }
        }

        if (savedInstanceState == null && groupId != -1L) {
            detailVm.load(groupId)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                detailVm.state.collect { state ->
                    when (state) {
                        is TrackerDetailUiState.Success -> {
                            // 여기서 state.data.status / layoutRes가 결정됨
                            showTrackerBottomSheetIfNotShown()
                        }
                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.steps.collectLatest { steps ->
                    updateStatusList(steps)
                }
            }
        }
    }

    private fun showTrackerBottomSheetIfNotShown() {
        if (supportFragmentManager.findFragmentByTag(TrackerContainerBottomSheetFragment.TAG) != null) return

        TrackerContainerBottomSheetFragment()
            .show(supportFragmentManager, TrackerContainerBottomSheetFragment.TAG)
    }

    private fun updateStatusList(steps: List<TradeStatusItem>) {
        binding.stepContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        steps.forEachIndexed { index, item ->
            val row = inflater.inflate(R.layout.item_trade_status, binding.stepContainer, false)

            row.findViewById<TextView>(R.id.tv_title).text = item.title
            row.findViewById<TextView>(R.id.tv_desc).text = item.description
            row.findViewById<TextView>(R.id.tv_badge).text = item.badge

            val divider = row.findViewById<View>(R.id.divider)
            divider.visibility = if (index == steps.lastIndex) View.GONE else View.VISIBLE

            binding.stepContainer.addView(row)
        }
    }
}
