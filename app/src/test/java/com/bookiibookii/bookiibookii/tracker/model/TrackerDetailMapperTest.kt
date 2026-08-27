package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDetailResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerStepDTO
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStepStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackerDetailMapperTest {
    private fun dto(
        dDay: Int? = 3,
        displayStatus: String? = "READING",
        steps: List<TrackerStepDTO>? = null,
        displayBookTitle: String? = "책제목",
        displayStatusLabel: String? = "읽는 중",
    ) = TrackerDetailResDTO(
        groupId = 1L, groupName = "그룹", tradeType = "DIRECT", myRole = "HOST",
        displayStatus = displayStatus, displayBookTitle = displayBookTitle,
        displayStatusLabel = displayStatusLabel, dDay = dDay,
        myBook = null, partnerBook = null, steps = steps,
    )

    private fun step(completed: Boolean?, status: String = "MY_BOOK_READING") =
        TrackerStepDTO(status = status, title = "t", description = "d", completed = completed)

    @Test fun `음수 D-day는 D-0으로 절삭`() = assertEquals("D-0", dto(dDay = -3).toUiState().dDay)

    @Test fun `null D-day도 D-0`() = assertEquals("D-0", dto(dDay = null).toUiState().dDay)

    @Test fun `steps null이면 빈 목록`() = assertTrue(dto(steps = null).toUiState().steps.isEmpty())

    @Test fun `전 단계 완료 시 InProgress 칩 없음`() {
        val ui = dto(steps = listOf(step(true), step(true))).toUiState()
        assertTrue(ui.steps.all { it.status == TrackerStepStatus.Completed })
    }

    @Test fun `첫 미완료 단계만 InProgress, 이후 미완료는 숨김, 최신이 앞`() {
        val ui = dto(steps = listOf(step(true), step(false), step(false))).toUiState()
        assertEquals(2, ui.steps.size) // completed 1 + inProgress 1
        assertTrue(ui.steps.first().status is TrackerStepStatus.InProgress) // reversed → 최신이 첫 번째
    }

    @Test fun `제목 blank면 상태 라벨만`() =
        assertEquals("읽는 중", dto(displayBookTitle = "", displayStatusLabel = "읽는 중").toUiState().statusLabel)

    @Test fun `전 단계 완료면 현재 phase는 반납-4단계`() {
        val ui = dto(steps = listOf(step(true))).toUiState()
        assertEquals("반납", ui.currentStepLabel)
        assertEquals(4, ui.currentStepPosition)
    }
}
