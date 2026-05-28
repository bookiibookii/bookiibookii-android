package com.bookiibookii.bookiibookii.tracker.data

import com.bookiibookii.bookiibookii.data.api.TrkApi
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListItemResDTO
import retrofit2.Response

class TrackerRepository(
    private val api: TrkApi
) {
    suspend fun fetchMyTrackers(): Response<ApiResponse<List<TrackerListItemResDTO>>> {
        return api.getMyTrackers()
    }
}
