package com.bookiibookii.bookiibookii.trkDirectHost

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
import com.bookiibookii.bookiibookii.databinding.ActivityDirectHostBinding
import com.bookiibookii.bookiibookii.trkHost.HostGroupManageBottomDialogFragment
import com.bookiibookii.bookiibookii.trkHost.TradeStatusItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class DirectHostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDirectHostBinding
    private val vm: DirectHostViewModel by viewModels()

    private val groupId: Long by lazy {
        intent.getLongExtra("group_id", -1L)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDirectHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnMore.setOnClickListener {
            HostGroupManageBottomDialogFragment.newInstance(groupId).show(
                supportFragmentManager,
                HostGroupManageBottomDialogFragment.TAG
            )
        }

        if (groupId <= 0) {
            finish()
            return
        }

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

                                binding.tvUserName.text = dto.partnerNickname.orEmpty()

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
            vm.loadTracker(groupId)
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
            row.findViewById<TextView>(R.id.tv_desc).text = item.description
            row.findViewById<TextView>(R.id.tv_badge).text = item.badge

            val divider = row.findViewById<View>(R.id.divider)
            divider.visibility = if (index == steps.lastIndex) View.GONE else View.VISIBLE

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

        val existing = supportFragmentManager.findFragmentByTag(DIRECT_HOST_SHEET_TAG)
        val existingClass = existing?.javaClass
        val newClass = newSheet.javaClass

        if (existing != null && existingClass == newClass) return

        dismissExistingSheetIfAny()
        newSheet.show(supportFragmentManager, DIRECT_HOST_SHEET_TAG)
    }

    private fun dismissExistingSheetIfAny() {
        val existing = supportFragmentManager.findFragmentByTag(DIRECT_HOST_SHEET_TAG)
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
            "READY" -> DirectHostStartBottomDialogFragment.newInstance(groupId)

            "HOST_READING",
            "HOST_EXTENSION" -> DirectHostReadingBottomDialogFragment.newInstance(groupId)

            "HOST_DONE" -> DirectHostAppointmentBottomDialogFragment.newInstance(groupId)

            "SHIPPING_TO_GUEST" -> {
                if (isMeetingPassed(meetingTime)) {
                    DirectHostExchangeBottomDialogFragment.newInstance(groupId)
                } else {
                    DirectHostAppointmentEditBottomDialogFragment.newInstance(groupId)
                }
            }

            "RECEIVED" -> null

            "GUEST_READING" -> DirectReadingStatusBottomDialogFragment.newInstance(groupId)
            "GUEST_EXTENSION" -> DirectHostExtendRequestBottomDialogFragment.newInstance(groupId)

            "GUEST_DONE" -> DirectHostMeetEmptyBottomSheetFragment.newInstance(groupId)

            "SHIPPING_TO_HOST" -> {
                if (isMeetingPassed(meetingTime)) {
                    DirectHostReceiveBottomDialogFragment.newInstance(groupId)
                } else {
                    DirectHostAppointmentStatusBottomDialogFragment.newInstance(groupId)
                }
            }

            "RETURNED" -> DirectTradeFinishBottomDialogFragment.newInstance(groupId)

            "COMPLETED" -> null

            else -> DirectHostStartBottomDialogFragment.newInstance(groupId)
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
        const val DIRECT_HOST_SHEET_TAG = "DIRECT_HOST_SHEET"
    }
}
