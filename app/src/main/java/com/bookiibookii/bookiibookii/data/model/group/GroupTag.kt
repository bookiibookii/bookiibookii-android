package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

data class GroupTagRequest(
    @SerializedName("type") val type: String,
    @SerializedName("value") val value: List<String>
)
