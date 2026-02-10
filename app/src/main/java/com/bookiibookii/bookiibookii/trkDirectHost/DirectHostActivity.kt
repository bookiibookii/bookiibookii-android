package com.bookiibookii.bookiibookii.trkDirectHost

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.databinding.ActivityDirectHostBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

        if (groupId <= 0) {
            finish()
            return
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.trackerState.collectLatest { state ->
                    when (state) {
                        UiState.Idle -> Unit
                        UiState.Loading -> Unit

                        is UiState.Success -> {
                            val dto = state.data
                            val status = dto.trackerStatus
                            val meetingTime = dto.meetingInfo?.meetingTime

                            showOrReplaceBottomSheetByStatus(groupId, status, meetingTime)
                        }

                        is UiState.Error -> {
                            showOrReplaceBottomSheetByStatus(groupId, null, null)
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
        if (existing is BottomSheetDialogFragment) {
            existing.dismissAllowingStateLoss()
        } else if (existing is androidx.fragment.app.DialogFragment) {
            existing.dismissAllowingStateLoss()
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

        return try {
            val meeting = parseAnyDateTime(meetingTime) ?: return false
            meeting.isBefore(LocalDateTime.now())
        } catch (e: Exception) {
            false
        }
    }

    private fun parseAnyDateTime(raw: String): LocalDateTime? {
        return try {
            if (raw.endsWith("Z")) {
                java.time.OffsetDateTime.parse(raw)
                    .atZoneSameInstant(java.time.ZoneId.systemDefault())
                    .toLocalDateTime()
            } else {
                LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        const val DIRECT_HOST_SHEET_TAG = "DIRECT_HOST_SHEET"
    }
}
