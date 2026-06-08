package com.bookiibookii.bookiibookii.common

import android.content.Context
import android.content.Intent
import android.net.Uri

// 부키부키 신고/문의 카카오 채널
private const val KAKAO_REPORT_CHANNEL_URL = "https://pf.kakao.com/_cIxlxjX"

// 서비스 이용약관 / 개인정보 처리방침 (설정·로그인 공통)
private const val TERMS_OF_SERVICE_URL = "https://www.bookiibookii.com/terms"
private const val PRIVACY_POLICY_URL = "https://www.bookiibookii.com/privacy"

// 신고하기 — 카카오 채널 외부 링크 열기 (앱 내 여러 신고 버튼 공통)
fun Context.openReportChannel() {
    openExternalUrl(KAKAO_REPORT_CHANNEL_URL)
}

// 서비스 이용약관 외부 링크 열기
fun Context.openTermsOfService() {
    openExternalUrl(TERMS_OF_SERVICE_URL)
}

// 개인정보 처리방침 외부 링크 열기
fun Context.openPrivacyPolicy() {
    openExternalUrl(PRIVACY_POLICY_URL)
}

// 외부 URL을 브라우저 등으로 열기. 처리할 앱이 없으면 토스트로 안내(크래시 방어)
fun Context.openExternalUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
        showCustomToast("링크를 열 수 없습니다.", false)
    }
}
