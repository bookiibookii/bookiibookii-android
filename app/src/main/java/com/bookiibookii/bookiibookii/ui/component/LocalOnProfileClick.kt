package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.runtime.compositionLocalOf

val LocalOnProfileClick = compositionLocalOf<(nickname: String) -> Unit> { {} }
