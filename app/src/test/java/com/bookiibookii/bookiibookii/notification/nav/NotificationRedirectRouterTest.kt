package com.bookiibookii.bookiibookii.notification.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// fromIntent는 Intent(Android) 의존이라 제외 — fromPayload만 검증
class NotificationRedirectRouterTest {
    @Test fun `redirectType 없으면 null`() =
        assertNull(NotificationRedirectRouter.fromPayload(mapOf("groupId" to "1")))

    @Test fun `redirectType blank면 null`() =
        assertNull(NotificationRedirectRouter.fromPayload(mapOf("redirectType" to "  ")))

    @Test fun `null 맵이면 null`() =
        assertNull(NotificationRedirectRouter.fromPayload(null))

    @Test fun `FCM String id는 Long 변환`() {
        val r = NotificationRedirectRouter.fromPayload(mapOf("redirectType" to "GROUP", "groupId" to "12"))
        assertEquals(12L, r!!.groupId)
    }

    @Test fun `인앱 Number id도 Long 변환 - Gson Double 케이스`() {
        val r = NotificationRedirectRouter.fromPayload(mapOf("redirectType" to "GROUP", "groupId" to 12.0))
        assertEquals(12L, r!!.groupId)
    }

    @Test fun `숫자 아닌 String id는 null`() {
        val r = NotificationRedirectRouter.fromPayload(mapOf("redirectType" to "GROUP", "groupId" to "abc"))
        assertNull(r!!.groupId)
    }

    @Test fun `title blank면 null로 정규화`() {
        val r = NotificationRedirectRouter.fromPayload(mapOf("redirectType" to "GROUP", "title" to " "))
        assertNull(r!!.title)
    }
}
