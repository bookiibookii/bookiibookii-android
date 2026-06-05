package com.bookiibookii.bookiibookii.tracker.data

import com.bookiibookii.bookiibookii.data.api.TrkApi
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.tracker.BookReviewReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.BookReviewResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressUpdateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryRegisterReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingRegisterReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MemberReviewCreateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MemberReviewResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.PartnerDeliveryResponseDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingProgressReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingProgressResDTO
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

    suspend fun recordReadingProgress(
        groupId: Long,
        currentPage: Int,
    ): Response<ApiResponse<ReadingProgressResDTO>> {
        return api.patchReadingProgress(groupId, ReadingProgressReqDTO(currentPage))
    }

    suspend fun submitBookReview(
        groupId: Long,
        star: Double,
        comment: String?,
    ): Response<ApiResponse<BookReviewResDTO>> {
        return api.postBookReview(groupId, BookReviewReqDTO(star, comment))
    }

    // 내 책 리뷰 수정 (PATCH /reviews/me)
    suspend fun updateMyBookReview(
        groupId: Long,
        star: Double,
        comment: String?,
    ): Response<ApiResponse<BookReviewResDTO>> {
        return api.patchMyBookReview(groupId, BookReviewReqDTO(star, comment))
    }

    suspend fun submitMemberReview(
        groupId: Long,
        reaction: String?,
        comment: String,
    ): Response<ApiResponse<MemberReviewResDTO>> {
        return api.postMemberReview(groupId, MemberReviewCreateReqDTO(reaction, comment))
    }

    suspend fun registerDelivery(
        groupId: Long,
        deliveryCompany: String,
        trackingNumber: String,
    ): Response<ApiResponse<String>> {
        return api.postDeliveryRegister(
            groupId,
            DeliveryRegisterReqDTO(deliveryCompany, trackingNumber),
        )
    }

    suspend fun registerMeeting(
        groupId: Long,
        request: MeetingRegisterReqDTO,
    ): Response<ApiResponse<MeetingResDTO>> {
        return api.postMeetingRegister(groupId, request)
    }

    suspend fun fetchMeeting(
        groupId: Long,
    ): Response<ApiResponse<MeetingResDTO>> {
        return api.getMeeting(groupId)
    }

    suspend fun completeMeeting(
        groupId: Long,
    ): Response<ApiResponse<MeetingResDTO>> {
        return api.patchMeetingCompletion(groupId)
    }

    suspend fun fetchDeliveryAddress(
        groupId: Long,
    ): Response<ApiResponse<DeliveryAddressResDTO>> {
        return api.getDeliveryAddress(groupId)
    }

    suspend fun updateMyDeliveryAddress(
        groupId: Long,
        request: DeliveryAddressUpdateReqDTO,
    ): Response<ApiResponse<String>> {
        return api.patchMyDeliveryAddress(groupId, request)
    }

    suspend fun fetchPartnerDelivery(
        groupId: Long,
    ): Response<ApiResponse<PartnerDeliveryResponseDTO>> {
        return api.getPartnerDelivery(groupId)
    }

    suspend fun confirmPartnerReceive(
        groupId: Long,
    ): Response<ApiResponse<String>> {
        return api.patchPartnerReceive(groupId)
    }
}
