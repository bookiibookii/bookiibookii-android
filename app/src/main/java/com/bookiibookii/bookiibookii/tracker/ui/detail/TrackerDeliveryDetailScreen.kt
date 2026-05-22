package com.bookiibookii.bookiibookii.tracker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun TrackerDeliveryDetailScreen(
    onBackClick: () -> Unit,
    onMessageClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        TrackerDetailHeader(
            onBackClick = onBackClick,
            onMessageClick = onMessageClick,
            onMoreClick = onMoreClick,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // TODO B1~B3: 트래커 상세 카드 (그룹 정보 + 두 트래커 + 액션 버튼)
            // TODO C: TrackerStepList
        }
    }
}

@Composable
private fun TrackerDetailHeader(
    onBackClick: () -> Unit,
    onMessageClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconCircleButton(
                iconRes = R.drawable.ic_back,
                onClick = onBackClick,
            )
            Text(
                text = "교환 현황",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconCircleButton(
                    iconRes = R.drawable.ic_message,
                    onClick = onMessageClick,
                )
                IconCircleButton(
                    iconRes = R.drawable.ic_meetball,
                    onClick = onMoreClick,
                )
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = BookiiBookiiTheme.colors.grey200,
        )
    }
}

@Composable
private fun IconCircleButton(
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = Color.Unspecified,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerDetailHeaderPreview() {
    BookiiPreview {
        TrackerDetailHeader(
            onBackClick = {},
            onMessageClick = {},
            onMoreClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun TrackerDeliveryDetailScreenPreview() {
    BookiiPreview {
        TrackerDeliveryDetailScreen(
            onBackClick = {},
            onMessageClick = {},
            onMoreClick = {},
        )
    }
}
