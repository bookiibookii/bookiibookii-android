package com.bookiibookii.bookiibookii.tracker.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.tracker.model.TrackerProfileItem
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerDetailContent
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStep
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStepStatus
import com.bookiibookii.bookiibookii.tracker.vm.TrackerDetailViewModel
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview

@Composable
fun TrackerDetailRoute(
    groupId: Long,
    onBackClick: () -> Unit,
    viewModel: TrackerDetailViewModel = viewModel(
        factory = TrackerDetailViewModel.factory(groupId)
    ),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    TrackerDetailScreen(
        groupName = uiState.groupName,
        dDay = uiState.dDay,
        statusLabel = uiState.statusLabel,
        currentStepLabel = uiState.currentStepLabel,
        myProfile = uiState.myProfile,
        partnerProfile = uiState.partnerProfile,
        exchangeLabel = uiState.exchangeLabel,
        secondaryActionLabel = uiState.secondaryActionLabel,
        primaryActionLabel = uiState.primaryActionLabel,
        steps = uiState.steps,
        onBackClick = onBackClick,
        // TODO: 메시지/더보기/액션 API 연동 전까지 placeholder
        onMessageClick = {},
        onMoreClick = {},
        onSecondaryActionClick = {},
        onPrimaryActionClick = {},
    )
}

@Composable
fun TrackerDetailScreen(
    groupName: String,
    dDay: String,
    statusLabel: String,
    currentStepLabel: String,
    myProfile: TrackerProfileItem,
    partnerProfile: TrackerProfileItem,
    exchangeLabel: String,
    secondaryActionLabel: String,
    primaryActionLabel: String,
    steps: List<TrackerStep>,
    onBackClick: () -> Unit,
    onMessageClick: () -> Unit,
    onMoreClick: () -> Unit,
    onSecondaryActionClick: () -> Unit,
    onPrimaryActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackerDetailContent(
        groupName = groupName,
        dDay = dDay,
        statusLabel = statusLabel,
        currentStepLabel = currentStepLabel,
        myProfile = myProfile,
        partnerProfile = partnerProfile,
        exchangeLabel = exchangeLabel,
        secondaryActionLabel = secondaryActionLabel,
        primaryActionLabel = primaryActionLabel,
        steps = steps,
        onBackClick = onBackClick,
        onMessageClick = onMessageClick,
        onMoreClick = onMoreClick,
        onSecondaryActionClick = onSecondaryActionClick,
        onPrimaryActionClick = onPrimaryActionClick,
        modifier = modifier,
    )
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun TrackerDetailScreenPreview() {
    BookiiPreview {
        TrackerDetailScreen(
            groupName = "김영하 도장깨기 하실 분",
            dDay = "D-2",
            statusLabel = "살인자의 기억법 · 후기 작성",
            currentStepLabel = "내 책 읽기",
            myProfile = TrackerProfileItem(
                nickname = "나",
                bookTitle = "살인자의 기억법",
                bookCoverUrl = null,
                profileImageUrl = null,
                progressPercent = 100,
                isOwnerBook = true,
            ),
            partnerProfile = TrackerProfileItem(
                nickname = "noshel",
                bookTitle = "작별인사",
                bookCoverUrl = null,
                profileImageUrl = null,
                progressPercent = 0,
                isOwnerBook = false,
            ),
            exchangeLabel = "직접 교환",
            secondaryActionLabel = "독서카드 작성",
            primaryActionLabel = "책 후기 작성",
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
            onBackClick = {},
            onMessageClick = {},
            onMoreClick = {},
            onSecondaryActionClick = {},
            onPrimaryActionClick = {},
        )
    }
}
