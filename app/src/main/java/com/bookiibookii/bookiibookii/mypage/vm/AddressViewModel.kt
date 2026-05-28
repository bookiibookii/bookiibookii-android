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
        data class ShowToast(val message: String) : Event()
    }

    fun fetchDeliveries() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().getDeliveries()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _deliveries.value = response.body()?.result ?: emptyList()
                } else {
                    _eventFlow.emit(Event.ShowToast("배송지 목록을 불러오지 못했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "fetchDeliveries error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun fetchExchanges() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().getExchanges()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _exchanges.value = response.body()?.result ?: emptyList()
                } else {
                    _eventFlow.emit(Event.ShowToast("교환 장소 목록을 불러오지 못했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "fetchExchanges error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun addDelivery(request: DeliveryAddressRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().addDelivery(request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchDeliveries()
                    onSuccess()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "배송지 추가에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "addDelivery error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun updateDelivery(id: Long, request: DeliveryAddressRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().updateDelivery(id, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchDeliveries()
                    onSuccess()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "배송지 수정에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "updateDelivery error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun deleteDelivery(id: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().deleteDelivery(id)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchDeliveries()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "배송지 삭제에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "deleteDelivery error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun addExchange(request: ExchangeAddressRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().addExchange(request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchExchanges()
                    onSuccess()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "교환 장소 추가에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "addExchange error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun updateExchange(id: Long, request: ExchangeAddressRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().updateExchange(id, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchExchanges()
                    onSuccess()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "교환 장소 수정에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "updateExchange error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun deleteExchange(id: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.locationApi().deleteExchange(id)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    fetchExchanges()
                } else {
                    _eventFlow.emit(Event.ShowToast(response.body()?.message ?: "교환 장소 삭제에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("AddressViewModel", "deleteExchange error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }
}
