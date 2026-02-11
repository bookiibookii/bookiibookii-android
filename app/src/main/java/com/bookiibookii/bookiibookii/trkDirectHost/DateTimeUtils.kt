package com.bookiibookii.bookiibookii.trkDirectHost

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtils {

    private val outputFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy.MM.dd")
    
    fun formatMeetingTime(
        raw: String?,
        fallback: String = "-",
        zoneId: ZoneId = ZoneId.systemDefault(),
        formatter: DateTimeFormatter = outputFormatter
    ): String {
        if (raw.isNullOrBlank()) return fallback

        return try {
            val localDt: LocalDateTime =
                if (raw.endsWith("Z")) {
                    OffsetDateTime.parse(raw)
                        .atZoneSameInstant(zoneId)
                        .toLocalDateTime()
                } else {
                    LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                }

            localDt.format(formatter)
        } catch (_: Exception) {
            fallback
        }
    }
}