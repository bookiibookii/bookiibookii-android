package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingCardShareBottomSheet(
    onDismiss: () -> Unit,
    onKakaoClick: () -> Unit = {},
    onInstaClick: () -> Unit = {},
    onXClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onCopyLinkClick: () -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BookiiBookiiTheme.colors.white,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),

        dragHandle = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.width(44.dp).height(4.dp).clip(RoundedCornerShape(50.dp)).background(BookiiBookiiTheme.colors.grey200))
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            Text(
                text = "공유하기",
                style = BookiiBookiiTheme.typography.semibold20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
            HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ShareOption(label = "카카오톡", onClick = onKakaoClick) {
                    // 로그인 화면과 동일: 노란 원(#FEE500) + 카카오 아이콘
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
                ShareOption(label = "인스타그램", onClick = onInstaClick) {
                    Icon(painter = painterResource(R.drawable.ic_insta), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(56.dp))
                }
                ShareOption(label = "X", onClick = onXClick) {
                    // 마이페이지 프로필 공유와 동일: img_share_x 56dp
                    Icon(painter = painterResource(R.drawable.img_share_x), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(56.dp))
                }
                ShareOption(label = "다운로드", onClick = onDownloadClick) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(BookiiBookiiTheme.colors.grey100), contentAlignment = Alignment.Center) {
                        Icon(painter = painterResource(R.drawable.ic_download), contentDescription = null, tint = BookiiBookiiTheme.colors.grey700, modifier = Modifier.size(24.dp))
                    }
                }
                ShareOption(label = "링크 복사", onClick = onCopyLinkClick) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(BookiiBookiiTheme.colors.grey100), contentAlignment = Alignment.Center) {
                        Icon(painter = painterResource(R.drawable.ic_link), contentDescription = null, tint = BookiiBookiiTheme.colors.grey700, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareOption(
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onClick() },
    ) {
        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) { icon() }
        Text(text = label, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey700)
    }
}
