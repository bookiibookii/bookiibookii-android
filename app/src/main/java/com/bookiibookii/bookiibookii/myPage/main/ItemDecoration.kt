package com.bookiibookii.bookiibookii.myPage.main

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MypReviewGridDecoration(
    private val spanCount: Int,
    private val spacingHorizontal: Int, // 12dp
    private val spacingVertical: Int,   // 8dp
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacingHorizontal - column * spacingHorizontal / spanCount
            outRect.right = (column + 1) * spacingHorizontal / spanCount
        } else {
            outRect.left = column * spacingHorizontal / spanCount
            outRect.right = spacingHorizontal - (column + 1) * spacingHorizontal / spanCount
        }

        // 첫 번째 줄이 아니면 위쪽 간격 추가 (Grid 간격)
        if (position >= spanCount) {
            outRect.top = spacingVertical
        }

        // 아이템 아래쪽 간격 추가
        outRect.bottom = spacingVertical
    }
}