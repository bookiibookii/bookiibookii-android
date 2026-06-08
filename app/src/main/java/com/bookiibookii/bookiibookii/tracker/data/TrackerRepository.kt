package com.bookiibookii.bookiibookii.tracker.data

import com.bookiibookii.bookiibookii.data.api.LocationApi
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.api.TrkApi
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddress
import com.bookiibookii.bookiibookii.data.model.tracker.BookReviewReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.BookReviewResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.GroupReviewsResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressDirectUpdateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressSavedUpdateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryRegisterReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingRegisterReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MemberReviewCreateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MemberReviewResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.PartnerDeliveryResponseDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingPeriodUpdateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingPeriodUpdateResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingProgressReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingProgressResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDetailResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListResDTO
import retrofit2.Response

class TrackerRepository(
    private val api: TrkApi,
    private val locationApi: LocationApi = RetrofitClient.locationApi(),
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

    // 독서 기간(예상 종료일) 수정
    suspend fun updateReadingPeriod(
        groupId: Long,
        newEndDate: String,
    ): Response<ApiResponse<ReadingPeriodUpdateResDTO>> {
        return api.patchReadingPeriod(groupId, ReadingPeriodUpdateReqDTO(newEndDate))
    }

    // 그룹 후기 전체 조회 — 기존 책 후기 프리필용(writerId로 내 후기 선별)
    suspend fun fetchGroupReviews(
        groupId: Long,
    ): Response<ApiResponse<GroupReviewsResDTO>> {
        return api.getGroupReviews(groupId)
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

    // 마이페이지에 등록된 배송지 목록 (초기 선택 매칭 + 기존 배송지 선택용)
    suspend fun fetchSavedDeliveries(): Response<ApiResponse<List<DeliveryAddress>>> {
        return locationApi.getDeliveries()
    }

    // 이번 교환 배송지 변경 - 기존(마이페이지 등록) 배송지 선택
    suspend fun changeDeliveryAddressSaved(
        groupId: Long,
        userDeliveryId: Long,
    ): Response<ApiResponse<DeliveryAddressResDTO>> {
        return api.putMyDeliveryAddressSaved(
            groupId = groupId,
            request = DeliveryAddressSavedUpdateReqDTO(userDeliveryId = userDeliveryId),
        )
    }

    // 이번 교환 배송지 변경 - 직접 입력 (주소만)
    suspend fun changeDeliveryAddressDirect(
        groupId: Long,
        zipCode: String,
        address: String,
        addressDetail: String,
    ): Response<ApiResponse<DeliveryAddressResDTO>> {
        return api.putMyDeliveryAddressDirect(
            groupId = groupId,
            request = DeliveryAddressDirectUpdateReqDTO(
                zipCode = zipCode,
                address = address,
                addressDetail = addressDetail,
            ),
        )
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
