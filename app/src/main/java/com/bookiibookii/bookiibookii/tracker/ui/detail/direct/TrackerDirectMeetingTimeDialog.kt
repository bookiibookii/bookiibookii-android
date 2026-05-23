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
import java.time.YearMonth

private val WEEKDAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")

private data class DayCell(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
)

// 직접교환 약속 잡기 1/3 - 일시 선택
@Composable
fun TrackerDirectMeetingTimeDialog(
    onDismiss: () -> Unit,
    onNextClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerDirectMeetingTimeDialogContent(
            onDismiss = onDismiss,
            onNextClick = onNextClick,
        )
    }
}

@Composable
private fun TrackerDirectMeetingTimeDialogContent(
    onDismiss: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.of(2026, 5, 23)
    var displayMonth by remember { mutableStateOf(YearMonth.of(2026, 5)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var isAm by remember { mutableStateOf(true) }

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
                hour = "06",
                minute = "00",
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
            CardButton(
                text = "다음",
                style = if (selectedDate != null) CardButtonStyle.Main else CardButtonStyle.Grey,
                onClick = if (selectedDate != null) onNextClick else {{}},
                modifier = Modifier.weight(1f),
            )
        }
    }
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

@Composable
private fun TimePickerRow(
    isAm: Boolean,
    onAmPmChange: (Boolean) -> Unit,
    hour: String,
    minute: String,
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
            TimeBox(value = hour, suffix = "시")
            TimeBox(value = minute, suffix = "분")
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
private fun TimeBox(value: String, suffix: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .border(
                    width = 1.dp,
                    color = BookiiBookiiTheme.colors.grey200,
                    shape = BookiiBookiiTheme.shape.round12,
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value,
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey900,
            )
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
