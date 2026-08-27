package com.bookiibookii.bookiibookii.common

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

class DateUtilsTest {
    private lateinit var originalTz: TimeZone

    @Before fun setUp() {
        // DateUtils.formatter가 ZoneId.systemDefault()를 쓰므로 KST 고정
        originalTz = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    }

    @After fun tearDown() = TimeZone.setDefault(originalTz)

    // --- formatDate: 3단계 파싱 폴백 ---
    @Test fun `Z 오프셋 인스턴트 파싱`() =
        assertEquals("2026. 05. 21.", DateUtils.formatDate("2026-05-20T16:00:00Z")) // UTC 16시 = KST 익일 01시

    @Test fun `오프셋 없는 datetime은 UTC로 간주`() =
        assertEquals("2026. 05. 21.", DateUtils.formatDate("2026-05-20T16:00:00"))

    @Test fun `날짜만 있으면 UTC 자정 기준`() =
        assertEquals("2026. 05. 20.", DateUtils.formatDate("2026-05-20"))

    @Test fun `이미 포맷된 문자열은 그대로`() =
        assertEquals("2026. 05. 20.", DateUtils.formatDate("2026. 05. 20."))

    @Test fun `파싱 불가면 원본 반환`() =
        assertEquals("notadate", DateUtils.formatDate("notadate"))

    @Test fun `null과 blank는 빈 문자열`() {
        assertEquals("", DateUtils.formatDate(null))
        assertEquals("", DateUtils.formatDate("  "))
    }

    // --- calculateTimeAgo: 경계값 (실제 시각 기준 상대 오프셋, 5초 여유) ---
    private fun ago(d: Duration): String = Instant.now().minus(d).toString()

    @Test fun `1분 미만은 방금 전`() =
        assertEquals("방금 전", DateUtils.calculateTimeAgo(ago(Duration.ofSeconds(30))))

    @Test fun `59분은 분 단위`() =
        assertEquals("59분 전", DateUtils.calculateTimeAgo(ago(Duration.ofMinutes(59).plusSeconds(5))))

    @Test fun `60분 경계는 1시간 전`() =
        assertEquals("1시간 전", DateUtils.calculateTimeAgo(ago(Duration.ofMinutes(60).plusSeconds(5))))

    @Test fun `24시간 경계는 1일 전`() =
        assertEquals("1일 전", DateUtils.calculateTimeAgo(ago(Duration.ofHours(24).plusSeconds(5))))

    @Test fun `7일 경계부터는 날짜 포맷`() {
        val sevenDaysAgo = Instant.now().minus(Duration.ofDays(7).plusSeconds(5))
        val expected = DateTimeFormatter.ofPattern("yyyy. MM. dd.", Locale.US)
            .withZone(ZoneId.systemDefault()).format(sevenDaysAgo)
        assertEquals(expected, DateUtils.calculateTimeAgo(sevenDaysAgo.toString()))
    }

    @Test fun `서버 시계가 앞서면(음수 diff) 방금 전`() =
        assertEquals("방금 전", DateUtils.calculateTimeAgo(Instant.now().plus(Duration.ofHours(1)).toString()))

    @Test fun `파싱 실패 시 빈 문자열 - formatDate의 원본 반환과 불일치가 현재 계약`() =
        assertEquals("", DateUtils.calculateTimeAgo("notadate"))

    // --- formatKstDateTime ---
    @Test fun `Z와 +0900은 같은 인스턴트로 표시`() {
        assertEquals("2026. 05. 20. 14:30", DateUtils.formatKstDateTime("2026-05-20T05:30:00Z"))
        assertEquals("2026. 05. 20. 14:30", DateUtils.formatKstDateTime("2026-05-20T14:30:00+09:00"))
    }

    @Test fun `오프셋 없으면 KST 벽시계로 간주`() =
        assertEquals("2026. 05. 20. 14:30", DateUtils.formatKstDateTime("2026-05-20T14:30:00"))

    @Test fun `formatKstDateTime 파싱 불가면 원본`() =
        assertEquals("garbage", DateUtils.formatKstDateTime("garbage"))

    // --- meetingAtFromKst / parseKstLocalDateTime 왕복 ---
    @Test fun `KST 왕복 보존`() {
        val local = LocalDateTime.of(2026, 5, 20, 14, 30)
        assertEquals(local, DateUtils.parseKstLocalDateTime(DateUtils.meetingAtFromKst(local)))
    }
}
