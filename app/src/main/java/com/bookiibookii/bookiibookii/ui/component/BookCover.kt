package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 알라딘 표지 URL의 사이즈 토큰을 지정 사이즈로 교체. 알라딘 URL이 아니면 원본 그대로.
// 일부 응답(예: 베스트셀러)이 coversum/cover 저해상도로 내려와도 cover500으로 올려 표시.
// 경로 마지막 세그먼트(파일명) 바로 앞의 cover/coversum/cover{n} 만 교체.
// TODO: 백엔드에서 수정하면 이거 삭제
private val ALADIN_COVER_SIZE = Regex("""/(coversum|cover\d+|cover)/(?=[^/]+$)""")

private fun String.toAladinCover(size: String): String =
    if (contains("image.aladin.co.kr")) replace(ALADIN_COVER_SIZE, "/$size/") else this

// 책 표지 영역. 사이즈는 호출처에서 modifier로 지정
@Composable
fun BookCover(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    aladinCoverSize: String = "cover500",
) {
    val shape = BookiiBookiiTheme.shape.round8
    Box(
        modifier = modifier
            .clip(shape)
            .background(BookiiBookiiTheme.colors.white)
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey100,
                shape = shape,
            ),
    ) {
        if (!imageUrl.isNullOrBlank()) {
            val highRes = imageUrl.toAladinCover(aladinCoverSize)
            val fallback = imageUrl.toAladinCover("cover200")
            // cover500이 없는 책은 로딩 실패 → cover200으로 1회 폴백
            var model by remember(imageUrl, aladinCoverSize) { mutableStateOf(highRes) }
            AsyncImage(
                model = model,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { if (model != fallback) model = fallback },
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize()
                    .clip(shape),
            )
        }
    }
}
