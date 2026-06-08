package com.bookiibookii.bookiibookii.mypage.ui.main

import android.net.Uri
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.ui.component.FooterButton
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.MypageReqDTO
import com.bookiibookii.bookiibookii.data.model.mypage.UserProfileResDTO
import com.bookiibookii.bookiibookii.onboarding.steps.model.NicknameCheckState
import com.bookiibookii.bookiibookii.onboarding.steps.ui.component.ProfilePhotoBottomSheet
import java.util.Calendar

private val nicknameAllowedCharRegex = Regex("[가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9._\\-_/()\\[\\]:!?]")
private val nicknameAllowedRegex = Regex("^[가-힣A-Za-z0-9._\\-_/()\\[\\]:!?]+$")

private fun validateNickname(nickname: String): Boolean {
    if (nickname.isBlank() || nickname.length > 10) return false
    if (nickname.any { it.isWhitespace() || Character.isSurrogate(it) }) return false
    return nicknameAllowedRegex.matches(nickname)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingScreen(
    profile: UserProfileResDTO? = null,
    profileImageUri: Uri? = null,
    nicknameCheckState: NicknameCheckState? = NicknameCheckState.Idle,
    onBackClick: () -> Unit = {},
    onOpenCamera: () -> Unit = {},
    onOpenGallery: () -> Unit = {},
    onCheckNickname: (String) -> Unit = {},
    onSaveClick: (MypageReqDTO) -> Unit = {},
) {
    val originalNickname = profile?.nickname ?: ""

    var nickname by remember(profile?.nickname) { mutableStateOf(profile?.nickname ?: "") }
    var selectedGenderIndex by remember { mutableStateOf<Int?>(null) }
    var birthYear by remember { mutableStateOf<Int?>(null) }
    var birthMonth by remember { mutableStateOf<Int?>(null) }
    var birthDay by remember { mutableStateOf<Int?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPhotoSheet by remember { mutableStateOf(false) }

    val nicknameChanged = nickname != originalNickname
    val isCheckEnabled = nicknameChanged && validateNickname(nickname) &&
            nicknameCheckState !is NicknameCheckState.Loading

    val canSave = if (nicknameChanged) {
        nicknameCheckState is NicknameCheckState.Available
    } else {
        true
    }

    val genderOptions = listOf("FEMALE", "MALE", null)
    val birthDateDisplay = if (birthYear != null && birthMonth != null && birthDay != null) {
        "%04d.%02d.%02d".format(birthYear!!, birthMonth!!, birthDay!!)
    } else ""

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
                    text = "내 프로필",
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
                .verticalScroll(rememberScrollState()),
        ) {
            // 프로필 이미지
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(128.dp)) {
                    if (profileImageUri != null) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(128.dp).clip(
                                androidx.compose.foundation.shape.GenericShape { size, _ ->
                                    val s = size.width / 128f
                                    moveTo(0f, 64f * s)
                                    cubicTo(0f, 11.296f * s, 11.296f * s, 0f, 64f * s, 0f)
                                    cubicTo(116.704f * s, 0f, size.width, 11.296f * s, size.width, 64f * s)
                                    cubicTo(size.width, 116.704f * s, 116.704f * s, size.height, 64f * s, size.height)
                                    cubicTo(11.296f * s, size.height, 0f, 116.704f * s, 0f, 64f * s)
                                    close()
                                }
                            ),
                        )
                    } else {
                        ProfilePlaceholder(
                            imageUrl = profile?.profileImageUrl,
                            modifier = Modifier.size(128.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(BookiiBookiiTheme.colors.grey600)
                            .clickable { showPhotoSheet = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_camera),
                            contentDescription = "프로필 사진 변경",
                            tint = BookiiBookiiTheme.colors.white,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                // 닉네임 (중복 확인 포함)
                NicknameFieldWithCheck(
                    value = nickname,
                    onValueChange = { filtered ->
                        val clean = filtered
                            .filter { ch -> !ch.isWhitespace() && !Character.isSurrogate(ch) && nicknameAllowedCharRegex.matches(ch.toString()) }
                            .take(10)
                        nickname = clean
                    },
                    nicknameCheckState = nicknameCheckState ?: NicknameCheckState.Idle,
                    isCheckEnabled = isCheckEnabled,
                    onCheckClick = { onCheckNickname(nickname.trim()) },
                )

                GenderField(selectedIndex = selectedGenderIndex, onSelect = { selectedGenderIndex = it })
                BirthDateField(value = birthDateDisplay, onClick = { showDatePicker = true })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        FooterButton(
            text = "수정하기",
            onClick = {
                if (canSave) {
                    val finalNickname = if (nicknameChanged) nickname else originalNickname
                    val gender = selectedGenderIndex?.let { genderOptions.getOrNull(it) }
                    val birth = if (birthYear != null && birthMonth != null && birthDay != null) {
                        "%04d-%02d-%02d".format(birthYear!!, birthMonth!!, birthDay!!)
                    } else null
                    onSaveClick(MypageReqDTO(nickname = finalNickname, gender = gender, birth = birth))
                }
            },
            enabled = canSave,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding(),
        )
    }

    if (showDatePicker) {
        DatePickerBottomSheet(
            initialYear = birthYear ?: 1997,
            initialMonth = birthMonth ?: 1,
            initialDay = birthDay ?: 1,
            onConfirm = { y, m, d ->
                birthYear = y; birthMonth = m; birthDay = d
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showPhotoSheet) {
        ProfilePhotoBottomSheet(
            onCamera = { onOpenCamera(); showPhotoSheet = false },
            onGallery = { onOpenGallery(); showPhotoSheet = false },
            onDismiss = { showPhotoSheet = false },
        )
    }
}

@Composable
private fun NicknameFieldWithCheck(
    value: String,
    onValueChange: (String) -> Unit,
    nicknameCheckState: NicknameCheckState,
    isCheckEnabled: Boolean,
    onCheckClick: () -> Unit,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    val (validationText, validationColor) = when (nicknameCheckState) {
        is NicknameCheckState.Available -> nicknameCheckState.message to colors.uiPointGreen200
        is NicknameCheckState.Duplicated -> nicknameCheckState.message to colors.uiPointRed
        is NicknameCheckState.Error -> nicknameCheckState.message to colors.uiPointRed
        else -> null to colors.uiPointRed
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "닉네임", style = typography.medium16, color = colors.grey900)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, colors.grey300, RoundedCornerShape(16.dp))
                .background(colors.white)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                textStyle = TextStyle(
                    fontFamily = typography.regular16.fontFamily,
                    fontSize = 16.sp,
                    color = colors.grey900,
                ),
                cursorBrush = SolidColor(colors.grey900),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxHeight()) {
                        if (value.isEmpty()) {
                            Text("닉네임을 입력하세요", style = typography.regular16, color = colors.grey400)
                        }
                        innerTextField()
                    }
                },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCheckEnabled) colors.grey900 else colors.grey400)
                    .clickable(enabled = isCheckEnabled, onClick = onCheckClick)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "중복 확인",
                    style = typography.medium14,
                    color = if (isCheckEnabled) colors.white else colors.grey100,
                )
            }
        }
        Text(
            text = "한글, 영문, 숫자 공백 포함 10자 이내",
            style = typography.regular12,
            color = colors.grey500,
        )
        if (validationText != null) {
            Text(
                text = validationText,
                style = typography.regular12,
                color = validationColor,
            )
        }
    }
}

