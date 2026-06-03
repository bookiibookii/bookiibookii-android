package com.bookiibookii.bookiibookii.mypage

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.model.mypage.UserProfileResDTO
import com.bookiibookii.bookiibookii.mypage.feat.detail.MyBookshelfFragment
import com.bookiibookii.bookiibookii.mypage.feat.detail.ReviewFragment
import com.bookiibookii.bookiibookii.mypage.feat.main.AddressManagementFragment
import com.bookiibookii.bookiibookii.mypage.feat.main.ProfileSettingFragment
import com.bookiibookii.bookiibookii.mypage.feat.setting.SettingFragment
import com.bookiibookii.bookiibookii.mypage.ui.detail.ReviewTab
import com.bookiibookii.bookiibookii.mypage.ui.main.MypageScreen
import com.bookiibookii.bookiibookii.mypage.ui.main.ProfileShareCardContent
import com.bookiibookii.bookiibookii.mypage.vm.MypageViewModel
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import java.io.File
import android.net.Uri

class MypageFragment : BaseMypageFragment() {

    private val viewModel: MypageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BookiiBookiiTheme {
                val profile by viewModel.profileData.observeAsState()
                MypageScreen(
                    profile = profile,
                    onSaveIntroduction = { viewModel.updateIntroduction(it) },
                    onBackClick = { parentFragmentManager.popBackStack() },
                    onSettingClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, SettingFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onProfileSettingClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ProfileSettingFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onAddressManagementClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, AddressManagementFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onBookshelfClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, MyBookshelfFragment())
                            .addToBackStack(null)
                            .commit()
                    },
                    onWrittenReviewClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ReviewFragment.newInstance(ReviewTab.WRITTEN))
                            .addToBackStack(null)
                            .commit()
                    },
                    onReceivedReviewClick = {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, ReviewFragment.newInstance(ReviewTab.RECEIVED))
                            .addToBackStack(null)
                            .commit()
                    },
                    onInstagramShareClick = {
                        viewModel.profileData.value?.let { shareProfileToInstagram(it) }
                    },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchMypageData()
    }

    private fun shareProfileToInstagram(profile: UserProfileResDTO) {
        if (!isAdded) return
        val context = requireContext()
        val density = resources.displayMetrics.density
        val cardWidth = (resources.displayMetrics.widthPixels - (40 * density).toInt())

        val cardView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            visibility = View.INVISIBLE
            setContent {
                BookiiBookiiTheme {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(BookiiBookiiTheme.colors.white),
                    ) {
                        ProfileShareCardContent(
                            name = profile.nickname,
                            motto = profile.introduction ?: "",
                            imageUrl = profile.profileImageUrl,
                            representativeBooks = profile.userBooks ?: emptyList(),
                            isDark = false,
                        )
                    }
                }
            }
        }

        val container = (requireView().parent as? ViewGroup) ?: return
        container.addView(cardView, ViewGroup.LayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT))

        cardView.postDelayed({
            if (!isAdded) {
                if (cardView.isAttachedToWindow) container.removeView(cardView)
                return@postDelayed
            }
            try {
                val w = cardView.width
                val h = cardView.height
                if (w <= 0 || h <= 0) {
                    container.removeView(cardView)
                    context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
                    return@postDelayed
                }

                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                cardView.draw(canvas)
                container.removeView(cardView)

                val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
                val file = File(imagesDir, "profile_card_${System.currentTimeMillis()}.png")
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                launchInstagramStoryIntent(uri)
            } catch (e: Exception) {
                if (cardView.isAttachedToWindow) container.removeView(cardView)
                context.showCustomToast("공유 준비 중 오류가 발생했습니다.", false)
            }
        }, 500L)
    }

    private fun launchInstagramStoryIntent(stickerUri: Uri) {
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setPackage("com.instagram.android")
            type = "image/*"
            putExtra("interactive_asset_uri", stickerUri)
            putExtra("source_application", requireContext().packageName)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val clipData = ClipData.newRawUri("Sticker", stickerUri)
        intent.clipData = clipData
        requireContext().grantUriPermission(
            "com.instagram.android",
            stickerUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
        try {
            startActivity(intent)
        } catch (e: Exception) {
            requireContext().showCustomToast("인스타그램 앱을 찾을 수 없습니다.", false)
        }
    }
}
