package com.bookiibookii.bookiibookii.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.BookItem
import com.bookiibookii.bookiibookii.databinding.ItemSearchBookListBinding
import com.bumptech.glide.Glide

class GrpSearchBookAdapter(
    private val onItemClicked: (BookItem) -> Unit
) : RecyclerView.Adapter<GrpSearchBookAdapter.BookViewHolder>() {

    private var items: List<BookItem> = emptyList()

    fun submitList(newItems: List<BookItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemSearchBookListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BookViewHolder(private val binding: ItemSearchBookListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookItem) {
            // 바인딩 연결
            binding.itemSearchTitleTv.text = item.title
            binding.itemSearchAuthorTv.text = item.author
            binding.itemSearchAuthorTv.text = "${item.author} | ${item.publisher}"

            Glide.with(binding.root.context)
                .load(item.image)
                .placeholder(R.color.grey_200)
                .error(R.color.grey_200)
                .into(binding.itemSearchCoverIv)

            // 클릭 시 액티비티로 전달
            binding.root.setOnClickListener {
                onItemClicked(item)
            }
        }
    }
}