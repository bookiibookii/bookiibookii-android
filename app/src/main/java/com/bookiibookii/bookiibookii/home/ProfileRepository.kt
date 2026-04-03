package com.bookiibookii.bookiibookii.home

import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.OtherProfileApiResult
import com.bookiibookii.bookiibookii.data.model.ProfileResult

class ProfileRepository {

    private val api = RetrofitClient.api()

    suspend fun getUserProfile(nickname: String): ProfileResult? {
        val response = api.getUserProfile(nickname)
        val body = response.body()

        if (!response.isSuccessful || body?.isSuccess != true) return null

        val r: OtherProfileApiResult = body.result ?: return null

        return ProfileResult(
            userId = r.userId,
            profileImageUrl = r.profileImageUrl,
            nickname = r.nickname,
            manner = r.manner,
            topTags = r.topTags.orEmpty(),
            completeBook = r.completeBook,
            readingGroup = r.readingGroup,
            togetherGroup = r.togetherGroup,
            userBadges = r.userBadges.orEmpty(),
            groups = r.groups.orEmpty(),
            books = r.books.orEmpty()
        )
    }
}