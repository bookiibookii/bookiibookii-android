package com.bookiibookii.bookiibookii.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val formatter = DateTimeFormatter
        .ofPattern("yyyy. MM. dd.", Locale.US)
        .withZone(ZoneId.systemDefault())

    // 직접교환 약속 시간 전용
    // 요청: offset 포함(+09:00), 응답: UTC Z. OffsetDateTime.parse가 둘 다 동일 instant로 파싱.
    private val KST = ZoneId.of("Asia/Seoul")
    private val MEETING_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm", Locale.US)

    // 서버 응답(Z)/요청형(+09:00)/offset 없는 문자열 모두 받아 KST "yyyy. MM. dd. HH:mm" 로 표시
    fun formatKstDateTime(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return try {
            OffsetDateTime.parse(raw).atZoneSameInstant(KST).format(MEETING_FORMATTER)
        } catch (_: Exception) { try {
            LocalDateTime.parse(raw).atZone(KST).format(MEETING_FORMATTER) // offset 없으면 KST 벽시계 간주
        } catch (_: Exception) {
            raw
        } }
    }

    // 피커가 고른 KST 벽시계 LocalDateTime → offset 포함 요청 문자열(예: 2026-05-20T14:30:00+09:00)
    fun meetingAtFromKst(local: LocalDateTime): String =
        local.atZone(KST).toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    // 서버 응답(Z)/offset 문자열 → 수정 모드 프리필용 KST LocalDateTime (실패 시 null)
    fun parseKstLocalDateTime(raw: String?): LocalDateTime? {
        if (raw.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(raw).atZoneSameInstant(KST).toLocalDateTime()
        } catch (_: Exception) {
            runCatching { LocalDateTime.parse(raw) }.getOrNull()
        }
    }

    // 약속 시각 → Instant (수정 가능 여부 판정용, 실패 시 null)
    fun toInstantOrNull(raw: String?): Instant? {
        if (raw.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(raw).toInstant()
        } catch (_: Exception) {
            runCatching { LocalDateTime.parse(raw).atZone(KST).toInstant() }.getOrNull()
        }
    }

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