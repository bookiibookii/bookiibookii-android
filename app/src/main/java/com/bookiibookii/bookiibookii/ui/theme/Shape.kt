package com.bookiibookii.bookiibookii.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class BookiiShape(
    val round5: Shape,
    val round8: Shape,
    val round16: Shape,
    val round20: Shape,
    val round24: Shape,
    val round26: Shape,
    val round50: Shape,
)

val bookiiShape = BookiiShape(
    round5 = RoundedCornerShape(5.dp),
    round8 = RoundedCornerShape(8.dp),
    round16 = RoundedCornerShape(16.dp),
    round20 = RoundedCornerShape(20.dp),
    round24 = RoundedCornerShape(24.dp),
    round26 = RoundedCornerShape(26.dp),
    round50 = RoundedCornerShape(50.dp),
)

val LocalBookiiShape = staticCompositionLocalOf<BookiiShape> {
    error("BookiiShape not provided")
}
