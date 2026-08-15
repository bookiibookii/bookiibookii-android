# 1단계: 청소 + 테스트 인프라 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 릴리즈 보안 결함 제거, 죽은 코드 청소, 테스트 인프라 구축, 발견된 버그 3건의 테스트 재현·수정.

**Architecture:** 단일 모듈 Android 앱. 테스트는 JVM 단위 테스트(`app/src/test`)로만 작성하며, Android 프레임워크 의존 대상(Base64, org.json, Log)은 Robolectric으로 실행. `AuthInterceptor`는 생성자 주입 심(seam) 2개(`AuthTokenStore`, `AuthRouter`)를 도입해 테스트 가능하게 리팩토링.

**Tech Stack:** Kotlin 2.1, Compose, Retrofit 2.9/OkHttp 4.12, JUnit4, MockK, Turbine, kotlinx-coroutines-test, MockWebServer, Robolectric.

**Spec:** `docs/superpowers/specs/2026-08-15-phase1-cleanup-and-tests-design.md`

## Global Constraints

- **커밋·이슈 생성·푸시는 사용자가 직접 한다.** 실행자는 각 커밋 단계에서 커밋 메시지와 이슈 본문을 제안하고 사용자 확인을 기다린다. 브랜치도 사용자가 만든다 (`type/#이슈번호/작업명`).
- 커밋 메시지 형식: `[TYPE] 작업 내용 #이슈번호` (TYPE: FIX/CHORE/FEAT/REFACTOR/DOCS).
- CLI 빌드는 반드시 JDK 21 사용 (시스템 기본 JDK 26은 Gradle 8.13 미지원):
  `JAVA_HOME=/Users/qhrtj07/Library/Java/JavaVirtualMachines/ms-21.0.11/Contents/Home ./gradlew ...`
  이하 모든 gradlew 명령에 이 접두를 붙인다 (계획에서는 `[JDK21]`로 표기).
- 테스트 파일 위치: `app/src/test/java/com/bookiibookii/bookiibookii/` 하위, 대상과 같은 패키지.
- 명시된 버그 수정 3건(AuthInterceptor 2건, PlaceSearchViewModel 1건) 외에는 프로덕션 동작 변경 금지. `private`→`internal` 승격은 허용.
- 순수 로직 테스트는 현재 동작을 계약으로 고정하는 특성화 테스트다. 테스트 중 이상 동작을 발견해도 수정하지 않고 테스트 주석으로 문서화한다.

---

### Task 1: [이슈 1] 릴리즈 빌드 보안 설정 수정 — ✅ 코드 완료, 커밋만 남음

**Files:**
- Modified: `app/src/main/java/com/bookiibookii/bookiibookii/data/api/RetrofitClient.kt` (로깅 `BuildConfig.DEBUG` 게이트)
- Modified: `app/src/main/java/com/bookiibookii/bookiibookii/data/api/KakaoRetrofitClient.kt` (동일 + import 추가)
- Modified: `app/src/main/AndroidManifest.xml` (`usesCleartextTraffic` 제거)
- Deleted: `Get`, `Run` (레포 루트 빈 파일)

- [x] **Step 1: 코드 수정** (완료)
- [x] **Step 2: 빌드 검증** — `[JDK21] ./gradlew assembleDebug assembleRelease` PASS (완료)
- [ ] **Step 3: 이슈 본문 + 커밋 메시지 제안, 사용자 커밋 대기**

커밋 메시지 제안:
```
[FIX] 릴리즈 빌드 HTTP 로깅 비활성화 및 cleartext 설정 제거 #이슈번호
```

---

### Task 2: [이슈 2] 죽은 코드·미사용 의존성 제거

**Files:**
- Delete: `app/src/main/java/com/bookiibookii/bookiibookii/common/BaseActivity.kt`
- Delete: `app/src/main/java/com/bookiibookii/bookiibookii/common/BaseFragment.kt`
- Delete: `app/src/main/java/com/bookiibookii/bookiibookii/common/BaseDetailFragment.kt`
- Delete: `app/src/main/java/com/bookiibookii/bookiibookii/data/viewModel/LibraryViewModel.kt`
- Delete: `app/src/main/java/com/bookiibookii/bookiibookii/data/viewModel/LibraryCardViewModel.kt`
- Delete: `app/src/main/res/menu/menu_notification.xml`
- Delete: `app/src/main/res/values/styles.xml` (전체 — 사전 검증 필수)
- Modify: `app/src/main/AndroidManifest.xml` (BaseActivity `<activity>` 등록 제거)
- Modify: `app/build.gradle.kts` (buildFeatures 플래그, 의존성)
- Modify: `gradle/libs.versions.toml` (constraintlayout 항목 제거)

- [ ] **Step 1: 참조 0건 재검증** (하나라도 참조가 나오면 해당 파일은 삭제 목록에서 제외하고 보고)

```bash
grep -rn --include="*.kt" --include="*.xml" -E "BaseActivity|BaseFragment|BaseDetailFragment" app/src/main | grep -v "common/Base"
grep -rn --include="*.kt" "data.viewModel" app/src/main | grep -v "data/viewModel/"
grep -rn "menu_notification\|R.menu" app/src --include="*.kt"
# styles.xml의 모든 스타일명을 추출해 각각 참조 검색 (Theme_Bookii_Toast는 themes.xml 소속이라 무관)
grep -o 'name="[^"]*"' app/src/main/res/values/styles.xml | sed 's/name="//;s/"//' | while read s; do
  grep -rn "$s" app/src --include="*.kt" --include="*.xml" | grep -v "res/values/styles.xml" && echo "REFERENCED: $s"
done
```
Expected: 출력 없음 (모두 미참조)

- [ ] **Step 2: 파일 5개 + 리소스 2개 삭제, manifest에서 BaseActivity 등록 제거**

- [ ] **Step 3: `app/build.gradle.kts` 수정**

```kotlin
// buildFeatures에서 아래 두 줄 삭제
dataBinding = true
viewBinding = true

// dependencies에서 아래 항목 삭제
implementation(libs.androidx.constraintlayout)
implementation("com.github.bumptech.glide:glide:4.16.0")
annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
implementation("com.vanniktech:android-image-cropper:4.5.0")
implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
implementation("com.google.android.flexbox:flexbox:3.0.0")
```
`libs.versions.toml`에서 `constraintlayout` 버전·라이브러리 항목 삭제.
유지: `appcompat`, `material`, `fragment-ktx` (Fragment/Activity가 남아있는 동안), `activity-ktx`.

- [ ] **Step 4: 빌드 검증**

Run: `[JDK21] ./gradlew assembleDebug assembleRelease`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 이슈 본문 + 커밋 메시지 제안, 사용자 커밋 대기**

```
[CHORE] 미사용 View 시스템 잔재 및 죽은 코드 제거 #이슈번호
```

---

