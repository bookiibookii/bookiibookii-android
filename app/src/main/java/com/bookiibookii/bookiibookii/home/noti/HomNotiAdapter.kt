package com.bookiibookii.bookiibookii.home.noti

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R

class HomNotiAdapter(
    private val onItemClick: ((HomNotiItem) -> Unit)? = null
) : RecyclerView.Adapter<HomNotiAdapter.VH>() {

    private val items = mutableListOf<HomNotiItem>()

    fun setItems(newItems: List<HomNotiItem>) {
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
        holder.bind(items[position])

        holder.itemView.setOnClickListener {
            val item = items[position]

            // 읽지 않은 알림이면 읽음 처리
            if (item.isUnread) {
                item.isUnread = false
                notifyItemChanged(position)
            }

            // Fragment로 클릭 이벤트 전달
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

        fun bind(item: HomNotiItem) {
            tvTitle.text = item.title
            tvBody.text = item.body

            tvTime.text = item.timeText

            // bookTitle 없으면 메타 뒷부분 숨김
            val hasBook = item.bookTitle.isNotBlank()
            tvDotMeta.visibility = if (hasBook) View.VISIBLE else View.GONE
            tvBook.visibility = if (hasBook) View.VISIBLE else View.GONE
            if (hasBook) tvBook.text = item.bookTitle

            // 읽지 않은 점 표시
            dotUnread.visibility = if (item.isUnread) View.VISIBLE else View.GONE

            // 아이콘 스타일 적용
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