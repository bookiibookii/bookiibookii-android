package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem // ★ 수정된 모델 사용
import com.bookiibookii.bookiibookii.databinding.ItemLibDetailBookBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryBookmarkAdapter(
    private val itemClickListener: (CardItem) -> Unit
) : RecyclerView.Adapter<LibraryBookmarkAdapter.BookmarkViewHolder>() {

    private var items: List<CardItem> = emptyList()

    fun submitList(newItems: List<CardItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding = ItemLibDetailBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BookmarkViewHolder(private val binding: ItemLibDetailBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CardItem) {
            // 1. 텍스트 바인딩
            binding.itemReviewTitleTv.text = item.bookTitle
            binding.itemReviewPageTv.text = "${item.page}pg"
            binding.itemReviewTextTv.text = item.memo // CardItem에서는 content가 아니라 memo
            binding.itemReviewNameIv.text = item.creatorName // 작성자 이름

            // 2. 카드 이미지 바인딩
            // item.cardImage가 null이 아니면 URL 사용
            val imageUrl = item.cardImage?.presignedGetUrl

            if (!imageUrl.isNullOrEmpty()) {
                binding.itemReviewPhotoIv.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .transform(CenterCrop(), RoundedCorners(dpToPx(5)))
                    .placeholder(R.drawable.bg_round_8dp_gray300)
                    .into(binding.itemReviewPhotoIv)
            } else {
                binding.itemReviewPhotoIv.visibility = View.GONE
            }


            // 3. 프로필 이미지 (CardItem에 프로필 URL이 없다면 기본 이미지)
            // 현재 CardItem에는 작성자 이름만 있고 프로필 URL 필드가 안 보임.
            // 기본 이미지로 처리하거나 추후 필드가 추가되면 수정.
            binding.itemReviewProfileIv.setImageResource(R.drawable.img_profile_default)

            val launch = CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 닉네임으로 프로필 조회
                    val response = RetrofitClient.api().getUserProfile(item.creatorName)
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val userImageKey = response.body()?.result?.profileImageUrl

                        withContext(Dispatchers.Main) {
                            if (userImageKey != null) {
                                Glide.with(itemView.context)
                                    .load(userImageKey)
                                    .circleCrop()
                                    .placeholder(R.drawable.img_profile_default)
                                    .into(binding.itemReviewProfileIv)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 4. 클릭 리스너
            itemView.setOnClickListener {
                itemClickListener(item)
            }
        }
        private fun dpToPx(dp: Int): Int {
            return (dp * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}