### Task 3: [이슈 3] 테스트 인프라 세팅

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Delete: `app/src/test/java/com/bookiibookii/bookiibookii/ExampleUnitTest.kt`
- Delete: `app/src/androidTest/java/com/bookiibookii/bookiibookii/ExampleInstrumentedTest.kt`

**Interfaces:**
- Produces: 이후 모든 테스트 태스크가 사용하는 의존성 앨리어스 — `libs.kotlinx.coroutines.test`, `libs.mockk`, `libs.turbine`, `libs.okhttp.mockwebserver`, `libs.robolectric`

- [ ] **Step 1: `libs.versions.toml`에 추가**

```toml
[versions]
coroutines = "1.7.3"          # 기존 kotlinx-coroutines 1.7.3과 일치시킬 것
mockk = "1.13.16"
turbine = "1.2.0"
robolectric = "4.14.1"
okhttp = "4.12.0"

[libraries]
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
okhttp-mockwebserver = { group = "com.squareup.okhttp3", name = "mockwebserver", version.ref = "okhttp" }
```

- [ ] **Step 2: `app/build.gradle.kts`에 추가**

```kotlin
// android { } 블록 안
testOptions {
    unitTests {
        isIncludeAndroidResources = true   // Robolectric용
    }
}

// dependencies
testImplementation(libs.kotlinx.coroutines.test)
testImplementation(libs.mockk)
testImplementation(libs.turbine)
testImplementation(libs.robolectric)
testImplementation(libs.okhttp.mockwebserver)
```

- [ ] **Step 3: Example 테스트 2개 삭제 후 sync 검증**

Run: `[JDK21] ./gradlew compileDebugUnitTestKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋 메시지 제안, 사용자 커밋 대기** (Task 4와 묶어 한 커밋도 가능 — 사용자 선택)

```
[CHORE] 단위 테스트 인프라 세팅 #이슈번호
```

---

### Task 4: [이슈 3] 순수 로직 엣지케이스 테스트 (Android 의존 없음)

**Files:**
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/common/BookTitleTest.kt`
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/common/GroupTagMapperTest.kt`
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/common/DateUtilsTest.kt`
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/tracker/model/TrackerActionTest.kt`
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/tracker/model/TrackerDetailMapperTest.kt`
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/group/model/GroupEditorUiStateTest.kt`
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/group/model/GroupApplyUiStateTest.kt`
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/ui/component/BookCoverUrlTest.kt`
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/notification/nav/NotificationRedirectRouterTest.kt`
- Modify: `app/src/main/java/com/bookiibookii/bookiibookii/ui/component/BookCover.kt:24-27` (`private` → `internal`, 로직 변경 없음)

각 파일: 테스트 작성 → 실행 → (BookCover만: internal 승격 후) 통과 확인. 전부 특성화 테스트이므로 "실패 → 구현" 사이클이 아니라 "작성 → 통과" 확인이다. 통과하지 않는 케이스는 실제 동작을 확인해 기대값을 실제 동작으로 수정하고 주석으로 명시한다.

- [ ] **Step 1: BookTitleTest 작성**

```kotlin
package com.bookiibookii.bookiibookii.common

import org.junit.Assert.assertEquals
import org.junit.Test

class BookTitleTest {
    @Test fun `null은 빈 문자열`() = assertEquals("", (null as String?).stripBookSubtitle())
    @Test fun `구분자 없으면 원본`() = assertEquals("프로젝트 헤일메리", "프로젝트 헤일메리".stripBookSubtitle())
    @Test fun `첫 구분자 앞만 남김`() = assertEquals("마션", "마션 - 스페셜 에디션 - 개정판".stripBookSubtitle())
    @Test fun `단어 내 하이픈은 유지`() = assertEquals("K-팝 시대", "K-팝 시대".stripBookSubtitle())
    @Test fun `선행 구분자면 빈 문자열`() = assertEquals("", " - 부제목만".stripBookSubtitle().trim())
    @Test fun `끝 공백 제거`() = assertEquals("제목", "제목  - 부제".stripBookSubtitle())
}
```

- [ ] **Step 2: GroupTagMapperTest 작성** (`bindTags`는 Chip 의존이라 제외)

```kotlin
package com.bookiibookii.bookiibookii.common

import org.junit.Assert.assertEquals
import org.junit.Test

class GroupTagMapperTest {
    @Test fun `알려진 태그 매핑`() = assertEquals("#메모환영", GroupTagMapper.toKoreanTag("MEMO"))
    @Test fun `미지 태그는 # 접두`() = assertEquals("#커스텀", GroupTagMapper.toKoreanTag("커스텀"))
    @Test fun `이미 #로 시작하면 그대로`() = assertEquals("#이미태그", GroupTagMapper.toKoreanTag("#이미태그"))
    @Test fun `빈 문자열은 # 하나`() = assertEquals("#", GroupTagMapper.toKoreanTag(""))
    @Test fun `대소문자 구분 - 소문자는 미매핑`() = assertEquals("#memo", GroupTagMapper.toKoreanTag("memo"))
    @Test fun `중복 한글 매핑 - POEM_ESSAY와 POETRY_ESSAY 동일`() =
        assertEquals(GroupTagMapper.toKoreanTag("POEM_ESSAY"), GroupTagMapper.toKoreanTag("POETRY_ESSAY"))
}
```

- [ ] **Step 3: DateUtilsTest 작성** (TZ 고정 필수)

```kotlin
package com.bookiibookii.bookiibookii.common

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone

class DateUtilsTest {
    private lateinit var originalTz: TimeZone

    @Before fun setUp() {
        originalTz = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    }
    @After fun tearDown() = TimeZone.setDefault(originalTz)

    // --- formatDate: 3단계 파싱 폴백 ---
    @Test fun `Z 오프셋 인스턴트 파싱`() =
        assertEquals("2026. 05. 21.", DateUtils.formatDate("2026-05-20T16:00:00Z")) // UTC 16시 = KST 익일 01시
    @Test fun `오프셋 없는 datetime은 UTC로 간주`() =
        assertEquals("2026. 05. 21.", DateUtils.formatDate("2026-05-20T16:00:00"))
    @Test fun `날짜만 있으면 UTC 자정 기준`() =
        assertEquals("2026. 05. 20.", DateUtils.formatDate("2026-05-20"))
    @Test fun `이미 포맷된 문자열은 그대로`() =
        assertEquals("2026. 05. 20.", DateUtils.formatDate("2026. 05. 20."))
    @Test fun `파싱 불가면 원본 반환`() =
        assertEquals("notadate", DateUtils.formatDate("notadate"))
    @Test fun `null과 blank는 빈 문자열`() {
        assertEquals("", DateUtils.formatDate(null))
        assertEquals("", DateUtils.formatDate("  "))
    }

    // --- calculateTimeAgo: 경계값 (실제 시각 기준 상대 오프셋, 5초 여유) ---
    private fun ago(d: Duration): String = Instant.now().minus(d).toString()

