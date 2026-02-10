package com.bookiibookii.bookiibookii.trkDirectGuest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bookiibookii.bookiibookii.databinding.ActivityDirectGuestBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

        val existing = supportFragmentManager.findFragmentByTag(DIRECT_GUEST_SHEET_TAG)
        val existingClass = existing?.javaClass
        val newClass = newSheet.javaClass

        if (existing != null && existingClass == newClass) return

        dismissExistingSheetIfAny()
        newSheet.show(supportFragmentManager, DIRECT_GUEST_SHEET_TAG)
    }

    private fun dismissExistingSheetIfAny() {
        val existing = supportFragmentManager.findFragmentByTag(DIRECT_GUEST_SHEET_TAG)
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
        const val DIRECT_GUEST_SHEET_TAG = "DIRECT_GUEST_SHEET"
    }
}
