package com.bookiibookii.bookiibookii.home.notification.ui

import com.bookiibookii.bookiibookii.data.model.NotificationItemDto
import org.json.JSONObject

object NotificationPayloadParser {

    fun getGroupId(item: NotificationItemDto): Long? {
        val payload = item.payload ?: return null

        if (payload is Map<*, *>) {
            return toLong(payload["groupId"])
        }

        // payload가 String(JSON)로 오는 케이스 대비
        return try {
            val obj = JSONObject(payload.toString())
            if (obj.has("groupId")) obj.getLong("groupId") else null
        } catch (e: Exception) {
            null
        }
    }

    private fun toLong(value: Any?): Long? {
        return when (value) {
            is Int -> value.toLong()
            is Long -> value
            is Double -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }
}