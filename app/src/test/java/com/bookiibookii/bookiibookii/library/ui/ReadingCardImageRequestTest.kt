package com.bookiibookii.bookiibookii.library.ui

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.decode.DataSource
import coil.disk.DiskCache
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ReadingCardImageRequestTest {
    @get:Rule val temporaryFolder = TemporaryFolder()
    private val server = MockWebServer()
    private lateinit var loader: ImageLoader

    @Before fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        server.start()
        val image = ByteArrayOutputStream().apply {
            Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
                .compress(Bitmap.CompressFormat.PNG, 100, this)
        }.toByteArray()
        repeat(4) {
            server.enqueue(MockResponse().setHeader("Content-Type", "image/png")
                .setHeader("Cache-Control", "max-age=3600")
                .setBody(Buffer().write(image)))
        }
        loader = ImageLoader.Builder(RuntimeEnvironment.getApplication())
            .diskCache(DiskCache.Builder().directory(temporaryFolder.newFolder()).build())
            .build()
    }

    @After fun tearDown() {
        loader.shutdown()
        server.shutdown()
        Dispatchers.resetMain()
    }

    private suspend fun load(signature: String, key: String?, software: Boolean = false): SuccessResult {
        val request = readingCardImageRequest(
            RuntimeEnvironment.getApplication(),
            server.url("/image?X-Amz-Signature=$signature").toString(),
            key,
            allowHardware = !software,
        ).newBuilder().size(8, 8).build()
        val result = loader.execute(request)
        check(result is SuccessResult) { "Image request failed: $result" }
        return result
    }

    @Test fun `new signature reuses the decoded image`() = runBlocking {
        assertEquals(DataSource.NETWORK, load("first", "cards/a").dataSource)
        assertEquals(DataSource.MEMORY_CACHE, load("second", "cards/a").dataSource)
        assertEquals(1, server.requestCount)
    }

    @Test fun `new signature reuses disk after memory is cleared`() = runBlocking {
        load("first", "cards/a")
        loader.memoryCache!!.clear()
        assertEquals(DataSource.DISK, load("second", "cards/a").dataSource)
        assertEquals(1, server.requestCount)
    }

    @Test fun `replacing the image uses the new object key`() = runBlocking {
        load("first", "cards/a")
        assertEquals(DataSource.NETWORK, load("second", "cards/b").dataSource)
        assertEquals(2, server.requestCount)
    }

    @Test fun `missing object keys do not merge unrelated images`() = runBlocking {
        load("first", "")
        assertEquals(DataSource.NETWORK, load("second", " ").dataSource)
        assertEquals(DataSource.NETWORK, load("third", null).dataSource)
        assertEquals(3, server.requestCount)
    }

    @Test fun `software share image reuses disk without a second download`() = runBlocking {
        load("first", "cards/a")
        val shared = load("second", "cards/a", software = true)
        assertEquals(DataSource.DISK, shared.dataSource)
        assertFalse(shared.request.allowHardware)
        assertEquals(DataSource.MEMORY_CACHE, load("third", "cards/a", software = true).dataSource)
        assertEquals(1, server.requestCount)
    }
}
