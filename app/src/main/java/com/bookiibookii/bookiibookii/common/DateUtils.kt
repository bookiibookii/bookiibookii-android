package com.bookiibookii.bookiibookii.common

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    // 1. UTC 서버 시간을 "yyyy. MM. dd." 형식으로 변환
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val format = if (dateString.contains(".")) "yyyy-MM-dd'T'HH:mm:ss.SSS" else "yyyy-MM-dd'T'HH:mm:ss"
            val parser = SimpleDateFormat(format, Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(dateString) ?: return dateString

            val formatter = SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault())
            formatter.timeZone = TimeZone.getDefault()
            formatter.format(date)
        } catch (e: Exception) {
            dateString ?: ""
        }
    }

    // 2. 채팅이나 댓글용 "방금 전", "N시간 전" 변환
    fun calculateTimeAgo(serverTime: String?): String {
        if (serverTime.isNullOrEmpty()) return ""
        return try {
            val format = if (serverTime.contains(".")) "yyyy-MM-dd'T'HH:mm:ss.SSS" else "yyyy-MM-dd'T'HH:mm:ss"
            val parser = SimpleDateFormat(format, Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(serverTime) ?: return serverTime.substring(0, 10)

            val now = System.currentTimeMillis()
            val diff = now - date.time

            val minutes = diff / (1000 * 60)
            val hours = minutes / 60

            when {
                minutes < 1 -> "방금 전"
                minutes < 60 -> "${minutes}분 전"
                hours < 24 -> "${hours}시간 전"
                else -> {
                    val formatter = SimpleDateFormat("yyyy. MM. dd", Locale.getDefault())
                    formatter.timeZone = TimeZone.getDefault()
                    formatter.format(date)
                }
            }
        } catch (e: Exception) {
            if (serverTime.length >= 10) serverTime.substring(0, 10).replace("-", ".") else serverTime
        }
    }
}