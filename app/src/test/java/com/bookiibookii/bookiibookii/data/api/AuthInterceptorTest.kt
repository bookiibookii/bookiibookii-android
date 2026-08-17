package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.error.model.ErrorType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class FakeTokenStore(
    @Volatile var access: String? = "A1",
    @Volatile var refresh: String? = "R1",
) : AuthTokenStore {
    @Volatile var cleared = false

    // 캡처-후-회전 레이스 재현용: 첫 getRefreshToken() 직후 다른 스레드의 리프레시 완료를 시뮬레이션
    @Volatile var rotateRefreshOnFirstRead: String? = null
    private var refreshReads = 0

    override fun getAccessToken() = access

    @Synchronized
    override fun getRefreshToken(): String? {
        val v = refresh
        if (refreshReads++ == 0) rotateRefreshOnFirstRead?.let { refresh = it }
        return v
    }

    override fun saveTokens(access: String, refresh: String, userId: Long) {
        this.access = access
        this.refresh = refresh
    }

    override fun clear() {
        cleared = true
        access = null
        refresh = null
    }
}

class FakeRouter : AuthRouter {
    val logouts = AtomicInteger(0)
    val comErrors = java.util.Collections.synchronizedList(mutableListOf<ErrorType>())
    override fun routeLogout() { logouts.incrementAndGet() }
    override fun routeComError(type: ErrorType) { comErrors.add(type) }
}

// Robolectric: 프로덕션 코드의 android.util.Log·org.json 때문. MyApplication은 KakaoSdk.init 크래시로 배제
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class AuthInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var store: FakeTokenStore
    private lateinit var router: FakeRouter
    private lateinit var client: OkHttpClient
    private val refreshRequests = java.util.Collections.synchronizedList(mutableListOf<RecordedRequest>())

    @Before fun setUp() {
        AuthInterceptor.unlockRouting() // companion 쿨다운 상태 초기화 (테스트 간 누수 방지)
        server = MockWebServer().apply { start() }
        store = FakeTokenStore()
        router = FakeRouter()
        val refreshRetrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val authApi = refreshRetrofit.create(AuthApi::class.java)
        client = OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor(
                    context = RuntimeEnvironment.getApplication(),
                    tokenStore = store,
                    refreshApi = { authApi },
                    router = router,
                )
            )
            .build()
    }

    @After fun tearDown() = server.shutdown()

    private fun call(path: String = "/api/data") =
        client.newCall(Request.Builder().url(server.url(path)).build()).execute()

    private fun refreshSuccessBody(access: String = "A2", refresh: String = "R2") =
        """{"isSuccess":true,"code":"OK","message":"","result":
           {"accessToken":"$access","refreshToken":"$refresh","userId":1}}"""

    // 경로 기반 디스패처: /api/auth/refresh 와 일반 요청을 구분
    private fun dispatch(
        refreshResponse: () -> MockResponse,
        apiResponses: ArrayDeque<MockResponse>,
    ) {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                if (request.path == "/api/auth/refresh") {
                    refreshRequests.add(request)
                    refreshResponse()
                } else synchronized(apiResponses) { apiResponses.removeFirst() }
        }
    }

    // --- 회귀 테스트: 현재 정상 동작을 계약으로 고정 ---

    @Test fun `401이면 리프레시 후 새 액세스 토큰으로 재시도`() {
        dispatch(
            refreshResponse = { MockResponse().setBody(refreshSuccessBody()) },
            apiResponses = ArrayDeque(
                listOf(
                    MockResponse().setResponseCode(401),
                    MockResponse().setResponseCode(200).setBody("""{"ok":true}"""),
                )
            ),
        )
        val res = call()
        assertEquals(200, res.code)
        assertEquals("A2", store.access)
        // 요청 순서: 401요청 → refresh → 재시도. 재시도 요청의 헤더가 새 토큰인지 확인
        server.takeRequest(); server.takeRequest()
        assertEquals("Bearer A2", server.takeRequest(1, TimeUnit.SECONDS)!!.getHeader("Authorization"))
    }

    @Test fun `동시 401 5건은 리프레시를 1회만 수행`() {
        val n = 5
        dispatch(
            refreshResponse = {
                MockResponse().setBody(refreshSuccessBody()).setBodyDelay(300, TimeUnit.MILLISECONDS)
            },
            apiResponses = ArrayDeque(
                List(n) { MockResponse().setResponseCode(401) } +
                    List(n) { MockResponse().setResponseCode(200).setBody("{}") }
            ),
        )
        val done = CountDownLatch(n)
        val codes = java.util.Collections.synchronizedList(mutableListOf<Int>())
        repeat(n) { Thread { codes.add(call().code); done.countDown() }.start() }
        assertTrue(done.await(10, TimeUnit.SECONDS))
        assertTrue("모두 200이어야 함: $codes", codes.all { it == 200 })
        assertEquals(1, refreshRequests.size)
    }

    // --- 버그 1: stale 리프레시 토큰 레이스 ---
    // 401 시점에 캡처한 리프레시 토큰이 락 진입 전에 회전되면(다른 스레드의 리프레시 완료),
    // 무효화된 옛 토큰으로 2차 리프레시 → 서버 400 → 정상 세션 강제 로그아웃
    @Test fun `리프레시 직전 토큰이 회전되어도 최신 리프레시 토큰을 사용`() {
        store.rotateRefreshOnFirstRead = "R2" // 첫 캡처 직후 저장소가 R2로 회전됨을 시뮬레이션
        dispatch(
            refreshResponse = { MockResponse().setBody(refreshSuccessBody(access = "A2", refresh = "R3")) },
            apiResponses = ArrayDeque(
                listOf(
                    MockResponse().setResponseCode(401),
                    MockResponse().setResponseCode(200).setBody("{}"),
                )
            ),
        )
        call()
        val body = refreshRequests.single().body.readUtf8()
        assertTrue(
            "리프레시 요청은 회전된 최신 토큰 R2를 보내야 함, 실제: $body",
            body.contains("\"refreshToken\":\"R2\""),
        )
    }
}
