package com.bookiibookii.bookiibookii.tracker.ui.detail.direct

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.CardButton
import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle
import com.bookiibookii.bookiibookii.ui.component.CloseButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

private val WEEKDAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")

private data class DayCell(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
)

// 직접교환 약속 잡기 1/3 - 일시 선택
// initialScheduledAt: 수정 진입 시 기존 일시(ISO) 프리필용 (등록은 null)
@Composable
fun TrackerDirectMeetingTimeDialog(
    onDismiss: () -> Unit,
    onNextClick: (scheduledAt: String) -> Unit,
    initialScheduledAt: String? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDirectMeetingTimeDialogContent(
            onDismiss = onDismiss,
            onNextClick = onNextClick,
            initialScheduledAt = initialScheduledAt,
        )
    }
}

@Composable
private fun TrackerDirectMeetingTimeDialogContent(
    onDismiss: () -> Unit,
    onNextClick: (scheduledAt: String) -> Unit,
    modifier: Modifier = Modifier,
    initialScheduledAt: String? = null,
) {
    val today = remember { LocalDate.now() }
    // 기존 일시 파싱(수정 모드). 실패하거나 없으면 null → 기본값 사용
    val initial = remember(initialScheduledAt) {
        initialScheduledAt?.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDateTime.parse(it) }.getOrNull()
        }
    }
    var displayMonth by remember {
        mutableStateOf(YearMonth.from(initial?.toLocalDate() ?: today))
    }
    var selectedDate by remember { mutableStateOf(initial?.toLocalDate()) }
    var isAm by remember { mutableStateOf(initial == null || initial.hour < 12) }
    var hour by remember { mutableStateOf(initial?.let { to12Hour(it.hour) } ?: 6) }
    var minute by remember { mutableStateOf(initial?.minute ?: 0) }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StepChip(text = "1/3")
                Text(
                    text = "언제 만날까요?",
                    style = BookiiBookiiTheme.typography.bold24,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }
            CloseButton(onClick = onDismiss)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { displayMonth = displayMonth.minusMonths(1) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron),
                        contentDescription = "이전 달",
                        tint = BookiiBookiiTheme.colors.black,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    text = "${displayMonth.year}년 ${displayMonth.monthValue}월",
                    style = BookiiBookiiTheme.typography.semibold20,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { displayMonth = displayMonth.plusMonths(1) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron),
                        contentDescription = "다음 달",
                        tint = BookiiBookiiTheme.colors.black,
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(180f),
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    WEEKDAY_LABELS.forEach { label ->
                        Box(
                            modifier = Modifier.width(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                style = BookiiBookiiTheme.typography.medium16,
                                color = BookiiBookiiTheme.colors.grey700,
                            )
                        }
                    }
                }

                buildCalendarGrid(displayMonth).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        week.forEach { cell ->
                            DayCellView(
                                cell = cell,
                                today = today,
                                selectedDate = selectedDate,
                                onClick = { date ->
                                    selectedDate = if (selectedDate == date) null else date
                                },
                            )
                        }
                    }
                }
            }

            TimePickerRow(
                isAm = isAm,
                onAmPmChange = { isAm = it },
                hour = hour,
                minute = minute,
                onHourChange = { hour = it },
                onMinuteChange = { minute = it },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "취소",
                style = CardButtonStyle.Grey,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            val date = selectedDate
            CardButton(
                text = "다음",
                style = if (date != null) CardButtonStyle.Main else CardButtonStyle.Grey,
                onClick = if (date != null) {
                    { onNextClick(buildScheduledAt(date, isAm, hour, minute)) }
                } else {
                    {}
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// 선택값(날짜 + 오전/오후 + 12시간제 시 + 분)을 ISO date-time 문자열로 조립
private fun to12Hour(hour24: Int): Int = when (val h = hour24 % 12) {
    0 -> 12
    else -> h
}

private fun buildScheduledAt(date: LocalDate, isAm: Boolean, hour12: Int, minute: Int): String {
    val hour24 = when {
        isAm && hour12 == 12 -> 0        // 오전 12시 → 00시
        !isAm && hour12 != 12 -> hour12 + 12 // 오후 1~11시 → 13~23시
        else -> hour12                   // 오전 1~11시, 오후 12시
    }
    return "%04d-%02d-%02dT%02d:%02d:00".format(
        date.year, date.monthValue, date.dayOfMonth, hour24, minute,
    )
}

@Composable
private fun StepChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round8,
            )
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey200,
                shape = BookiiBookiiTheme.shape.round8,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium14,
            color = BookiiBookiiTheme.colors.grey900,
        )
    }
}