    @Test fun `1분 미만은 방금 전`() = assertEquals("방금 전", DateUtils.calculateTimeAgo(ago(Duration.ofSeconds(30))))
    @Test fun `59분은 분 단위`() = assertEquals("59분 전", DateUtils.calculateTimeAgo(ago(Duration.ofMinutes(59).plusSeconds(5))))
    @Test fun `60분 경계는 1시간 전`() = assertEquals("1시간 전", DateUtils.calculateTimeAgo(ago(Duration.ofMinutes(60).plusSeconds(5))))
    @Test fun `24시간 경계는 1일 전`() = assertEquals("1일 전", DateUtils.calculateTimeAgo(ago(Duration.ofHours(24).plusSeconds(5))))
    @Test fun `7일 경계부터는 날짜 포맷`() {
        val sevenDaysAgo = Instant.now().minus(Duration.ofDays(7).plusSeconds(5))
        val expected = java.time.format.DateTimeFormatter.ofPattern("yyyy. MM. dd.", java.util.Locale.US)
            .withZone(java.time.ZoneId.systemDefault()).format(sevenDaysAgo)
        assertEquals(expected, DateUtils.calculateTimeAgo(sevenDaysAgo.toString()))
    }
    @Test fun `서버 시계가 앞서면(음수 diff) 방금 전`() =
        assertEquals("방금 전", DateUtils.calculateTimeAgo(Instant.now().plus(Duration.ofHours(1)).toString()))
    @Test fun `파싱 실패 시 빈 문자열 - formatDate와 불일치가 현재 계약`() =
        assertEquals("", DateUtils.calculateTimeAgo("notadate"))

    // --- formatKstDateTime ---
    @Test fun `Z와 +0900은 같은 인스턴트로 표시`() {
        assertEquals("2026. 05. 20. 14:30", DateUtils.formatKstDateTime("2026-05-20T05:30:00Z"))
        assertEquals("2026. 05. 20. 14:30", DateUtils.formatKstDateTime("2026-05-20T14:30:00+09:00"))
    }
    @Test fun `오프셋 없으면 KST 벽시계로 간주`() =
        assertEquals("2026. 05. 20. 14:30", DateUtils.formatKstDateTime("2026-05-20T14:30:00"))
    @Test fun `파싱 불가면 원본`() = assertEquals("garbage", DateUtils.formatKstDateTime("garbage"))

    // --- meetingAtFromKst / parseKstLocalDateTime 왕복 ---
    @Test fun `KST 왕복 보존`() {
        val local = LocalDateTime.of(2026, 5, 20, 14, 30)
        assertEquals(local, DateUtils.parseKstLocalDateTime(DateUtils.meetingAtFromKst(local)))
    }
}
```

- [ ] **Step 4: TrackerActionTest 작성**

```kotlin
package com.bookiibookii.bookiibookii.tracker.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackerActionTest {
    @Test fun `미지 상태와 null은 None 페어`() {
        assertEquals(TrackerAction.None to TrackerAction.None, actionsForStatus(null))
        assertEquals(TrackerAction.None to TrackerAction.None, actionsForStatus("UNKNOWN_STATUS"))
    }
    @Test fun `17개 상태 전수 매핑`() {
        val expected = mapOf(
            "READING" to (TrackerAction.RecordProgress to TrackerAction.WriteReadingCard),
            "REVIEW_WRITING" to (TrackerAction.WriteBookReview to TrackerAction.WriteReadingCard),
            "REVIEW_WAITING_PARTNER" to (TrackerAction.EditBookReview to TrackerAction.WriteReadingCard),
            "EXCHANGE_REVIEW_WRITING" to (TrackerAction.WritePartnerReview to TrackerAction.None),
            "EXCHANGE_REVIEW_WAITING_PARTNER" to (TrackerAction.WritePartnerReview to TrackerAction.None),
            "TRACKING_REQUIRED" to (TrackerAction.RegisterTrackingNumber to TrackerAction.CheckDeliveryInfo),
            "RETURN_TRACKING_REQUIRED" to (TrackerAction.RegisterTrackingNumber to TrackerAction.CheckDeliveryInfo),
            "SHIPPING" to (TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo),
            "RETURNING" to (TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo),
            "WAITING_PARTNER_TRACKING_REGISTER" to (TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo),
            "WAITING_PARTNER_RECEIPT_CONFIRM" to (TrackerAction.ConfirmReceive to TrackerAction.CheckShippingInfo),
            "MEETING_REGISTER_REQUIRED" to (TrackerAction.GoToComments to TrackerAction.RegisterMeeting),
            "WAITING_HOST_MEETING_REGISTER" to (TrackerAction.GoToComments to TrackerAction.RegisterMeeting),
            "EXCHANGING" to (TrackerAction.ConfirmExchange to TrackerAction.CheckMeeting),
            "WAITING_PARTNER_MEETING_COMPLETE" to (TrackerAction.CompleteExchange to TrackerAction.None),
        )
        expected.forEach { (status, pair) -> assertEquals(status, pair, actionsForStatus(status)) }
    }
    @Test fun `양쪽 버튼 모두 비활성 상태`() {
        listOf("WAITING_PARTNER_TRACKING_REGISTER", "WAITING_PARTNER_RECEIPT_CONFIRM").forEach {
            assertTrue(isPrimaryActionDisabled(it)); assertTrue(isSecondaryActionDisabled(it))
        }
    }
    @Test fun `primary만 비활성 상태`() {
        listOf("WAITING_PARTNER_MEETING_COMPLETE", "EXCHANGE_REVIEW_WAITING_PARTNER").forEach {
            assertTrue(isPrimaryActionDisabled(it)); assertFalse(isSecondaryActionDisabled(it))
        }
    }
    @Test fun `null 상태는 비활성 아님`() {
        assertFalse(isPrimaryActionDisabled(null)); assertFalse(isSecondaryActionDisabled(null))
    }
    @Test fun `수령 확인 대기 상태의 진행률 텍스트는 시점에 따라 다름`() {
        assertEquals("수령 완료", progressTextOverride("WAITING_PARTNER_RECEIPT_CONFIRM", isMine = true))
        assertEquals("수령 전", progressTextOverride("WAITING_PARTNER_RECEIPT_CONFIRM", isMine = false))
        assertNull(progressTextOverride("READING", isMine = true))
        assertNull(progressTextOverride("REVIEW_WAITING_PARTNER", isMine = false))
    }
}
```

- [ ] **Step 5: TrackerDetailMapperTest 작성**

```kotlin
package com.bookiibookii.bookiibookii.tracker.model

