package com.bookiibookii.bookiibookii.lib

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentLibShareBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class LibraryShareFragment : DialogFragment() {

    private var _binding: FragmentLibShareBinding? = null
    private val binding get() = _binding!!

    private var cardId: Long = -1L
    private var bookTitle: String = ""
    private var isTypeA = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cardId = it.getLong("cardId", -1L)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibShareBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateTypeVisibility(true)
        initListeners()
        fetchShareData()
    }

    private fun fetchShareData() {
        if (cardId == -1L) {
            dismiss()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getCardDetail(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    if (result != null) {
                        bindData(
                            title = result.bookTitle,
                            content = result.memo,
                            author = result.creatorName ?: "Unknown",
                            // ★ 공백 제거만 안전하게 처리
                            imgUrl = result.cardImage?.presignedGetUrl?.trim()
                        )
                    }
                } else {
                    Toast.makeText(context, "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun bindData(title: String, content: String, author: String, imgUrl: String?) {
        this.bookTitle = title

        val safeContent = if (content.length > 100) content.take(100) + "..." else content
        val titleForView = if (title.length > 18) title.take(18) + "..." else title

        // 텍스트 바인딩
        binding.shareTitleA.text = titleForView
        binding.shareContentA.text = safeContent
        binding.shareAuthorA.text = author

        binding.shareTitleB.text = titleForView
        binding.shareContentB.text = safeContent
        binding.shareAuthorB.text = "by. $author"

        // ★ [핵심 수정] Glide 로드 로직 변경
        if (!imgUrl.isNullOrEmpty()) {
            // Type A
            Glide.with(requireContext()) // 1. Context 변경
                .load(imgUrl)
                .placeholder(R.drawable.bg_round_top_20dp_white)
                .error(R.drawable.bg_round_top_20dp_white)
                .override(Target.SIZE_ORIGINAL) // 2. ★ 중요: 뷰 크기 무시하고 이미지 원본 크기로 강제 로드
                .dontAnimate() // 3. 애니메이션 제거 (흰 깜빡임 방지)
                .centerCrop()
                .into(binding.shareImageA)

            // Type B
            Glide.with(requireContext())
                .load(imgUrl)
                .placeholder(R.drawable.bg_round_20dp_white)
                .error(R.drawable.bg_round_20dp_white)
                .override(Target.SIZE_ORIGINAL) // ★ 중요
                .dontAnimate()
                .centerCrop()
                .into(binding.shareImageB)
        } else {
            binding.shareImageA.setImageResource(R.drawable.bg_round_top_20dp_white)
            binding.shareImageB.setImageResource(R.drawable.bg_round_20dp_white)
        }
    }

    private fun initListeners() {
        binding.shareCloseIv.setOnClickListener { dismiss() }

        binding.btnTypeA.setOnClickListener {
            isTypeA = true
            updateTypeVisibility(true)
        }
        binding.btnTypeB.setOnClickListener {
            isTypeA = false
            updateTypeVisibility(false)
        }

        binding.shareInsta.setOnClickListener { shareToInstagramStory() }
        binding.shareKakao.setOnClickListener { shareLinkToApp("com.kakao.talk") }
        binding.shareX.setOnClickListener { shareToTwitter() }
        binding.shareLink.setOnClickListener { copyLinkToClipboard() }
    }

    private fun updateTypeVisibility(isA: Boolean) {
        if (isA) {
            binding.typeACard.visibility = View.VISIBLE
            binding.typeBCard.visibility = View.GONE
            binding.btnTypeA.alpha = 1.0f
            binding.btnTypeB.alpha = 0.5f
        } else {
            binding.typeACard.visibility = View.GONE
            binding.typeBCard.visibility = View.VISIBLE
            binding.btnTypeA.alpha = 0.5f
            binding.btnTypeB.alpha = 1.0f
        }
    }

    // --- 공유 기능 ---

    private fun getShareUrl(): String = "https://bookiibookii.com/card/$cardId"

    private fun shareToInstagramStory() {
        val targetView = if (isTypeA) binding.typeACard else binding.typeBCard
        targetView.post {
            val uri = getViewBitmapUri(targetView)
            if (uri != null) {
                val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
                    // ★ [수정 1] 배경 이미지 설정 제거
                    // 기존: setDataAndType(uri, "image/*") -> 배경으로 설정됨
                    // 변경: setType("image/*") -> 데이터 타입만 명시 (배경 안 깔림)
                    type = "image/*"

                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    // ★ [수정 2] 스티커(내용)로만 이미지 전달
                    putExtra("interactive_asset_uri", uri)

                    // (선택 사항) 배경색을 지정하고 싶다면 아래 주석 해제 (기본값: 이미지에서 추출한 그라데이션)
                    // putExtra("top_background_color", "#333333")
                    // putExtra("bottom_background_color", "#333333")
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "인스타그램이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "이미지 생성 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun shareLinkToApp(packageName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getShareUrl())
            setPackage(packageName)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            shareGenericLink()
        }
    }

    private fun shareToTwitter() {
        val text = "[$bookTitle] 독서 카드 공유\n${getShareUrl()}"
        val tweetUrl = "https://twitter.com/intent/tweet?text=${Uri.encode(text)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "브라우저를 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyLinkToClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("BookCardLink", getShareUrl())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "링크가 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun shareGenericLink() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getShareUrl())
        }
        startActivity(Intent.createChooser(intent, "공유하기"))
    }

    // ★ 뷰 캡처 (흰 화면 방지 로직)
    private fun getViewBitmapUri(view: View): Uri? {
        try {
            if (view.width == 0 || view.height == 0) return null

            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.TRANSPARENT)
            view.draw(canvas)

            val imagesFolder = File(requireContext().cacheDir, "images")
            if (!imagesFolder.exists()) imagesFolder.mkdirs()

            val file = File(imagesFolder, "share_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            return FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            return null
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}