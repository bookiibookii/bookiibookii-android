package com.bookiibookii.bookiibookii.error.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.error.model.ErrorType
import com.bookiibookii.bookiibookii.ui.component.FooterButton
import com.bookiibookii.bookiibookii.ui.component.FooterButtonStyle
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 공통 에러 화면
@Composable
fun ErrorScreen(
    type: ErrorType,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onGoMain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val is404 = type == ErrorType.NO_PERMISSION || type == ErrorType.GROUP_DELETED || type == ErrorType.GROUP_CLOSED

    val illustration = if (is404) R.drawable.il_404_graphic else R.drawable.il_error_graphic

    val title = when (type) {
        ErrorType.SYSTEM -> stringResource(R.string.type_system_error_title)
        ErrorType.NETWORK -> stringResource(R.string.type_network_error_title)
        ErrorType.NO_PERMISSION -> stringResource(R.string.type_permission_title)
        ErrorType.GROUP_DELETED -> stringResource(R.string.type_group_delete_title)
        ErrorType.GROUP_CLOSED -> stringResource(R.string.type_group_close_title)
    }
    val desc = if (type == ErrorType.NETWORK) {
        stringResource(R.string.type_network_error_desc)
    } else {
        null
    }

    // 그래픽은 상단 고정, Footer는 하단 고정
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.grey100)
            .padding(bottom = 16.dp),
    ) {
        // 상단: 그래픽 + 텍스트
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 그래픽 영역: Figma 비율 412:560 유지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(412f / 560f),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.il_error_graphic_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                Image(
                    painter = painterResource(illustration),
                    contentDescription = null,
                    modifier = Modifier
                        .width(220.dp)
                        .height(260.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = title,
                style = BookiiBookiiTheme.typography.medium18,
                color = BookiiBookiiTheme.colors.grey900,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            if (desc != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey700,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }

        // 하단 CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
        ) {
            if (is404) {
                FooterButton(
                    text = stringResource(R.string.com_btn_main),
                    onClick = onGoMain,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            } else {
                FooterButton(
                    text = stringResource(R.string.com_btn_back),
                    onClick = onBack,
                    style = FooterButtonStyle.Grey,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(12.dp))
                FooterButton(
                    text = stringResource(R.string.com_btn_retry),
                    onClick = onRetry,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Preview(name = "시스템 장애", showBackground = true)
@Composable
private fun ErrorScreenSystemPreview() {
    BookiiPreview {
        ErrorScreen(
            type = ErrorType.SYSTEM,
            onRetry = {},
            onBack = {},
            onGoMain = {},
        )
    }
}

@Preview(name = "네트워크 오류", showBackground = true)
@Composable
private fun ErrorScreenNetworkPreview() {
    BookiiPreview {
        ErrorScreen(
            type = ErrorType.NETWORK,
            onRetry = {},
            onBack = {},
            onGoMain = {},
        )
    }
}

@Preview(name = "접근 권한 없음", showBackground = true)
@Composable
private fun ErrorScreenNoPermissionPreview() {
    BookiiPreview {
        ErrorScreen(
            type = ErrorType.NO_PERMISSION,
            onRetry = {},
            onBack = {},
            onGoMain = {},
        )
    }
}

@Preview(name = "삭제된 페이지", showBackground = true)
@Composable
private fun ErrorScreenGroupDeletedPreview() {
    BookiiPreview {
        ErrorScreen(
            type = ErrorType.GROUP_DELETED,
            onRetry = {},
            onBack = {},
            onGoMain = {},
        )
    }
}

@Preview(name = "종료된 그룹", showBackground = true)
@Composable
private fun ErrorScreenGroupClosedPreview() {
    BookiiPreview {
        ErrorScreen(
            type = ErrorType.GROUP_CLOSED,
            onRetry = {},
            onBack = {},
            onGoMain = {},
        )
    }
}
