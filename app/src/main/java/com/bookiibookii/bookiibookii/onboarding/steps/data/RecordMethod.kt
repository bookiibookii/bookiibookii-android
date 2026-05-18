package com.bookiibookii.bookiibookii.onboarding.steps.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bookiibookii.bookiibookii.R

// TODO: drawable 다 처리 좀. 이상한 거로 넣어둠
enum class RecordMethod(
    @StringRes val titleResId: Int,
    val serverValue: String,
    val tagLabel: String,
    @DrawableRes val iconRes: Int
) {
    MEMO(
        titleResId = R.string.onb_step2_record_memo,
        serverValue = "MEMO",
        tagLabel = "#메모환영",
        iconRes = R.drawable.ic_up
    ),
    POSTIT(
        titleResId = R.string.onb_step2_record_postit,
        serverValue = "POSTIT",
        tagLabel = "#포스트잇",
        iconRes = R.drawable.ic_up
    ),
    PHOTO(
        titleResId = R.string.onb_step2_record_photo,
        serverValue = "CLEAN",
        tagLabel = "#깔끔",
        iconRes = R.drawable.ic_up
    ),
    FOCUS(
        titleResId = R.string.onb_step2_record_focus,
        serverValue = "CLEAN",
        tagLabel = "#깔끔",
        iconRes = R.drawable.ic_up
    );

    companion object {
        fun fromServerValue(value: String): RecordMethod? {
            return entries.firstOrNull { it.serverValue == value }
        }
    }
}