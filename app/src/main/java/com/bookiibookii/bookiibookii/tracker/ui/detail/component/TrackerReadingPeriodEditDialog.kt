package com.bookiibookii.bookiibookii.tracker.ui.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import java.time.format.DateTimeFormatter

private val WEEKDAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")

private data class DayCell(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
)

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")

// 예상 독서 기간 수정 다이얼로그 (택배/직접교환 공통)
// originalEndDate: 기존 예정 종료일(오늘 + dDay로 산출)
@Composable
fun TrackerReadingPeriodEditDialog(
    originalEndDate: LocalDate?,
    onConfirm: (newEndDate: LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        TrackerReadingPeriodEditDialogContent(
            originalEndDate = originalEndDate,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun TrackerReadingPeriodEditDialogContent(
    originalEndDate: LocalDate?,
    onConfirm: (newEndDate: LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { LocalDate.now() }
    var displayMonth by remember { mutableStateOf(YearMonth.from(originalEndDate ?: today)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val originalDate: LocalDate? = originalEndDate

    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round20,
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "예상 독서 기간 수정",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
            )
            CloseButton(onClick = onDismiss)
        }

        Text(
            text = "파트너와 협의 후 예상 독서 기간을 수정해주세요.",
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey500,
            modifier = Modifier.fillMaxWidth(),
        )

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
                style = BookiiBookiiTheme.typography.medium20,
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
                            originalDate = originalDate,
                            selectedDate = selectedDate,
                            onClick = { date ->
                                selectedDate = if (selectedDate == date) null else date
                            },
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = BookiiBookiiTheme.colors.white,
                    shape = BookiiBookiiTheme.shape.round12,
                )
                .border(
                    width = 1.dp,
                    color = BookiiBookiiTheme.colors.grey200,
                    shape = BookiiBookiiTheme.shape.round12,
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "기존 예정 독서 종료일",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey700,
                )
                Text(
                    text = "|",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey700,
                )
                Text(
                    text = originalDate?.let { "${it.format(DATE_FORMATTER)}." } ?: "-",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey700,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "수정 예정 독서 종료일",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey700,
                )
                Text(
                    text = "|",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey700,
                )
                val selected = selectedDate
                if (selected == null) {
                    Text(
                        text = "-",
                        style = BookiiBookiiTheme.typography.regular14,
                        color = BookiiBookiiTheme.colors.grey700,
                    )
                } else {
                    Text(
                        text = "${selected.format(DATE_FORMATTER)}.",
                        style = BookiiBookiiTheme.typography.medium14,
                        color = BookiiBookiiTheme.colors.uiMain,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardButton(
                text = "취소",
                style = CardButtonStyle.White,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            CardButton(
                text = "수정",
                style = if (selectedDate != null) CardButtonStyle.Main else CardButtonStyle.Grey,
                onClick = { selectedDate?.let(onConfirm) },
                modifier = Modifier.weight(1f),
            )
        }
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
    originalDate: LocalDate?,
    selectedDate: LocalDate?,
    onClick: (LocalDate) -> Unit,
) {
    val isCurrentMonth = cell.isCurrentMonth
    val isSelectable = isCurrentMonth && cell.date.isAfter(today)
    val isToday = isCurrentMonth && cell.date == today
    val isOriginal = isCurrentMonth && cell.date == originalDate
    val isSelected = isCurrentMonth && cell.date == selectedDate

    val textColor = when {
        !isCurrentMonth -> BookiiBookiiTheme.colors.grey400
        isSelected -> BookiiBookiiTheme.colors.uiMain
        isToday -> BookiiBookiiTheme.colors.uiMainSub
        isOriginal -> BookiiBookiiTheme.colors.uiMain
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
private fun TrackerReadingPeriodEditDialogPreview() {
    BookiiPreview {
        TrackerReadingPeriodEditDialogContent(
            originalEndDate = LocalDate.now().plusDays(14),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
