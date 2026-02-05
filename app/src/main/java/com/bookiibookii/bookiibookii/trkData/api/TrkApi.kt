package com.bookiibookii.bookiibookii.trkData.api

import com.bookiibookii.bookiibookii.trkData.dto.ApiResponse
import com.bookiibookii.bookiibookii.trkData.dto.HostTrackerListItemDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TrkApi {
    @GET("/api/groups/me/trackers/host")
    suspend fun getHostTrackers(): Response<ApiResponse<List<HostTrackerListItemDto>>>

    @GET("/api/groups/{groupId}/tracker")
    suspend fun getTrackerDetail(
        @Path("groupId") groupId: Long
    ): Response<ApiResponse<TrackerDetailDto>>
}

