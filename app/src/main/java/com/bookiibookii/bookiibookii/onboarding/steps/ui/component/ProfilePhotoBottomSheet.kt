package com.bookiibookii.bookiibookii.onboarding.steps.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfilePhotoBottomSheet(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onDefaultImage: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        shape = RectangleShape,
        dragHandle = null,
    ) {
        ProfilePhotoBottomSheetContent(
            onCamera = onCamera,
            onGallery = onGallery,
            onDefaultImage = onDefaultImage,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun ProfilePhotoBottomSheetContent(
    onCamera: () -> Unit = {},
    onGallery: () -> Unit = {},
    onDefaultImage: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
    ) {
        // ── 시트 카드 ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0x1A000000),
                    ambientColor = Color(0x1A000000),
                )
                .clip(RoundedCornerShape(20.dp))
                .background(colors.white)
                .padding(16.dp),
        ) {
            // 제목
            Text(
                text = "프로필 사진 변경",
                style = typography.semibold20,
                color = colors.grey900,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
            )
            HorizontalDivider(thickness = 1.dp, color = colors.grey100)

            // 사진 촬영
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onCamera,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    )
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("사진 촬영", style = typography.regular16, color = colors.grey900)
            }
            HorizontalDivider(thickness = 1.dp, color = colors.grey100)

            // 앨범에서 선택
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onGallery,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    )
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_image),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("앨범에서 선택", style = typography.regular16, color = colors.grey900)
            }
            HorizontalDivider(thickness = 1.dp, color = colors.grey100)

            // 기본 이미지 선택
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onDefaultImage,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    )
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("기본 이미지 선택", style = typography.regular16, color = colors.grey900)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── 취소 버튼 ──────────────────────────────────────────────────────────
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.grey200, RoundedCornerShape(20.dp))
                .background(colors.white)
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("취소", style = typography.regular16, color = colors.grey900)
        }
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "프로필 사진 바텀시트")
@Composable
private fun ProfilePhotoBottomSheetPreview() {
    BookiiBookiiTheme {
        ProfilePhotoBottomSheetContent()
    }
}