import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDetailResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerStepDTO
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStepStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackerDetailMapperTest {
    private fun dto(
        dDay: Int? = 3,
        displayStatus: String? = "READING",
        steps: List<TrackerStepDTO>? = null,
        displayBookTitle: String? = "책제목",
        displayStatusLabel: String? = "읽는 중",
    ) = TrackerDetailResDTO(
        groupId = 1L, groupName = "그룹", tradeType = "DIRECT", myRole = "HOST",
        displayStatus = displayStatus, displayBookTitle = displayBookTitle,
        displayStatusLabel = displayStatusLabel, dDay = dDay,
        myBook = null, partnerBook = null, steps = steps,
    )
    private fun step(completed: Boolean?, status: String = "MY_BOOK_READING") =
        TrackerStepDTO(status = status, title = "t", description = "d", completed = completed)

    @Test fun `음수 D-day는 D-0으로 절삭`() = assertEquals("D-0", dto(dDay = -3).toUiState().dDay)
    @Test fun `null D-day도 D-0`() = assertEquals("D-0", dto(dDay = null).toUiState().dDay)
    @Test fun `steps null이면 빈 목록`() = assertTrue(dto(steps = null).toUiState().steps.isEmpty())
    @Test fun `전 단계 완료 시 InProgress 칩 없음`() {
        val ui = dto(steps = listOf(step(true), step(true))).toUiState()
        assertTrue(ui.steps.all { it.status == TrackerStepStatus.Completed })
    }
    @Test fun `첫 미완료 단계만 InProgress, 이후 미완료는 숨김, 최신이 앞`() {
        val ui = dto(steps = listOf(step(true), step(false), step(false))).toUiState()
        assertEquals(2, ui.steps.size)                                  // completed 1 + inProgress 1
        assertTrue(ui.steps.first().status is TrackerStepStatus.InProgress) // reversed → 최신이 첫 번째
    }
    @Test fun `제목 blank면 상태 라벨만`() =
        assertEquals("읽는 중", dto(displayBookTitle = "", displayStatusLabel = "읽는 중").toUiState().statusLabel)
    @Test fun `전 단계 완료면 현재 phase는 반납-4단계`() {
        val ui = dto(steps = listOf(step(true))).toUiState()
        assertEquals("반납", ui.currentStepLabel)
        assertEquals(4, ui.currentStepPosition)
    }
}
```

- [ ] **Step 6: GroupEditorUiStateTest + GroupApplyUiStateTest 작성**

```kotlin
package com.bookiibookii.bookiibookii.group.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupEditorUiStateTest {
    // enum 값 이름에 의존하지 않도록 entries.first() 사용
    private val style = ReadingStyle.entries.first()
    private val trade = ExchangeType.entries.first()
    private fun createValid() = GroupEditorUiState(
        isbn13 = "9791234567890", groupName = "그룹", tradeType = trade,
        selectedPlaceId = 1L, ruleStyle = style,
    )

    @Test fun `생성 모드 필수값 충족 시 제출 가능`() = assertTrue(createValid().canSubmit)
    @Test fun `소개 500자는 허용, 501자는 불가`() {
        assertTrue(createValid().copy(groupComment = "가".repeat(500)).canSubmit)
        assertFalse(createValid().copy(groupComment = "가".repeat(501)).canSubmit)
    }
    @Test fun `커스텀 규칙 4개까지 허용, 5개는 불가 - 프리셋 포함 5 상한`() {
        assertTrue(createValid().copy(customRules = List(4) { "규칙$it" }).canSubmit)
        assertFalse(createValid().copy(customRules = List(5) { "규칙$it" }).canSubmit)
    }
    @Test fun `blank 규칙은 카운트 제외`() =
        assertTrue(createValid().copy(customRules = listOf("규칙", "", "  ", "", "")).canSubmit)
    @Test fun `수정 모드는 도서-교환유형-주소 없이도 제출 가능`() =
        assertTrue(GroupEditorUiState(isEdit = true, groupName = "그룹", ruleStyle = style).canSubmit)
    @Test fun `생성 모드는 항상 dirty`() = assertTrue(createValid().isDirty)
    @Test fun `수정 모드 - 원본과 같으면 not dirty, blank 규칙 추가도 not dirty`() {
        val original = GroupEditorUiState.EditOriginal("그룹", 0, "", style, listOf("규칙1"))
        val state = GroupEditorUiState(
            isEdit = true, groupName = "그룹", readingPeriodIndex = 0,
            ruleStyle = style, customRules = listOf("규칙1", "", "  "), editOriginal = original,
        )
        assertFalse(state.isDirty)
        assertTrue(state.copy(groupComment = "변경").isDirty)
    }
}

class GroupApplyUiStateTest {
    @Test fun `책 미선택이면 불가`() = assertFalse(GroupApplyUiState(applyMsg = "한 마디").canSubmit)
    @Test fun `메시지 blank면 불가`() = assertFalse(GroupApplyUiState(isbn13 = "979", applyMsg = " ").canSubmit)
    @Test fun `50자는 허용, 51자는 불가`() {
        assertTrue(GroupApplyUiState(isbn13 = "979", applyMsg = "가".repeat(50)).canSubmit)
        assertFalse(GroupApplyUiState(isbn13 = "979", applyMsg = "가".repeat(51)).canSubmit)
    }
}
```

- [ ] **Step 7: BookCover.kt의 `ALADIN_COVER_SIZE`·`toAladinCover`를 `internal`로 승격 후 BookCoverUrlTest 작성**

```kotlin
// BookCover.kt 변경 (로직 동일)
internal val ALADIN_COVER_SIZE = Regex("""/(coversum|cover\d+|cover)/(?=[^/]+$)""")
internal fun String.toAladinCover(size: String): String =
    if (contains("image.aladin.co.kr")) replace(ALADIN_COVER_SIZE, "/$size/") else this
```

```kotlin
package com.bookiibookii.bookiibookii.ui.component

import org.junit.Assert.assertEquals
import org.junit.Test

