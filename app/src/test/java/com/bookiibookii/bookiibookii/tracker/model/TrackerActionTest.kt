package com.bookiibookii.bookiibookii.tracker.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackerActionTest {
    @Test fun `미지 상태와 null은 None 페어`() {
        assertEquals(TrackerAction.None to TrackerAction.None, actionsForStatus(null))
        assertEquals(TrackerAction.None to TrackerAction.None, actionsForStatus("UNKNOWN_STATUS"))
    }

    @Test fun `15개 상태 전수 매핑`() {
        val expected = mapOf(
            "READING" to (TrackerAction.RecordProgress to TrackerAction.WriteReadingCard),
            "REVIEW_WRITING" to (TrackerAction.WriteBookReview to TrackerAction.WriteReadingCard),
            "REVIEW_WAITING_PARTNER" to (TrackerAction.EditBookReview to TrackerAction.WriteReadingCard),
            "EXCHANGE_REVIEW_WRITING" to (TrackerAction.WritePartnerReview to TrackerAction.None),
            "EXCHANGE_REVIEW_WAITING_PARTNER" to (TrackerAction.WritePartnerReview to TrackerAction.None),
            "TRACKING_REQUIRED" to (TrackerAction.RegisterTrackingNumber to TrackerAction.CheckDeliveryInfo),
            "RETURN_TRACKING_REQUIRED" to (TrackerAction.RegisterTrackingNumber to TrackerAction.CheckDeliveryInfo),
            "SHIPPING" to (TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo),
            "RETURNING" to (TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo),
            "WAITING_PARTNER_TRACKING_REGISTER" to (TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo),
            "WAITING_PARTNER_RECEIPT_CONFIRM" to (TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo),
            "MEETING_REGISTER_REQUIRED" to (TrackerAction.GoToComments to TrackerAction.RegisterMeeting),
            "WAITING_HOST_MEETING_REGISTER" to (TrackerAction.GoToComments to TrackerAction.RegisterMeeting),
            "EXCHANGING" to (TrackerAction.ConfirmExchange to TrackerAction.CheckMeeting),
            "WAITING_PARTNER_MEETING_COMPLETE" to (TrackerAction.CompleteExchange to TrackerAction.None),
        )
        expected.forEach { (status, pair) -> assertEquals(status, pair, actionsForStatus(status)) }
    }

    @Test fun `양쪽 버튼 모두 비활성 상태`() {
        listOf("WAITING_PARTNER_TRACKING_REGISTER", "WAITING_PARTNER_RECEIPT_CONFIRM").forEach {
            assertTrue(it, isPrimaryActionDisabled(it))
            assertTrue(it, isSecondaryActionDisabled(it))
        }
    }

    @Test fun `primary만 비활성 상태`() {
        listOf("WAITING_PARTNER_MEETING_COMPLETE", "EXCHANGE_REVIEW_WAITING_PARTNER").forEach {
            assertTrue(it, isPrimaryActionDisabled(it))
            assertFalse(it, isSecondaryActionDisabled(it))
        }
    }

    @Test fun `null 상태는 비활성 아님`() {
        assertFalse(isPrimaryActionDisabled(null))
        assertFalse(isSecondaryActionDisabled(null))
    }

    @Test fun `교환 약속 단계는 진행률 숨김`() {
        listOf(
            "MEETING_REGISTER_REQUIRED", "WAITING_HOST_MEETING_REGISTER",
            "EXCHANGING", "WAITING_PARTNER_MEETING_COMPLETE",
        ).forEach { assertTrue(it, isReadingProgressHidden(it)) }
        assertFalse(isReadingProgressHidden("READING"))
        assertFalse(isReadingProgressHidden(null))
    }

    @Test fun `수령 확인 대기 상태의 진행률 텍스트는 시점에 따라 다름`() {
        assertEquals("수령 완료", progressTextOverride("WAITING_PARTNER_RECEIPT_CONFIRM", isMine = true))
        assertEquals("수령 전", progressTextOverride("WAITING_PARTNER_RECEIPT_CONFIRM", isMine = false))
        assertEquals("교환 준비 완료", progressTextOverride("REVIEW_WAITING_PARTNER", isMine = true))
        assertNull(progressTextOverride("REVIEW_WAITING_PARTNER", isMine = false))
        assertNull(progressTextOverride("READING", isMine = true))
    }
}
