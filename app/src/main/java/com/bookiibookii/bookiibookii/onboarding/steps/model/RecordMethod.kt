package com.bookiibookii.bookiibookii.onboarding.steps.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.bookiibookii.bookiibookii.R

enum class RecordMethod(
    @StringRes val titleResId: Int,
    val serverValue: String,
    @DrawableRes val iconRes: Int
) {
    MEMO(
        titleResId = R.string.onb_step3_record_memo,
        serverValue = "MEMO",
        iconRes = R.drawable.ic_edit
    ),
    POSTIT(
        titleResId = R.string.onb_step3_record_postit,
        serverValue = "POSTIT",
        iconRes = R.drawable.ic_bookmark
    ),
    PHOTO(
        titleResId = R.string.onb_step3_record_photo,
        serverValue = "PHOTO",
        iconRes = R.drawable.ic_camera
    ),
    ANY(
        titleResId = R.string.onb_step3_record_any,
        serverValue = "All_ROUNDER",
        iconRes = R.drawable.ic_book
    );
}