class BookCoverUrlTest {
    private val base = "https://image.aladin.co.kr/product/123/45"
    @Test fun `coversum을 지정 사이즈로 교체`() =
        assertEquals("$base/cover500/img.jpg", "$base/coversum/img.jpg".toAladinCover("cover500"))
    @Test fun `cover200 등 숫자 토큰 교체`() =
        assertEquals("$base/cover500/img.jpg", "$base/cover200/img.jpg".toAladinCover("cover500"))
    @Test fun `bare cover 토큰 교체`() =
        assertEquals("$base/cover500/img.jpg", "$base/cover/img.jpg".toAladinCover("cover500"))
    @Test fun `파일명 직전 세그먼트만 교체 - 중간 세그먼트는 유지`() =
        assertEquals("$base/cover/extra/img.jpg", "$base/cover/extra/img.jpg".toAladinCover("cover500"))
    @Test fun `알라딘 도메인 아니면 원본 유지`() {
        val other = "https://example.com/cover200/img.jpg"
        assertEquals(other, other.toAladinCover("cover500"))
    }
}
```

- [ ] **Step 8: NotificationRedirectRouterTest 작성** (`fromPayload`만 — `fromIntent`는 Intent 의존이라 제외)

```kotlin
package com.bookiibookii.bookiibookii.notification.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationRedirectRouterTest {
    @Test fun `redirectType 없으면 null`() =
        assertNull(NotificationRedirectRouter.fromPayload(mapOf("groupId" to "1")))
    @Test fun `redirectType blank면 null`() =
        assertNull(NotificationRedirectRouter.fromPayload(mapOf("redirectType" to "  ")))
    @Test fun `null 맵이면 null`() =
        assertNull(NotificationRedirectRouter.fromPayload(null))
    @Test fun `FCM String id는 Long 변환`() {
        val r = NotificationRedirectRouter.fromPayload(mapOf("redirectType" to "GROUP", "groupId" to "12"))
        assertEquals(12L, r!!.groupId)
    }
    @Test fun `인앱 Number id도 Long 변환 - Gson Double 케이스`() {
        val r = NotificationRedirectRouter.fromPayload(mapOf("redirectType" to "GROUP", "groupId" to 12.0))
        assertEquals(12L, r!!.groupId)
    }
    @Test fun `숫자 아닌 String id는 null`() {
        val r = NotificationRedirectRouter.fromPayload(mapOf("redirectType" to "GROUP", "groupId" to "abc"))
        assertNull(r!!.groupId)
    }
    @Test fun `title blank면 null로 정규화`() {
        val r = NotificationRedirectRouter.fromPayload(mapOf("redirectType" to "GROUP", "title" to " "))
        assertNull(r!!.title)
    }
}
```

- [ ] **Step 9: 전체 실행**

Run: `[JDK21] ./gradlew testDebugUnitTest`
Expected: 전부 PASS. 실패 케이스는 실제 동작 확인 후 기대값을 실제 동작으로 교정하고 주석 문서화.

- [ ] **Step 10: 커밋 메시지 제안, 사용자 커밋 대기**

```
[CHORE] 순수 로직 엣지케이스 단위 테스트 추가 #이슈번호
```

---

### Task 5: [이슈 3] TokenManager JWT 만료 판정 테스트 (Robolectric)

**Files:**
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/onboarding/login/TokenManagerTest.kt`

- [ ] **Step 1: 테스트 작성** (`android.util.Base64`, `org.json` → Robolectric 필요. prefs 관련 메서드는 Keystore 의존이라 대상 제외)

```kotlin
package com.bookiibookii.bookiibookii.onboarding.login

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Base64 as JBase64

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class TokenManagerTest {
    private fun jwt(payloadJson: String): String {
        val enc = JBase64.getUrlEncoder().withoutPadding()
        val header = enc.encodeToString("""{"alg":"HS256"}""".toByteArray())
        val payload = enc.encodeToString(payloadJson.toByteArray())
        return "$header.$payload.sig"
    }
    private fun now() = System.currentTimeMillis() / 1000L

    @Test fun `미래 exp는 만료 아님`() = assertFalse(TokenManager.isAccessTokenExpired(jwt("""{"exp":${now() + 3600}}""")))
    @Test fun `과거 exp는 만료`() = assertTrue(TokenManager.isAccessTokenExpired(jwt("""{"exp":${now() - 1}}""")))
    @Test fun `정확히 now == exp 는 만료`() = assertTrue(TokenManager.isAccessTokenExpired(jwt("""{"exp":${now()}}""")))
    @Test fun `exp 필드 없으면 만료`() = assertTrue(TokenManager.isAccessTokenExpired(jwt("""{"sub":"1"}""")))
    @Test fun `exp가 0이면 만료`() = assertTrue(TokenManager.isAccessTokenExpired(jwt("""{"exp":0}""")))
    @Test fun `점 구분 세그먼트 부족은 만료`() = assertTrue(TokenManager.isAccessTokenExpired("onlyonepart"))
    @Test fun `payload가 base64 불가면 만료`() = assertTrue(TokenManager.isAccessTokenExpired("a.!!!!.c"))
    @Test fun `payload가 JSON 아니면 만료`() {
        val enc = JBase64.getUrlEncoder().withoutPadding()
        val bad = "h." + enc.encodeToString("notjson".toByteArray()) + ".s"
        assertTrue(TokenManager.isAccessTokenExpired(bad))
    }
}
```

- [ ] **Step 2: 실행**

Run: `[JDK21] ./gradlew testDebugUnitTest --tests "com.bookiibookii.bookiibookii.onboarding.login.TokenManagerTest"`
Expected: PASS

- [ ] **Step 3: 커밋 메시지 제안, 사용자 커밋 대기** (Task 4와 같은 이슈 — 별도 커밋 권장)

```
[CHORE] TokenManager JWT 만료 판정 테스트 추가 #이슈번호
```

---

### Task 6: [이슈 4] AuthInterceptor 주입 심 리팩토링 (동작 변경 없음)

**Files:**
- Create: `app/src/main/java/com/bookiibookii/bookiibookii/data/api/AuthInterceptorDeps.kt`
- Modify: `app/src/main/java/com/bookiibookii/bookiibookii/data/api/AuthInterceptor.kt`

**Interfaces:**
- Produces (Task 7이 사용):
```kotlin
interface AuthTokenStore {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(access: String, refresh: String, userId: Long)
    fun clear()
}
interface AuthRouter {          // 화면 라우팅만 담당 (쿨다운 포함). 토큰 clear는 담당하지 않음
    fun routeLogout()
    fun routeComError(type: ErrorType)
}
class AuthInterceptor(
    context: Context,
    private val tokenStore: AuthTokenStore = TokenManagerStore(context.applicationContext),
    private val refreshApi: () -> AuthApi = { RetrofitClient.authApiNoAuth() },
    private val router: AuthRouter = ActivityAuthRouter(context.applicationContext),
) : Interceptor
```

- [ ] **Step 1: `AuthInterceptorDeps.kt` 생성** — 인터페이스 2개 + 기본 구현 2개.
  - `TokenManagerStore(appContext)`: 각 메서드가 `TokenManager`의 동명 메서드에 위임.
  - `ActivityAuthRouter(appContext)`: 기존 `routeLogout`/`routeComError` 본문 이동. **이 단계에서는 `TokenManager.clear` 호출도 기존 위치(쿨다운 게이트 안) 그대로 유지** — 버그 수정은 Task 7에서 테스트 재현 후 진행. `tryClaimRoute`/`unlockRouting`/`ROUTE_COOLDOWN_MS` companion은 `AuthInterceptor`에 남기고 라우터가 참조 (기존 `ErrorActivity` 호출부 `AuthInterceptor.unlockRouting()` 시그니처 유지).

- [ ] **Step 2: `AuthInterceptor` 본문 치환** — `TokenManager.getAccessToken(appContext)` → `tokenStore.getAccessToken()`, `TokenManager.getRefreshToken` → `tokenStore.getRefreshToken()`, `TokenManager.saveTokens(...)` → `tokenStore.saveTokens(...)`, `RetrofitClient.authApiNoAuth()` → `refreshApi()`, `routeLogout(appContext)` → `router.routeLogout()`, `routeComError(appContext, type)` → `router.routeComError(type)`. 분기·락·로그 로직은 그대로.

