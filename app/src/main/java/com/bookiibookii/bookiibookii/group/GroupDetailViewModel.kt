package com.bookiibookii.bookiibookii.group.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import kotlinx.coroutines.launch
import org.json.JSONObject

class GroupDetailViewModel : ViewModel() {


    // 그룹 상세 데이터
    private val _groupDetail = MutableLiveData<GroupItemDto.GroupDetailResult?>()
    val groupDetail: LiveData<GroupItemDto.GroupDetailResult?> get() = _groupDetail

    // 댓글 목록 데이터
    private val _commentList = MutableLiveData<List<GroupItemDto.CommentItem>?>()
    val commentList: LiveData<List<GroupItemDto.CommentItem>?> get() = _commentList

    // 상태 알림 이벤트 (작성 성공, 삭제 성공, 에러 메시지)
    private val _commentWriteSuccess = MutableLiveData<Boolean>()
    val commentWriteSuccess: LiveData<Boolean> get() = _commentWriteSuccess

    private val _commentDeleteEvent = MutableLiveData<Boolean>()
    val commentDeleteEvent: LiveData<Boolean> = _commentDeleteEvent

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage


    // Business Logic - Fetch


    // 그룹 상세 정보 조회

    fun fetchGroupDetail(groupId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getGroupDetail(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _groupDetail.value = response.body()!!.result
                    Log.d("GroupDetailVM", "그룹 데이터 로드 성공")
                } else {
                    Log.e("GroupDetailVM", "그룹 데이터 로드 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "네트워크 오류", e)
                _errorMessage.value = "데이터를 불러오는 중 오류가 발생했습니다."
            }
        }
    }


     //댓글 목록 조회
    fun fetchComments(groupId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getComments(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _commentList.value = response.body()!!.result
                } else {
                    Log.e("GroupDetailVM", "댓글 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "댓글 조회 중 네트워크 오류", e)
            }
        }
    }


    // Business Logic - Mutation


     //댓글/답글 작성
     //@param parentId 값이 있으면 답글, 없으면 일반 댓글로 처리
    fun postComment(groupId: Long, content: String, parentId: Long? = null, secret: Boolean) {
        viewModelScope.launch {
            try {
                val request = GroupItemDto.CommentCreateRequest(
                    content = content,
                    parentId = parentId,
                    secret = secret
                )
                val response = RetrofitClient.api().postComment(groupId, request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _commentWriteSuccess.value = true
                } else {
                    // 서버 에러 응답 처리 (예: 비속어 포함 등)
                    val errorMsg = try {
                        JSONObject(response.errorBody()?.string() ?: "{}").optString("message", "댓글 작성 실패")
                    } catch (e: Exception) { "댓글 작성 실패" }
                    _errorMessage.value = errorMsg
                    _commentWriteSuccess.value = false
                }
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "댓글 전송 오류", e)
                _errorMessage.value = "네트워크 연결을 확인해주세요."
                _commentWriteSuccess.value = false
            }
        }
    }

     // 댓글 삭제

    fun deleteComment(groupId: Int, commentId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().deleteComment(groupId, commentId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.isSuccess) {
                        _commentDeleteEvent.value = true
                        // 삭제 성공 후 목록 새로고침하여 UI 동기화
                        fetchComments(groupId.toLong())
                        Log.d("GroupDetailVM", "댓글 삭제 성공: $commentId")
                    } else {
                        val errorMsg = body?.message ?: "삭제 권한이 없습니다."
                        _errorMessage.value = errorMsg
                        _commentDeleteEvent.value = false
                    }
                } else {
                    Log.e("GroupDetailVM", "HTTP 오류 - Code: ${response.code()}")
                    _commentDeleteEvent.value = false
                }
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "삭제 중 네트워크 예외 발생", e)
                _errorMessage.value = "네트워크 오류가 발생했습니다."
                _commentDeleteEvent.value = false
            }
        }
    }

}