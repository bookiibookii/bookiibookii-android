package com.bookiibookii.bookiibookii.data.api

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

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
        val bitmap = decodeAndResize(contentResolver, uri)
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

    private fun decodeAndResize(contentResolver: ContentResolver, uri: Uri): Bitmap {
        val (width, height) = readImageSize(contentResolver, uri)
        val sampleSize = calculateInSampleSize(width, height, MAX_LONG_SIDE_PX)

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val sampled = contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: error("이미지 파일을 불러오지 못했습니다.")

        val rotated = applyExifRotation(contentResolver, uri, sampled)
        return scaleToLongSide(rotated, MAX_LONG_SIDE_PX)
    }

    private fun readImageSize(contentResolver: ContentResolver, uri: Uri): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: error("이미지 파일을 불러오지 못했습니다.")
        return options.outWidth to options.outHeight
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxLongSide: Int): Int {
        val longSide = maxOf(width, height)
        if (longSide <= maxLongSide) return 1
        var sample = 1
        while (longSide / (sample * 2) >= maxLongSide) {
            sample *= 2
        }
        return sample
    }

    private fun applyExifRotation(
        contentResolver: ContentResolver,
        uri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        val orientation = contentResolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: return bitmap

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    private fun scaleToLongSide(bitmap: Bitmap, maxLongSide: Int): Bitmap {
        val longSide = maxOf(bitmap.width, bitmap.height)
        if (longSide <= maxLongSide) return bitmap
        val ratio = maxLongSide.toFloat() / longSide
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }

    private fun Bitmap.toJpegBytes(quality: Int): ByteArray {
        val output = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, quality, output)
        return output.toByteArray()
    }
}
