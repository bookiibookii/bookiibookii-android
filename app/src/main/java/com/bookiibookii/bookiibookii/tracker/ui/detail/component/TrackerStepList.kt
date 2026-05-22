package com.bookiibookii.bookiibookii.tracker.ui.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 트래커 상세 단계 리스트 (택배/직접교환 공통)
// 진행중인 단계가 맨 위, 완료된 단계가 아래로 쌓이는 구조
sealed class TrackerStepStatus {
    data object Pending : TrackerStepStatus()

    // chipText는 외부 주입
    data class InProgress(val chipText: String) : TrackerStepStatus()
    data object Completed : TrackerStepStatus()
}

data class TrackerStep(
    val title: String,
    val description: String,
    val status: TrackerStepStatus,
)

@Composable
fun TrackerStepList(
    steps: List<TrackerStep>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = BookiiBookiiTheme.colors.white,
                shape = BookiiBookiiTheme.shape.round20,
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        steps.forEachIndexed { index, step ->
            TrackerStepItem(
                step = step,
                showDivider = index < steps.lastIndex,
            )
        }
    }
}

@Composable
private fun TrackerStepItem(
    step: TrackerStep,
    showDivider: Boolean,
) {
    val isCompleted = step.status is TrackerStepStatus.Completed
    val titleStyle = if (isCompleted) {
        BookiiBookiiTheme.typography.regular16
    } else {
        BookiiBookiiTheme.typography.medium16
    }
    val titleColor = if (isCompleted) {
        BookiiBookiiTheme.colors.grey600
    } else {
        BookiiBookiiTheme.colors.grey900
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = step.title,
                    style = titleStyle,
                    color = titleColor,
                )
                StepChip(status = step.status)
            }
            Text(
                text = step.description,
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey500,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp),
                thickness = 1.dp,
                color = BookiiBookiiTheme.colors.grey100,
            )
        }
    }
}

@Composable
private fun StepChip(status: TrackerStepStatus) {
    when (status) {
        is TrackerStepStatus.InProgress -> {
            Box(
                modifier = Modifier
                    .background(
                        color = BookiiBookiiTheme.colors.white,
                        shape = BookiiBookiiTheme.shape.round8,
                    )
                    .border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.uiMain150,
                        shape = BookiiBookiiTheme.shape.round8,
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = status.chipText,
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.uiMain,
                )
            }
        }
        TrackerStepStatus.Pending -> {
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
            ) {
                Text(
                    text = "예정",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey500,
                )
            }
        }
        TrackerStepStatus.Completed -> {
            Box(
                modifier = Modifier
                    .background(
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = BookiiBookiiTheme.shape.round8,
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "완료",
                    style = BookiiBookiiTheme.typography.regular11,
                    color = BookiiBookiiTheme.colors.grey500,
                )
            }
        }
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun TrackerStepListPreview() {
    BookiiPreview {
        TrackerStepList(
            steps = listOf(
                TrackerStep(
                    title = "반납",
                    description = "파트너에게 책을 돌려보내주세요",
                    status = TrackerStepStatus.Pending,
                ),
                TrackerStep(
                    title = "파트너 책 읽기",
                    description = "작별인사를 읽고 진행률을 기록해주세요",
                    status = TrackerStepStatus.InProgress(chipText = "D-2"),
                ),
                TrackerStep(
                    title = "교환",
                    description = "파트너와 책을 교환해주세요",
                    status = TrackerStepStatus.Completed,
                ),
                TrackerStep(
                    title = "살인자의 기억법 읽기",
                    description = "독서카드를 작성하면 교환독서가 더 즐거워져요",
                    status = TrackerStepStatus.Completed,
                ),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
