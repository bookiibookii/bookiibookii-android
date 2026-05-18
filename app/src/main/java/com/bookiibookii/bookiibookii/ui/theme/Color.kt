package com.bookiibookii.bookiibookii.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Monotone
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

val Grey100 = Color(0xFFF5F4F3)
val Grey200 = Color(0xFFE2E1DF)
val Grey300 = Color(0xFFC6C5C2)
val Grey400 = Color(0xFFA4A3A0)
val Grey500 = Color(0xFF858481)
val Grey600 = Color(0xFF72716F)
val Grey700 = Color(0xFF525D5B)
val Grey800 = Color(0xFF42413F)
val Grey900 = Color(0xFF242322)

// Main Color
val UiMain = Color(0xFFFF7618)
val UiMain150 = Color(0xFFFFC9A4)
val UiMainPale = Color(0xFFFFEADB)

// Sub Color
val UiMainSub = Color(0xFF37AAFF)
val UiMainSub150 = Color(0xFF9CD5FF)
val UiMainSubPale = Color(0xFFD4EDFF)

// Ui Color
val UiBg = Color(0xFFF6F6F6)

val UiPointRed = Color(0xFFFF4D4D)
val UiPointRedPale = Color(0xFFFFBBBB)
val UiPointRed150 = Color(0xFFFFF3F3)

val UiPointGreen200 = Color(0xFF3CCF4D)
val UiPointGreenPale = Color(0xFFE7FFEA)
val UiPointGreen150 = Color(0xFF74D27F)

// Social
val Kakao = Color(0xFFFEE500)

@Immutable
data class BookiiColors(
    val black: Color,
    val white: Color,
    val grey100: Color,
    val grey200: Color,
    val grey300: Color,
    val grey400: Color,
    val grey500: Color,
    val grey600: Color,
    val grey700: Color,
    val grey800: Color,
    val grey900: Color,
    val uiMain: Color,
    val uiMain150: Color,
    val uiMainPale: Color,
    val uiMainSub: Color,
    val uiMainSub150: Color,
    val uiMainSubPale: Color,
    val uiBg: Color,
    val uiPointRed: Color,
    val uiPointRedPale: Color,
    val uiPointRed150: Color,
    val uiPointGreen200: Color,
    val uiPointGreen150: Color,
    val uiPointGreenPale: Color,
    val kakao: Color,
)

val bookiiColors = BookiiColors(
    black = Black,
    white = White,
    grey100 = Grey100,
    grey200 = Grey200,
    grey300 = Grey300,
    grey400 = Grey400,
    grey500 = Grey500,
    grey600 = Grey600,
    grey700 = Grey700,
    grey800 = Grey800,
    grey900 = Grey900,
    uiMain = UiMain,
    uiMain150 = UiMain150,
    uiMainPale = UiMainPale,
    uiMainSub = UiMainSub,
    uiMainSub150 = UiMainSub150,
    uiMainSubPale = UiMainSubPale,
    uiBg = UiBg,
    uiPointRed = UiPointRed,
    uiPointRedPale = UiPointRedPale,
    uiPointRed150 = UiPointRed150,
    uiPointGreen200 = UiPointGreen200,
    uiPointGreen150 = UiPointGreen150,
    uiPointGreenPale = UiPointGreenPale,
    kakao = Kakao,
)

val LocalBookiiColors = staticCompositionLocalOf<BookiiColors> {
    error("BookiiColors not provided")
}
