package com.bookiibookii.bookiibookii.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val formatter = DateTimeFormatter
        .ofPattern("yyyy. MM. dd.", Locale.US)
        .withZone(ZoneId.systemDefault())

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "0000. 00. 00."
        return try {
            formatter.format(Instant.parse(dateString))
        } catch (e: Exception) {
            "0000. 00. 00."
        }
    }

    fun calculateTimeAgo(serverTime: String?): String {
        if (serverTime.isNullOrEmpty()) return ""
        return try {
            val instant = Instant.parse(serverTime)
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