- [ ] **Step 3: 빌드로 무변경 확인**

Run: `[JDK21] ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (기존 생성자 호출 `AuthInterceptor(context.applicationContext)`는 기본 인자로 그대로 동작)

- [ ] **Step 4: 커밋 메시지 제안, 사용자 커밋 대기**

```
[REFACTOR] AuthInterceptor 의존성 주입 구조로 분리 #이슈번호
```

---

### Task 7: [이슈 4] AuthInterceptor 테스트 + 버그 2건 수정 (TDD)

**Files:**
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/data/api/AuthInterceptorTest.kt`
- Modify: `app/src/main/java/com/bookiibookii/bookiibookii/data/api/AuthInterceptor.kt`
- Modify: `app/src/main/java/com/bookiibookii/bookiibookii/data/api/AuthInterceptorDeps.kt`

**Interfaces:**
- Consumes: Task 6의 `AuthTokenStore`/`AuthRouter`/`refreshApi` 생성자.

- [ ] **Step 1: 테스트 하네스 작성** (Robolectric — 프로덕션 코드의 `android.util.Log`·`org.json` 때문)

```kotlin
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class FakeTokenStore(
    @Volatile var access: String? = "A1",
    @Volatile var refresh: String? = "R1",
) : AuthTokenStore {
    @Volatile var cleared = false
    // 캡처-후-회전 레이스 재현용: 첫 getRefreshToken() 직후 값을 바꿔치기
    @Volatile var rotateRefreshOnFirstRead: String? = null
    private var refreshReads = 0
    override fun getAccessToken() = access
    @Synchronized override fun getRefreshToken(): String? {
        val v = refresh
        if (refreshReads++ == 0) rotateRefreshOnFirstRead?.let { refresh = it }
        return v
    }
    override fun saveTokens(access: String, refresh: String, userId: Long) {
        this.access = access; this.refresh = refresh
    }
    override fun clear() { cleared = true; access = null; refresh = null }
}

class FakeRouter : AuthRouter {
    val logouts = AtomicInteger(0)
    val comErrors = mutableListOf<ErrorType>()
    override fun routeLogout() { logouts.incrementAndGet() }
    override fun routeComError(type: ErrorType) { comErrors.add(type) }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class AuthInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var store: FakeTokenStore
    private lateinit var router: FakeRouter
    private lateinit var client: OkHttpClient
    private val refreshRequests = mutableListOf<RecordedRequest>()

    @Before fun setUp() {
        AuthInterceptor.unlockRouting()   // companion 쿨다운 상태 초기화 (테스트 간 누수 방지)
        server = MockWebServer().apply { start() }
        store = FakeTokenStore()
        router = FakeRouter()
        val refreshRetrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val authApi = refreshRetrofit.create(AuthApi::class.java)
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(
                context = RuntimeEnvironment.getApplication(),
                tokenStore = store,
                refreshApi = { authApi },
                router = router,
            ))
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
                    synchronized(refreshRequests) { refreshRequests.add(request) }
                    refreshResponse()
                } else apiResponses.removeFirst()
        }
    }
    // ... 개별 테스트는 Step 2~6
}
```

- [ ] **Step 2: 회귀 테스트 — 401 → 리프레시 성공 → 새 토큰으로 재시도** (현재 코드로 통과해야 함)

```kotlin
@Test fun `401이면 리프레시 후 새 액세스 토큰으로 재시도`() {
    dispatch(
        refreshResponse = { MockResponse().setBody(refreshSuccessBody()) },
        apiResponses = ArrayDeque(listOf(
            MockResponse().setResponseCode(401),
            MockResponse().setResponseCode(200).setBody("""{"ok":true}"""),
        )),
    )
    val res = call()
    assertEquals(200, res.code)
    assertEquals("A2", store.access)
    // 재시도 요청의 Authorization 헤더가 새 토큰인지 — 서버가 받은 마지막 일반 요청 확인
    // (MockWebServer takeRequest 순서: 401요청 → refresh → 재시도)
    server.takeRequest(); server.takeRequest()
    assertEquals("Bearer A2", server.takeRequest(1, TimeUnit.SECONDS)!!.getHeader("Authorization"))
}
```

- [ ] **Step 3: 회귀 테스트 — 동시 401 N개는 리프레시 1회 공유** (현재 코드로 통과해야 함)

```kotlin
@Test fun `동시 401 5건은 리프레시를 1회만 수행`() {
    val n = 5
    dispatch(
        refreshResponse = { MockResponse().setBody(refreshSuccessBody()).setBodyDelay(300, TimeUnit.MILLISECONDS) },
        apiResponses = ArrayDeque(
            List(n) { MockResponse().setResponseCode(401) } +
            List(n) { MockResponse().setResponseCode(200).setBody("{}") }
        ),
    )
    val done = CountDownLatch(n)
    val codes = java.util.Collections.synchronizedList(mutableListOf<Int>())
    repeat(n) { Thread { codes.add(call().code); done.countDown() }.start() }
    assertTrue(done.await(10, TimeUnit.SECONDS))
    assertTrue(codes.all { it == 200 })
    assertEquals(1, refreshRequests.size)
}
```
※ 주의: 이 테스트는 5개 401이 모두 리프레시 완료 전에 도착해야 성립. `setBodyDelay`로 리프레시를 지연시켜 보장. 간헐 실패 시 delay를 늘린다.

- [ ] **Step 4: 실패 테스트 — stale 리프레시 토큰 레이스 → 수정**

버그: `intercept()`가 401 시점(락 진입 전)에 읽은 refreshToken을 `waitOrRefreshToken(context, refreshToken)`으로 전달. 그 사이 다른 스레드의 리프레시가 토큰을 회전시키면 이미 무효화된 토큰으로 2차 리프레시 → 서버 400 → 정상 세션 강제 로그아웃.

```kotlin
@Test fun `리프레시 직전 토큰이 회전되어도 최신 리프레시 토큰을 사용`() {
    // 첫 getRefreshToken() 호출(=401 핸들러의 캡처) 직후 저장소가 R2로 회전됨을 시뮬레이션
    store.rotateRefreshOnFirstRead = "R2"
    dispatch(
        refreshResponse = { MockResponse().setBody(refreshSuccessBody(access = "A2", refresh = "R3")) },
        apiResponses = ArrayDeque(listOf(
            MockResponse().setResponseCode(401),
            MockResponse().setResponseCode(200).setBody("{}"),
        )),
    )
    call()
    val body = refreshRequests.single().body.readUtf8()
    assertTrue("리프레시 요청은 회전된 최신 토큰 R2를 보내야 함, 실제: $body", body.contains("\"refreshToken\":\"R2\""))
}
```

Run: 실행 → **FAIL** (현재는 캡처된 R1 전송) 확인.

