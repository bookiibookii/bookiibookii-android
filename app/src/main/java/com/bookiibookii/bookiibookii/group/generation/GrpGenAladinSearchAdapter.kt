package com.bookiibookii.bookiibookii.group.generation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.BookItem
import com.bookiibookii.bookiibookii.databinding.ItemSearchBookListBinding
import com.bumptech.glide.Glide

class GrpGenAladinSearchAdapter(
    private val onItemClicked: (BookItem) -> Unit
) : RecyclerView.Adapter<GrpGenAladinSearchAdapter.BookViewHolder>() {

    private var items: List<BookItem> = emptyList()

    // Data Update
    fun submitList(newItems: List<BookItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    // endregion

    // Adapter Overrides
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
    // endregion

    // ViewHolder
    inner class BookViewHolder(private val binding: ItemSearchBookListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookItem) {
            with(binding) {
                // 1. 텍스트 데이터 매핑
                itemSearchTitleTv.text = item.title
                itemSearchAuthorTv.text = "${item.author} | ${item.publisher}"

                // 2. 이미지 로딩 (Glide)
                Glide.with(root.context)
                    .load(item.image)
                    .placeholder(R.color.grey_200)
                    .error(R.color.grey_200)
                    .into(itemSearchCoverIv)

                // 3. 클릭 리스너
                root.setOnClickListener {
                    onItemClicked(item)
                }
            }
        }
    }
}