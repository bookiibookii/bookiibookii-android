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

    // [1] 그룹 상세 데이터
    private val _groupDetail = MutableLiveData<GroupItemDto.GroupDetailResult?>()
    val groupDetail: LiveData<GroupItemDto.GroupDetailResult?> get() = _groupDetail

    // [2] 댓글 작성 성공 여부
    private val _commentWriteSuccess = MutableLiveData<Boolean>()
    val commentWriteSuccess: LiveData<Boolean> get() = _commentWriteSuccess

    // [3] 에러 메시지
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // --- 그룹 상세 조회 ---
    fun fetchGroupDetail(groupId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getGroupDetail(groupId)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _groupDetail.value = response.body()!!.result
                    Log.d("GroupDetailVM", "데이터 로드 성공")
                } else {
                    Log.e("GroupDetailVM", "데이터 로드 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GroupDetailVM", "네트워크 오류", e)
            }
        }
    }

    // --- 댓글 작성 ---
    fun postComment(groupId: Long, content: String, parentId: Long? = null, secret : Boolean) {
        viewModelScope.launch {
            try {
                // parentId가 있으면 답글 요청, 없으면 일반 댓글 요청
                // (서버 API 스펙에 따라 Request 객체 생성 부분이 달라질 수 있음)
                val request = GroupItemDto.CommentCreateRequest(
                    content = content,
                    parentId = parentId, // ★ 서버로 이 값을 보내야 함
                    secret = secret
                )
                val response = RetrofitClient.api().postComment(groupId, request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _commentWriteSuccess.value = true
                } else {
                    // 에러 처리
                }
            } catch (e: Exception) {
                // 예외 처리
            }
        }
    }

    private val _commentList = MutableLiveData<List<GroupItemDto.CommentItem>?>()
    val commentList: LiveData<List<GroupItemDto.CommentItem>?> get() = _commentList

    // --- 댓글 목록 조회 ---
    fun fetchComments(groupId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api().getComments(groupId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 성공 시 데이터 저장
                    _commentList.value = response.body()!!.result
                } else {
                    Log.e("GroupDetailVM", "댓글 조회 실패")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}