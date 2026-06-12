package com.bookiibookii.bookiibookii.onboarding.steps.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BirthdatePickerBottomSheet(
    onDone: (year: Int, month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit,
    initialDate: String? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = BookiiBookiiTheme.colors.white,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null
    ) {
        BirthdatePickerContent(onDone = onDone, initialDate = initialDate)
    }
}

@Composable
private fun BirthdatePickerContent(
    onDone: (year: Int, month: Int, day: Int) -> Unit = { _, _, _ -> },
    initialDate: String? = null,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    val today = remember { java.util.Calendar.getInstance() }
    val currentYear = remember { today.get(java.util.Calendar.YEAR) }
    val currentMonth = remember { today.get(java.util.Calendar.MONTH) + 1 }
    val currentDay = remember { today.get(java.util.Calendar.DAY_OF_MONTH) }
    val years = remember { (1924..currentYear).map { "${it}년" } }
    val months = remember { (1..12).map { "${it}월" } }
    val days = remember { (1..31).map { "${it}일" } }

    // "yyyy-MM-dd" 형식의 이전 선택값을 각 휠의 초기 인덱스로 변환. 없거나 파싱 실패 시 기본값.
    val initial = remember(initialDate) {
        initialDate?.split("-")?.takeIf { it.size == 3 }?.let { parts ->
            val y = parts[0].toIntOrNull()
            val m = parts[1].toIntOrNull()
            val d = parts[2].toIntOrNull()
            if (y != null && m != null && d != null) Triple(y, m, d) else null
        }
    }

    var selectedYearIndex by remember {
        mutableStateOf(initial?.first?.let { (it - 1924).coerceIn(0, years.size - 1) } ?: (years.size - 1))
    }
    var selectedMonthIndex by remember {
        mutableStateOf(initial?.second?.let { (it - 1).coerceIn(0, months.size - 1) } ?: 0)
    }
    var selectedDayIndex by remember {
        mutableStateOf(initial?.third?.let { (it - 1).coerceIn(0, days.size - 1) } ?: 0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(4.dp)
                .clip(BookiiBookiiTheme.shape.round20)
                .background(colors.grey200)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("생년월일", style = typography.semibold20, color = colors.grey900)
            // 선택값이 오늘 이후면 미래 날짜이므로 완료를 막는다.
            val selYear = 1924 + selectedYearIndex
            val selMonth = selectedMonthIndex + 1
            val selDay = selectedDayIndex + 1
            val isFuture = selYear > currentYear ||
                    (selYear == currentYear && selMonth > currentMonth) ||
                    (selYear == currentYear && selMonth == currentMonth && selDay > currentDay)
            Text(
                text = "완료",
                style = typography.regular20,
                color = if (isFuture) colors.grey300 else colors.grey500,
                modifier = if (isFuture) Modifier else Modifier.clickable {
                    onDone(selYear, selMonth, selDay)
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 휠에서 발생한 스크롤/플링이 시트로 전파돼 시트가 따라 내려가는 걸 막는다.
        // 남은 델타를 모두 소비(available 반환)해 부모인 ModalBottomSheet로 넘기지 않는다.
        val blockSheetDragConnection = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset = available

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity
                ): Velocity = available
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(blockSheetDragConnection),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WheelPickerColumn(
                items = years,
                initialIndex = selectedYearIndex,
                onSelectedChanged = { selectedYearIndex = it },
                selectedItemShape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                modifier = Modifier.weight(1f)
            )
            WheelPickerColumn(
                items = months,
                initialIndex = selectedMonthIndex,
                onSelectedChanged = { selectedMonthIndex = it },
                selectedItemShape = RectangleShape,
                modifier = Modifier.weight(1f)
            )
            WheelPickerColumn(
                items = days,
                initialIndex = selectedDayIndex,
                onSelectedChanged = { selectedDayIndex = it },
                selectedItemShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WheelPickerColumn(
    items: List<String>,
    initialIndex: Int,
    onSelectedChanged: (Int) -> Unit,
    selectedItemShape: Shape = RoundedCornerShape(20.dp),
    modifier: Modifier = Modifier,
) {
    val typography = BookiiBookiiTheme.typography
    val colors = BookiiBookiiTheme.colors
    val itemHeightDp = 41.dp
    val itemGapDp = 10.dp
    val paddingCount = 2
    val paddedItems = remember(items) { List(paddingCount) { "" } + items + List(paddingCount) { "" } }
    val density = LocalDensity.current
    val itemTotalHeightPx = with(density) { (itemHeightDp + itemGapDp).toPx() }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()
    val selectedIndex by remember {
        derivedStateOf {
            val first = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val adjusted = if (offset > itemTotalHeightPx / 2f) first + 1 else first
            adjusted.coerceIn(0, items.size - 1)
        }
    }
    LaunchedEffect(selectedIndex) { onSelectedChanged(selectedIndex) }

    LazyColumn(
        state = listState,
        modifier = modifier.height(itemHeightDp * 5 + itemGapDp * 4),
        flingBehavior = snapBehavior,
        verticalArrangement = Arrangement.spacedBy(itemGapDp)
    ) {
        itemsIndexed(paddedItems) { index, item ->
            val isSelected = index == selectedIndex + paddingCount
            val realIndex = index - paddingCount
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeightDp)
                    .then(
                        if (isSelected) Modifier.background(colors.uiMainPale, selectedItemShape)
                        else Modifier
                    )
                    .then(
                        if (item.isNotBlank() && !isSelected) {
                            Modifier.clickable {
                                scope.launch { listState.animateScrollToItem(realIndex) }
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    style = if (isSelected) typography.semibold18 else typography.regular18,
                    color = if (isSelected) colors.uiMain else colors.grey900
                )
            }
        }
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "날짜 피커 - 전체")
@Composable
private fun BirthdatePickerContentPreview() {
    BookiiBookiiTheme {
        BirthdatePickerContent()
    }
}

@Preview(showBackground = true, name = "날짜 피커 - 바텀시트(드래그)")
@Composable
private fun BirthdatePickerBottomSheetPreview() {
    BookiiBookiiTheme {
        BirthdatePickerBottomSheet(
            onDone = { _, _, _ -> },
            onDismiss = {},
            initialDate = "1995-07-15"
        )
    }
}
