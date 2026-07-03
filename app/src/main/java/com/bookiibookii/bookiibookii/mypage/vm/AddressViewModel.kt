package com.bookiibookii.bookiibookii.mypage.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddress
import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddressRequest
import com.bookiibookii.bookiibookii.data.model.location.ExchangeAddress
import com.bookiibookii.bookiibookii.data.model.location.ExchangeAddressRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AddressViewModel : ViewModel() {

    private val _deliveries = MutableLiveData<List<DeliveryAddress>>(emptyList())
    val deliveries: LiveData<List<DeliveryAddress>> get() = _deliveries

    private val _exchanges = MutableLiveData<List<ExchangeAddress>>(emptyList())
    val exchanges: LiveData<List<ExchangeAddress>> get() = _exchanges

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class Event {
        data class ShowToast(val message: String, val isSuccess: Boolean = false) : Event()
    }

    // 주소 추가/수정 진행 중 여부. 폼에서 하나씩만 제출되므로 단일 플래그로 더블탭 중복 제출을 차단.
    private var mutating = false

    private suspend fun loadDeliveries(): List<DeliveryAddress> = try {
        val response = RetrofitClient.locationApi().getDeliveries()
        if (response.isSuccessful && response.body()?.isSuccess == true) {
            val list = response.body()?.result ?: emptyList()
            _deliveries.value = list
            list
        } else {
            Log.w("AddressViewModel", "[loadDeliveries] 실패: ${response.body()?.message}")
            _eventFlow.emit(Event.ShowToast("배송지 목록을 불러오지 못했습니다."))
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("AddressViewModel", "[loadDeliveries] 예외", e)
        _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
        emptyList()
    }

    private suspend fun loadExchanges(): List<ExchangeAddress> = try {
        val response = RetrofitClient.locationApi().getExchanges()
        if (response.isSuccessful && response.body()?.isSuccess == true) {
            val list = response.body()?.result ?: emptyList()
            _exchanges.value = list
            list
        } else {
            Log.w("AddressViewModel", "[loadExchanges] 실패: ${response.body()?.message}")
            _eventFlow.emit(Event.ShowToast("교환 장소 목록을 불러오지 못했습니다."))
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("AddressViewModel", "[loadExchanges] 예외", e)
        _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
        emptyList()
    }

    fun fetchDeliveries() { viewModelScope.launch { loadDeliveries() } }
    fun fetchExchanges() { viewModelScope.launch { loadExchanges() } }

    fun addDelivery(request: DeliveryAddressRequest, makeDefault: Boolean, onSuccess: () -> Unit) {
        if (mutating) return
        mutating = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().addDelivery(request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = loadDeliveries()
                    if (makeDefault) {
                        val added = list.firstOrNull {
                            it.placeName == request.placeName && it.address == request.address && it.phone == request.phone
                        }
                        if (added != null && !added.isDefault) applyDefaultDelivery(added.id)
                    }
                    onSuccess()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "배송지 추가에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "addDelivery error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            } finally {
                mutating = false
            }
        }
    }

    fun updateDelivery(id: Long, request: DeliveryAddressRequest, makeDefault: Boolean, onSuccess: () -> Unit) {
        if (mutating) return
        mutating = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().updateDelivery(id, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    if (makeDefault) applyDefaultDelivery(id) else loadDeliveries()
                    onSuccess()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "배송지 수정에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "updateDelivery error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            } finally {
                mutating = false
            }
        }
    }

    fun deleteDelivery(id: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().deleteDelivery(id)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    loadDeliveries()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "배송지 삭제에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "deleteDelivery error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    private suspend fun applyDefaultDelivery(id: Long) {
        try {
            val response = RetrofitClient.locationApi().setDefaultDelivery(id)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                loadDeliveries()
            } else {
                _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "대표 배송지 설정에 실패했습니다."))
            }
        } catch (e: Exception) {
            Log.e("AddressViewModel", "setDefaultDelivery error", e)
            _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
        }
    }

    fun addExchange(request: ExchangeAddressRequest, makeDefault: Boolean, onSuccess: () -> Unit) {
        if (mutating) return
        mutating = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().addExchange(request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val list = loadExchanges()
                    if (makeDefault) {
                        val added = list.firstOrNull {
                            it.placeName == request.placeName && it.address == request.address
                        }
                        if (added != null && !added.isDefault) applyDefaultExchange(added.id)
                    }
                    onSuccess()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "교환 장소 추가에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "addExchange error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            } finally {
                mutating = false
            }
        }
    }

    fun updateExchange(id: Long, request: ExchangeAddressRequest, makeDefault: Boolean, onSuccess: () -> Unit) {
        if (mutating) return
        mutating = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().updateExchange(id, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    if (makeDefault) applyDefaultExchange(id) else loadExchanges()
                    onSuccess()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "교환 장소 수정에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "updateExchange error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            } finally {
                mutating = false
            }
        }
    }

    fun deleteExchange(id: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().deleteExchange(id)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    loadExchanges()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "교환 장소 삭제에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "deleteExchange error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    private suspend fun applyDefaultExchange(id: Long) {
        try {
            val response = RetrofitClient.locationApi().setDefaultExchange(id)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                loadExchanges()
            } else {
                _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "대표 교환 장소 설정에 실패했습니다."))
            }
        } catch (e: Exception) {
            Log.e("AddressViewModel", "setDefaultExchange error", e)
            _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
        }
    }
}