수정: `intercept()`의 사전 캡처 값은 "존재 여부 검사"에만 쓰고, 실제 리프레시 요청 토큰은 **락 획득 후** `tokenStore.getRefreshToken()`으로 재읽기.

```kotlin
// intercept(): waitOrRefreshToken에 토큰을 넘기지 않도록 변경
val outcome: RefreshOutcome = waitOrRefreshToken()

// waitOrRefreshToken(): 시그니처에서 refreshToken 파라미터 제거, runBlocking 내부에서
val currentRefreshToken = tokenStore.getRefreshToken()
if (currentRefreshToken.isNullOrEmpty()) return@runBlocking RefreshOutcome.INVALID_TOKEN
// ... postRefresh(request = TokenRefreshRequest(currentRefreshToken))
```

Run: 재실행 → PASS. Step 2·3 회귀 테스트도 PASS 유지 확인.

- [ ] **Step 5: 실패 테스트 — 쿨다운이 토큰 clear를 삼키는 문제 → 수정**

버그: `routeLogout`이 `tryClaimRoute()` 실패 시 `TokenManager.clear` 전에 return → 3초 내 두 번째 로그아웃 사유 발생 시 토큰이 살아남음. 수정 방향: **토큰 clear는 인터셉터가 라우팅과 무관하게 항상 수행**, 라우터는 화면 이동(쿨다운 게이트)만 담당.

```kotlin
@Test fun `로그아웃 사유 발생 시 쿨다운과 무관하게 토큰은 항상 클리어`() {
    // 리프레시가 400 → INVALID_TOKEN → 로그아웃 경로
    dispatch(
        refreshResponse = { MockResponse().setResponseCode(400).setBody("""{"isSuccess":false}""") },
        apiResponses = ArrayDeque(listOf(
            MockResponse().setResponseCode(401),
            MockResponse().setResponseCode(401),
        )),
    )
    runCatching { call() }        // 1차: 로그아웃 라우팅 + clear
    assertTrue(store.cleared)
    // 저장소를 되살려 "3초 쿨다운 내 두 번째 로그아웃" 상황 재현
    store.cleared = false; store.access = "A1"; store.refresh = "R1"
    runCatching { call() }        // 2차: 쿨다운에 걸려도 clear는 되어야 함
    assertTrue("쿨다운 중에도 토큰은 클리어되어야 함", store.cleared)
}
```

Run: 실행 → **FAIL** 확인 (Task 6에서 기존 동작을 보존했으므로 2차 clear가 스킵됨).

수정:
- `AuthInterceptor`의 로그아웃 경로 3곳(400/404 인증 실패, refresh 엔드포인트 401, refreshToken 부재, INVALID_TOKEN)에서 `tokenStore.clear()`를 먼저 호출한 뒤 `router.routeLogout()` 호출.
- `ActivityAuthRouter.routeLogout()`에서 `TokenManager.clear` 제거 (쿨다운 게이트 + Activity 실행만 남김).

Run: 재실행 → PASS.

- [ ] **Step 6: 나머지 경로 테스트 작성·실행** (현재 코드로 통과해야 하는 계약 고정)

```kotlin
@Test fun `리프레시 400이면 로그아웃 라우팅 + IOException`() {
    dispatch(
        refreshResponse = { MockResponse().setResponseCode(400).setBody("{}") },
        apiResponses = ArrayDeque(listOf(MockResponse().setResponseCode(401))),
    )
    var thrown = false
    try { call() } catch (_: IOException) { thrown = true }
    assertTrue(thrown)
    assertEquals(1, router.logouts.get())
}

@Test fun `400 응답 body의 code가 AUTH로 시작하면 즉시 로그아웃`() {
    dispatch(
        refreshResponse = { MockResponse().setResponseCode(500) },
        apiResponses = ArrayDeque(listOf(
            MockResponse().setResponseCode(400).setBody("""{"code":"AUTH4001","message":"만료"}"""),
        )),
    )
    var thrown = false
    try { call() } catch (_: IOException) { thrown = true }
    assertTrue(thrown)
    assertEquals(1, router.logouts.get())
    assertTrue(store.cleared)
}

@Test fun `400인데 body가 JSON이 아니면 응답 그대로 통과`() {
    dispatch(
        refreshResponse = { MockResponse().setResponseCode(500) },
        apiResponses = ArrayDeque(listOf(MockResponse().setResponseCode(400).setBody("<html>err</html>"))),
    )
    assertEquals(400, call().code)
    assertEquals(0, router.logouts.get())
}

@Test fun `500이면 SYSTEM 에러 라우팅 후 응답 반환`() {
    dispatch(
        refreshResponse = { MockResponse().setResponseCode(500) },
        apiResponses = ArrayDeque(listOf(MockResponse().setResponseCode(503).setBody("{}"))),
    )
    assertEquals(503, call().code)
    assertEquals(listOf(ErrorType.SYSTEM), router.comErrors)
}

@Test fun `리프레시 토큰이 없으면 리프레시 시도 없이 로그아웃`() {
    store.refresh = null
    dispatch(
        refreshResponse = { MockResponse().setResponseCode(200) },
        apiResponses = ArrayDeque(listOf(MockResponse().setResponseCode(401))),
    )
    var thrown = false
    try { call() } catch (_: IOException) { thrown = true }
    assertTrue(thrown)
    assertEquals(0, refreshRequests.size)
    assertEquals(1, router.logouts.get())
    assertTrue(store.cleared)
}
```

- [ ] **Step 7: 부수 수정 — `waitOrRefreshToken`의 `catch (_: InterruptedException) {}`에 `Thread.currentThread().interrupt()` 추가** (인터럽트 플래그 복원)

- [ ] **Step 8: 전체 실행 + 빌드**

Run: `[JDK21] ./gradlew testDebugUnitTest assembleDebug`
Expected: 전부 PASS

- [ ] **Step 9: 이슈 본문 + 커밋 메시지 제안, 사용자 커밋 대기** (리팩토링 커밋과 분리)

```
[FIX] 토큰 리프레시 stale 토큰 레이스 및 쿨다운 시 토큰 미삭제 수정 #이슈번호
```

---

### Task 8: [이슈 5] PlaceSearchViewModel stale-append 수정 + 페이지네이션 테스트

**Files:**
- Create: `app/src/test/java/com/bookiibookii/bookiibookii/placesearch/vm/PlaceSearchViewModelTest.kt`
- Modify: `app/src/main/java/com/bookiibookii/bookiibookii/placesearch/vm/PlaceSearchViewModel.kt`

**Interfaces:**
- Consumes: `PlaceSearchRepository.searchPlaces(query, page): PlaceSearchPage?` (MockK로 모킹), `PlaceSearchUiState`, `PlaceSearchPage(results, isEnd, totalCount)`, `PlaceSearchResult(placeName, address, x, y)`

- [ ] **Step 1: 테스트 하네스 + 회귀 테스트 작성** (현재 코드로 통과해야 함)

