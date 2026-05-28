package com.bookiibookii.bookiibookii.data.api

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

object S3Uploader {

    private const val MAX_LONG_SIDE_PX = 1600
    private const val INITIAL_QUALITY = 80
    private const val MIN_QUALITY = 10
    private const val QUALITY_STEP = 10
    private const val MAX_BYTES = 1024 * 1024
    private const val MIME_JPEG = "image/jpeg"

    // Presigned URL 업로드는 Authorization 인터셉터가 없어야 안전
    private val client = OkHttpClient()

    suspend fun uploadImage(
        contentResolver: ContentResolver,
        uri: Uri,
        presignedPutUrl: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = compressImage(contentResolver, uri)

            // TODO: 추후 로그 삭제
            android.util.Log.d("IMG_UPLOAD", "upload size=${bytes.size}B (${bytes.size / 1024}KB)")

            val requestBody = bytes.toRequestBody(MIME_JPEG.toMediaTypeOrNull())
            val request = Request.Builder()
                .url(presignedPutUrl)
                .put(requestBody)
                .addHeader("Content-Type", MIME_JPEG)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("S3 업로드 실패: HTTP ${response.code}")
                }
            }
        }
    }

    private fun compressImage(contentResolver: ContentResolver, uri: Uri): ByteArray {
        val bitmap = decodeImage(contentResolver, uri)
        try {
            var quality = INITIAL_QUALITY
            var bytes = bitmap.toJpegBytes(quality)
            while (bytes.size > MAX_BYTES && quality > MIN_QUALITY) {
                quality -= QUALITY_STEP
                bytes = bitmap.toJpegBytes(quality)
            }
            return bytes
        } finally {
            bitmap.recycle()
        }
    }

    // ImageDecoder 사용: JPEG, PNG, WebP, HEIF/HEIC 등 모든 포맷 + EXIF 회전 자동 처리
    // minSdk 28 = Android 9 이상이므로 별도 버전 분기 불필요
    private fun decodeImage(contentResolver: ContentResolver, uri: Uri): Bitmap {
        val source = if (uri.scheme == "file") {
            ImageDecoder.createSource(File(uri.path ?: error("이미지 경로를 확인할 수 없습니다.")))
        } else {
            ImageDecoder.createSource(contentResolver, uri)
        }

        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val longSide = maxOf(info.size.width, info.size.height)
            if (longSide > MAX_LONG_SIDE_PX) {
                val ratio = MAX_LONG_SIDE_PX.toFloat() / longSide
                decoder.setTargetSize(
                    maxOf(1, (info.size.width * ratio).toInt()),
                    maxOf(1, (info.size.height * ratio).toInt())
                )
            }
            // ALLOCATOR_SOFTWARE: hardware bitmap은 Bitmap.compress() 불가
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun Bitmap.toJpegBytes(quality: Int): ByteArray {
        val output = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, quality, output)
        return output.toByteArray()
    }
}
