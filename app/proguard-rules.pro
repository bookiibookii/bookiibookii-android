# R8 규칙 (#506)
# Retrofit 3·Gson 2.15·AndroidX 등은 라이브러리에 규칙이 들어 있어 여기서 다루지 않는다.
# WebView의 @JavascriptInterface 메서드는 AGP 기본 규칙(proguard-android-optimize.txt)이 유지한다.
# 앱 전체 패키지를 통째로 유지하는 규칙은 쓰지 않는다.

# 운영 크래시의 스택 트레이스를 mapping.txt로 줄 번호까지 복원하기 위해 남긴다.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 서버 DTO: Gson이 필드 이름을 JSON 키로 쓰고 리플렉션으로 객체를 만든다.
# @SerializedName이 없는 필드가 대부분이라 패키지 단위로 유지한다. enum 상수 이름도 JSON 값이다.
-keep class com.bookiibookii.bookiibookii.data.model.** { *; }

# 카드 상세 라우트 인자로 Gson 직렬화되는 UI 모델.
# #491에서 라우트의 JSON 인자를 없애면 이 규칙도 지운다.
-keep class com.bookiibookii.bookiibookii.library.ui.ReadingCard { *; }
-keep class com.bookiibookii.bookiibookii.library.ui.ReadingCardType { *; }

# 카카오 SDK: 자체 규칙이 없고 model 패키지를 Gson과 리플렉션으로 다룬다.
# ClientErrorCause 같은 enum 상수를 이름으로 찾기 때문에 난독화되면 NoSuchFieldException으로 시작 시 크래시한다.
-keep class com.kakao.sdk.**.model.** { *; }
