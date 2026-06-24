package com.bookiibookii.bookiibookii.notification.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.notification.model.toUiModel
import com.bookiibookii.bookiibookii.notification.nav.NotificationRedirect
import com.bookiibookii.bookiibookii.notification.nav.NotificationRedirectRouter
import com.bookiibookii.bookiibookii.notification.vm.NotificationCenterViewModel

@Composable
fun NotificationRoute(
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onRedirect: (NotificationRedirect) -> Unit,
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
            // 원본 알림(payload 보유)을 찾아 redirectType 기반으로 화면 이동
            uiState.items.firstOrNull { it.id == item.id }
                ?.let { NotificationRedirectRouter.fromPayload(it.payload) }
                ?.let(onRedirect)
        },
        onLoadMore = viewModel::loadNextPage,
        modifier = modifier,
    )
}
