package com.bookiibookii.bookiibookii.data.api

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object S3Uploader {

    // Presigned URL 업로드는 Authorization 인터셉터가 없어야 안전
    private val client = OkHttpClient()

    suspend fun uploadImage(
        contentResolver: ContentResolver,
        uri: Uri,
        presignedPutUrl: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val mime = contentResolver.getType(uri) ?: "application/octet-stream"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("이미지 파일을 불러오지 못했습니다.")

            val requestBody = bytes.toRequestBody(mime.toMediaTypeOrNull())
            val request = Request.Builder()
                .url(presignedPutUrl)
                .put(requestBody)
                .addHeader("Content-Type", mime)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("S3 업로드 실패: HTTP ${response.code}")
                }
            }
        }
    }
}