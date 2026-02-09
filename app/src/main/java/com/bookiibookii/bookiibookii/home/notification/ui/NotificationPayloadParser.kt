package com.bookiibookii.bookiibookii.home.notification.util

import com.bookiibookii.bookiibookii.data.model.NotificationItemDto
import org.json.JSONObject

object NotificationPayloadParser {

    fun getGroupId(item: NotificationItemDto): Long? {
        val payload = item.payload ?: return null

        // payload가 Map으로 잘 들어온 경우
        if (payload is Map<*, *>) {
            return toLong(payload["groupId"])
        }

        // 혹시 문자열/기타로 오는 경우 대비
        return try {
            val obj = JSONObject(payload.toString())
            if (obj.has("groupId")) obj.getLong("groupId") else null
        } catch (e: Exception) {
            null
        }
    }

    // TODO: 아래는 스펙 확정되면 사용 (문의/신고/공지/댓글 등)
    fun getInquiryId(item: NotificationItemDto): Long? {
        val payload = item.payload ?: return null
        if (payload is Map<*, *>) return toLong(payload["inquiryId"])
        return try {
            val obj = JSONObject(payload.toString())
            if (obj.has("inquiryId")) obj.getLong("inquiryId") else null
        } catch (e: Exception) {
            null
        }
    }

    fun getReportId(item: NotificationItemDto): Long? {
        val payload = item.payload ?: return null
        if (payload is Map<*, *>) return toLong(payload["reportId"])
        return try {
            val obj = JSONObject(payload.toString())
            if (obj.has("reportId")) obj.getLong("reportId") else null
        } catch (e: Exception) {
            null
        }
    }

    fun getNoticeId(item: NotificationItemDto): Long? {
        val payload = item.payload ?: return null
        if (payload is Map<*, *>) return toLong(payload["noticeId"])
        return try {
            val obj = JSONObject(payload.toString())
            if (obj.has("noticeId")) obj.getLong("noticeId") else null
        } catch (e: Exception) {
            null
        }
    }

    fun getCardId(item: NotificationItemDto): Long? {
        val payload = item.payload ?: return null
        if (payload is Map<*, *>) return toLong(payload["cardId"])
        return try {
            val obj = JSONObject(payload.toString())
            if (obj.has("cardId")) obj.getLong("cardId") else null
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