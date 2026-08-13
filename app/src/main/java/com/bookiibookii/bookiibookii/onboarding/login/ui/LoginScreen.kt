package com.bookiibookii.bookiibookii.onboarding.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import com.bookiibookii.bookiibookii.ui.theme.Kakao

@Composable
fun LoginScreen(
    isLoading: Boolean,
    showLoginComplete: Boolean,
    onKakaoClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiMain),
    ) {
        // 중앙 로고 + 워드마크 + 태그라인
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo_symbol),
                contentDescription = null,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(20.dp))
            Image(
                painter = painterResource(R.drawable.ic_logo_wordmark),
                contentDescription = null,
                modifier = Modifier
                    .width(204.dp)
                    .height(22.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append("읽고, 교환하고, 기록하다 –\n")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("부키부키")
                    }
                    append("에서 교환독서 파트너를 찾아보세요")
                },
                style = BookiiBookiiTheme.typography.regular14,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }

        // 하단 섹션 (SNS 로그인 + 약관)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 로그인 완료 메시지 OR SNS 로그인 버튼
            if (showLoginComplete) {
                Text(
                    text = "로그인이 성공적으로 완료되었습니다",
                    style = BookiiBookiiTheme.typography.medium16,
                    color = Color.White,
                )
            } else if (!isLoading) {
                // SNS 안내 문구
                Text(
                    text = "SNS 계정으로 간편 가입하기",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = Color.White,
                )
                Spacer(Modifier.height(12.dp))
                // SNS 버튼
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SnsLoginButton(
                        iconRes = R.drawable.ic_kakao,
                        backgroundColor = Kakao,
                        onClick = onKakaoClick,
                    )
                    Spacer(Modifier.width(24.dp))
                    SnsLoginButton(
                        iconRes = R.drawable.ic_google,
                        backgroundColor = Color.White,
                        onClick = onGoogleClick,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 약관 안내 (로딩/완료 상태와 무관하게 숨김)
            if (!isLoading && !showLoginComplete) {
                TermsNoticeText(
                    onTermsClick = onTermsClick,
                    onPrivacyClick = onPrivacyClick,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun SnsLoginButton(
    iconRes: Int,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .background(backgroundColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun TermsNoticeText(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val termsLabel = "서비스 약관"
    val privacyLabel = "개인정보 처리방침"
    val fullText = "로그인하면 부키부키의 $termsLabel 및 $privacyLabel\n에 동의하게 됩니다."

    val annotatedString = buildAnnotatedString {
        val termsStart = fullText.indexOf(termsLabel)
        val privacyStart = fullText.indexOf(privacyLabel)

        append(fullText.substring(0, termsStart))

        withStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = Color.White)) {
            pushStringAnnotation(tag = "TERMS", annotation = "TERMS")
            append(termsLabel)
            pop()
        }

        append(fullText.substring(termsStart + termsLabel.length, privacyStart))

        withStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = Color.White)) {
            pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY")
            append(privacyLabel)
            pop()
        }

        append(fullText.substring(privacyStart + privacyLabel.length))
    }

    androidx.compose.foundation.text.ClickableText(
        text = annotatedString,
        style = BookiiBookiiTheme.typography.regular14.copy(
            color = Color.White,
            textAlign = TextAlign.Center,
        ),
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                .firstOrNull()?.let { onTermsClick() }
            annotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                .firstOrNull()?.let { onPrivacyClick() }
        },
    )
}

@Preview(name = "로그인 화면 - 기본", showBackground = true)
@Composable
private fun LoginScreenPreview() {
    BookiiPreview {
        LoginScreen(
            isLoading = false,
            showLoginComplete = false,
            onKakaoClick = {},
            onGoogleClick = {},
            onTermsClick = {},
            onPrivacyClick = {},
        )
    }
}

@Preview(name = "로그인 화면 - 로딩 중", showBackground = true)
@Composable
private fun LoginScreenLoadingPreview() {
    BookiiPreview {
        LoginScreen(
            isLoading = true,
            showLoginComplete = false,
            onKakaoClick = {},
            onGoogleClick = {},
            onTermsClick = {},
            onPrivacyClick = {},
        )
    }
}

@Preview(name = "로그인 화면 - 완료", showBackground = true)
@Composable
private fun LoginScreenCompletePreview() {
    BookiiPreview {
        LoginScreen(
            isLoading = true,
            showLoginComplete = true,
            onKakaoClick = {},
            onGoogleClick = {},
            onTermsClick = {},
            onPrivacyClick = {},
        )
    }
}
