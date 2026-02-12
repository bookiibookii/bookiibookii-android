package com.bookiibookii.bookiibookii.home

import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.MypageResult
import com.bookiibookii.bookiibookii.data.model.ProfileResult

class ProfileRepository {

    private val api = RetrofitClient.api()

    suspend fun getUserProfile(nickname: String): ProfileResult? {
        val response = api.getUserProfile(nickname)
        val body = response.body()

        if (!response.isSuccessful || body?.isSuccess != true) return null

        val r: MypageResult = body.result ?: return null

        return ProfileResult(
            userId = r.userId,
            profileImageUrl = null,          // MypageResult에는 URL이 없음 (s3Key만 있음)
            nickname = r.nickname,
            manner = r.manner,
            topTags = r.topTags,             // 널 아님 → ?: emptyList() 쓰면 안 됨
            completeBook = r.completeBook,
            readingGroup = r.relayGroup,     // 여기 중요: relayGroup -> readingGroup
            togetherGroup = r.togetherGroup
        )
    }
}