# 1단계: 긴급 수정·죽은 코드 청소 + 테스트 인프라 설계

- 작성일: 2026-08-15
- 상태: 사용자 리뷰 대기
- 후속 단계: 2단계(단일 Activity Compose 마이그레이션, 별도 스펙), 3단계(ViewModel 엣지케이스 테스트)

## 1. 배경

앱은 XML → Compose 마이그레이션이 약 95% 완료된 상태다. XML 레이아웃은 0개이고 모든 화면 본체는 `@Composable`이며, View 시스템은 껍데기(Fragment 7개 + MainActivity의 프로그래매틱 View 트리)로만 남아 있다. 테스트는 0개이며, 근본 원인은 DI 부재와 `RetrofitClient` 전역 싱글톤 구조다.

전체 코드 탐색에서 다음이 확인되었다.

- **보안**: 릴리즈 빌드에서 `HttpLoggingInterceptor.Level.BODY`가 무조건 활성화되어 Authorization 토큰이 Logcat에 노출됨. `usesCleartextTraffic="true"`가 불필요하게 설정됨(BASE_URL은 모두 HTTPS).
- **죽은 코드**: `BaseActivity`/`BaseFragment`/`BaseDetailFragment`(서브클래스 0개), `data/viewModel/`의 ViewModel 2개(미참조), 죽은 리소스와 미사용 View 시스템 의존성 다수.
- **실제 버그 의심 사례**:
  - `AuthInterceptor`: 리프레시 토큰을 락 진입 전에 캡처 → 동시 401 시 이미 회전된(stale) 토큰으로 2차 리프레시 → 정상 세션 강제 로그아웃 레이스.
  - `AuthInterceptor`: 3초 라우팅 쿨다운(`tryClaimRoute`)에 밀린 두 번째 실패는 에러 화면·로그아웃 없이 조용히 삼켜짐. `routeLogout`이 쿨다운에 지면 `TokenManager.clear` 자체를 건너뜀.
  - `PlaceSearchViewModel`: `loadMore` 진행 중 새 검색 시 이전 페이지 응답이 새 결과에 붙는 stale-append(요청 가드 없음).

## 2. 목표

1. 릴리즈 빌드의 보안 설정 결함을 즉시 제거한다.
2. 죽은 코드·미사용 의존성을 제거해 2단계 마이그레이션의 diff를 줄인다.
3. 테스트 인프라를 세우고, UI 마이그레이션과 무관한 로직에 대해 "특수 상황(엣지케이스)" 테스트를 작성한다.
4. 탐색에서 발견된 인증·페이지네이션 버그를 실패하는 테스트로 재현한 뒤 수정한다(TDD).

## 3. 비범위 (이번 단계에서 하지 않는 것)

- 단일 Activity 마이그레이션 (2단계, 별도 스펙)
- 나머지 ViewModel 엣지케이스 테스트 (3단계 — NotificationCenter ABA, GroupComment 이모지 절단 등)
- `isMinifyEnabled` 활성화, 버전 카탈로그 통일, 하드코딩 문자열 리소스화
- Repository 레이어 전면 도입, 공용 Loading/Error 컴포저블 (마이그레이션과 함께)
- `appcompat`·`material`·`fragment-ktx` 제거 (Fragment/Activity가 남아있는 동안 유지)

## 4. 이슈 분할

| # | 유형 | 내용 | 브랜치 |
|---|---|---|---|
| 1 | FIX | 릴리즈 빌드 보안 설정 수정 | `fix/#N/release-security-config` |
| 2 | CHORE | 죽은 코드·미사용 의존성 제거 | `chore/#N/remove-dead-code` |
| 3 | CHORE | 테스트 인프라 세팅 + 순수 로직 엣지케이스 테스트 | `chore/#N/test-infra-pure-logic` |
| 4 | FIX | AuthInterceptor 토큰 리프레시 레이스 수정 (+재현 테스트) | `fix/#N/auth-refresh-race` |
| 5 | FIX | PlaceSearchViewModel stale-append 수정 (+페이지네이션 테스트) | `fix/#N/place-search-stale-append` |

의존성: 3번이 4·5번의 선행 조건. 1·2번은 독립.

## 5. 상세 설계

### 이슈 1 — 릴리즈 빌드 보안 설정 수정

- `RetrofitClient`, `KakaoRetrofitClient`: `HttpLoggingInterceptor` 레벨을 `BuildConfig.DEBUG`일 때만 `BODY`, 릴리즈에서는 `NONE`.
- `AndroidManifest.xml`: `android:usesCleartextTraffic="true"` 제거.
- 레포 루트의 빈 파일 `Get`, `Run` 삭제.
- 검증: `assembleDebug` + `assembleRelease` 빌드 통과.

### 이슈 2 — 죽은 코드·미사용 의존성 제거

삭제 대상 (전부 미참조 확인됨):

- `common/BaseActivity.kt`, `common/BaseFragment.kt`, `common/BaseDetailFragment.kt` + manifest의 stale `BaseActivity` 등록
- `data/viewModel/LibraryViewModel.kt`, `data/viewModel/LibraryCardViewModel.kt`
- `res/menu/menu_notification.xml`
- `res/values/styles.xml`의 죽은 스타일 (단, `Theme_Bookii_Toast`는 `CommonToast`가 사용하므로 유지)
- `buildFeatures`의 `dataBinding = true`, `viewBinding = true`
- Gradle 의존성: Glide + `glide:compiler`(annotationProcessor), `android-image-cropper`, `swiperefreshlayout`, `flexbox`, `constraintlayout`
- 검증: 각 삭제 그룹 후 빌드 통과. `grep`으로 참조 0건 재확인 후 삭제.

