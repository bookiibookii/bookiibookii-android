package com.bookiibookii.bookiibookii.common

import java.time.Instant
import java.time.LocalDate
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
        Instant.parse(serverTime)                                          // ISO-8601 with Z
    } catch (_: Exception) { try {
        LocalDateTime.parse(serverTime).toInstant(ZoneOffset.UTC)         // yyyy-MM-ddTHH:mm:ss
    } catch (_: Exception) {
        java.time.LocalDate.parse(serverTime).atStartOfDay().toInstant(ZoneOffset.UTC) // yyyy-MM-dd
    } }

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""
        // 서버가 이미 "yyyy. MM. dd." 형태로 보내는 경우 그대로 반환
        if (dateString.matches(Regex("\\d{4}\\. \\d{2}\\. \\d{2}\\."))) return dateString
        return try {
            formatter.format(parseInstant(dateString))
        } catch (e: Exception) {
            // 파싱 실패 시 원본 문자열 반환 (빈 문자열보다 낫기 때문)
            dateString
        }
    }

    fun calculateTimeAgo(serverTime: String?): String {
        if (serverTime.isNullOrEmpty()) return ""
        return try {
            val instant = parseInstant(serverTime)
            val diff = System.currentTimeMillis() - instant.toEpochMilli()
            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24
            when {
                minutes < 1 -> "방금 전"
                minutes < 60 -> "${minutes}분 전"
                hours < 24 -> "${hours}시간 전"
                days < 7 -> "${days}일 전"
                else -> formatter.format(instant)
            }
        } catch (e: Exception) {
            ""
        }
    }
}