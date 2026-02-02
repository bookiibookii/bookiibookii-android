package com.bookiibookii.bookiibookii.lib

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LibDetailGridDecoration (
    private val spanCount: Int, // 열 개수
    private val spacingHorizontal: Int, // 좌우 간격
    private val spacingVertical: Int, // 상하 간격
    private val includeEdge: Boolean // 가장자리 포함 여부h
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // 아이템 위치
        val column = position % spanCount // 현재 열 (0 또는 1)

        if (includeEdge) {
            outRect.left = spacingHorizontal - column * spacingHorizontal / spanCount
            outRect.right = (column + 1) * spacingHorizontal / spanCount
        } else {
            outRect.left = column * spacingHorizontal / spanCount
            outRect.right = spacingHorizontal - (column + 1) * spacingHorizontal / spanCount
        }


        if (position >= spanCount) {
            outRect.top = spacingVertical
        }
    }
}