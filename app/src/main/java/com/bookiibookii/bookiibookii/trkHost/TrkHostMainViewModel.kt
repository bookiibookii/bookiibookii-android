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
                // 필요하면 에러 상태 Flow 추가
            }
        }
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
