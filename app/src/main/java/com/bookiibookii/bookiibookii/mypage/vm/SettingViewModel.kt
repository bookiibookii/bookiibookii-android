package com.bookiibookii.bookiibookii.mypage.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.mypage.FaqItem
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryRequest
import com.bookiibookii.bookiibookii.data.model.mypage.InquirySummary
import com.bookiibookii.bookiibookii.data.model.mypage.NoticeDetail
import com.bookiibookii.bookiibookii.data.model.mypage.NoticeSummary
import com.bookiibookii.bookiibookii.data.model.mypage.WithdrawalReqDTO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SettingViewModel : ViewModel() {

    private val _notices = MutableLiveData<List<NoticeSummary>>(emptyList())
    val notices: LiveData<List<NoticeSummary>> get() = _notices

    private val _noticeDetail = MutableLiveData<NoticeDetail?>(null)
    val noticeDetail: LiveData<NoticeDetail?> get() = _noticeDetail

    private val _withdrawFailed = MutableLiveData(false)
    val withdrawFailed: LiveData<Boolean> get() = _withdrawFailed

    private val _faqItems = MutableLiveData<List<FaqItem>>(emptyList())
    val faqItems: LiveData<List<FaqItem>> get() = _faqItems

    private val _inquiries = MutableLiveData<List<InquirySummary>>(emptyList())
    val inquiries: LiveData<List<InquirySummary>> get() = _inquiries

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class Event {
        data class ShowToast(val message: String, val isSuccess: Boolean = false) : Event()
        object InquirySuccess : Event()
        object WithdrawSuccess : Event()
    }

    // 문의 등록/회원탈퇴 진행 중 여부 — 더블탭으로 중복 등록/중복 탈퇴 요청을 차단
    private var mutating = false

    fun fetchNotices() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().getNoticeList()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _notices.value = response.body()?.result ?: emptyList()
                } else {
                    _eventFlow.emit(Event.ShowToast("공지사항을 불러오지 못했습니다."))
                }
            } catch (e: Exception) {
                Log.e("SettingViewModel", "fetchNotices error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun fetchNoticeDetail(noticeId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().getNoticeDetail(noticeId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _noticeDetail.value = response.body()?.result
                } else {
                    Log.e("SettingViewModel", "fetchNoticeDetail failed: code=${response.code()} body=${response.body()}")
                    _eventFlow.emit(Event.ShowToast("공지사항 내용을 불러오지 못했습니다."))
                }
            } catch (e: Exception) {
                Log.e("SettingViewModel", "fetchNoticeDetail error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun fetchFaq() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().getFaq()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _faqItems.value = response.body()?.result ?: emptyList()
                } else {
                    Log.e("SettingViewModel", "fetchFaq failed: code=${response.code()} body=${response.body()}")
                    _eventFlow.emit(Event.ShowToast("자주 묻는 질문을 불러오지 못했습니다."))
                }
            } catch (e: Exception) {
                Log.e("SettingViewModel", "fetchFaq error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun fetchInquiries() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().getInquiryList()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _inquiries.value = response.body()?.result ?: emptyList()
                } else {
                    _eventFlow.emit(Event.ShowToast("문의 목록을 불러오지 못했습니다."))
                }
            } catch (e: Exception) {
                Log.e("SettingViewModel", "fetchInquiries error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            }
        }
    }

    fun postInquiry(title: String, content: String) {
        if (mutating) return
        mutating = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().postInquiry(InquiryRequest(title = title, content = content))
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.InquirySuccess)
                } else {
                    _eventFlow.emit(Event.ShowToast("문의 등록에 실패했습니다."))
                }
            } catch (e: Exception) {
                Log.e("SettingViewModel", "postInquiry error", e)
                _eventFlow.emit(Event.ShowToast("네트워크 오류가 발생했습니다."))
            } finally {
                mutating = false
            }
        }
    }

    fun withdraw(reason: String, customReason: String?) {
        if (mutating) return
        mutating = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.mypApi().withdraw(
                    WithdrawalReqDTO(reason = reason, customReason = customReason)
                )
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _eventFlow.emit(Event.WithdrawSuccess)
                } else {
                    _withdrawFailed.value = true
                }
            } catch (e: Exception) {
                Log.e("SettingViewModel", "withdraw error", e)
                _withdrawFailed.value = true
            } finally {
                mutating = false
            }
        }
    }

    fun clearWithdrawFailed() {
        _withdrawFailed.value = false
    }
}
