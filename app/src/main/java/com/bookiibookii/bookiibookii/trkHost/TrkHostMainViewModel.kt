package com.bookiibookii.bookiibookii.trkHost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.trkData.dto.HostTrackerListItemDto
import com.bookiibookii.bookiibookii.trkData.dto.HostTrackerRelayDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrkHostMainViewModel : ViewModel() {

    private val _trackers = MutableStateFlow<List<TrackerData>>(emptyList())
    val trackers: StateFlow<List<TrackerData>> = _trackers.asStateFlow()

    // 더미데이터
    fun loadHostTrackersDummy() {
        val dummyDtos = listOf(
            HostTrackerListItemDto(
                groupId = 1L,
                groupType = "RELAY",
                bookTitle = "살인자의 기억법",
                image = null,
                author = "김영하",
                category = "소설",
                tradeType = "DELIVERY",
                relayDetail = HostTrackerRelayDetailDto(
                    partnerNickname = "noshel",
                    hostProfileImage = null,
                    guestProfileImages = listOf("guest_img_url"),
                    trackerStatus = "READY",
                    stepDates = listOf(null, null, null, null)
                ),
                togetherDetail = null
            ),

            HostTrackerListItemDto(
                groupId = 2L,
                groupType = "RELAY",
                bookTitle = "아몬드",
                image = null,
                author = "손원평",
                category = "청소년 문학",
                tradeType = "DELIVERY",
                relayDetail = HostTrackerRelayDetailDto(
                    partnerNickname = "guest1",
                    hostProfileImage = null,
                    guestProfileImages = null,
                    trackerStatus = "COMPLETED",
                    stepDates = listOf("2024.01.01", "2024.01.05", "2024.01.05", "2024.01.05")
                ),
                togetherDetail = null
            ),

            // 같이 독서
//            HostTrackerListItemDto(
//                groupId = 3L,
//                groupType = "TOGETHER",
//                bookTitle = "클린 아키텍처",
//                image = null,
//                author = "로버트 C. 마틴",
//                category = "IT/개발",
//                relayDetail = null,
//                togetherDetail = HostTrackerTogetherDetailDto(
//                    hostNickname = "DevMaster",
//                    participantCount = 5,
//                    myReadingRate = 30,
//                    groupReadingRate = 45
//                )
//            )
        )

        _trackers.value = dummyDtos.map { dto -> dto.toTrackerData() }
    }

    // 실제 API 호출 부분 나중에 수정하고 주석 해제
    fun loadHostTrackers() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getHostTrackers()

                if (!response.isSuccessful) {
                    android.util.Log.e("HOST", "HTTP ${response.code()} ${response.message()}")
                    android.util.Log.e("HOST", "errorBody=${response.errorBody()?.string()}")
                    return@launch
                }

                val body = response.body()
                if (body == null) {
                    android.util.Log.e("HOST", "body is null")
                    return@launch
                }

                if (!body.isSuccess) {
                    android.util.Log.e("HOST", "API fail code=${body.code} msg=${body.message}")
                    return@launch
                }

                val list = body.result
                if (list == null) {
                    android.util.Log.e("HOST", "result is null (unexpected)")
                    _trackers.value = emptyList()
                    return@launch
                }

                _trackers.value = list.map { it.toTrackerData() }

            } catch (e: Exception) {
                android.util.Log.e("HOST", "exception", e)
            }
        }
    }



    private fun HostTrackerListItemDto.toTrackerData(): TrackerData {
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
            // 얘는 나중에 생각
            ExchangeType.NONE -> {
                val detail = this.togetherDetail
                baseData.copy(
                    withUserName = detail?.hostNickname,
                    currentStatus = TrackerStatus.HOST_READING
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
