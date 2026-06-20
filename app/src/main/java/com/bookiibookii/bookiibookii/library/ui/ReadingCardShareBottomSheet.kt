package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun ReadingCardShareBottomSheet(
    onDismiss: () -> Unit,
    onKakaoClick: () -> Unit = {},
    onInstaClick: () -> Unit = {},
    onXClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onCopyLinkClick: () -> Unit = {},
) {
    val visibleState = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) { visibleState.targetState = true }
    LaunchedEffect(visibleState.currentState, visibleState.targetState) {
        if (!visibleState.targetState && !visibleState.currentState) onDismiss()
    }
    val hide: () -> Unit = { visibleState.targetState = false }

    val navigationBarBottomPadding = WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()
        .coerceAtLeast(24.dp)

    Popup(
        alignment = Alignment.BottomCenter,
        properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = false),
        onDismissRequest = hide,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val scrimAlpha by animateFloatAsState(
                targetValue = if (visibleState.targetState) 0.5f else 0f,
                animationSpec = tween(durationMillis = 220),
                label = "scrim",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { hide() },
            )
            AnimatedVisibility(
                visibleState = visibleState,
                enter = slideInVertically(animationSpec = tween(durationMillis = 220)) { it },
                exit = slideOutVertically(animationSpec = tween(durationMillis = 220)) { it },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(BookiiBookiiTheme.colors.white),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(modifier = Modifier.width(44.dp).height(4.dp).clip(RoundedCornerShape(50.dp)).background(BookiiBookiiTheme.colors.grey200))
                    }
                    ShareSheetContent(
                        bottomPadding = navigationBarBottomPadding,
                        onKakaoClick = { hide(); onKakaoClick() },
                        onInstaClick = { hide(); onInstaClick() },
                        onXClick = { hide(); onXClick() },
                        onDownloadClick = { hide(); onDownloadClick() },
                        onCopyLinkClick = { hide(); onCopyLinkClick() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareSheetContent(
    onKakaoClick: () -> Unit,
    onInstaClick: () -> Unit,
    onXClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    bottomPadding: Dp = 0.dp,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = bottomPadding)) {
        Text(
            text = "공유하기",
            style = BookiiBookiiTheme.typography.semibold20,
            color = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ShareOption(label = "카카오톡", onClick = onKakaoClick, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE500)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_kakao),
                        contentDescription = null,
                        tint = Color(0xFF3A1D1D),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            ShareOption(label = "인스타그램", onClick = onInstaClick, modifier = Modifier.weight(1f)) {
                Icon(painter = painterResource(R.drawable.ic_insta), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(56.dp))
            }
            ShareOption(label = "X", onClick = onXClick, modifier = Modifier.weight(1f)) {
                Icon(painter = painterResource(R.drawable.img_share_x), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(56.dp))
            }
            ShareOption(label = "다운로드", onClick = onDownloadClick, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(BookiiBookiiTheme.colors.grey100), contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(R.drawable.ic_download), contentDescription = null, tint = BookiiBookiiTheme.colors.grey700, modifier = Modifier.size(24.dp))
                }
            }
            ShareOption(label = "링크 복사", onClick = onCopyLinkClick, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(BookiiBookiiTheme.colors.grey100), contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(R.drawable.ic_link), contentDescription = null, tint = BookiiBookiiTheme.colors.grey700, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ShareOption(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clickable { onClick() },
    ) {
        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) { icon() }
        Text(
            text = label,
            style = BookiiBookiiTheme.typography.regular12,
            color = BookiiBookiiTheme.colors.grey700,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadingCardShareSheetContentPreview() {
    BookiiPreview {
        ShareSheetContent(
            onKakaoClick = {},
            onInstaClick = {},
            onXClick = {},
            onDownloadClick = {},
            onCopyLinkClick = {},
        )
    }
}
