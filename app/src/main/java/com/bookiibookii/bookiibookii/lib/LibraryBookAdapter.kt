package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.bookData.Data.LibBook
import com.bookiibookii.bookiibookii.bookData.Data.ReadStatus
import com.bookiibookii.bookiibookii.databinding.ItemLibBookBinding

class LibraryBookAdapter(
    private var items: List<LibBook>,
    private val itemClickListener: (LibBook) -> Unit
) : RecyclerView.Adapter<LibraryBookAdapter.BookViewHolder>(){
    inner class BookViewHolder(private val binding: ItemLibBookBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item : LibBook){
            binding.libItemBookTitleTv.text = item.title
            binding.libItemProfileTv.text = item.author

            //  이미지 Glide 사용해 추후 추가 예정

            when (item.readStatus) {
                ReadStatus.READING -> {
                    binding.libItemStateTv.visibility = View.VISIBLE
                    binding.libRateList.visibility = View.GONE

                    binding.libItemStateTv.text = item.progress ?: "독서 중"
                }

                ReadStatus.DONE -> {
                    binding.libItemStateTv.visibility = View.GONE
                    binding.libRateList.visibility = View.VISIBLE

                    // 별점 로직
                }
            }
            itemView.setOnClickListener {
                itemClickListener(item)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemLibBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    // 탭이 바뀔 때 데이터를 갱신하고 뷰 모드를 변경하는 함수
    fun updateList(newItems: List<LibBook>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    fun submitList(newItems: List<LibBook>){
        this.items = newItems
        notifyDataSetChanged()
    }

}