package com.bookiibookii.bookiibookii.onboarding.steps.model

import android.net.Uri
import com.bookiibookii.bookiibookii.data.model.group.BookItem

data class OnbState(
    // Step 1: 프로필
    val nickname: String = "",
    val gender: String? = null,
    val birthdate: String? = null,
    val profileUri: Uri? = null,
    val profileS3Key: String? = null,
    // Step 2: 인생 책 (3슬롯)
    val lifeBooks: List<BookItem?> = listOf(null, null, null),
    // Step 3: 기록 방식
    val recordMethods: Set<RecordMethod> = emptySet(),
    val isUnknownMethod: Boolean = false,
    // Step 4: 나를 표현하는 한 문장 (선택)
    val selfIntro: String = ""
)
