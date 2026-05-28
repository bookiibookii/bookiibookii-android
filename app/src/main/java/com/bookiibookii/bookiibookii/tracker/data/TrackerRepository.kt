package com.bookiibookii.bookiibookii.tracker.data

import com.bookiibookii.bookiibookii.data.api.TrkApi
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDetailResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListResDTO
import retrofit2.Response

class TrackerRepository(
    private val api: TrkApi
) {
    suspend fun fetchMyTrackers(): Response<ApiResponse<TrackerListResDTO>> {
        return api.getMyTrackers()
    }

    suspend fun fetchTrackerDetail(groupId: Long): Response<ApiResponse<TrackerDetailResDTO>> {
        return api.getTrackerDetail(groupId)
    }
}
