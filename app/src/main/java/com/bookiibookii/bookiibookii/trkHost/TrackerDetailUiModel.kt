package com.bookiibookii.bookiibookii.trkHost

import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailDto

data class TrackerDetailUiModel(
    val trackerId: Long,
    val status: TrackerStatus,
    val layoutRes: Int
) {
    companion object {
        fun from(dto: TrackerDetailDto): TrackerDetailUiModel {
            val status = TrackerStatus.from(dto.trackerStatus)

            val layoutRes = when (status) {
                TrackerStatus.READY -> R.layout.fragment_start_bottom_sheet_dialog

                TrackerStatus.HOST_READING -> R.layout.fragment_reading_bottom_sheet_dialog

                TrackerStatus.HOST_DONE -> R.layout.fragment_host_shipping_bottom_dialog

                TrackerStatus.SHIPPING_TO_GUEST -> R.layout.fragment_host_shipped_bottom_dialog

                TrackerStatus.RECEIVED,
                TrackerStatus.GUEST_READING -> R.layout.fragment_host_reading_status_bottom_dialog

                TrackerStatus.GUEST_DONE -> R.layout.fragment_host_reading_done_bottom_dialog


                TrackerStatus.SHIPPING_TO_HOST -> R.layout.fragment_host_shipped_bottom_dialog

                TrackerStatus.RETURNED,
                TrackerStatus.COMPLETED,
                TrackerStatus.UNKNOWN -> R.layout.fragment_host_trade_finish_bottom_dialog
            }

            return TrackerDetailUiModel(
                trackerId = dto.trackerId,
                status = status,
                layoutRes = layoutRes
            )
        }
    }
}