package com.bookiibookii.bookiibookii.common

import android.content.ContentResolver
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.InputStream

/**
 * 사진 캐시 복사 (#505).
 *
 * 실패하거나 취소돼도 반쯤 만든 파일이 캐시에 남으면 안 된다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class CacheFileCopyTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val resolver: ContentResolver get() = RuntimeEnvironment.getApplication().contentResolver

    @Test
    fun `사진을 캐시 폴더에 같은 내용으로 복사한다`() = runBlocking {
        val bytes = ByteArray(20_000) { (it % 256).toByte() }
        val source = tmp.newFile("source.jpg").apply { writeBytes(bytes) }
        val dir = tmp.newFolder("cache")

        val copied = copyUriToCacheFile(resolver, Uri.fromFile(source), dir)

        assertNotNull(copied)
        assertEquals(dir, copied!!.parentFile)
        assertTrue(copied.name.startsWith("profile_"))
        assertArrayEquals(bytes, copied.readBytes())
    }

    @Test
    fun `원본을 열 수 없으면 null을 반환하고 파일을 남기지 않는다`() = runBlocking {
        val dir = tmp.newFolder("cache")

        val copied = copyUriToCacheFile(resolver, Uri.fromFile(java.io.File(dir, "missing.jpg")), dir)

        assertNull(copied)
        assertEquals(0, dir.listFiles()!!.size)
    }

    @Test
    fun `빈 원본은 실패로 보고 파일을 남기지 않는다`() = runBlocking {
        val source = tmp.newFile("empty.jpg")
        val dir = tmp.newFolder("cache")

        assertNull(copyUriToCacheFile(resolver, Uri.fromFile(source), dir))
        assertEquals(0, dir.listFiles()!!.size)
    }

    @Test
    fun `복사 중 취소되면 만들던 파일을 지운다`() = runBlocking {
        val dir = tmp.newFolder("cache")
        lateinit var job: Job
        // 첫 조각을 읽는 순간 화면을 떠난 상황을 만든다
        val stream = object : InputStream() {
            override fun read(): Int = 1
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                job.cancel()
                b[off] = 1
                return 1
            }
        }
        val cancellingResolver = mockk<ContentResolver> {
            every { openInputStream(any()) } returns stream
        }

        job = launch { copyUriToCacheFile(cancellingResolver, Uri.EMPTY, dir) }
        job.join()

        assertTrue(job.isCancelled)
        assertEquals(0, dir.listFiles()!!.size)
    }
}
