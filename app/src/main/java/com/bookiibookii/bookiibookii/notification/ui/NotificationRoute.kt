package com.bookiibookii.bookiibookii.notification.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.notification.model.toUiModel
import com.bookiibookii.bookiibookii.notification.vm.NotificationCenterViewModel

@Composable
fun NotificationRoute(
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationCenterViewModel = viewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    NotificationScreen(
        selectedTab = uiState.selectedTab,
        notifications = uiState.items.map { it.toUiModel() },
        onBackClick = onBackClick,
        onAddClick = onAddClick,
        onTabSelect = viewModel::selectTab,
        onNotificationClick = { item ->
            if (item.isUnread) viewModel.markAsRead(item.id)
            // TODO: 알림 type/payload 기반 화면 이동 (그룹 상세/트래커 등)
        },
        onLoadMore = viewModel::loadNextPage,
        modifier = modifier,
    )
}
