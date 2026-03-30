package com.bookiibookii.bookiibookii.trkHost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.trkData.dto.GuestTrackerListItemDto
import com.bookiibookii.bookiibookii.trkData.dto.HostTrackerListItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TrackerTab { MY_GROUP, JOINED_GROUP }

class TrkMainViewModel : ViewModel() {

    private val _hostTrackers = MutableStateFlow<List<TrackerData>>(emptyList())
    private val _guestTrackers = MutableStateFlow<List<TrackerData>>(emptyList())

    private val _currentTab = MutableStateFlow(TrackerTab.MY_GROUP)
    val currentTab: StateFlow<TrackerTab> = _currentTab.asStateFlow()

    val trackers: StateFlow<List<TrackerData>> = _currentTab
        .flatMapLatest { tab ->
            when (tab) {
                TrackerTab.MY_GROUP -> _hostTrackers
                TrackerTab.JOINED_GROUP -> _guestTrackers
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadHostTrackers()
    }

    fun selectTab(tab: TrackerTab) {
        _currentTab.value = tab
        when (tab) {
            TrackerTab.MY_GROUP -> if (_hostTrackers.value.isEmpty()) loadHostTrackers()
            TrackerTab.JOINED_GROUP -> if (_guestTrackers.value.isEmpty()) loadGuestTrackers()
        }
    }

    private fun loadHostTrackers() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getHostTrackers()
                if (!response.isSuccessful) {
                    android.util.Log.e("TRACKER", "HOST HTTP ${response.code()} ${response.message()}")
                    return@launch
                }
                val body = response.body() ?: return@launch
                if (!body.isSuccess) {
                    android.util.Log.e("TRACKER", "HOST API fail: ${body.message}")
                    return@launch
                }
                _hostTrackers.value = body.result?.map { it.toTrackerData() } ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("TRACKER", "HOST exception", e)
            }
        }
    }

    private fun loadGuestTrackers() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getGuestTrackers()
                if (!response.isSuccessful) {
                    android.util.Log.e("TRACKER", "GUEST HTTP ${response.code()} ${response.message()}")
                    return@launch
                }
                val body = response.body() ?: return@launch
                if (!body.isSuccess) {
                    android.util.Log.e("TRACKER", "GUEST API fail: ${body.message}")
                    return@launch
                }
                _guestTrackers.value = body.result?.map { it.toTrackerData() } ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("TRACKER", "GUEST exception", e)
            }
        }
    }

    private fun HostTrackerListItemDto.toTrackerData(): TrackerData {
        val base = buildBase(groupId, bookTitle, author, category, image, tradeType)
        return when (base.exchangeType) {
            ExchangeType.DELIVERY, ExchangeType.DIRECT ->
                buildRelayData(base, relayDetail?.partnerNickname, relayDetail?.hostProfileImage,
                    relayDetail?.guestProfileImages, relayDetail?.trackerStatus, relayDetail?.stepDates)
            ExchangeType.NONE ->
                buildTogetherData(base, togetherDetail?.hostNickname, togetherDetail?.participantCount,
                    togetherDetail?.myReadingRate, togetherDetail?.groupReadingRate, TrackerStatus.HOST_READING)
        }
    }

    private fun GuestTrackerListItemDto.toTrackerData(): TrackerData {
        val base = buildBase(groupId, bookTitle, author, category, image, tradeType)
        return when (base.exchangeType) {
            ExchangeType.DELIVERY, ExchangeType.DIRECT ->
                buildRelayData(base, relayDetail?.partnerNickname, relayDetail?.hostProfileImage,
                    relayDetail?.guestProfileImages, relayDetail?.trackerStatus, relayDetail?.stepDates)
            ExchangeType.NONE ->
                buildTogetherData(base, togetherDetail?.hostNickname, togetherDetail?.participantCount,
                    togetherDetail?.myReadingRate, togetherDetail?.groupReadingRate, TrackerStatus.GUEST_READING)
        }
    }

    private fun buildBase(
        groupId: Long, bookTitle: String, author: String?, category: String?,
        image: String?, tradeType: String?
    ) = TrackerData(
        id = groupId,
        groupId = groupId,
        bookTitle = bookTitle,
        bookAuthor = author.orEmpty(),
        bookCategory = category,
        coverImageUrl = image,
        exchangeType = mapExchangeType(tradeType),
        withUserName = null,
        stepDates = emptyList(),
        currentStatus = TrackerStatus.UNKNOWN,
        hostProfileImageUrl = null,
        guestProfileImageUrl = null,
        myReadingRate = null,
        groupReadingRate = null
    )

    private fun buildRelayData(
        base: TrackerData,
        partnerNickname: String?,
        hostProfileImage: String?,
        guestProfileImages: List<String>?,
        trackerStatusStr: String?,
        rawStepDates: List<String?>?
    ): TrackerData {
        val stepDates = normalizeStepDates(rawStepDates)
        val status = trackerStatusStr?.let { TrackerStatus.from(it) } ?: calculateRelayStatus(stepDates)
        return base.copy(
            withUserName = partnerNickname,
            hostProfileImageUrl = hostProfileImage,
            guestProfileImageUrl = guestProfileImages?.firstOrNull(),
            stepDates = stepDates,
            currentStatus = status
        )
    }

    private fun buildTogetherData(
        base: TrackerData,
        hostNickname: String?,
        participantCount: Int?,
        myReadingRate: Int?,
        groupReadingRate: Int?,
        noneStatus: TrackerStatus
    ): TrackerData {
        val withText = buildString {
            if (!hostNickname.isNullOrBlank()) append(hostNickname)
            if (participantCount != null && participantCount > 0) {
                if (isNotEmpty()) append("  +$participantCount") else append("+$participantCount")
            }
        }.ifBlank { null }
        return base.copy(
            withUserName = withText,
            currentStatus = noneStatus,
            myReadingRate = myReadingRate?.coerceIn(0, 100),
            groupReadingRate = groupReadingRate?.coerceIn(0, 100)
        )
    }

    private fun normalizeStepDates(raw: List<String?>?): List<String?> =
        List(4) { idx -> raw.orEmpty().getOrNull(idx) }

    private fun calculateRelayStatus(stepDates: List<String?>): TrackerStatus {
        return when (stepDates.indexOfLast { !it.isNullOrBlank() }) {
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
            else -> ExchangeType.NONE
        }
    }
}
