package com.bookiibookii.bookiibookii.mypage.ui.detail

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.verticalRotation(): Modifier = layout { measurable, constraints ->
    val unconstrained = constraints.copy(maxWidth = Int.MAX_VALUE, maxHeight = Int.MAX_VALUE)
    val placeable = measurable.measure(unconstrained)

    layout(placeable.height, placeable.width) {
        placeable.place(
            x = -(placeable.width / 2 - placeable.height / 2),
            y = -(placeable.height / 2 - placeable.width / 2)
        )
    }
}.graphicsLayer { rotationZ = 90f }
