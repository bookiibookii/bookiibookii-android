package com.bookiibookii.bookiibookii.onboarding.Intro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R

class IntroPagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int) = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutRes = when (viewType) {
            0 -> R.layout.item_intro_card_1
            1 -> R.layout.item_intro_card_2
            else -> R.layout.item_intro_card_3
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 정적 mock 데이터이므로 별도 바인딩 불필요
    }

    override fun getItemCount() = 3
}