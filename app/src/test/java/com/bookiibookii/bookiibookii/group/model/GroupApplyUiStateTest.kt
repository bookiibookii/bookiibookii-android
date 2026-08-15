package com.bookiibookii.bookiibookii.group.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupApplyUiStateTest {
    @Test fun `책 미선택이면 불가`() = assertFalse(GroupApplyUiState(applyMsg = "한 마디").canSubmit)

    @Test fun `메시지 blank면 불가`() = assertFalse(GroupApplyUiState(isbn13 = "979", applyMsg = " ").canSubmit)

    @Test fun `50자는 허용, 51자는 불가`() {
        assertTrue(GroupApplyUiState(isbn13 = "979", applyMsg = "가".repeat(50)).canSubmit)
        assertFalse(GroupApplyUiState(isbn13 = "979", applyMsg = "가".repeat(51)).canSubmit)
    }
}
