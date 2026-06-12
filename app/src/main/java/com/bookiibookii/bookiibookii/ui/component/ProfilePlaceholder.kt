package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// profile_bg.xml의 squircle path (viewport 44×44 기준) → Compose Shape으로 변환
// 호출처가 modifier.size()로 정한 크기에 맞춰 자동 scale
private val ProfileSquircleShape: Shape = GenericShape { size, _ ->
    val sx = size.width / 44f
    val sy = size.height / 44f
    moveTo(0f, 22f * sy)
    cubicTo(0f, 3.883f * sy, 3.883f * sx, 0f, 22f * sx, 0f)
    cubicTo(40.117f * sx, 0f, size.width, 3.883f * sy, size.width, 22f * sy)
    cubicTo(size.width, 40.117f * sy, 40.117f * sx, size.height, 22f * sx, size.height)
    cubicTo(3.883f * sx, size.height, 0f, 40.117f * sy, 0f, 22f * sy)
    close()
}

// 프로필 영역 (placeholder + 실제 이미지)
// - 크기는 호출처에서 modifier.size()
// - imageUrl == null/blank → ic_profile_placeholder (grey300 squircle + 사람 실루엣)
// - imageUrl != null → 위에 AsyncImage 오버레이
// - innerStroke == true → 안쪽 1dp grey100 stroke. BookCover와 겹칠 때만 true 넣기
@Composable
fun ProfilePlaceholder(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    innerStroke: Boolean = false,
) {
    Box(modifier = modifier.clip(ProfileSquircleShape)) {
        // 배경 placeholder — 항상 깔아둠. AsyncImage 로딩 전/실패 시 노출
        Image(
            painter = painterResource(R.drawable.ic_profile_placeholder),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(ProfileSquircleShape),
            )
        }
        // 안쪽 stroke
        if (innerStroke) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey100,
                        shape = ProfileSquircleShape,
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun ProfilePlaceholderPreview() {
    BookiiPreview {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfilePlaceholder(modifier = Modifier.size(20.dp))
            ProfilePlaceholder(modifier = Modifier.size(40.dp))
            ProfilePlaceholder(modifier = Modifier.size(44.dp))
            ProfilePlaceholder(modifier = Modifier.size(48.dp))
        }
    }
}

@Preview
@Composable
private fun ProfilePlaceholderWithStrokePreview() {
    BookiiPreview {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfilePlaceholder(
                modifier = Modifier.size(44.dp),
                innerStroke = true,
            )
            ProfilePlaceholder(
                modifier = Modifier.size(44.dp),
                innerStroke = false,
            )
        }
    }
}
