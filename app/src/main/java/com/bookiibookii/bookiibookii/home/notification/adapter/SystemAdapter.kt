package com.bookiibookii.bookiibookii.home.notification.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.home.notification.model.NotificationItem

class SystemAdapter(
    private val onItemClick: ((NotificationItem) -> Unit)? = null
) : RecyclerView.Adapter<SystemAdapter.VH>() {

    private val items = mutableListOf<NotificationItem>()

    fun setItems(newItems: List<NotificationItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hom_notification, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            // 읽음 처리/이동 로직은 Fragment에서 결정
            onItemClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)

        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvBody: TextView = itemView.findViewById(R.id.tv_body)

        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvDotMeta: TextView = itemView.findViewById(R.id.tv_dot_meta)
        private val tvBook: TextView = itemView.findViewById(R.id.tv_book)

        private val dotUnread: View = itemView.findViewById(R.id.view_dot)

        fun bind(item: NotificationItem) {
            tvTitle.text = item.title
            tvBody.text = item.body
            tvTime.text = item.timeText

            val hasBook = item.bookTitle.isNotBlank()
            tvDotMeta.visibility = if (hasBook) View.VISIBLE else View.GONE
            tvBook.visibility = if (hasBook) View.VISIBLE else View.GONE
            if (hasBook) tvBook.text = item.bookTitle

            dotUnread.visibility = if (item.isUnread) View.VISIBLE else View.GONE

            applyIconStyle(item.isUnread)
        }

        private fun applyIconStyle(isUnread: Boolean) {
            if (isUnread) {
                ivIcon.setBackgroundResource(R.drawable.bg_circle_main_pale)
                ivIcon.setColorFilter(itemView.context.getColor(R.color.pre_main))
            } else {
                ivIcon.setBackgroundResource(R.drawable.bg_circle_grey_200)
                ivIcon.setColorFilter(itemView.context.getColor(R.color.white))
            }
        }
    }
}