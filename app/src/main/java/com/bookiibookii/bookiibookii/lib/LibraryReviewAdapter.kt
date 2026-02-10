package com.bookiibookii.bookiibookii.lib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.data.model.CardItem
import com.bookiibookii.bookiibookii.databinding.ItemLibDetailReviewBinding
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryReviewAdapter(
    private val onItemClick: (CardItem) -> Unit,
    private val onBookmarkClick: (CardItem, Int) -> Unit
) : RecyclerView.Adapter<LibraryReviewAdapter.ReviewViewHolder>() {

    private var items: List<CardItem> = emptyList()

    fun submitList(newItems: List<CardItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemLibDetailReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class ReviewViewHolder(private val binding: ItemLibDetailReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CardItem) {
            // 1. 기본 텍스트 정보
            binding.itemReviewNameIv.text = item.creatorName
            binding.itemReviewTextTv.text = item.memo
            binding.itemReviewPageTv.text = "${item.page}pg"

            // 2. 북마크 아이콘
            binding.itemReviewBookmark.setImageResource(
                if (item.isBookmarked) R.drawable.ic_bookmark_orange
                else R.drawable.ic_bookmark_gray
            )

            // 3. 카드 이미지 (item_review_photo_iv)
            // item.cardImage가 null이 아니고, URL이 있어야 함
            val cardImageUrl = item.cardImage?.presignedGetUrl
            if (!cardImageUrl.isNullOrEmpty()) {
                binding.itemReviewPhotoIv.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(cardImageUrl)
                    .transform(CenterCrop(), RoundedCorners(dpToPx(5))) // ★ 5dp 적용
                    .placeholder(R.drawable.bg_round_8dp_gray300)
                    .error(R.drawable.bg_round_8dp_gray300)
                    .into(binding.itemReviewPhotoIv)
            } else {
                // 이미지가 없으면 공간을 숨기거나 기본 이미지 처리
                binding.itemReviewPhotoIv.visibility = View.GONE
            }

            // 4. [비동기] 작성자 프로필 이미지 가져오기
            // 기본 이미지로 먼저 설정
            binding.itemReviewProfileIv.setImageResource(R.drawable.img_profile_default)

            // 뷰홀더에서 코루틴 실행 (API 호출)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.api().getUserProfile(item.creatorName)
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val userImageKey = response.body()?.result?.userImage?.s3Key // JSON 구조에 따라 경로 확인 필요

                        // UI 업데이트는 Main 스레드에서
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

            // 5. [비동기] 댓글 개수 가져오기
            binding.itemReviewChatTv.text = "0" // 로딩 전 초기값

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.api().getCardComments(item.cardId.toLong())
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val count = response.body()?.result?.totalCount ?: 0
                        withContext(Dispatchers.Main) {
                            binding.itemReviewChatTv.text = count.toString()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 6. 클릭 리스너 (상세 이동)
            itemView.setOnClickListener {
                onItemClick(item)
            }

            // 7. 북마크 클릭
            binding.itemReviewBookmark.setOnClickListener {
                onBookmarkClick(item, bindingAdapterPosition)
            }

        }
        private fun dpToPx(dp: Int): Int {
            return (dp * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}