### 이슈 3 — 테스트 인프라 + 순수 로직 테스트

**의존성 추가 (버전 카탈로그 경유):**

- `kotlinx-coroutines-test` — `Dispatchers.setMain` + virtual time
- `mockk` — 페이크/모킹
- `turbine` — Flow 검증
- `okhttp3:mockwebserver` — 이슈 4의 인터셉터 테스트
- `robolectric` — `android.util.Base64`(`TokenManager`), `org.json` 의존 테스트

**테스트 대상 (JVM 단위 테스트, `app/src/test`):**

| 대상 | 핵심 엣지케이스 |
|---|---|
| `common/DateUtils.kt` | 3단계 파싱 폴백(`Z`/`+09:00`/오프셋 없음/`yyyy-MM-dd`/쓰레기 값), 1분·60분·24시간·7일 경계, 음수 diff(서버 시계 앞섬) → "방금 전", 실패 시 `formatDate`는 원본·`calculateTimeAgo`는 `""` 반환 불일치 문서화. 테스트에서 TZ 고정 |
| `tracker/model/TrackerAction.kt` | 17개 상태 분기 × `progressTextOverride(status, isMine)`, 미지의 상태 |
| `tracker/model/TrackerDetailMapper.kt` | 전체 완료 → InProgress 칩 없음, 빈 steps, 음수 D-day → `"D-0"` |
| `group/model/GroupEditorUiState.kt` | 500/501자 경계, 규칙 1..5개 카운트, 생성/수정 모드 `isDirty` |
| `group/model/GroupApplyUiState.kt` | 50자 경계 |
| `common/BookTitle.kt` | null, 구분자 없음, 선행 `" - "`, `"K-팝"` 미절단 |
| `common/GroupTagMapper.toKoreanTag` | 미지 태그 → `#` 접두, 이미 `#`, 빈 문자열, 대소문자 |
| `notification/nav/NotificationRedirectRouter.fromPayload` | Number/String/null/파싱 불가/blank type |
| `ui/component/BookCover.kt` 알라딘 커버 regex | 비알라딘 URL, `cover200`→`cover500`, 파일명 내 `cover` 미치환 |
| `onboarding/login/TokenManager.isAccessTokenExpired` | 변조 JWT, `exp` 부재, `exp==0`, `now==exp` 경계 (Robolectric) |

일부 `private` 함수는 테스트를 위해 `internal`로 승격만 하고 로직은 변경하지 않는다.

### 이슈 4 — AuthInterceptor 토큰 리프레시 레이스 수정

MockWebServer 기반 재현 테스트 → 수정 순서(TDD):

1. **동시 401 시 리프레시 1회 공유** — 성공 케이스 회귀 테스트.
2. **stale 리프레시 토큰 레이스** — 401 처리 시 리프레시 토큰을 락 진입 전에 읽는 문제. 수정: 락 안(`waitOrRefreshToken` 내부)에서 `TokenManager`로부터 재읽기.
3. **쿨다운이 두 번째 실패를 삼키는 문제** — 수정: `routeLogout`에서 `TokenManager.clear`를 쿨다운 판정과 무관하게 항상 수행. 라우팅(화면 이동)만 쿨다운 적용.
4. 리프레시 실패(400/401) → 로그아웃, 타임아웃 → NETWORK 에러 경로 검증.

동작 계약(성공 시 새 토큰으로 재시도, 실패 시 로그아웃/에러 화면)은 유지하고 내부 동기화만 고친다.

### 이슈 5 — PlaceSearchViewModel stale-append 수정

- 재현 테스트: `loadMore` 응답 지연 중 `onSearch` 호출 → 이전 페이지가 새 결과에 append됨.
- 수정: 검색 세대(요청 ID) 가드 추가 — 응답 도착 시 세대가 다르면 폐기.
- 추가 테스트: `hasNext=false` 시 `loadMore` 차단, `loading`/`loadingMore` 중복 호출 가드, `loadMore` 실패 시 기존 결과 유지, 빈 검색어 → 호출 없이 상태 초기화.

## 6. 테스트 전략

- 버그 수정(이슈 4·5)은 "실패하는 재현 테스트 → 수정 → 통과" 순서를 지킨다.
- 순수 로직 테스트(이슈 3)는 현재 동작을 계약으로 고정하는 특성화 테스트다. 테스트 중 발견되는 불일치(예: `DateUtils` 반환값 불일치)는 수정하지 않고 테스트에 문서화만 하고, 수정 여부는 별도 논의.
- 전 이슈 공통 검증: `./gradlew test assembleDebug assembleRelease` 통과 (JDK 21 필요 — 시스템 기본 JDK 26은 Gradle 8.13 미지원).

## 7. 리스크

- `AuthInterceptor` 수정은 인증 전체 경로에 영향 → 재현 테스트 + 기존 성공 경로 회귀 테스트를 반드시 함께 작성.
- 의존성 제거(이슈 2)는 R8/컴파일 타임에만 검증됨 → 빌드 + 앱 주요 화면 수동 확인 1회.
- `internal` 승격은 API 노출 범위 확대지만 단일 모듈이라 실질 영향 없음.