@Composable
private fun GenderField(selectedIndex: Int?, onSelect: (Int) -> Unit) {
    val options = listOf("여성", "남성", "선택 안함")
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "성별", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
        // 여성/남성: 큰 버튼 고정 너비, 선택 안함: 작은 버튼 (온보딩과 동일)
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            options.forEachIndexed { index, label ->
                val isSelected = selectedIndex == index
                val modifier = if (index < 2) Modifier.width(119.dp) else Modifier.weight(1f)
                Box(
                    modifier = modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) BookiiBookiiTheme.colors.uiMainPale else BookiiBookiiTheme.colors.white)
                        .border(
                            1.dp,
                            if (isSelected) BookiiBookiiTheme.colors.uiMain150 else BookiiBookiiTheme.colors.grey300,
                            RoundedCornerShape(16.dp),
                        )
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = BookiiBookiiTheme.typography.regular15,
                        color = if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey500,
                    )
                }
            }
        }
    }
}

@Composable
private fun BirthDateField(value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "생년월일", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BookiiBookiiTheme.colors.white)
                .border(1.dp, BookiiBookiiTheme.colors.grey300, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (value.isEmpty()) "YYYY.MM.DD" else value,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = if (value.isEmpty()) BookiiBookiiTheme.colors.grey400 else BookiiBookiiTheme.colors.grey900,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_calender),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.grey500,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerBottomSheet(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onConfirm: (year: Int, month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = remember { Calendar.getInstance() }
    val currentYear = today.get(Calendar.YEAR)
    val years = remember { (1900..currentYear).toList() }
    val months = remember { (1..12).toList() }

    val yearState = rememberLazyListState(initialFirstVisibleItemIndex = (initialYear - 1900).coerceIn(0, years.lastIndex))
    val monthState = rememberLazyListState(initialFirstVisibleItemIndex = (initialMonth - 1).coerceIn(0, 11))

    val selectedYear by remember { derivedStateOf { years.getOrElse(yearState.firstVisibleItemIndex) { currentYear } } }
    val selectedMonth by remember { derivedStateOf { monthState.firstVisibleItemIndex + 1 } }
    val daysInMonth by remember {
        derivedStateOf {
            Calendar.getInstance().apply { set(selectedYear, selectedMonth - 1, 1) }
                .getActualMaximum(Calendar.DAY_OF_MONTH)
        }
    }
    val days = remember(daysInMonth) { (1..daysInMonth).toList() }
    val dayState = rememberLazyListState(initialFirstVisibleItemIndex = (initialDay - 1).coerceIn(0, 30))
    val selectedDay by remember { derivedStateOf { (dayState.firstVisibleItemIndex + 1).coerceAtMost(daysInMonth) } }

    LaunchedEffect(daysInMonth) {
        if (dayState.firstVisibleItemIndex >= daysInMonth) dayState.scrollToItem(daysInMonth - 1)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BookiiBookiiTheme.colors.white,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 4.dp)
                        .clip(RoundedCornerShape(300.dp))
                        .background(BookiiBookiiTheme.colors.grey200),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "생년월일", style = BookiiBookiiTheme.typography.semibold20, color = BookiiBookiiTheme.colors.grey900)
                Text(
                    text = "완료",
                    style = BookiiBookiiTheme.typography.regular20,
                    color = BookiiBookiiTheme.colors.grey500,
                    modifier = Modifier.clickable { onConfirm(selectedYear, selectedMonth, selectedDay) },
                )
            }

            val itemHeightDp = 41.dp
            Row(
                modifier = Modifier.fillMaxWidth().height(itemHeightDp * 5),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                WheelPickerColumn(items = years.map { "${it}년" }, listState = yearState, itemHeight = itemHeightDp, modifier = Modifier.weight(1f), highlightShape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                WheelPickerColumn(items = months.map { "${it}월" }, listState = monthState, itemHeight = itemHeightDp, modifier = Modifier.weight(1f), highlightShape = RoundedCornerShape(0.dp))
                WheelPickerColumn(items = days.map { "${it}일" }, listState = dayState, itemHeight = itemHeightDp, modifier = Modifier.weight(1f), highlightShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WheelPickerColumn(
    items: List<String>,
    listState: LazyListState,
    itemHeight: Dp,
    modifier: Modifier = Modifier,
    highlightShape: Shape = RoundedCornerShape(20.dp),
) {
    val snapBehavior = rememberSnapFlingBehavior(listState)
    val selectedIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    Box(modifier = modifier.height(itemHeight * 5)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.Center)
                .clip(highlightShape)
                .background(BookiiBookiiTheme.colors.uiMainPale),
        )
        LazyColumn(state = listState, flingBehavior = snapBehavior, modifier = Modifier.fillMaxSize()) {
            items(count = 2) { Spacer(modifier = Modifier.height(itemHeight)) }
            itemsIndexed(items) { index, text ->
                val isSelected = index == selectedIndex
                Box(modifier = Modifier.fillMaxWidth().height(itemHeight), contentAlignment = Alignment.Center) {
                    Text(
                        text = text,
                        style = if (isSelected) BookiiBookiiTheme.typography.semibold18 else BookiiBookiiTheme.typography.regular18,
                        color = if (isSelected) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey900,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            items(count = 2) { Spacer(modifier = Modifier.height(itemHeight)) }
        }
    }
}

private val previewProfile = UserProfileResDTO(
    userId = 1L,
    profileImageUrl = null,
    nickname = "부키",
    introduction = "매일 한 챕터씩 읽는 중입니다.",
    userBooks = emptyList(),
    bookReviewCount = 0,
    recentBookReviews = emptyList(),
    boomUpCount = 0,
    recentReceivedReviews = emptyList(),
)

@Preview(name = "프로필 수정 - 닉네임 사용가능", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun ProfileSettingScreenPreview() {
    BookiiBookiiTheme {
        ProfileSettingScreen(
            profile = previewProfile,
            nicknameCheckState = NicknameCheckState.Available("사용 가능한 닉네임이에요"),
        )
    }
}

@Preview(name = "프로필 수정 - 빈 상태", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun ProfileSettingScreenEmptyPreview() {
    BookiiBookiiTheme {
        ProfileSettingScreen()
    }
}
