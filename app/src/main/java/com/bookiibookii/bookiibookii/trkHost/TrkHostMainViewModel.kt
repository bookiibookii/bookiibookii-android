package com.bookiibookii.bookiibookii.trkHost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrkHostMainViewModel : ViewModel() {

    private val _trackers = MutableStateFlow<List<TrackerData>>(emptyList())
    val trackers: StateFlow<List<TrackerData>> = _trackers.asStateFlow()

    // 더미 테스트용
    fun setInitialList(list: List<TrackerData>) {
        _trackers.value = list
    }

    fun loadHostTrackers() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getHostTrackers()
                if (!response.isSuccessful) return@launch

                val body = response.body() ?: return@launch
                if (!body.isSuccess) return@launch

                val list = body.result.orEmpty()

                _trackers.value = list.map { dto ->
                    val stepDates = normalizeStepDates(dto.relayDetail?.stepDates)
                    val currentStep = computeCurrentStep(stepDates)

                    val hostImg = dto.relayDetail?.hostProfileImage
                    val guestImg = dto.relayDetail?.guestProfileImages?.firstOrNull()

                    TrackerData(
                        id = dto.groupId,
                        bookTitle = dto.bookTitle,
                        bookAuthor = dto.author.orEmpty(),
                        bookCategory = dto.category,
                        withUserName = dto.relayDetail?.partnerNickname,
                        coverImageUrl = dto.image,
                        exchangeType = mapExchangeType(dto.groupType),

                        stepDates = stepDates,
                        currentStep = currentStep,

                        hostProfileImageUrl = hostImg,
                        guestProfileImageUrl = guestImg
                    )
                }

            } catch (_: Exception) {

            }
        }
    }

    // 더미데이터
    fun loadHostTrackersDummy() {
        _trackers.value = listOf(
            TrackerData(
                id = 1L,
                bookTitle = "살인자의 기억법",
                bookAuthor = "김영하",
                bookCategory = "소설",
                withUserName = "noshel",
                coverImageUrl = null,
                exchangeType = ExchangeType.SHIPPING,
                stepDates = listOf("2024.01.01", null, null, null),
                currentStep = TrackerStep.HOST_READING,
                hostProfileImageUrl = null,
                guestProfileImageUrl = null
            ),
            TrackerData(
                id = 2L,
                bookTitle = "아몬드",
                bookAuthor = "손원평",
                bookCategory = "청소년 문학",
                withUserName = "guest1",
                coverImageUrl = null,
                exchangeType = ExchangeType.SHIPPING,
                stepDates = listOf("2024.01.01", "2024.01.05", null, null),
                currentStep = TrackerStep.SHIPPING,
                hostProfileImageUrl = null,
                guestProfileImageUrl = null
            )
        )
    }


    private fun normalizeStepDates(raw: List<String?>?): List<String?> {
        val list = raw.orEmpty()
        return List(4) { idx -> list.getOrNull(idx) }
    }

    private fun computeCurrentStep(stepDates: List<String?>): TrackerStep {
        val lastFilled = stepDates.indexOfLast { !it.isNullOrBlank() }
        return when (lastFilled) {
            0 -> TrackerStep.HOST_READING
            1 -> TrackerStep.SHIPPING
            2 -> TrackerStep.GUEST_READING
            3 -> TrackerStep.RETURNING
            else -> TrackerStep.HOST_READING
        }
    }

    private fun mapExchangeType(groupType: String): ExchangeType {
        return when (groupType.uppercase()) {
            "DIRECT" -> ExchangeType.DIRECT
            "SHIPPING" -> ExchangeType.SHIPPING
            "RELAY" -> ExchangeType.SHIPPING
            "TOGETHER" -> ExchangeType.DIRECT
            else -> ExchangeType.SHIPPING
        }
    }
}
