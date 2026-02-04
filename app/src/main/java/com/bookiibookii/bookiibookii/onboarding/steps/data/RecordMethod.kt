package com.bookiibookii.bookiibookii.onboarding.steps.data

import androidx.annotation.DrawableRes
import com.bookiibookii.bookiibookii.R

enum class RecordMethod(
    val displayName: String,
    val serverValue: String,
    val tagLabel: String,
    @DrawableRes val iconRes: Int
) {
    MEMO(
        displayName = "펜으로 밑줄을 긋고 메모해요!",
        serverValue = "MEMO",
        tagLabel = "#메모환영",
        iconRes = R.drawable.ic_onb_record_pen
    ),
    POST_IT(
        displayName = "포스트잇이나 인덱스를 활용해요!",
        serverValue = "POST_IT",
        tagLabel = "#포스트잇",
        iconRes = R.drawable.ic_onb_record_postit
    ),
    PHOTO(
        displayName = "사진을 찍어 기록해요!",
        serverValue = "PHOTO",
        tagLabel = "#깔끔",
        iconRes = R.drawable.ic_onb_record_camera
    ),
    FOCUS(
        displayName = "기록보다는 읽는 것에 집중해요!",
        serverValue = "FOCUS",
        tagLabel = "#깔끔",
        iconRes = R.drawable.ic_onb_record_focus
    );

    companion object {
        fun fromServerValue(value: String): RecordMethod? {
            return entries.firstOrNull { it.serverValue == value }
        }
    }
}