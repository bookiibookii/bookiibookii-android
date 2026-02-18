package com.bookiibookii.bookiibookii.lib

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.LoadingDialog
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

    // ★ 로딩 다이얼로그와 이미지 로드 상태 카운터
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

        loadingDialog = LoadingDialog(requireContext()) // ★ 초기화

        updateTypeVisibility(true)
        initListeners()
        fetchShareData()
    }

    private fun fetchShareData() {
        if (cardId == -1L) {
            dismiss()
            return
        }

        loadingDialog.show() // ★ API 호출 시작 시 화면 클릭 방지(로딩 띄우기)

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
                            imgUrl = result.cardImage?.presignedGetUrl?.trim()
                        )
                    } else {
                        if (loadingDialog.isShowing) loadingDialog.dismiss()
                    }
                } else {
                    Toast.makeText(context, "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
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

            // ★ Glide가 이미지를 화면에 다 그렸는지(성공/실패) 감지하는 리스너
            val listener = object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    checkImageLoad() // ★ 이것만 추가
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    checkImageLoad() // ★ 이것만 추가
                    return false
                }
            }

            // requireContext() 대신 this를 써서 Fragment 수명주기를 따르도록 함
            Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.bg_round_top_20dp_white)
                .error(R.drawable.bg_round_top_20dp_white)
                .override(Target.SIZE_ORIGINAL)
                .dontAnimate()
                .centerCrop()
                .listener(listener) // ★ 리스너 부착
                .into(binding.shareImageA)

            Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.bg_round_20dp_white)
                .error(R.drawable.bg_round_20dp_white)
                .override(Target.SIZE_ORIGINAL)
                .dontAnimate()
                .centerCrop()
                .listener(listener) // ★ 리스너 부착
                .into(binding.shareImageB)
        } else {
            binding.shareImageA.setImageResource(R.drawable.bg_round_top_20dp_white)
            binding.shareImageB.setImageResource(R.drawable.bg_round_20dp_white)
            if (loadingDialog.isShowing) loadingDialog.dismiss()
        }
    }

    // ★ 이미지 2개가 다 로드되었는지 체크하고 로딩 다이얼로그를 닫아주는 함수
    private fun checkImageLoad() {
        loadedImageCount++
        if (loadedImageCount >= 2) { // A타입, B타입 모두 처리가 끝났다면
            if (isAdded && loadingDialog.isShowing) {
                loadingDialog.dismiss() // 로딩 종료! 사용자가 이제 버튼을 누를 수 있음
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

    // =========================================================================
    // ★ [핵심] 뷰를 캡처하면서 완벽한 라운드와 그림자를 입혀주는 마법의 함수
    // =========================================================================
    private fun captureCardBitmap(view: View): Bitmap? {
        if (view.width == 0 || view.height == 0) return null

        val density = resources.displayMetrics.density
        val radius = 20f * density // 20dp 라운드
        val shadowRadius = 8f * density // 그림자 퍼짐 정도 (8dp)
        val shadowDy = 2f * density // 그림자가 아래로 향하는 정도 (2dp)

        // 1. 그림자가 잘리지 않도록 여백을 포함한 넉넉한 사이즈의 비트맵 생성
        val bitmapWidth = view.width + (shadowRadius * 2).toInt()
        val bitmapHeight = view.height + (shadowRadius * 2).toInt() + shadowDy.toInt()
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 배경을 완전히 투명하게 초기화 (이게 없으면 모서리에 까만색이 남음)
        canvas.drawColor(Color.TRANSPARENT)

        // 2. 카드가 그려질 위치 계산 (여백만큼 밀어줌)
        val rectF = android.graphics.RectF(
            shadowRadius,
            shadowRadius,
            shadowRadius + view.width,
            shadowRadius + view.height
        )

        // 3. 그림자 직접 그리기 (소프트웨어 렌더링 지원)
        val shadowPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            // 20% 투명도의 검정색 그림자 부여
            setShadowLayer(shadowRadius, 0f, shadowDy, Color.parseColor("#33000000"))
        }
        // 둥근 사각형을 그리면서 그림자도 같이 렌더링됨
        canvas.drawRoundRect(rectF, radius, radius, shadowPaint)

        // 4. 뷰의 내용물이 둥근 테두리 밖으로 삐져나가지 못하게 캔버스를 둥글게 자르기 (클리핑)
        canvas.save()
        val path = android.graphics.Path().apply {
            addRoundRect(rectF, radius, radius, android.graphics.Path.Direction.CW)
        }
        canvas.clipPath(path)

        // 5. 뷰의 실제 내용을 캔버스(그림자가 시작되는 위치)에 그리기
        canvas.translate(shadowRadius, shadowRadius)
        // 뷰의 배경이 투명할 경우를 대비해 흰색 한 번 깔아주기
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        canvas.restore()

        return bitmap
    }

    // =========================================================================
    // 인스타, 카톡, X 공유
    // =========================================================================
    private fun processAndShareImage(isInstagram: Boolean, targetPackage: String?) {
        val targetView = if (isTypeA) binding.typeACard else binding.typeBCard

        // ★ 새로 만든 완벽한 캡처 함수 사용
        val bitmap = captureCardBitmap(targetView) ?: run {
            Toast.makeText(context, "화면을 불러오는 중입니다. 잠시 후 시도해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, "이미지를 준비 중입니다...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val imagesFolder = File(requireContext().cacheDir, "images")
                if (!imagesFolder.exists()) imagesFolder.mkdirs()
                imagesFolder.listFiles()?.forEach { it.delete() }

                val file = File(imagesFolder, "share_${System.currentTimeMillis()}.png")
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                stream.close()

                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                withContext(Dispatchers.Main) {
                    if (isInstagram) {
                        launchInstagramStoryIntent(uri)
                    } else {
                        launchGenericImageShareIntent(uri, targetPackage)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun launchInstagramStoryIntent(uri: Uri) {
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            type = "image/*"
            setPackage("com.instagram.android")
            putExtra("interactive_asset_uri", uri)
            putExtra("top_background_color", "#FAE0D4")
            putExtra("bottom_background_color", "#FFFFFF")
        }
        requireActivity().grantUriPermission("com.instagram.android", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "인스타그램 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
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

    // =========================================================================
    // ImgBB 업로드 후 링크 복사
    // =========================================================================
    private fun uploadToImgBBAndCopyLink() {
        val targetView = if (isTypeA) binding.typeACard else binding.typeBCard

        // ★ 새로 만든 완벽한 캡처 함수 사용
        val bitmap = captureCardBitmap(targetView) ?: run {
            Toast.makeText(context, "화면을 불러오는 중입니다. 잠시 후 시도해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, "링크를 생성하고 있습니다. 잠시만 기다려주세요...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
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
                        Toast.makeText(context, "링크가 복사되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "링크 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        Log.e("ImgBBError", "Code: ${response.code()}, Message: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
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