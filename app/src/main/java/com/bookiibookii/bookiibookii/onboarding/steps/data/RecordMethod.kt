package com.bookiibookii.bookiibookii.onboarding.steps.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bookiibookii.bookiibookii.R

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
        iconRes = R.drawable.ic_onb_record_pen
    ),
    POST_IT(
        titleResId = R.string.onb_step2_record_postit,
        serverValue = "POST_IT",
        tagLabel = "#포스트잇",
        iconRes = R.drawable.ic_onb_record_postit
    ),
    PHOTO(
        titleResId = R.string.onb_step2_record_photo,
        serverValue = "PHOTO",
        tagLabel = "#깔끔",
        iconRes = R.drawable.ic_onb_record_camera
    ),
    FOCUS(
        titleResId = R.string.onb_step2_record_focus,
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