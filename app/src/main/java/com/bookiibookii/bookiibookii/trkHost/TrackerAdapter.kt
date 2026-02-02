package com.bookiibookii.bookiibookii.trkHost

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.databinding.ItemTrkBinding
import com.bumptech.glide.Glide

class TrackerAdapter(
    private val onItemClicked: (TrackerData) -> Unit
): ListAdapter<TrackerData, TrackerAdapter.ViewHolder>(DIFF){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackerAdapter.ViewHolder {
        val binding = ItemTrkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackerAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

    class ViewHolder(
        private val binding: ItemTrkBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(
            item: TrackerData,
            onItemClicked: (TrackerData) -> Unit
        ) {
            binding.tvBookTitle.text = item.bookTitle
            binding.tvBookAuthor.text = item.bookAuthor
            binding.tvWithUser.text = item.withUserName?.let { "with  $it" } ?: ""

            Glide.with(binding.ivBookCover)
                .load(item.coverImageUrl)
                .placeholder(R.color.grey_200)
                .error(R.color.grey_200)
                .into(binding.ivBookCover)

            binding.updateStepUI(item.currentStep)

            binding.root.setOnClickListener{
                onItemClicked(item)
            }
        }

        private fun ItemTrkBinding.updateStepUI(step: TrackerStep){
            // 진행상황 색 변화 추가
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<TrackerData>() {
            override fun areItemsTheSame(old: TrackerData, new: TrackerData): Boolean {
                return old.id == new.id
            }

            override fun areContentsTheSame(old: TrackerData, new: TrackerData): Boolean {
                return old == new
            }
        }
    }
}