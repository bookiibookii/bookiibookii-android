package com.bookiibookii.bookiibookii.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bookiibookii.bookiibookii.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
)

@Immutable
data class BookiiTypography(
    val regular10: TextStyle,
    val regular11: TextStyle,
    val regular12: TextStyle,
    val regular14: TextStyle,
    val regular15: TextStyle,
    val regular16: TextStyle,
    val regular18: TextStyle,
    val regular20: TextStyle,
    val regular24: TextStyle,
    val medium11: TextStyle,
    val medium12: TextStyle,
    val medium14: TextStyle,
    val medium15: TextStyle,
    val medium16: TextStyle,
    val medium18: TextStyle,
    val medium20: TextStyle,
    val medium24: TextStyle,
    val medium32: TextStyle,
    val semibold10: TextStyle,
    val semibold12: TextStyle,
    val semibold14: TextStyle,
    val semibold18: TextStyle,
    val semibold20: TextStyle,
)

private fun pretendard(weight: FontWeight, size: Int) = TextStyle(
    fontFamily = Pretendard,
    fontWeight = weight,
    fontSize = size.sp,
)

val bookiiTypography = BookiiTypography(
    regular10 = pretendard(FontWeight.Normal, 10),
    regular11 = pretendard(FontWeight.Normal, 11),
    regular12 = pretendard(FontWeight.Normal, 12),
    regular14 = pretendard(FontWeight.Normal, 14),
    regular15 = pretendard(FontWeight.Normal, 15),
    regular16 = pretendard(FontWeight.Normal, 16),
    regular18 = pretendard(FontWeight.Normal, 18),
    regular20 = pretendard(FontWeight.Normal, 20),
    regular24 = pretendard(FontWeight.Normal, 24),
    medium11 = pretendard(FontWeight.Medium, 11),
    medium12 = pretendard(FontWeight.Medium, 12),
    medium14 = pretendard(FontWeight.Medium, 14),
    medium15 = pretendard(FontWeight.Medium, 15),
    medium16 = pretendard(FontWeight.Medium, 16),
    medium18 = pretendard(FontWeight.Medium, 18),
    medium20 = pretendard(FontWeight.Medium, 20),
    medium24 = pretendard(FontWeight.Medium, 24),
    medium32 = pretendard(FontWeight.Medium, 32),
    semibold10 = pretendard(FontWeight.SemiBold, 10),
    semibold12 = pretendard(FontWeight.SemiBold, 12),
    semibold14 = pretendard(FontWeight.SemiBold, 14),
    semibold18 = pretendard(FontWeight.SemiBold, 18),
    semibold20 = pretendard(FontWeight.SemiBold, 20),
)

val LocalBookiiTypography = staticCompositionLocalOf<BookiiTypography> {
    error("BookiiTypography not provided")
}
