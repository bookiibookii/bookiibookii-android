package com.bookiibookii.bookiibookii.common

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 선택한 사진을 [dir] 아래 `${prefix}*.jpg` 파일로 복사한다. IO 스레드에서 실행된다.
 *
 * 갤러리·클라우드 URI는 나중에 다시 열지 못할 수 있어, 업로드 전에 앱 캐시로 복사해 둔다.
 * 실패하면 null, 취소되면 예외를 그대로 던진다. 두 경우 모두 만들던 파일은 지운다.
 */
suspend fun copyUriToCacheFile(
    contentResolver: ContentResolver,
    uri: Uri,
    dir: File,
    prefix: String = "profile_",
): File? = withContext(Dispatchers.IO) {
    var file: File? = null
    try {
        val target = File.createTempFile(prefix, ".jpg", dir).also { file = it }
        val input = contentResolver.openInputStream(uri) ?: run {
            target.delete()
            return@withContext null
        }
        input.use { src ->
            target.outputStream().use { dst ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    // 큰 사진 복사 중 화면을 떠나면 바로 멈춘다
                    ensureActive()
                    val read = src.read(buffer)
                    if (read < 0) break
                    dst.write(buffer, 0, read)
                }
            }
        }
        if (target.length() > 0) target else {
            target.delete()
            null
        }
    } catch (e: Exception) {
        file?.delete()
        if (e is CancellationException) throw e
        null
    }
}
