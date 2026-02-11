package com.bookiibookii.bookiibookii.trkDirectGuest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ActivityDirectGuestBinding
import com.bookiibookii.bookiibookii.trkHost.TradeStatusItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class DirectGuestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDirectGuestBinding
    private val vm: DirectGuestViewModel by viewModels()

    private val groupId: Long by lazy {
        intent.getLongExtra("group_id", -1L)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDirectGuestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (groupId <= 0) {
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    vm.tradeStepList.collectLatest { list: List<TradeStatusItem> ->
                        updateStatusList(list)
                    }
                }

                launch {
                    vm.trackerState.collectLatest { state ->
                        when (state) {
                            UiState.Idle -> Unit
                            UiState.Loading -> Unit

                            is UiState.Success -> {
                                val dto = state.data
                                val status = dto.trackerStatus
                                val meetingTime = dto.meetingInfo?.meetingTime

                                binding.tvToolbarTitle.text = dto.bookTitle.orEmpty()

                                showOrReplaceBottomSheetByStatus(groupId, status, meetingTime)
                            }

                            is UiState.Error -> {
                                showOrReplaceBottomSheetByStatus(groupId, null, null)
                            }
                        }
                    }
                }
            }
        }

        vm.loadTracker(groupId)

        binding.cardWidget.setOnClickListener {
            val dto = (vm.trackerState.value as? UiState.Success)?.data
            val status = dto?.trackerStatus
            val meetingTime = dto?.meetingInfo?.meetingTime
            showOrReplaceBottomSheetByStatus(groupId, status, meetingTime)
        }
    }

    private fun updateStatusList(steps: List<TradeStatusItem>) {
        binding.stepContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        steps.forEachIndexed { index, item ->
            val row = inflater.inflate(R.layout.item_trade_status, binding.stepContainer, false)
            row.findViewById<TextView>(R.id.tv_title).text = item.title
            row.findViewById<TextView>(R.id.tv_desc).text = item.description  // TradeStatusItem에 description이 있을 때
            row.findViewById<TextView>(R.id.tv_badge).text = item.badge

            row.findViewById<View>(R.id.divider).visibility =
                if (index == steps.lastIndex) View.GONE else View.VISIBLE

            binding.stepContainer.addView(row)
        }
    }

    private fun showOrReplaceBottomSheetByStatus(
        groupId: Long,
        status: String?,
        meetingTime: String?
    ) {
        val newSheet = createBottomSheetByStatus(groupId, status, meetingTime)

        if (newSheet == null) {
            dismissExistingSheetIfAny()
            return
        }

        val existing = supportFragmentManager.findFragmentByTag(DIRECT_GUEST_SHEET_TAG)
        val existingClass = existing?.javaClass
        val newClass = newSheet.javaClass

        if (existing != null && existingClass == newClass) return

        dismissExistingSheetIfAny()
        newSheet.show(supportFragmentManager, DIRECT_GUEST_SHEET_TAG)
    }

    private fun dismissExistingSheetIfAny() {
        val existing = supportFragmentManager.findFragmentByTag(DIRECT_GUEST_SHEET_TAG)
        when (existing) {
            is BottomSheetDialogFragment -> existing.dismissAllowingStateLoss()
            is androidx.fragment.app.DialogFragment -> existing.dismissAllowingStateLoss()
        }
    }

    private fun createBottomSheetByStatus(
        groupId: Long,
        status: String?,
        meetingTime: String?
    ): BottomSheetDialogFragment? {
        return when (status) {
            "READY",
            "HOST_READING" -> DirectGuestReadingStatusBottomDialogFragment.newInstance(groupId)

            "HOST_EXTENSION" -> DirectGuestExtendRequestBottomDialogFragment.newInstance(groupId)

            "HOST_DONE" -> DirectGuestMeetEmptyBottomSheetFragment.newInstance(groupId)

            "SHIPPING_TO_GUEST" -> {
                if (isMeetingPassed(meetingTime)) {
                    DirectGuestReceiveBottomDialogFragment.newInstance(groupId)
                } else {
                    DirectGuestAppointmentStatusBottomDialogFragment.newInstance(groupId)
                }
            }

            "RECEIVED" -> DirectGuestStartBottomDialogFragment.newInstance(groupId)

            "GUEST_READING",
            "GUEST_EXTENSION" -> DirectGuestReadingBottomDialogFragment.newInstance(groupId)

            "GUEST_DONE" -> DirectGuestAppointmentBottomDialogFragment.newInstance(groupId)

            "SHIPPING_TO_HOST" -> {
                if (isMeetingPassed(meetingTime)) {
                    DirectGuestExchangeBottomDialogFragment.newInstance(groupId)
                } else {
                    DirectGuestAppointmentEditBottomDialogFragment.newInstance(groupId)
                }
            }

            "RETURNED" -> DirectGuestTradeFinishBottomDialogFragment.newInstance(groupId)

            "COMPLETED" -> null

            else -> DirectGuestStartBottomDialogFragment.newInstance(groupId)
        }
    }

    private fun isMeetingPassed(meetingTime: String?): Boolean {
        if (meetingTime.isNullOrBlank()) return false
        val meetingInstant = parseMeetingInstant(meetingTime) ?: return false
        return meetingInstant.isBefore(Instant.now())
    }

    private val inputDotFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("uuuu.MM.dd.HH:mm")
            .withResolverStyle(ResolverStyle.STRICT)

    private fun parseMeetingInstant(raw: String): Instant? {
        val s = raw.trim()
        if (s.isBlank()) return null

        runCatching {
            return java.time.OffsetDateTime.parse(s).toInstant()
        }

        runCatching {
            val ldt = LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return ldt.atZone(ZoneId.of("Asia/Seoul")).toInstant()
        }

        runCatching {
            val ldt = LocalDateTime.parse(s, inputDotFormatter)
            return ldt.atZone(ZoneId.of("Asia/Seoul")).toInstant()
        }

        return null
    }

    private companion object {
        const val DIRECT_GUEST_SHEET_TAG = "DIRECT_GUEST_SHEET"
    }
}
