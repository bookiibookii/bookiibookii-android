package com.bookiibookii.bookiibookii.mypage.ui.main

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressManagementScreen(
    onBackClick: () -> Unit = {},
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showDeliveryAddSheet by remember { mutableStateOf(false) }
    var showExchangeAddSheet by remember { mutableStateOf(false) }

    val deliveryAddresses = remember {
        mutableStateOf(
            listOf(
                DeliveryAddress(nickname = "집", recipientName = "김스카이", phone = "010-1234-5678", address = "서울 동작구 사당로 50", detail = "101동 1234호", isPrimary = true),
            )
        )
    }
    val exchangePlaces = remember { mutableStateOf(emptyList<ExchangePlace>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BookiiBookiiTheme.colors.uiBg)
        ) {
            Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            tint = BookiiBookiiTheme.colors.grey900,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    Text(
                        text = "주소지 관리",
                        style = BookiiBookiiTheme.typography.medium20,
                        color = BookiiBookiiTheme.colors.grey900,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.size(40.dp))
                }
                HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    listOf("배송지", "희망 교환 장소").forEachIndexed { index, label ->
                        val isSelected = selectedTabIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.white)
                                .then(
                                    if (!isSelected) Modifier.border(1.dp, BookiiBookiiTheme.colors.grey200, RoundedCornerShape(20.dp))
                                    else Modifier
                                )
                                .clickable { selectedTabIndex = index },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                style = BookiiBookiiTheme.typography.medium16,
                                color = if (isSelected) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey900,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTabIndex == 0) {
                    if (deliveryAddresses.value.isEmpty()) {
                        AddressEmptyState(message = "등록된 배송지가 없습니다\n배송 교환을 하려면 배송지를 등록하세요")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            deliveryAddresses.value.forEach { address ->
                                DeliveryAddressCard(address = address)
                            }
                        }
                    }
                } else {
                    if (exchangePlaces.value.isEmpty()) {
                        AddressEmptyState(message = "등록된 주소가 없습니다\n직접 교환 그룹에 참여하려면 장소를 등록하세요")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            exchangePlaces.value.forEach { place ->
                                ExchangePlaceCard(place = place)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            FooterButton(
                text = "추가하기",
                onClick = {
                    if (selectedTabIndex == 0) showDeliveryAddSheet = true
                    else showExchangeAddSheet = true
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
            )
        }

        if (showDeliveryAddSheet) {
            AddDeliveryBottomSheet(
                onDismiss = { showDeliveryAddSheet = false },
                onSave = { showDeliveryAddSheet = false },
            )
        }

        if (showExchangeAddSheet) {
            AddExchangePlaceBottomSheet(
                onDismiss = { showExchangeAddSheet = false },
                onSave = { showExchangeAddSheet = false },
            )
        }
    }
}

@Composable
private fun AddressEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(vertical = 40.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey600,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DeliveryAddressCard(address: DeliveryAddress) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (address.isPrimary) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BookiiBookiiTheme.colors.uiMainPale)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(text = "대표", style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.uiMain)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = address.nickname,
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(R.drawable.ic_meetball),
                contentDescription = "더보기",
                tint = BookiiBookiiTheme.colors.grey400,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = address.address, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
            if (address.detail.isNotEmpty()) {
                Text(text = address.detail, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = address.recipientName, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.width(1.dp).height(15.dp).background(BookiiBookiiTheme.colors.grey300))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = address.phone, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
        }
    }
}

