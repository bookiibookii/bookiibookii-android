package com.bookiibookii.bookiibookii.library.ui

import android.content.Context
import coil.memory.MemoryCache
import coil.request.ImageRequest

// 이미지 교체 시 새 S3 키를 발급받는 독서카드 전용 요청이다.
// 같은 키의 내용을 덮어쓰는 API로 변경되면 캐시 키에도 이미지 버전이 필요하다.
internal fun readingCardImageRequest(
    context: Context,
    imageUrl: String?,
    s3Key: String?,
    allowHardware: Boolean = true,
): ImageRequest = ImageRequest.Builder(context)
    .data(imageUrl)
    .allowHardware(allowHardware)
    .apply {
        // 키가 없을 때는 Coil의 URL 기반 캐시를 사용해 다른 이미지와 섞이지 않게 한다.
        if (!s3Key.isNullOrBlank()) {
            val cacheKey = "reading-card:$s3Key"
            diskCacheKey(cacheKey)
            // 공유 캡처용 software bitmap과 화면용 bitmap은 메모리에서 분리한다.
            // 원본 파일은 같으므로 디스크 캐시는 공유한다.
            memoryCacheKey(MemoryCache.Key(cacheKey, mapOf("allowHardware" to allowHardware.toString())))
        }
    }
    .build()
