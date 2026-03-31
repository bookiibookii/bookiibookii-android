package com.bookiibookii.bookiibookii.lib

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.LoadingDialog
import com.bookiibookii.bookiibookii.common.showCustomToast // ★ 커스텀 토스트 임포트
import com.bookiibookii.bookiibookii.data.api.RetrofitClient
import com.bookiibookii.bookiibookii.databinding.FragmentLibShareBinding
import com.bookiibookii.bookiibookii.lib.imgModel.ImgBBRetrofitClient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class LibraryShareFragment : DialogFragment() {

    private var _binding: FragmentLibShareBinding? = null
    private val binding get() = _binding!!

    private var cardId: Long = -1L
    private var bookTitle: String = ""
    private var isTypeA = true

    private lateinit var loadingDialog: LoadingDialog
    private var loadedImageCount = 0

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

        loadingDialog = LoadingDialog(requireContext())

        updateTypeVisibility(true)
        initListeners()
        fetchShareData()
    }

    private fun fetchShareData() {
        if (cardId == -1L) {
            dismiss()
            return
        }

        loadingDialog.show()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.api().getCardDetail(cardId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    if (result != null) {
                        bindData(
                            title = result.bookTitle,
                            content = result.memo,
                            author = result.creatorName ?: "Unknown",
                            imgUrl = result.cardImage?.presignedGetUrl?.trim()
                        )
                    } else {
                        if (loadingDialog.isShowing) loadingDialog.dismiss()
                    }
                } else {
                    requireContext().showCustomToast("정보를 불러오지 못했습니다.", false)
                    if (loadingDialog.isShowing) loadingDialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (loadingDialog.isShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun bindData(title: String, content: String, author: String, imgUrl: String?) {
        this.bookTitle = title

        val safeContent = if (content.length > 100) content.take(100) + "..." else content
        val titleForView = if (title.length > 18) title.take(18) + "..." else title

        binding.shareTitleA.text = titleForView
        binding.shareContentA.text = safeContent
        binding.shareAuthorA.text = author

        binding.shareTitleB.text = titleForView
        binding.shareContentB.text = safeContent
        binding.shareAuthorB.text = "by. $author"

        if (!imgUrl.isNullOrEmpty()) {
            loadedImageCount = 0

            val listener = object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    checkImageLoad()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    checkImageLoad()
                    return false
                }
            }

            Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.bg_round_top_20dp_white)
                .error(R.drawable.bg_round_top_20dp_white)
                .override(Target.SIZE_ORIGINAL)
                .dontAnimate()
                .centerCrop()
                .listener(listener)
                .into(binding.shareImageA)

            Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.bg_round_20dp_white)
                .error(R.drawable.bg_round_20dp_white)
                .override(Target.SIZE_ORIGINAL)
                .dontAnimate()
                .centerCrop()
                .listener(listener)
                .into(binding.shareImageB)
        } else {
            binding.shareImageA.setImageResource(R.drawable.bg_round_top_20dp_white)
            binding.shareImageB.setImageResource(R.drawable.bg_round_20dp_white)
            if (loadingDialog.isShowing) loadingDialog.dismiss()
        }
    }

    private fun checkImageLoad() {
        loadedImageCount++
        if (loadedImageCount >= 2) {
            if (isAdded && loadingDialog.isShowing) {
                loadingDialog.dismiss()
            }
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

        binding.shareInsta.setOnClickListener { processAndShareImage(isInstagram = true, targetPackage = null) }
        binding.shareKakao.setOnClickListener { processAndShareImage(isInstagram = false, targetPackage = "com.kakao.talk") }
        binding.shareX.setOnClickListener { processAndShareImage(isInstagram = false, targetPackage = "com.twitter.android") }
        binding.shareLink.setOnClickListener { uploadToImgBBAndCopyLink() }
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

    private fun captureCardBitmap(view: View): Bitmap? {
        if (view.width == 0 || view.height == 0) return null

        val density = resources.displayMetrics.density
        val radius = 20f * density
        val shadowRadius = 8f * density
        val shadowDy = 2f * density

        val bitmapWidth = view.width + (shadowRadius * 2).toInt()
        val bitmapHeight = view.height + (shadowRadius * 2).toInt() + shadowDy.toInt()
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.TRANSPARENT)

        val rectF = android.graphics.RectF(
            shadowRadius,
            shadowRadius,
            shadowRadius + view.width,
            shadowRadius + view.height
        )

        val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            setShadowLayer(shadowRadius, 0f, shadowDy, Color.parseColor("#33000000"))
        }
        canvas.drawRoundRect(rectF, radius, radius, shadowPaint)

        canvas.save()
        val path = android.graphics.Path().apply {
            addRoundRect(rectF, radius, radius, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(path)

        canvas.translate(shadowRadius, shadowRadius)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        canvas.restore()

        return bitmap
    }

    private fun processAndShareImage(isInstagram: Boolean, targetPackage: String?) {
        val targetView = if (isTypeA) binding.typeACard else binding.typeBCard

        val cardBitmap = captureCardBitmap(targetView) ?: run {
            requireContext().showCustomToast("화면을 불러오는 중입니다. 잠시 후 시도해주세요.", false)
            return
        }

        requireContext().showCustomToast("이미지를 준비 중입니다...", true)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val imagesFolder = File(requireContext().cacheDir, "images")
                if (!imagesFolder.exists()) imagesFolder.mkdirs()

                imagesFolder.listFiles()?.forEach { it.delete() }

                val stickerFile = File(imagesFolder, "sticker_${System.currentTimeMillis()}.png")
                val stickerStream = FileOutputStream(stickerFile)
                cardBitmap.compress(Bitmap.CompressFormat.PNG, 100, stickerStream)
                stickerStream.flush()
                stickerStream.close()

                val stickerUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    stickerFile
                )

                var backgroundUri: Uri? = null
                if (isInstagram) {
                    try {
                        val bgBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_background)

                        if (bgBitmap != null) {
                            val bgFile = File(imagesFolder, "background_${System.currentTimeMillis()}.png")
                            val bgStream = FileOutputStream(bgFile)
                            bgBitmap.compress(Bitmap.CompressFormat.PNG, 100, bgStream)
                            bgStream.flush()
                            bgStream.close()

                            backgroundUri = FileProvider.getUriForFile(
                                requireContext(),
                                "${requireContext().packageName}.fileprovider",
                                bgFile
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                withContext(Dispatchers.Main) {
                    if (isInstagram) {
                        launchInstagramStoryIntent(stickerUri, backgroundUri)
                    } else {
                        launchGenericImageShareIntent(stickerUri, targetPackage)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    requireContext().showCustomToast("이미지 저장에 실패했습니다.", false)
                }
            }
        }
    }

    private fun launchInstagramStoryIntent(stickerUri: Uri, backgroundUri: Uri?) {
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setPackage("com.instagram.android")

            if (backgroundUri != null) {
                setDataAndType(backgroundUri, "image/*")
            } else {
                type = "image/*"
            }

            putExtra("interactive_asset_uri", stickerUri)
            putExtra("source_application", requireContext().packageName)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val clipData = ClipData.newRawUri("Sticker", stickerUri)
        if (backgroundUri != null) {
            clipData.addItem(ClipData.Item(backgroundUri))
        }
        intent.clipData = clipData

        val resInfoList = requireContext().packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            requireContext().grantUriPermission(
                packageName,
                stickerUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            if (backgroundUri != null) {
                requireContext().grantUriPermission(
                    packageName,
                    backgroundUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            requireContext().showCustomToast("인스타그램 앱을 찾을 수 없습니다.", false)
        }
    }

    private fun launchGenericImageShareIntent(uri: Uri, packageName: String?) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            if (packageName != null) setPackage(packageName)
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(Intent.createChooser(intent, "공유하기"))
        }
    }

    private fun uploadToImgBBAndCopyLink() {
        val targetView = if (isTypeA) binding.typeACard else binding.typeBCard

        val bitmap = captureCardBitmap(targetView) ?: run {
            requireContext().showCustomToast("화면을 불러오는 중입니다. 잠시 후 시도해주세요.", false)
            return
        }

        requireContext().showCustomToast("링크를 생성하고 있습니다. 잠시만 기다려주세요...", true)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val imageBytes = baos.toByteArray()
                val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

                val apiKey = "303037b8fddf70fbb18c66be562b475a"
                val response = ImgBBRetrofitClient.api.uploadImage(apiKey, base64Image)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val imgbbLink = response.body()?.data?.url ?: ""
                        copyToClipboard(imgbbLink)
                        requireContext().showCustomToast("링크가 복사되었습니다.", true)
                    } else {
                        requireContext().showCustomToast("링크 생성에 실패했습니다.", false)
                        Log.e("ImgBBError", "Code: ${response.code()}, Message: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    requireContext().showCustomToast("오류가 발생했습니다.", false)
                }
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("BookCardLink", text)
        clipboard.setPrimaryClip(clip)
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