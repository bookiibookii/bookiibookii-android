package com.bookiibookii.bookiibookii.home.notification.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TimeAgoFormatter {

    // 서버 createdAt 형식에 맞게 수정
    private val serverFormatter = DateTimeFormatter.ISO_DATE_TIME

    fun format(createdAt: String): String {
        return try {
            val createdTime = LocalDateTime.parse(createdAt, serverFormatter)
            val now = LocalDateTime.now()

            val duration = Duration.between(createdTime, now)

            val minutes = duration.toMinutes()
            val hours = duration.toHours()
            val days = duration.toDays()

            when {
                minutes < 1 -> "방금 전"
                minutes < 60 -> "${minutes}분 전"
                hours < 24 -> "${hours}시간 전"
                days < 7 -> "${days}일 전"
                else -> createdTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            }
        } catch (e: Exception) {
            createdAt // 파싱 실패 시 원본 반환
        }
    }
}