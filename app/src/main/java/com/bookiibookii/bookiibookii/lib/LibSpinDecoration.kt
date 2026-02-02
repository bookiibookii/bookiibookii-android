package com.bookiibookii.bookiibookii.lib

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class LibSpineDecoration(
    private val spanCount: Int,
    private val spacingHorizontal: Int, // 8dp
    private val paddingTopBottom: Int   // 60dp
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val layoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
        val spanIndex = layoutParams.spanIndex
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        outRect.left = spacingHorizontal / 2
        outRect.right = spacingHorizontal / 2

        outRect.bottom = spacingHorizontal


        if (position < spanCount) {
            outRect.top = paddingTopBottom
        } else {
            outRect.top = 0
        }

        if (position >= itemCount - spanCount) {
            outRect.bottom = paddingTopBottom
        }
    }
}