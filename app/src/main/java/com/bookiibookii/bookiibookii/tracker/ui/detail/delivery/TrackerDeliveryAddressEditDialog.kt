package com.bookiibookii.bookiibookii.tracker.ui.detail.delivery

import android.view.ViewGroup
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.bookiibookii.bookiibookii.ui.component.AddressButton
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.CardButton
import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle
import com.bookiibookii.bookiibookii.ui.component.CloseButton
import com.bookiibookii.bookiibookii.ui.component.DaumAddressWebView
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 배송지 수정 다이얼로그에 표시할 "나의 배송지" 항목
data class DeliveryAddressOption(
    val userDeliveryId: Long,
    val title: String,
    val address: String,
    val addressDetail: String,
    val zipCode: String,
) {
    // 버튼에 표시할 전체 주소 (도로명 + 상세)
    val displayAddress: String
        get() = listOfNotNull(
            address.takeIf { it.isNotBlank() },
            addressDetail.takeIf { it.isNotBlank() },
        ).joinToString(" ")
}

// 택배 배송 정보 수정 다이얼로그 (택배교환 전용)
// - 나의 배송지 선택 → 기존 배송지 선택 API
// - 직접 입력(주소 검색) → 직접 입력 API
@Composable
fun TrackerDeliveryAddressEditDialog(
    savedAddresses: List<DeliveryAddressOption>,
    initialSelectedUserDeliveryId: Long?,
    onDismiss: () -> Unit,
    onConfirmSaved: (userDeliveryId: Long) -> Unit,
    onConfirmDirect: (zipCode: String, address: String, addressDetail: String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDeliveryAddressEditDialogContent(
            savedAddresses = savedAddresses,
            initialSelectedUserDeliveryId = initialSelectedUserDeliveryId,
            onDismiss = onDismiss,
            onConfirmSaved = onConfirmSaved,
            onConfirmDirect = onConfirmDirect,
        )
    }
}

@Composable
private fun TrackerDeliveryAddressEditDialogContent(
    savedAddresses: List<DeliveryAddressOption>,
    initialSelectedUserDeliveryId: Long?,
    onDismiss: () -> Unit,
    onConfirmSaved: (userDeliveryId: Long) -> Unit,
    onConfirmDirect: (zipCode: String, address: String, addressDetail: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 선택된 기존 배송지 id
    var selectedUserDeliveryId by remember { mutableStateOf(initialSelectedUserDeliveryId) }
    var isDirectMode by remember { mutableStateOf(false) }
    var directAddress by remember { mutableStateOf("") }
    var directZipCode by remember { mutableStateOf("") }
    var directDetail by remember { mutableStateOf("") }
    // 우편번호 검색 화면 표시 여부
    var showAddressSearch by remember { mutableStateOf(false) }

    val canConfirm = if (isDirectMode) directAddress.isNotBlank() else selectedUserDeliveryId != null

    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round24,
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "배송 정보 수정",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
            )
            CloseButton(onClick = onDismiss)
        }

        if (savedAddresses.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "나의 배송지",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                savedAddresses.forEach { option ->
                    AddressButton(
                        title = option.title,
                        address = option.displayAddress,
                        selected = !isDirectMode && selectedUserDeliveryId == option.userDeliveryId,
                        onClick = {
                            selectedUserDeliveryId = option.userDeliveryId
                            isDirectMode = false
                            // 선택한 배송지 주소를 직접 입력 칸에도 반영
                            directAddress = option.address
                            directZipCode = option.zipCode
                            directDetail = option.addressDetail
                        },
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "직접 입력",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                // 주소: 탭하면 우편번호 검색으로 이동 (직접 타이핑 불가)
                AddressSearchBox(
                    value = directAddress,
                    placeholder = "건물명, 도로명, 지번으로 검색",
                    onClick = { showAddressSearch = true },
                )
            }
            // 상세 주소: 직접 입력
            AddressDetailInputBox(
                value = directDetail,
                onValueChange = {
                    directDetail = it
                    isDirectMode = true
                    selectedUserDeliveryId = null
                },
                placeholder = "상세 주소",
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "이전",
                style = CardButtonStyle.White,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = "확인",
                style = CardButtonStyle.Main,
                onClick = {
                    if (!canConfirm) return@CardButton
                    if (isDirectMode) {
                        onConfirmDirect(directZipCode, directAddress, directDetail)
                    } else {
                        selectedUserDeliveryId?.let(onConfirmSaved)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showAddressSearch) {
        AddressSearchDialog(
            onResult = { address, zipCode ->
                directAddress = address
                directZipCode = zipCode
                isDirectMode = true
                selectedUserDeliveryId = null
                showAddressSearch = false
            },
            onDismiss = { showAddressSearch = false },
        )
    }
}

// 주소 검색 진입 박스 (클릭 전용)
@Composable
private fun AddressSearchBox(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round16,
            )
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey300,
                shape = BookiiBookiiTheme.shape.round16,
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = value.ifEmpty { placeholder },
            style = BookiiBookiiTheme.typography.regular16,
            color = if (value.isEmpty()) {
                BookiiBookiiTheme.colors.grey500
            } else {
                BookiiBookiiTheme.colors.grey900
            },
        )
    }
}

// 상세 주소 입력 박스 (직접 타이핑)
@Composable
private fun AddressDetailInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round16,
            )
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey300,
                shape = BookiiBookiiTheme.shape.round16,
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.grey500,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = BookiiBookiiTheme.typography.regular16.copy(
                color = BookiiBookiiTheme.colors.grey900,
            ),
            cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// 우편번호 검색 (Daum) 풀스크린 다이얼로그
@Composable
private fun AddressSearchDialog(
    onResult: (address: String, zipCode: String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
        SideEffect {
            dialogWindowProvider?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BookiiBookiiTheme.colors.white)
                .systemBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BookiiBackButton(onClick = onDismiss)
                Text(
                    text = "주소 검색",
                    style = BookiiBookiiTheme.typography.semibold18,
                    color = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(40.dp))
            }
            HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
            DaumAddressWebView(
                onResult = onResult,
                onBack = onDismiss,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerDeliveryAddressEditDialogPreview() {
    BookiiPreview {
        TrackerDeliveryAddressEditDialogContent(
            savedAddresses = listOf(
                DeliveryAddressOption(1L, "자취방", "서울 용산구 한강로2가 426", "101동 202호", "04379"),
                DeliveryAddressOption(2L, "회사", "서울 강남구 테헤란로 123", "5층", "06234"),
            ),
            initialSelectedUserDeliveryId = 1L,
            onDismiss = {},
            onConfirmSaved = {},
            onConfirmDirect = { _, _, _ -> },
        )
    }
}
