package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.bookData.Data.LibBookmarkItem
import com.bookiibookii.bookiibookii.databinding.ItemLibDetailBookBinding

class LibraryBookmarkAdapter(
    private var items: List<LibBookmarkItem>,
    private val itemClickListener: (LibBookmarkItem) -> Unit
) : RecyclerView.Adapter<LibraryBookmarkAdapter.BookmarkViewHolder>() {

    inner class BookmarkViewHolder(private val binding: ItemLibDetailBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LibBookmarkItem) {
            binding.itemReviewTitleTv.text = item.title
            binding.itemReviewPageTv.text = "${item.page}pg"
            binding.itemReviewTextTv.text = item.content
            binding.itemReviewNameIv.text = item.userName

            // 이미지 설정
            if (item.imageRes != null) {
                binding.itemReviewPhotoIv.setImageResource(item.imageRes)
                binding.itemReviewPhotoIv.visibility = android.view.View.VISIBLE
            } else {
                binding.itemReviewPhotoIv.visibility = android.view.View.GONE
            }

            itemView.setOnClickListener { itemClickListener(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding = ItemLibDetailBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<LibBookmarkItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}