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

    // ★ 1. 공유 버튼 함수 (post 복구 및 색상 코드 완전 수정)
    private fun shareToInstagramStory() {
        val targetView = if (isTypeA) binding.typeACard else binding.typeBCard

        // 뷰가 화면에 완전히 그려진 후 안전하게 캡처하도록 targetView.post 블록을 사용합니다.
        targetView.post {
            val uri = getViewBitmapUri(targetView)

            if (uri != null) {
                val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
                    type = "image/*"
                    setPackage("com.instagram.android")
                    putExtra("interactive_asset_uri", uri)

                    // ★ [핵심] HEX 코드 6자리 (#FFFFFF) 정확히 기입
                    putExtra("top_background_color", "#FAE0D4")
                    putExtra("bottom_background_color", "#FFFFFF")
                }

                requireActivity().grantUriPermission(
                    "com.instagram.android",
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "인스타그램이 설치되어 있지 않거나 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "이미지 캡처에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ★ 2. 뷰 캡처 함수 (Type A 크기 측정 완전 보장)
// ★ 2. 뷰 캡처 함수 (그림자 효과 추가 버전)
    private fun getViewBitmapUri(view: View): Uri? {
        try {
            // 1. 강제 측정 로직 (기존 유지)
            // Type A: 340dp(다이얼로그) - 48dp(좌우패딩) = 292dp
            val targetWidthPx = (292 * resources.displayMetrics.density).toInt()

            if (view.width == 0 || view.height == 0) {
                view.measure(
                    View.MeasureSpec.makeMeasureSpec(targetWidthPx, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            }

            val width = view.width.takeIf { it > 0 } ?: view.measuredWidth
            val height = view.height.takeIf { it > 0 } ?: view.measuredHeight

            if (width <= 0 || height <= 0) return null

            // ---------------------------------------------------------
            // ★ [수정] 그림자를 위한 여백 및 Paint 설정
            // ---------------------------------------------------------
            val shadowMargin = 24 // 그림자가 그려질 여백 (px 단위, 적절히 조절 가능)
            val cornerRadius = 20f * resources.displayMetrics.density // 카드 둥글기 (XML과 동일하게)

            // 전체 비트맵 크기 = 뷰 크기 + 양쪽 여백
            val bitmap = Bitmap.createBitmap(
                width + (shadowMargin * 2),
                height + (shadowMargin * 2),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)

            // 배경 투명 초기화
            canvas.drawColor(Color.TRANSPARENT)

            // 그림자 설정을 위한 페인트 객체
            val paint = android.graphics.Paint().apply {
                color = Color.WHITE // 카드 배경색 (흰색)
                style = android.graphics.Paint.Style.FILL
                isAntiAlias = true

                // 그림자 설정: (반경, X오프셋, Y오프셋, 그림자색상)
                // 0x33000000: 투명도 약 20% 검정색
                setShadowLayer(16f, 0f, 8f, 0x33000000.toInt())
            }

            // 그림자가 포함된 흰색 배경 그리기 (중앙에 위치하도록 shadowMargin 만큼 띄움)
            val rectF = android.graphics.RectF(
                shadowMargin.toFloat(),
                shadowMargin.toFloat(),
                (width + shadowMargin).toFloat(),
                (height + shadowMargin).toFloat()
            )
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

            // ---------------------------------------------------------
            // ★ [수정] 실제 뷰 그리기
            // ---------------------------------------------------------
            canvas.save()
            // 뷰를 그림자 안쪽 중앙으로 이동
            canvas.translate(shadowMargin.toFloat(), shadowMargin.toFloat())

            // 뷰 내부 내용이 둥근 모서리를 벗어나지 않도록 클리핑 (기존 로직 응용)
            val path = android.graphics.Path().apply {
                addRoundRect(
                    android.graphics.RectF(0f, 0f, width.toFloat(), height.toFloat()),
                    cornerRadius,
                    cornerRadius,
                    android.graphics.Path.Direction.CW
                )
            }
            canvas.clipPath(path)

            // 뷰 그리기
            view.draw(canvas)
            canvas.restore()

            // ---------------------------------------------------------
            // 파일 저장 (기존 유지)
            // ---------------------------------------------------------
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
            Log.e("ShareError", "Bitmap Capture Error", e)
            return null
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

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}