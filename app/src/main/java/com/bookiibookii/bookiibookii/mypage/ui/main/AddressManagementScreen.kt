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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddress
import com.bookiibookii.bookiibookii.ui.component.DaumAddressWebView
import com.bookiibookii.bookiibookii.data.model.location.DeliveryAddressRequest
import com.bookiibookii.bookiibookii.data.model.location.ExchangeAddress
import com.bookiibookii.bookiibookii.data.model.location.ExchangeAddressRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressManagementScreen(
    deliveries: List<DeliveryAddress> = emptyList(),
    exchanges: List<ExchangeAddress> = emptyList(),
    initialTabIndex: Int = 0,
    onBackClick: () -> Unit = {},
    onFetchDeliveries: () -> Unit = {},
    onFetchExchanges: () -> Unit = {},
    onAddDelivery: (DeliveryAddressRequest, () -> Unit) -> Unit = { _, _ -> },
    onUpdateDelivery: (Long, DeliveryAddressRequest, () -> Unit) -> Unit = { _, _, _ -> },
    onDeleteDelivery: (Long) -> Unit = {},
    onAddExchange: (ExchangeAddressRequest, () -> Unit) -> Unit = { _, _ -> },
    onUpdateExchange: (Long, ExchangeAddressRequest, () -> Unit) -> Unit = { _, _, _ -> },
    onDeleteExchange: (Long) -> Unit = {},
) {
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    var showDeliverySheet by remember { mutableStateOf(false) }
    var showExchangeSheet by remember { mutableStateOf(false) }
    var editDelivery by remember { mutableStateOf<DeliveryAddress?>(null) }
    var editExchange by remember { mutableStateOf<ExchangeAddress?>(null) }
    var expandedDeliveryId by remember { mutableStateOf<Long?>(null) }
    var expandedExchangeId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 0) onFetchDeliveries() else onFetchExchanges()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
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
                // 탭
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    listOf("배송지", "희망 교환 장소").forEachIndexed { index, label ->
                        val isSelected = selectedTabIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
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
                    if (deliveries.isEmpty()) {
                        AddressEmptyState("등록된 주소가 없습니다.\n택배 교환 그룹에 참여하려면 배송지를 등록하세요")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            deliveries.forEach { address ->
                                DeliveryAddressCard(
                                    address = address,
                                    isMenuExpanded = expandedDeliveryId == address.id,
                                    onMenuClick = {
                                        expandedDeliveryId = if (expandedDeliveryId == address.id) null else address.id
                                    },
                                    onMenuDismiss = { expandedDeliveryId = null },
                                    onEditClick = {
                                        expandedDeliveryId = null
                                        editDelivery = address
                                        showDeliverySheet = true
                                    },
                                    onDeleteClick = {
                                        expandedDeliveryId = null
                                        onDeleteDelivery(address.id)
                                    },
                                )
                            }
                        }
                    }
                } else {
                    if (exchanges.isEmpty()) {
                        AddressEmptyState("등록된 주소가 없습니다.\n직접 교환 그룹에 참여하려면 장소를 등록하세요")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            exchanges.forEach { place ->
                                ExchangePlaceCard(
                                    place = place,
                                    isMenuExpanded = expandedExchangeId == place.id,
                                    onMenuClick = {
                                        expandedExchangeId = if (expandedExchangeId == place.id) null else place.id
                                    },
                                    onMenuDismiss = { expandedExchangeId = null },
                                    onEditClick = {
                                        expandedExchangeId = null
                                        editExchange = place
                                        showExchangeSheet = true
                                    },
                                    onDeleteClick = {
                                        expandedExchangeId = null
                                        onDeleteExchange(place.id)
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            FooterButton(
                text = "추가하기",
                onClick = {
                    if (selectedTabIndex == 0) {
                        editDelivery = null
                        showDeliverySheet = true
                    } else {
                        editExchange = null
                        showExchangeSheet = true
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
            )
        }

        if (showDeliverySheet) {
            DeliveryBottomSheet(
                editTarget = editDelivery,
                onDismiss = { showDeliverySheet = false; editDelivery = null },
                onSave = { req ->
                    val target = editDelivery
                    if (target == null) {
                        onAddDelivery(req) { showDeliverySheet = false }
                    } else {
                        onUpdateDelivery(target.id, req) { showDeliverySheet = false; editDelivery = null }
                    }
                },
            )
        }

        if (showExchangeSheet) {
            ExchangePlaceBottomSheet(
                editTarget = editExchange,
                onDismiss = { showExchangeSheet = false; editExchange = null },
                onSave = { req ->
                    val target = editExchange
                    if (target == null) {
                        onAddExchange(req) { showExchangeSheet = false }
                    } else {
                        onUpdateExchange(target.id, req) { showExchangeSheet = false; editExchange = null }
                    }
                },
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
            .padding(24.dp),
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
private fun DeliveryAddressCard(
    address: DeliveryAddress,
    isMenuExpanded: Boolean,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
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
            if (address.isDefault) {
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
                text = address.placeName,
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
            )
            Box {
                Icon(
                    painter = painterResource(R.drawable.ic_meetball),
                    contentDescription = "더보기",
                    tint = BookiiBookiiTheme.colors.grey400,
                    modifier = Modifier.size(24.dp).clickable { onMenuClick() },
                )
                // 총 160×88, 각 항목 44 높이, 좌우 패딩 16, 아이콘 24
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onMenuDismiss,
                    modifier = Modifier
                        .width(160.dp)
                        .background(BookiiBookiiTheme.colors.white),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { onEditClick() }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("수정하기", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey900)
                        Icon(painterResource(R.drawable.ic_edit), null, tint = BookiiBookiiTheme.colors.grey600, modifier = Modifier.size(24.dp))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { onDeleteClick() }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("삭제하기", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey900)
                        Icon(painterResource(R.drawable.ic_trash), null, tint = BookiiBookiiTheme.colors.grey600, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = address.address, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
            if (!address.addressDetail.isNullOrEmpty()) {
                Text(text = address.addressDetail, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = address.receiverName, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.width(1.dp).height(15.dp).background(BookiiBookiiTheme.colors.grey300))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = address.phone, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
        }
    }
}

@Composable
private fun ExchangePlaceCard(
    place: ExchangeAddress,
    isMenuExpanded: Boolean,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
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
            if (place.isDefault) {
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
                text = place.placeName,
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
            )
            Box {
                Icon(
                    painter = painterResource(R.drawable.ic_meetball),
                    contentDescription = "더보기",
                    tint = BookiiBookiiTheme.colors.grey400,
                    modifier = Modifier.size(24.dp).clickable { onMenuClick() },
                )
                // 총 160×88, 각 항목 44 높이, 좌우 패딩 16, 아이콘 24
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onMenuDismiss,
                    modifier = Modifier
                        .width(160.dp)
                        .background(BookiiBookiiTheme.colors.white),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { onEditClick() }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("수정하기", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey900)
                        Icon(painterResource(R.drawable.ic_edit), null, tint = BookiiBookiiTheme.colors.grey600, modifier = Modifier.size(24.dp))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { onDeleteClick() }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("삭제하기", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey900)
                        Icon(painterResource(R.drawable.ic_trash), null, tint = BookiiBookiiTheme.colors.grey600, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        Text(text = place.address, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
        if (!place.addressDetail.isNullOrEmpty()) {
            Text(text = place.addressDetail, style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey700)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeliveryBottomSheet(
    editTarget: DeliveryAddress?,
    onDismiss: () -> Unit,
    onSave: (DeliveryAddressRequest) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nickname by remember(editTarget) { mutableStateOf(editTarget?.placeName ?: "") }
    var address by remember(editTarget) { mutableStateOf(editTarget?.address ?: "") }
    var zipCode by remember(editTarget) { mutableStateOf(editTarget?.zipCode ?: "") }
    var detail by remember(editTarget) { mutableStateOf(editTarget?.addressDetail ?: "") }
    var recipientName by remember(editTarget) { mutableStateOf(editTarget?.receiverName ?: "") }
    var phone by remember(editTarget) { mutableStateOf(editTarget?.phone ?: "") }
    var isPrimary by remember(editTarget) { mutableStateOf(editTarget?.isDefault ?: false) }
    var showAddressSearch by remember { mutableStateOf(false) }

    var phoneError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BookiiBookiiTheme.colors.white,
        dragHandle = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp).height(4.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(BookiiBookiiTheme.colors.grey200),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = if (editTarget == null) "배송지 추가" else "배송지 수정",
                style = BookiiBookiiTheme.typography.semibold20,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Spacer(modifier = Modifier.height(20.dp))

            AddressFormField(label = "별명", isRequired = false, value = nickname, placeholder = "별명을 입력하세요", onValueChange = { nickname = it })
            Spacer(modifier = Modifier.height(16.dp))
            AddressSearchField(
                label = "주소",
                isRequired = true,
                value = address,
                placeholder = "주소 검색",
                onClick = { showAddressSearch = true },
            )
            Spacer(modifier = Modifier.height(16.dp))
            AddressFormField(label = "상세 주소", isRequired = false, value = detail, placeholder = "상세 주소를 입력하세요", onValueChange = { detail = it })
            Spacer(modifier = Modifier.height(16.dp))
            AddressFormField(label = "수령인", isRequired = true, value = recipientName, placeholder = "수령인 이름", onValueChange = { recipientName = it })
            Spacer(modifier = Modifier.height(16.dp))
            AddressFormField(label = "전화번호", isRequired = true, value = phone, placeholder = "010-0000-0000", onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(11)
                phone = if (digits.length == 11) {
                    "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
                } else {
                    digits
                }
            })
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
                BottomSheetTwoBtnShort(text = "취소", style = BottomSheetBtnStyle.White, onClick = onDismiss, modifier = Modifier.weight(1f))
                BottomSheetTwoBtnShort(
                    text = "저장",
                    style = BottomSheetBtnStyle.Dark,
                    onClick = {
                        val phoneDigits = phone.filter { it.isDigit() }
                        if (phoneDigits.length != 11 || !phoneDigits.startsWith("010")) {
                            phoneError = true
                            return@BottomSheetTwoBtnShort
                        }
                        phoneError = false
                        onSave(DeliveryAddressRequest(
                            placeName = nickname,
                            address = address,
                            zipCode = zipCode,
                            addressDetail = detail.ifBlank { "" },
                            receiverName = recipientName,
                            phone = phone,
                        ))
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showAddressSearch) {
        AddressSearchDialog(
            onResult = { addr, zip ->
                address = addr
                zipCode = zip
                showAddressSearch = false
            },
            onDismiss = { showAddressSearch = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExchangePlaceBottomSheet(
    editTarget: ExchangeAddress?,
    onDismiss: () -> Unit,
    onSave: (ExchangeAddressRequest) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nickname by remember(editTarget) { mutableStateOf(editTarget?.placeName ?: "") }
    var placeAddress by remember(editTarget) { mutableStateOf(editTarget?.address ?: "") }
    var zipCode by remember(editTarget) { mutableStateOf(editTarget?.zipCode ?: "") }
    var detail by remember(editTarget) { mutableStateOf(editTarget?.addressDetail ?: "") }
    var isPrimary by remember(editTarget) { mutableStateOf(editTarget?.isDefault ?: false) }
    var showAddressSearch by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BookiiBookiiTheme.colors.white,
        dragHandle = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp).height(4.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(BookiiBookiiTheme.colors.grey200),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = if (editTarget == null) "희망 교환 장소 추가" else "희망 교환 장소 수정",
                style = BookiiBookiiTheme.typography.semibold20,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Spacer(modifier = Modifier.height(20.dp))

            AddressFormField(label = "별명", isRequired = false, value = nickname, placeholder = "별명을 입력하세요", onValueChange = { nickname = it })
            Spacer(modifier = Modifier.height(16.dp))
            AddressSearchField(
                label = "장소",
                isRequired = true,
                value = placeAddress,
                placeholder = "장소 검색",
                onClick = { showAddressSearch = true },
            )
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
                BottomSheetTwoBtnShort(text = "취소", style = BottomSheetBtnStyle.White, onClick = onDismiss, modifier = Modifier.weight(1f))
                BottomSheetTwoBtnShort(
                    text = "저장",
                    style = BottomSheetBtnStyle.Dark,
                    onClick = {
                        onSave(ExchangeAddressRequest(
                            placeName = nickname,
                            address = placeAddress,
                            zipCode = zipCode,
                            // TODO: 카카오 장소검색 연동 시 실제 좌표 전달 (현재 Daum 우편번호는 좌표 미제공)
                            x = 0.0,
                            y = 0.0,
                            addressDetail = detail.ifBlank { "" },
                        ))
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showAddressSearch) {
        AddressSearchDialog(
            onResult = { addr, zip ->
                placeAddress = addr
                zipCode = zip
                showAddressSearch = false
            },
            onDismiss = { showAddressSearch = false },
        )
    }
}

@Composable
private fun AddressSearchDialog(
    onResult: (address: String, zipCode: String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Dialog 윈도우를 MATCH_PARENT로 강제 설정 — 이렇게 해야 fillMaxSize()가 풀스크린으로 동작
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
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "뒤로가기",
                        tint = BookiiBookiiTheme.colors.grey900,
                        modifier = Modifier.size(24.dp),
                    )
                }
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

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun AddressManagementScreenPreview() {
    BookiiBookiiTheme {
        AddressManagementScreen(
            deliveries = listOf(
                DeliveryAddress(
                    id = 1L,
                    placeName = "우리집",
                    address = "서울특별시 강남구 테헤란로 123",
                    zipCode = "06234",
                    addressDetail = "456동 789호",
                    receiverName = "북이",
                    phone = "010-1234-5678",
                    isDefault = true,
                ),
                DeliveryAddress(
                    id = 2L,
                    placeName = "회사",
                    address = "서울특별시 중구 세종대로 110",
                    zipCode = "04524",
                    addressDetail = null,
                    receiverName = "북이",
                    phone = "010-9876-5432",
                    isDefault = false,
                ),
            ),
            exchanges = listOf(
                ExchangeAddress(
                    id = 1L,
                    placeName = "강남역 11번 출구",
                    address = "서울특별시 강남구 강남대로 396",
                    zipCode = "06241",
                    x = 127.027621,
                    y = 37.497942,
                    addressDetail = "스타벅스 앞",
                    isDefault = true,
                ),
            ),
        )
    }
}