private val HOUR_OPTIONS = (1..12).toList()
private val MINUTE_OPTIONS = (0..55 step 5).toList()

@Composable
private fun TimePickerRow(
    isAm: Boolean,
    onAmPmChange: (Boolean) -> Unit,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = BookiiBookiiTheme.colors.uiBg)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AmPmToggle(isAm = isAm, onChange = onAmPmChange)
            TimeBox(
                value = hour,
                suffix = "시",
                options = HOUR_OPTIONS,
                onSelect = onHourChange,
            )
            TimeBox(
                value = minute,
                suffix = "분",
                options = MINUTE_OPTIONS,
                onSelect = onMinuteChange,
            )
        }
        HorizontalDivider(thickness = 1.dp, color = BookiiBookiiTheme.colors.uiBg)
    }
}

@Composable
private fun AmPmToggle(
    isAm: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(
                color = BookiiBookiiTheme.colors.grey200,
                shape = BookiiBookiiTheme.shape.round12,
            )
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AmPmChip(text = "오전", selected = isAm, onClick = { onChange(true) })
        AmPmChip(text = "오후", selected = !isAm, onClick = { onChange(false) })
    }
}

@Composable
private fun AmPmChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) {
                    BookiiBookiiTheme.colors.white
                } else {
                    BookiiBookiiTheme.colors.grey200
                },
                shape = BookiiBookiiTheme.shape.round10,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey900,
        )
    }
}

@Composable
private fun TimeBox(
    value: Int,
    suffix: String,
    options: List<Int>,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = BookiiBookiiTheme.shape.round12,
                    )
                    .clickable { expanded = true }
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "%02d".format(value),
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                // 기본 surface 톤 대신 흰 배경으로 고정
                containerColor = BookiiBookiiTheme.colors.white,
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "%02d".format(option),
                                style = BookiiBookiiTheme.typography.regular14,
                                color = BookiiBookiiTheme.colors.grey900,
                            )
                        },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
        Text(
            text = suffix,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey900,
        )
    }
}

private fun buildCalendarGrid(month: YearMonth): List<List<DayCell>> {
    val firstDayOfMonth = month.atDay(1)
    val daysFromPrevMonth = firstDayOfMonth.dayOfWeek.value % 7
    val gridStart = firstDayOfMonth.minusDays(daysFromPrevMonth.toLong())
    return List(6) { week ->
        List(7) { day ->
            val date = gridStart.plusDays((week * 7 + day).toLong())
            DayCell(
                date = date,
                isCurrentMonth = YearMonth.from(date) == month,
            )
        }
    }
}

@Composable
private fun DayCellView(
    cell: DayCell,
    today: LocalDate,
    selectedDate: LocalDate?,
    onClick: (LocalDate) -> Unit,
) {
    val isCurrentMonth = cell.isCurrentMonth
    val isSelectable = isCurrentMonth && cell.date.isAfter(today)
    val isToday = isCurrentMonth && cell.date == today
    val isSelected = isCurrentMonth && cell.date == selectedDate

    val textColor = when {
        !isCurrentMonth -> BookiiBookiiTheme.colors.grey400
        isSelected -> BookiiBookiiTheme.colors.uiMain
        isToday -> BookiiBookiiTheme.colors.uiMainSub
        else -> BookiiBookiiTheme.colors.grey900
    }

    val bgColor = if (isSelected) {
        BookiiBookiiTheme.colors.uiMainPale
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color = bgColor, shape = BookiiBookiiTheme.shape.round10)
            .clickable(enabled = isSelectable) { onClick(cell.date) },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cell.date.dayOfMonth.toString(),
            style = BookiiBookiiTheme.typography.medium15,
            color = textColor,
        )
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        color = BookiiBookiiTheme.colors.uiMainSub,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerDirectMeetingTimeDialogPreview() {
    BookiiPreview {
        TrackerDirectMeetingTimeDialogContent(
            onDismiss = {},
            onNextClick = {},
        )
    }
}
