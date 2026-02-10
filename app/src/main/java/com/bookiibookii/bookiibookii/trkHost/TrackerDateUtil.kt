package com.bookiibookii.bookiibookii.trkHost

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TrackerDateUtil {

    private val outputFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy.MM.dd")

    fun prettyDate(raw: String?): String {
        val date = parseLocalDate(raw) ?: return "미정"
        return date.format(outputFormatter)
    }

    fun parseLocalDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        return runCatching { LocalDate.parse(raw.take(10)) }.getOrNull()
    }

    fun extendedEndDateText(endRaw: String?, days: Int): String {
        val base = parseLocalDate(endRaw) ?: return "미정"
        val safeDays = if (days > 0) days else 0
        return base.plusDays(safeDays.toLong()).format(outputFormatter)
    }
}