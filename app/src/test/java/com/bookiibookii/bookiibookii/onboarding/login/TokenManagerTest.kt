package com.bookiibookii.bookiibookii.onboarding.login

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Base64 as JBase64

// isAccessTokenExpired만 검증 — prefs 계열은 Android Keystore 의존이라 JVM 테스트 불가
// application 지정: 실제 MyApplication은 KakaoSdk.init을 호출해 Robolectric에서 크래시
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TokenManagerTest {
    private fun jwt(payloadJson: String): String {
        val enc = JBase64.getUrlEncoder().withoutPadding()
        val header = enc.encodeToString("""{"alg":"HS256"}""".toByteArray())
        val payload = enc.encodeToString(payloadJson.toByteArray())
        return "$header.$payload.sig"
    }

    private fun now() = System.currentTimeMillis() / 1000L

    @Test fun `미래 exp는 만료 아님`() =
        assertFalse(TokenManager.isAccessTokenExpired(jwt("""{"exp":${now() + 3600}}""")))

    @Test fun `과거 exp는 만료`() =
        assertTrue(TokenManager.isAccessTokenExpired(jwt("""{"exp":${now() - 60}}""")))

    @Test fun `정확히 now == exp 는 만료`() =
        assertTrue(TokenManager.isAccessTokenExpired(jwt("""{"exp":${now()}}""")))

    @Test fun `exp 필드 없으면 만료`() =
        assertTrue(TokenManager.isAccessTokenExpired(jwt("""{"sub":"1"}""")))

    @Test fun `exp가 0이면 만료`() =
        assertTrue(TokenManager.isAccessTokenExpired(jwt("""{"exp":0}""")))

    @Test fun `점 구분 세그먼트 부족은 만료`() =
        assertTrue(TokenManager.isAccessTokenExpired("onlyonepart"))

    @Test fun `payload가 base64 불가면 만료`() =
        assertTrue(TokenManager.isAccessTokenExpired("a.!!!!.c"))

    @Test fun `payload가 JSON 아니면 만료`() {
        val enc = JBase64.getUrlEncoder().withoutPadding()
        val bad = "h." + enc.encodeToString("notjson".toByteArray()) + ".s"
        assertTrue(TokenManager.isAccessTokenExpired(bad))
    }
}
