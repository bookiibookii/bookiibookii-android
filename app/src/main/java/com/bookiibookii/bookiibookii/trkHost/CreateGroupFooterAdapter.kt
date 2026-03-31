package com.bookiibookii.bookiibookii.trkHost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.google.android.material.button.MaterialButton

class CreateGroupFooterAdapter(
    mode: FooterMode,
    private val onActionClick: () -> Unit
) : RecyclerView.Adapter<CreateGroupFooterAdapter.VH>() {

    private var mode: FooterMode = mode
    private var showEmptyText: Boolean = true

    fun setShowEmptyText(show: Boolean) {
        showEmptyText = show
        notifyItemChanged(0)
    }

    fun updateMode(newMode: FooterMode) {
        mode = newMode
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trk_create_group_footer, parent, false)
        return VH(view, onActionClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(showEmptyText, mode)
    }

    override fun getItemCount(): Int = 1

    class VH(
        itemView: View,
        onActionClick: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val card: View = itemView.findViewById(R.id.card_create_group)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_empty_title)
        private val tvDesc: TextView = itemView.findViewById(R.id.tv_empty_desc)
        private val btnAction: MaterialButton =
            itemView.findViewById(R.id.btn_create_group)

        init {
            btnAction.setOnClickListener { onActionClick() }
        }

        fun bind(showEmptyText: Boolean, mode: FooterMode) {
            card.visibility = View.VISIBLE
            tvTitle.visibility = View.VISIBLE
            btnAction.visibility = View.VISIBLE

            if (!showEmptyText) {
                card.visibility = View.GONE
                return
            }

            when (mode) {
                FooterMode.HOST_CREATE -> {
                    tvTitle.text = "아직 그룹을 만들지 않았어요 😭"
                    tvDesc.text = "읽고 싶은 책을 골라 그룹을 만들어볼까요?"
                    btnAction.text = "그룹 둘러보기"
                }

                FooterMode.GUEST_JOIN -> {
                    tvTitle.text = "아직 참여한 그룹이 없어요 😭"
                    tvDesc.text = "독서 그룹에 참여하러 가볼까요?"
                    btnAction.text = "그룹 둘러보기"
                }
            }
        }
    }
}
