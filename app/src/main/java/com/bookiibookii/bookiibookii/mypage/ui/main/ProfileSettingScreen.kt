package com.bookiibookii.bookiibookii.mypage.ui.main

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.ui.component.FooterButton

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import androidx.compose.ui.graphics.Shape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
) {
    var nickname by remember { mutableStateOf("김스카이") }
    var selectedGenderIndex by remember { mutableStateOf<Int?>(null) }
    var birthYear by remember { mutableStateOf<Int?>(null) }
    var birthMonth by remember { mutableStateOf<Int?>(null) }
    var birthDay by remember { mutableStateOf<Int?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

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
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(128.dp)) {
                    ProfilePlaceholder(modifier = Modifier.size(128.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(BookiiBookiiTheme.colors.grey600)
                            .clickable { },
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
                NicknameField(value = nickname, onValueChange = { nickname = it })
                GenderField(selectedIndex = selectedGenderIndex, onSelect = { selectedGenderIndex = it })
                BirthDateField(value = birthDateDisplay, onClick = { showDatePicker = true })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        FooterButton(
            text = "수정하기",
            onClick = onSaveClick,
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
                birthYear = y
                birthMonth = m
                birthDay = d
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
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

    val yearState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialYear - 1900).coerceIn(0, years.lastIndex)
    )
    val monthState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialMonth - 1).coerceIn(0, 11)
    )

    val selectedYear by remember { derivedStateOf { years.getOrElse(yearState.firstVisibleItemIndex) { currentYear } } }
    val selectedMonth by remember { derivedStateOf { monthState.firstVisibleItemIndex + 1 } }
    val daysInMonth by remember {
        derivedStateOf {
            Calendar.getInstance().apply {
                set(selectedYear, selectedMonth - 1, 1)
            }.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
    }
    val days = remember(daysInMonth) { (1..daysInMonth).toList() }

    val dayState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialDay - 1).coerceIn(0, 30)
    )
    val selectedDay by remember { derivedStateOf { (dayState.firstVisibleItemIndex + 1).coerceAtMost(daysInMonth) } }

    LaunchedEffect(daysInMonth) {
        if (dayState.firstVisibleItemIndex >= daysInMonth) {
            dayState.scrollToItem(daysInMonth - 1)
        }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "생년월일",
                    style = BookiiBookiiTheme.typography.semibold20,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = "완료",
                    style = BookiiBookiiTheme.typography.regular20,
                    color = BookiiBookiiTheme.colors.grey500,
                    modifier = Modifier.clickable {
                        onConfirm(selectedYear, selectedMonth, selectedDay)
                    },
                )
            }

            val itemHeightDp = 41.dp

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeightDp * 5),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                WheelPickerColumn(
                    items = years.map { "${it}년" },
                    listState = yearState,
                    itemHeight = itemHeightDp,
                    modifier = Modifier.weight(1f),
                    highlightShape = RoundedCornerShape(
                        topStart = 20.dp, bottomStart = 20.dp,
                        topEnd = 0.dp, bottomEnd = 0.dp,
                    ),
                )
                WheelPickerColumn(
                    items = months.map { "${it}월" },
                    listState = monthState,
                    itemHeight = itemHeightDp,
                    modifier = Modifier.weight(1f),
                    highlightShape = RoundedCornerShape(0.dp),
                )
                WheelPickerColumn(
                    items = days.map { "${it}일" },
                    listState = dayState,
                    itemHeight = itemHeightDp,
                    modifier = Modifier.weight(1f),
                    highlightShape = RoundedCornerShape(
                        topStart = 0.dp, bottomStart = 0.dp,
                        topEnd = 20.dp, bottomEnd = 20.dp,
                    ),
                )
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
    LazyColumn(
        state = listState,
        flingBehavior = snapBehavior,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(count = 2) {
            Spacer(modifier = Modifier.height(itemHeight))
        }
        itemsIndexed(items) { index, text ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    style = if (isSelected) BookiiBookiiTheme.typography.semibold18
                    else BookiiBookiiTheme.typography.regular18,
                    color = if (isSelected) BookiiBookiiTheme.colors.uiMain
                    else BookiiBookiiTheme.colors.grey900,
                    textAlign = TextAlign.Center,
                )
            }
        }
        items(count = 2) {
            Spacer(modifier = Modifier.height(itemHeight))
        }
    }
    }
}

@Composable
private fun NicknameField(value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "닉네임", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
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
                textStyle = BookiiBookiiTheme.typography.regular16.copy(color = BookiiBookiiTheme.colors.grey900),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(text = "닉네임을 입력하세요", style = BookiiBookiiTheme.typography.regular16, color = BookiiBookiiTheme.colors.grey400)
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Composable
private fun GenderField(selectedIndex: Int?, onSelect: (Int) -> Unit) {
    val options = listOf("여성", "남성", "선택 안함")
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "성별", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            options.forEachIndexed { index, label ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
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

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun ProfileSettingScreenPreview() {
    ProfileSettingScreen()
}
