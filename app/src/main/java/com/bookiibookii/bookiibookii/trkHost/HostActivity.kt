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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHostBinding
    private val vm: HostViewModel by viewModels()

    private var didAutoShowSheet = false

    private val groupId: Long by lazy {
        intent.getLongExtra("group_id", -1L)
    }

    private var currentStatus: TrackerStatus = TrackerStatus.UNKNOWN

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

        binding.cardWidget.isEnabled = false

        // 임시 로직 이거 나중에 삭제
        supportFragmentManager.setFragmentResultListener(
            HostStartBottomDialogFragment.RESULT_KEY,
            this
        ) { _, bundle ->
            when (bundle.getString(HostStartBottomDialogFragment.BUNDLE_ACTION)) {
                "START_READING" -> {
                    vm.onAction(HostAction.SET_HOST_READING)
                    currentStatus = TrackerStatus.HOST_READING
                }
                "HOST_SHIPPING_READY" -> {
                    vm.onAction(HostAction.SET_HOST_SHIPPING_READY)
                    currentStatus = TrackerStatus.HOST_DONE
                }
                "HOST_SHIPPED" -> {
                    vm.onAction(HostAction.SET_HOST_SHIPPED)
                    currentStatus = TrackerStatus.SHIPPING_TO_GUEST
                }
                "GUEST_READING" -> {
                    vm.onAction(HostAction.SET_GUEST_READING)
                    currentStatus = TrackerStatus.GUEST_READING
                }
                "GUEST_SHIPPING_READY" -> {
                    vm.onAction(HostAction.SET_GUEST_SHIPPING_READY)
                    currentStatus = TrackerStatus.GUEST_DONE
                }
                "GUEST_SHIPPED" -> {
                    vm.onAction(HostAction.SET_GUEST_SHIPPED)
                    currentStatus = TrackerStatus.SHIPPING_TO_HOST
                }
                "FINISHED" -> {
                    vm.onAction(HostAction.SET_FINISHED)
                    currentStatus = TrackerStatus.COMPLETED
                }
            }
        }

        binding.cardWidget.setOnClickListener {
            showSheetOnceForStatus(currentStatus)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.steps.collectLatest { steps ->
                    updateStatusList(steps)
                }
            }
        }

        if (savedInstanceState == null) {
            if (groupId <= 0L) {
                android.util.Log.e("HOST", "group_id missing: $groupId")
            } else {
                android.util.Log.d("HOST", "HostActivity start: group_id=$groupId")
                vm.loadTracker(groupId)
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collectLatest { state ->
                    if (state.isLoading) return@collectLatest

                    if (state.errorMessage != null) {
                        android.util.Log.e("HOST", "loadTracker error=${state.errorMessage}")

                        binding.cardWidget.isEnabled = false
                        currentStatus = TrackerStatus.UNKNOWN
                        vm.setPhaseFromApiStatus(null)
                        return@collectLatest
                    }

                    val dto = state.data ?: return@collectLatest

                    currentStatus = TrackerStatus.from(dto.trackerStatus)

                    binding.cardWidget.isEnabled = true

                    if (!didAutoShowSheet && savedInstanceState == null) {
                        didAutoShowSheet = true
                        binding.root.post {
                            showSheetOnceForStatus(currentStatus)
                        }
                    }

                    android.util.Log.d(
                        "HOST",
                        "loaded: groupId=$groupId trackerId=${dto.trackerId} status=${dto.trackerStatus} title=${dto.bookTitle}"
                    )
                }
            }
        }
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

    private fun createSheetForStatus(status: TrackerStatus): BottomSheetDialogFragment {
        return when (status) {
            TrackerStatus.READY -> HostStartBottomDialogFragment.newInstance(groupId)
            TrackerStatus.HOST_READING,
            TrackerStatus.HOST_EXTENSION -> HostReadingBottomDialogFragment.newInstance(groupId)
            TrackerStatus.HOST_DONE -> HostShippingBottomDialogFragment.newInstance(groupId)
            TrackerStatus.SHIPPING_TO_GUEST -> HostShippingStatusBottomDialogFragment()

            TrackerStatus.RECEIVED,
            TrackerStatus.GUEST_READING,
            TrackerStatus.GUEST_EXTENSION-> HostReadingStatusBottomDialogFragment()

            TrackerStatus.GUEST_DONE -> HostReadingDoneBottomDialogFragment.newInstance(groupId)
            TrackerStatus.SHIPPING_TO_HOST -> HostShippedBottomDialogFragment.newInstance(groupId)

            TrackerStatus.RETURNED,
            TrackerStatus.COMPLETED,
            TrackerStatus.UNKNOWN -> HostTradeFinishBottomDialogFragment()
        }
    }

    private fun showSheetOnceForStatus(status: TrackerStatus) {
        val tag = "tracker_sheet"
        if (supportFragmentManager.findFragmentByTag(tag) != null) return

        val sheet = createSheetForStatus(status)
        sheet.show(supportFragmentManager, tag)
    }
}
