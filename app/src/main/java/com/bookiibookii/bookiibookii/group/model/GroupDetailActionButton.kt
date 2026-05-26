package com.bookiibookii.bookiibookii.group.model

import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle

// 그룹 상세 하단 액션 버튼의 표시값
// VM에서 GroupDetailResponse.buttonStatus + waitingCount를 받아
// 화면에 내려주는 용도. null이면 버튼 자체를 안그림
data class GroupDetailActionButton(
    val text: String,
    val style: CardButtonStyle,
)

// - APPLY:   참여 신청하기            (Main)
// - MANAGE:  참여 요청 관리 {대기자 수} (Main)
// - CANCEL:  참여 신청 취소           (MainPale)
// - TRACKER / FULL / 그 외: 버튼 미표시 (null)
fun groupDetailActionButton(
    buttonStatus: String,
    waitingCount: Int,
): GroupDetailActionButton? = when (buttonStatus) {
    "APPLY" -> GroupDetailActionButton(
        text = "참여 신청하기",
        style = CardButtonStyle.Main,
    )
    "MANAGE" -> GroupDetailActionButton(
        text = "참여 요청 관리 ($waitingCount)",
        style = CardButtonStyle.Main,
    )
    "CANCEL" -> GroupDetailActionButton(
        text = "참여 신청 취소",
        style = CardButtonStyle.MainPale,
    )
    else -> null
}
