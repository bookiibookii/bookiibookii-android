package com.bookiibookii.bookiibookii.trkGuest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.trkData.dto.GuestTrackerListItemDto
import com.bookiibookii.bookiibookii.trkHost.ExchangeType
import com.bookiibookii.bookiibookii.trkHost.TrackerData
import com.bookiibookii.bookiibookii.trkHost.TrackerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrkGuestMainViewModel : ViewModel() {

    private val _trackers = MutableStateFlow<List<TrackerData>>(emptyList())
    val trackers: StateFlow<List<TrackerData>> = _trackers.asStateFlow()

    fun loadGuestTrackers() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getGuestTrackers()

                if (!response.isSuccessful) {
                    android.util.Log.e("GUEST", "HTTP ${response.code()} ${response.message()}")
                    android.util.Log.e("GUEST", "errorBody=${response.errorBody()?.string()}")
                    return@launch
                }

                val body = response.body()
                if (body == null) {
                    android.util.Log.e("GUEST", "body is null")
                    return@launch
                }

                if (!body.isSuccess) {
                    android.util.Log.e("GUEST", "API fail code=${body.code} msg=${body.message}")
                    return@launch
                }

                val list = body.result
                if (list == null) {
                    android.util.Log.e("GUEST", "result is null (unexpected)")
                    _trackers.value = emptyList()
                    return@launch
                }

                _trackers.value = list.map { it.toTrackerData() }

            } catch (e: Exception) {
                android.util.Log.e("GUEST", "exception", e)
            }
        }
    }

    private fun GuestTrackerListItemDto.toTrackerData(): TrackerData {
        val exchangeType = mapExchangeType(this.tradeType)

        val baseData = TrackerData(
            id = this.groupId,
            groupId = this.groupId,
            bookTitle = this.bookTitle,
            bookAuthor = this.author.orEmpty(),
            bookCategory = this.category,
            coverImageUrl = this.image,
            exchangeType = exchangeType,

            withUserName = null,
            stepDates = emptyList(),
            currentStatus = TrackerStatus.UNKNOWN,
            hostProfileImageUrl = null,
            guestProfileImageUrl = null
        )

        return when (exchangeType) {
            ExchangeType.DELIVERY,
            ExchangeType.DIRECT -> {
                val detail = this.relayDetail
                val normalizedDates = normalizeStepDates(detail?.stepDates)

                val status = detail?.trackerStatus?.let {
                    TrackerStatus.from(it)
                } ?: calculateRelayStatus(normalizedDates)

                baseData.copy(
                    withUserName = detail?.partnerNickname,
                    hostProfileImageUrl = detail?.hostProfileImage,
                    guestProfileImageUrl = detail?.guestProfileImages?.firstOrNull(),
                    stepDates = normalizedDates,
                    currentStatus = status
                )
            }
            // 같이 읽기
            ExchangeType.NONE -> {
                val detail = this.togetherDetail
                baseData.copy(
                    withUserName = detail?.hostNickname,
                    currentStatus = TrackerStatus.GUEST_READING
                )
            }
        }
    }

    private fun normalizeStepDates(raw: List<String?>?): List<String?> {
        val list = raw.orEmpty()
        return List(4) { idx -> list.getOrNull(idx) }
    }

    private fun calculateRelayStatus(stepDates: List<String?>): TrackerStatus {
        val lastFilled = stepDates.indexOfLast { !it.isNullOrBlank() }

        return when (lastFilled) {
            0 -> TrackerStatus.HOST_READING
            1 -> TrackerStatus.SHIPPING_TO_GUEST
            2 -> TrackerStatus.GUEST_READING
            3 -> TrackerStatus.SHIPPING_TO_HOST
            else -> TrackerStatus.READY
        }
    }

    private fun mapExchangeType(typeString: String?): ExchangeType {
        return when (typeString?.uppercase()) {
            "DELIVERY", "SHIPPING" -> ExchangeType.DELIVERY
            "DIRECT" -> ExchangeType.DIRECT
            "TOGETHER" -> ExchangeType.NONE
            else -> ExchangeType.DELIVERY
        }
    }
}