@Composable
private fun ExchangePlaceCard(place: ExchangePlace) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (place.isPrimary) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BookiiBookiiTheme.colors.uiMainPale)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(text = "대표", style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.uiMain)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = place.nickname,
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(R.drawable.ic_meetball),
                contentDescription = "더보기",
                tint = BookiiBookiiTheme.colors.grey400,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(text = place.address, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
        if (place.detail.isNotEmpty()) {
            Text(text = place.detail, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDeliveryBottomSheet(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nickname by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BookiiBookiiTheme.colors.white,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = "배송지 추가",
                style = BookiiBookiiTheme.typography.semibold20,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Spacer(modifier = Modifier.height(20.dp))

            AddressFormField(label = "별명", isRequired = false, value = nickname, placeholder = "별명을 입력하세요", onValueChange = { nickname = it })
            Spacer(modifier = Modifier.height(16.dp))
            AddressSearchField(label = "주소", isRequired = true, value = address, placeholder = "주소 검색", onClick = { address = "서울 동작구 사당로 50" })
            Spacer(modifier = Modifier.height(16.dp))
            AddressFormField(label = "상세 주소", isRequired = false, value = detail, placeholder = "상세 주소를 입력하세요", onValueChange = { detail = it })
            Spacer(modifier = Modifier.height(16.dp))
            AddressFormField(label = "수령인", isRequired = true, value = recipientName, placeholder = "수령인 이름", onValueChange = { recipientName = it })
            Spacer(modifier = Modifier.height(16.dp))
            AddressFormField(label = "전화번호", isRequired = true, value = phone, placeholder = "010-0000-0000", onValueChange = { phone = it })
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "대표 배송지로 설정", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
                Switch(
                    checked = isPrimary,
                    onCheckedChange = { isPrimary = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BookiiBookiiTheme.colors.white,
                        checkedTrackColor = BookiiBookiiTheme.colors.uiMain,
                        uncheckedThumbColor = BookiiBookiiTheme.colors.white,
                        uncheckedTrackColor = BookiiBookiiTheme.colors.grey300,
                        uncheckedBorderColor = BookiiBookiiTheme.colors.grey300,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BottomSheetTwoBtnShort(
                    text = "취소",
                    style = BottomSheetBtnStyle.White,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                BottomSheetTwoBtnShort(
                    text = "저장",
                    style = BottomSheetBtnStyle.Dark,
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExchangePlaceBottomSheet(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nickname by remember { mutableStateOf("") }
    var placeAddress by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BookiiBookiiTheme.colors.white,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = "희망 교환 장소 추가",
                style = BookiiBookiiTheme.typography.semibold20,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Spacer(modifier = Modifier.height(20.dp))

            AddressFormField(label = "별명", isRequired = false, value = nickname, placeholder = "별명을 입력하세요", onValueChange = { nickname = it })
            Spacer(modifier = Modifier.height(16.dp))
            AddressSearchField(label = "장소", isRequired = true, value = placeAddress, placeholder = "장소 검색", onClick = { placeAddress = "서울 동작구 사당로 50" })
            Spacer(modifier = Modifier.height(16.dp))
            AddressFormField(label = "상세 안내", isRequired = false, value = detail, placeholder = "상세 안내를 입력하세요", onValueChange = { detail = it })
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "대표 장소로 설정", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
                Switch(
                    checked = isPrimary,
                    onCheckedChange = { isPrimary = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BookiiBookiiTheme.colors.white,
                        checkedTrackColor = BookiiBookiiTheme.colors.uiMain,
                        uncheckedThumbColor = BookiiBookiiTheme.colors.white,
                        uncheckedTrackColor = BookiiBookiiTheme.colors.grey300,
                        uncheckedBorderColor = BookiiBookiiTheme.colors.grey300,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BottomSheetTwoBtnShort(
                    text = "취소",
                    style = BottomSheetBtnStyle.White,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                BottomSheetTwoBtnShort(
                    text = "저장",
                    style = BottomSheetBtnStyle.Dark,
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AddressFormField(
    label: String,
    isRequired: Boolean,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
            if (isRequired) {
                Text(text = " *", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.uiMain)
            } else {
                Text(text = " (선택)", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey400)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BookiiBookiiTheme.colors.white)
                .border(1.dp, BookiiBookiiTheme.colors.grey300, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = BookiiBookiiTheme.typography.regular15.copy(color = BookiiBookiiTheme.colors.grey900),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = BookiiBookiiTheme.typography.regular15, color = BookiiBookiiTheme.colors.grey400)
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Composable
private fun AddressSearchField(
    label: String,
    isRequired: Boolean,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
            if (isRequired) {
                Text(text = " *", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.uiMain)
            } else {
                Text(text = " (선택)", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey400)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BookiiBookiiTheme.colors.white)
                .border(1.dp, BookiiBookiiTheme.colors.grey300, RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.grey500,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = value.ifEmpty { placeholder },
                style = BookiiBookiiTheme.typography.regular15,
                color = if (value.isEmpty()) BookiiBookiiTheme.colors.grey400 else BookiiBookiiTheme.colors.grey900,
            )
        }
    }
}

private data class DeliveryAddress(
    val nickname: String,
    val recipientName: String,
    val phone: String,
    val address: String,
    val detail: String,
    val isPrimary: Boolean,
)

private data class ExchangePlace(
    val nickname: String,
    val address: String,
    val detail: String,
    val isPrimary: Boolean,
)

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun AddressManagementScreenPreview() {
    AddressManagementScreen()
}
