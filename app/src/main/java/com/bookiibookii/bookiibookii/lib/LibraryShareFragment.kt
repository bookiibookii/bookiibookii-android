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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.bookiibookii.bookiibookii.databinding.FragmentLibShareBinding
import java.io.File
import java.io.FileOutputStream

class LibraryShareFragment : DialogFragment() {

    private var _binding: FragmentLibShareBinding? = null
    private val binding get() = _binding!!

    // 공유할 데이터 (arguments로 받아온다고 가정)
    private var bookTitle: String = "나는 당신을 편애합니다"
    private var content: String = "책을 쓰지 않고 한 우물만 팠다면 지금의 나는 어떤 모습일까? 상상만 해도 끔찍하다."
    private var author: String = "foryxxng"
    private val cardId: Long = 123L // 웹 링크 생성용 ID

    // 현재 선택된 타입 (A or B)
    private var isTypeA = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLibShareBinding.inflate(inflater, container, false)
        // 배경 투명 처리 (둥근 모서리 적용)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 데이터 바인딩 및 글자수 제한 적용
        bindData()

        // 초기 상태 설정 (Type A 보이기)
        updateTypeVisibility(true)

        initListeners()
    }

    private fun bindData() {
        // 공통 Content: 100자 제한, 4줄 처리는 XML에서 maxLines=4, ellipsize=end로 처리됨
        val safeContent = if (content.length > 100) content.take(100) + "..." else content


        val titleA = if (bookTitle.length > 20) bookTitle.take(20) + "..." else bookTitle
        binding.shareTitleA.text = titleA
        binding.shareContentA.text = safeContent
        binding.shareAuthorA.text = author


        val titleB = if (bookTitle.length > 30) bookTitle.take(30) + "..." else bookTitle
        binding.shareTitleB.text = titleB // XML ID: text_top
        binding.shareContentB.text = safeContent
        binding.shareAuthorB.text = "by. $author"
    }

    private fun initListeners() {
        // 닫기 버튼
        binding.shareCloseIv.setOnClickListener { dismiss() }

        // 타입 A 선택
        binding.btnTypeA.setOnClickListener {
            isTypeA = true
            updateTypeVisibility(true)
        }

        // 타입 B 선택
        binding.btnTypeB.setOnClickListener {
            isTypeA = false
            updateTypeVisibility(false)
        }

        // 1. 인스타그램 스토리 공유 (이미지)
        binding.shareInsta.setOnClickListener {
            shareToInstagramStory()
        }

        // 2. 카카오톡 공유 (링크)
        binding.shareKakao.setOnClickListener {
            shareLinkToApp("com.kakao.talk")
        }

        // 3. X (Twitter) 공유 (링크 + 텍스트)
        binding.shareX.setOnClickListener {
            shareToTwitter()
        }

        // 4. 링크 복사
        binding.shareLink.setOnClickListener {
            copyLinkToClipboard()
        }
    }

    private fun updateTypeVisibility(isA: Boolean) {
        if (isA) {
            binding.typeACard.visibility = View.VISIBLE
            binding.typeBLayout.visibility = View.GONE
            // 선택된 버튼 스타일 변경 (필요 시 구현)
            binding.btnTypeA.alpha = 1.0f
            binding.btnTypeB.alpha = 0.5f
        } else {
            binding.typeACard.visibility = View.GONE
            binding.typeBLayout.visibility = View.VISIBLE
            binding.btnTypeA.alpha = 0.5f
            binding.btnTypeB.alpha = 1.0f
        }
    }

    // --- 기능 구현 메서드 ---

    // 웹 링크 생성 (독서 카드 이미지를 띄우는 웹 페이지 주소)
    private fun getShareUrl(): String {
        return "https://bookiibookii.com/card/$cardId"
    }

    // 1. 인스타그램 스토리 공유
    private fun shareToInstagramStory() {
        val targetView = if (isTypeA) binding.typeACard else binding.typeBLayout
        val uri = getViewBitmapUri(targetView)

        if (uri != null) {
            val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
                setDataAndType(uri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra("interactive_asset_uri", uri) // 스티커로 추가
                // 배경으로 하려면: putExtra("source_application", requireContext().packageName)
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "인스타그램이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 2. 특정 앱으로 링크 공유 (카카오톡 등)
    private fun shareLinkToApp(packageName: String) {
        val url = getShareUrl()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
            setPackage(packageName)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            // 해당 앱이 없으면 일반 공유 시트로 전환 or 토스트
            shareGenericLink()
        }
    }

    // 3. X (트위터) 공유
    private fun shareToTwitter() {
        val url = getShareUrl()
        val text = "[$bookTitle] 독서 카드 공유\n$url"

        // 트윗 작성 화면으로 바로 이동
        val tweetUrl = "https://twitter.com/intent/tweet?text=${Uri.encode(text)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl))

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "브라우저를 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 4. 클립보드 복사
    private fun copyLinkToClipboard() {
        val url = getShareUrl()
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("BookCardLink", url)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "링크가 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }

    // 일반 공유 (Fallback)
    private fun shareGenericLink() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getShareUrl())
        }
        startActivity(Intent.createChooser(intent, "공유하기"))
    }

    // [핵심] 뷰를 비트맵으로 변환하여 URI 반환
    private fun getViewBitmapUri(view: View): Uri? {
        try {
            // 1. View -> Bitmap
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.background?.draw(canvas) // 배경 그리기
            view.draw(canvas) // 뷰 그리기

            // 2. Bitmap -> File (Cache Dir)
            val imagesFolder = File(requireContext().cacheDir, "images")
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()

            // 3. File -> Uri (FileProvider)
            return FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "이미지 생성 실패", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    override fun onResume() {
        super.onResume()
        // 다이얼로그 크기 조절 (XML width가 340dp로 고정되어 있어서 wrap_content로 충분)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}