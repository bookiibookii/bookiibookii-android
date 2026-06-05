package com.bookiibookii.bookiibookii.ui.component

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 바텀시트 윗변 위로 뜨는 그림자
// 피그마 "바깥쪽 그림자": X0 Y0 / Blur 35 / Spread 0 / #000000 10%

fun Modifier.bottomSheetTopShadow(
    cornerRadius: Dp = 20.dp,
    blur: Dp = 35.dp,
    color: Color = Color(0x1A000000),
): Modifier = drawBehind {
    val cornerPx = cornerRadius.toPx()
    val frameworkPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        this.color = color.toArgb()
        maskFilter = BlurMaskFilter(blur.toPx() / 2f, BlurMaskFilter.Blur.NORMAL)
    }
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawRoundRect(
            0f, 0f, size.width, size.height, cornerPx, cornerPx, frameworkPaint,
        )
    }
}
