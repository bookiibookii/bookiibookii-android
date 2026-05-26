package com.bookiibookii.bookiibookii.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val formatter = DateTimeFormatter
        .ofPattern("yyyy. MM. dd.", Locale.US)
        .withZone(ZoneId.systemDefault())

    // 서버 시간 문자열 → Instant
    // "...Z"/offset 있으면 Instant.parse, 없으면 UTC LocalDateTime으로 간주
    private fun parseInstant(serverTime: String): Instant = try {
        Instant.parse(serverTime)
    } catch (e: Exception) {
        LocalDateTime.parse(serverTime).toInstant(ZoneOffset.UTC)
    }

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "0000. 00. 00."
        return try {
            formatter.format(parseInstant(dateString))
        } catch (e: Exception) {
            "0000. 00. 00."
        }
    }

    fun calculateTimeAgo(serverTime: String?): String {
        if (serverTime.isNullOrEmpty()) return ""
        return try {
            val instant = parseInstant(serverTime)
            val diff = System.currentTimeMillis() - instant.toEpochMilli()
            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            when {
                minutes < 1 -> "방금 전"
                minutes < 60 -> "${minutes}분 전"
                hours < 24 -> "${hours}시간 전"
                else -> formatter.format(instant)
            }
        } catch (e: Exception) {
            ""
        }
    }
}