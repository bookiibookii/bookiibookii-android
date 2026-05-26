package com.bookiibookii.bookiibookii.data.model.group

import com.google.gson.annotations.SerializedName

// DELETE /api/groups/{groupId}/apply 응답 result
data class GroupCancelResponse(
    @SerializedName("groupId") val groupId: Long?,
    @SerializedName("canceledAt") val canceledAt: String?,
)
