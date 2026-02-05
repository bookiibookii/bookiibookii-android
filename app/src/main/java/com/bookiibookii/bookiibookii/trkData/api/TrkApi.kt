package com.bookiibookii.bookiibookii.trkData.api

import com.bookiibookii.bookiibookii.trkData.dto.HostTrackerListItemDto
import retrofit2.Response
import retrofit2.http.GET

interface TrkApi {
    @GET("/api/groups/me/trackers/host")
    suspend fun getHostTrackers(): Response<List<HostTrackerListItemDto>>
}