```kotlin
package com.bookiibookii.bookiibookii.placesearch.vm

import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchPage
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult
import com.bookiibookii.bookiibookii.placesearch.data.PlaceSearchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceSearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: PlaceSearchRepository
    private lateinit var vm: PlaceSearchViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mockk()
        vm = PlaceSearchViewModel(repo)
    }
    @After fun tearDown() = Dispatchers.resetMain()

    private fun place(name: String) = PlaceSearchResult(placeName = name, address = "주소", x = 127.0, y = 37.5)
    private fun page(vararg names: String, isEnd: Boolean = false, total: Int = names.size) =
        PlaceSearchPage(results = names.map(::place), isEnd = isEnd, totalCount = total)

    @Test fun `검색 성공 시 결과와 페이지 상태 반영`() = runTest(dispatcher) {
        coEvery { repo.searchPlaces("카페", 1) } returns page("A", "B", isEnd = false, total = 10)
        vm.onQueryChange("카페"); vm.onSearch(); dispatcher.scheduler.advanceUntilIdle()
        val s = vm.state.value
        assertEquals(listOf("A", "B"), s.results.map { it.placeName })
        assertEquals(true, s.hasNext); assertEquals(10, s.totalCount); assertFalse(s.loading)
    }

    @Test fun `빈 검색어는 API 호출 없이 상태 초기화`() = runTest(dispatcher) {
        vm.onQueryChange("   "); vm.onSearch(); dispatcher.scheduler.advanceUntilIdle()
        assertEquals(emptyList<PlaceSearchResult>(), vm.state.value.results)
        assertNull(vm.state.value.totalCount)
        coVerify(exactly = 0) { repo.searchPlaces(any(), any()) }
    }

    @Test fun `hasNext false면 loadMore는 호출 안 함`() = runTest(dispatcher) {
        coEvery { repo.searchPlaces("카페", 1) } returns page("A", isEnd = true)
        vm.onQueryChange("카페"); vm.onSearch(); dispatcher.scheduler.advanceUntilIdle()
        vm.loadMore(); dispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 0) { repo.searchPlaces(any(), 2) }
    }

    @Test fun `loadMore 실패는 기존 결과 유지하고 멈춤`() = runTest(dispatcher) {
        coEvery { repo.searchPlaces("카페", 1) } returns page("A")
        coEvery { repo.searchPlaces("카페", 2) } returns null
        vm.onQueryChange("카페"); vm.onSearch(); dispatcher.scheduler.advanceUntilIdle()
        vm.loadMore(); dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf("A"), vm.state.value.results.map { it.placeName })
        assertFalse(vm.state.value.loadingMore)
    }

    @Test fun `loadingMore 중 재호출은 무시`() = runTest(dispatcher) {
        val gate = CompletableDeferred<PlaceSearchPage?>()
        coEvery { repo.searchPlaces("카페", 1) } returns page("A")
        coEvery { repo.searchPlaces("카페", 2) } coAnswers { gate.await() }
        vm.onQueryChange("카페"); vm.onSearch(); dispatcher.scheduler.advanceUntilIdle()
        vm.loadMore(); dispatcher.scheduler.runCurrent()
        vm.loadMore(); dispatcher.scheduler.runCurrent()   // 두 번째는 가드에 걸려야 함
        gate.complete(page("B")); dispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { repo.searchPlaces("카페", 2) }
    }
}
```

- [ ] **Step 2: 실패 테스트 — stale-append 재현**

```kotlin
@Test fun `loadMore 진행 중 새 검색이 완료되면 이전 페이지는 폐기`() = runTest(dispatcher) {
    val slowPage2 = CompletableDeferred<PlaceSearchPage?>()
    coEvery { repo.searchPlaces("카페", 1) } returns page("카페1")
    coEvery { repo.searchPlaces("카페", 2) } coAnswers { slowPage2.await() }
    coEvery { repo.searchPlaces("서점", 1) } returns page("서점1")

    vm.onQueryChange("카페"); vm.onSearch(); dispatcher.scheduler.advanceUntilIdle()
    vm.loadMore(); dispatcher.scheduler.runCurrent()          // "카페" 2페이지 in-flight
    vm.onQueryChange("서점"); vm.onSearch(); dispatcher.scheduler.advanceUntilIdle()  // 새 검색 완료
    slowPage2.complete(page("카페2")); dispatcher.scheduler.advanceUntilIdle()        // 늦은 응답 도착

    assertEquals(listOf("서점1"), vm.state.value.results.map { it.placeName })  // 현재 구현은 카페2가 append되어 FAIL
}
```

Run: `[JDK21] ./gradlew testDebugUnitTest --tests "*.PlaceSearchViewModelTest"`
Expected: 이 테스트만 **FAIL** ("서점1, 카페2"가 나옴)

- [ ] **Step 3: 수정 — 검색 세대(generation) 가드**

```kotlin
// PlaceSearchViewModel에 추가
private var searchGeneration = 0

// onSearch()의 빈 검색어 분기와 loadFirstPage() 시작 시 세대 증가,
// 각 launch 블록은 시작 시점 세대를 캡처하고 응답 반영 직전에 비교:
private fun loadFirstPage() {
    val keyword = _state.value.searchKeyword
    if (keyword.isBlank()) return
    val gen = ++searchGeneration
    viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        try {
            val page = repository.searchPlaces(query = keyword, page = FIRST_PAGE)
            if (gen != searchGeneration) return@launch   // 이후 검색이 시작됨 → 폐기
            ...
        } catch (e: Exception) {
            if (gen != searchGeneration) return@launch
            ...
        }
    }
}
// loadMore()도 동일하게 launch 직전 val gen = searchGeneration 캡처 후 반영 직전 비교.
// onSearch()의 빈 검색어 분기에도 searchGeneration++ 추가 (클리어 후 늦은 응답 차단).
```

- [ ] **Step 4: 전체 실행**

Run: `[JDK21] ./gradlew testDebugUnitTest --tests "*.PlaceSearchViewModelTest"`
Expected: 전부 PASS

- [ ] **Step 5: 이슈 본문 + 커밋 메시지 제안, 사용자 커밋 대기**

```
[FIX] 장소검색 stale 페이지 응답이 새 검색 결과에 붙는 문제 수정 #이슈번호
```

---

### Task 9: 마무리 검증

- [ ] **Step 1: 전체 테스트 + 릴리즈 빌드**

Run: `[JDK21] ./gradlew testDebugUnitTest assembleDebug assembleRelease`
Expected: BUILD SUCCESSFUL, 전 테스트 PASS

- [ ] **Step 2: 수동 스모크 안내** — 사용자에게 앱 실행 후 로그인 → 홈/트래커/서재 탭 진입 → 장소 검색 1회를 확인 요청 (의존성 제거·인터셉터 수정 영향 확인)

- [ ] **Step 3: 스펙·플랜 문서 커밋 메시지 제안**

```
[DOCS] 1단계 리팩토링 설계 및 구현 계획 문서 추가 #이슈번호
```
