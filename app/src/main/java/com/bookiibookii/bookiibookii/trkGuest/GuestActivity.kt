package com.bookiibookii.bookiibookii.trkGuest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ActivityGuestBinding
import com.bookiibookii.bookiibookii.trkHost.HostActivity
import com.bookiibookii.bookiibookii.trkHost.TrackerStatus
import com.bookiibookii.bookiibookii.trkHost.TradeStatusItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GuestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuestBinding
    private val vm: GuestViewModel by viewModels()

    private var didAutoShowSheet = false

    private var currentIsVerified: Boolean? = null

    private val groupId: Long by lazy {
        intent.getLongExtra("group_id", -1L)
    }

    private var currentStatus: TrackerStatus = TrackerStatus.UNKNOWN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("GUEST", "intent extras=${intent.extras}")
        enableEdgeToEdge()

        binding = ActivityGuestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.cardWidget.isEnabled = false
        binding.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.steps.collectLatest { steps ->
                    updateStatusList(steps)
                }
            }
        }

        if (savedInstanceState == null) {
            if (groupId <= 0L) {
                android.util.Log.e("GUEST", "group_id missing: $groupId")
            } else {
                android.util.Log.d("GUEST", "GuestActivity start: group_id=$groupId")
                vm.loadTracker(groupId)
            }
        }

        binding.cardWidget.setOnClickListener {
            vm.loadTracker(groupId)

            lifecycleScope.launch {
                vm.uiState.first { !it.isLoading }
                showSheetOnceForStatus(currentStatus)
            }
        }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    vm.uiState.collectLatest { state ->
                        if (state.isLoading) return@collectLatest

                        if (state.errorMessage != null) {
                            android.util.Log.e("GUEST", "loadTracker error=${state.errorMessage}")
                            return@collectLatest
                        }

                        val dto = state.data ?: return@collectLatest

                        android.util.Log.d("GUEST", "raw status='${dto.trackerStatus}'")
                        android.util.Log.d("GUEST", "mapped=${TrackerStatus.from(dto.trackerStatus)}")

                        currentStatus = TrackerStatus.from(dto.trackerStatus)
                        currentIsVerified = dto.deliveryInfo?.isVerified
                        binding.cardWidget.isEnabled = true

                        val title = dto.bookTitle?.trim().orEmpty()
                        binding.tvToolbarTitle.text = if (title.isBlank()) " " else title

                        if (!didAutoShowSheet && savedInstanceState == null) {
                            didAutoShowSheet = true
                            binding.root.post {
                                showSheetOnceForStatus(currentStatus)
                            }
                        }

                        android.util.Log.d(
                            "GUEST",
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
            TrackerStatus.READY,
            TrackerStatus.HOST_READING -> GuestReadingStatusBottomDialogFragment.newInstance(groupId)
            TrackerStatus.HOST_EXTENSION -> GuestExtendRequestBottomDialogFragment.newInstance(groupId)
            TrackerStatus.HOST_DONE -> GuestReadingDoneBottomDialogFragment()
            TrackerStatus.SHIPPING_TO_GUEST -> GuestShippedBottomDialogFragment.newInstance(groupId)

            TrackerStatus.RECEIVED -> GuestStartBottomDialogFragment.newInstance(groupId)
            TrackerStatus.GUEST_READING,
            TrackerStatus.GUEST_EXTENSION-> GuestReadingBottomDialogFragment.newInstance(groupId)

            TrackerStatus.GUEST_DONE -> GuestShippingBottomDialogFragment.newInstance(groupId)
            TrackerStatus.SHIPPING_TO_HOST -> GuestShippingStatusBottomDialogFragment.newInstance(groupId)

            TrackerStatus.RETURNED -> {
                val verified = currentIsVerified ?: false
                if (!verified) {
                    GuestShippingStatusBottomDialogFragment.newInstance(groupId)
                } else {
                    GuestTradeFinishBottomDialogFragment.newInstance(groupId)
                }
            }

            TrackerStatus.COMPLETED,
            TrackerStatus.UNKNOWN -> GuestTradeFinishBottomDialogFragment()
        }
    }

    private fun showSheetOnceForStatus(status: TrackerStatus) {
        val tag = "tracker_sheet"

        (supportFragmentManager.findFragmentByTag(tag) as? BottomSheetDialogFragment)
            ?.dismissAllowingStateLoss()

        val sheet = createSheetForStatus(status)
        sheet.show(supportFragmentManager, tag)
    }
}
