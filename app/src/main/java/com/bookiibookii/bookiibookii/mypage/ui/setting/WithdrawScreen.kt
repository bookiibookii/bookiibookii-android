package com.bookiibookii.bookiibookii.mypage.ui.setting

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.ui.component.FooterButton

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview

private val withdrawOptions = listOf(
    "원하는 파트너를 찾기 어려워요",
    "교환 과정이 번거롭고 불편해요",
    "앱 사용이 어렵고 불편해요",
    "교환독서를 자주 하지 않아요",
    "개인정보가 걱정돼요",
    "직접 입력",
)

// API에 전송할 reason enum 값 (withdrawOptions와 동일 순서)
private val withdrawReasonCodes = listOf(
    "HARD_TO_FIND_PARTNER",
    "INCONVENIENT_EXCHANGE",
    "DIFFICULT_APP_USAGE",
    "INFREQUENT_EXCHANGE",
    "PRIVACY_CONCERN",
    "CUSTOM_INPUT",
)

@Composable
fun WithdrawScreen(
    userName: String = "noshel",
    onBackClick: () -> Unit = {},
    onWithdraw: (reason: String, customReason: String?) -> Unit = { _, _ -> },
    showWithdrawFailedDialog: Boolean = false,
    onFailureDialogDismiss: () -> Unit = {},
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var customInput by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isCustomInputSelected = selectedIndex == withdrawOptions.lastIndex
    val isNextEnabled = when {
        selectedIndex == null -> false
        isCustomInputSelected -> customInput.trim().isNotEmpty()
        else -> true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg)
            .imePadding()
    ) {
        WithdrawTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(BookiiBookiiTheme.typography.semibold28.toSpanStyle().copy(color = BookiiBookiiTheme.colors.uiMain)) { append(userName) }
                    withStyle(BookiiBookiiTheme.typography.regular28.toSpanStyle().copy(color = BookiiBookiiTheme.colors.grey900)) { append("님과 이별인가요?") }
                }
            )
            Text(
                text = "너무 아쉬워요",
                style = BookiiBookiiTheme.typography.regular28,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "서비스 개선을 위하여 사유를 선택해주세요.",
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey700,
            )
            Spacer(modifier = Modifier.height(24.dp))

            withdrawOptions.forEachIndexed { index, option ->
                WithdrawOptionRow(
                    text = option,
                    isSelected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                )
                if (index < withdrawOptions.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (isCustomInputSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                WithdrawCustomInput(value = customInput, onValueChange = { customInput = it })
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        FooterButton(
            text = "다음",
            onClick = { showConfirmDialog = true },
            enabled = isNextEnabled,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp).navigationBarsPadding(),
        )
    }

    if (showConfirmDialog) {
        val isCustom = selectedIndex == withdrawOptions.lastIndex
        val reason = selectedIndex?.let { withdrawReasonCodes[it] } ?: ""
        val customReason = if (isCustom) customInput.trim() else null

        BookiiDialog(
            title = "회원 탈퇴",
            body = "탈퇴 후에는 계정 정보 및 활동 내역이\n모두 삭제됩니다. 정말 탈퇴하시겠어요?",
            confirmText = "회원 탈퇴",
            confirmColor = BookiiBookiiTheme.colors.uiPointRed,
            onConfirm = {
                showConfirmDialog = false
                onWithdraw(reason, customReason)
            },
            cancelText = "취소",
            onDismiss = { showConfirmDialog = false },
        )
    }
    if (showWithdrawFailedDialog) {
        BookiiDialog(
            title = "탈퇴 불가",
            body = "진행 중인 그룹이 모두 종료되어야\n탈퇴 가능합니다.",
            confirmText = "닫기",
            confirmColor = BookiiBookiiTheme.colors.grey900,
            onConfirm = { onFailureDialogDismiss() },
            onDismiss = { onFailureDialogDismiss() },
        )
    }
}

@Composable
private fun WithdrawTopBar(onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(32.dp),
                )
            }
            Text(
                text = "회원 탈퇴",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Box(modifier = Modifier.size(40.dp))
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200)
    }
}

@Composable
private fun WithdrawOptionRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) BookiiBookiiTheme.colors.uiMainPale else BookiiBookiiTheme.colors.white
    val borderColor = if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isSelected) BookiiBookiiTheme.colors.uiMain150 else BookiiBookiiTheme.colors.uiMainPale),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.uiMain,
                modifier = Modifier.size(16.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular15,
            color = if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey900,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun WithdrawCustomInput(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = BookiiBookiiTheme.typography.medium16.copy(color = BookiiBookiiTheme.colors.grey900),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .border(1.dp, BookiiBookiiTheme.colors.grey300, RoundedCornerShape(20.dp)),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 60.dp),
                contentAlignment = Alignment.TopStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "사유를 입력해주세요",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey400,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun WithdrawScreenPreview() {
    BookiiPreview {
        WithdrawScreen()
    }
}
