package com.bookiibookii.bookiibookii.onboarding.steps.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.steps.OnbViewModel
import com.bookiibookii.bookiibookii.onboarding.steps.model.NicknameCheckState
import com.bookiibookii.bookiibookii.onboarding.steps.model.OnbState
import com.bookiibookii.bookiibookii.onboarding.steps.model.ProfileImageUploadState
import com.bookiibookii.bookiibookii.onboarding.steps.ui.content.OnbStep1Content
import com.bookiibookii.bookiibookii.onboarding.steps.ui.content.OnbStep2Content
import com.bookiibookii.bookiibookii.onboarding.steps.ui.content.OnbStep3Content
import com.bookiibookii.bookiibookii.onboarding.steps.ui.content.OnbStep4Content
import com.bookiibookii.bookiibookii.ui.component.FooterButton
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// ─── Stateful 진입점 ──────────────────────────────────────────────────────────

@Composable
fun OnbStepScreen(
    vm: OnbViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onOpenProfileCamera: () -> Unit,
    onOpenProfileGallery: () -> Unit,
) {
    val state by vm.state.observeAsState(OnbState())
    val nicknameCheckState by vm.nicknameCheckState.observeAsState(NicknameCheckState.Idle)
    val imageUploadState by vm.imageUploadState.observeAsState(ProfileImageUploadState.Idle)

    var currentStep by remember { mutableIntStateOf(1) }
    BackHandler(enabled = currentStep > 1) { currentStep-- }

    val isNextEnabled = when (currentStep) {
        1 -> nicknameCheckState is NicknameCheckState.Available &&
                state.gender != null &&
                state.birthdate != null &&
                imageUploadState !is ProfileImageUploadState.Loading
        2 -> state.lifeBooks.any { it != null }
        3 -> state.recordMethods.isNotEmpty() || state.isUnknownMethod
        4 -> true
        else -> false
    }

    OnbStepLayout(
        currentStep = currentStep,
        isNextEnabled = isNextEnabled,
        onBack = { if (currentStep == 1) onBack() else currentStep-- },
        onNext = { if (currentStep < 4) currentStep++ else onFinish() },
    ) {
        when (currentStep) {
            1 -> OnbStep1Content(
                vm = vm,
                state = state,
                nicknameCheckState = nicknameCheckState,
                imageUploadState = imageUploadState,
                onOpenCamera = onOpenProfileCamera,
                onOpenGallery = onOpenProfileGallery,
            )
            2 -> OnbStep2Content(vm = vm, state = state)
            3 -> OnbStep3Content(vm = vm, state = state)
            4 -> OnbStep4Content(vm = vm, state = state)
        }
    }
}

// ─── Stateless 레이아웃 (헤더 + 진행바 + 콘텐츠 슬롯 + 푸터) ─────────────────

@Composable
private fun OnbStepLayout(
    currentStep: Int,
    isNextEnabled: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = BookiiBookiiTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.uiBg)
    ) {
        OnbStepHeader(onBack = onBack)

        OnbStepProgressBar(
            currentStep = currentStep,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            content()
        }

        FooterButton(
            text = if (currentStep < 4) "다음" else "완료",
            enabled = isNextEnabled,
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )
    }
}

// ─── 헤더 ─────────────────────────────────────────────────────────────────────

@Composable
private fun OnbStepHeader(onBack: () -> Unit) {
    val colors = BookiiBookiiTheme.colors
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(colors.white)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.white)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.ic_logo_wordmark),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colors.uiMain),
                modifier = Modifier.width(204.dp).height(22.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(40.dp))
        }
        HorizontalDivider(thickness = 1.dp, color = colors.grey200)
    }
}

// ─── 진행바 ───────────────────────────────────────────────────────────────────

@Composable
private fun OnbStepProgressBar(currentStep: Int, modifier: Modifier = Modifier) {
    val colors = BookiiBookiiTheme.colors
    Row(
        modifier = modifier.height(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            if (index > 0) Spacer(modifier = Modifier.width(12.dp))
            val stepNum = index + 1
            if (stepNum <= currentStep) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.uiMain)
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(colors.grey200)
                )
            }
        }
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "헤더")
@Composable
private fun PreviewHeader() {
    BookiiBookiiTheme {
        OnbStepHeader(onBack = {})
    }
}

@Preview(showBackground = true, name = "진행바 - Step 1")
@Composable
private fun PreviewProgressBarStep1() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OnbStepProgressBar(currentStep = 1, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true, name = "진행바 - Step 3")
@Composable
private fun PreviewProgressBarStep3() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OnbStepProgressBar(currentStep = 3, modifier = Modifier.fillMaxWidth())
        }
    }
}


@Preview(showBackground = true, name = "Step 1 - 프로필")
@Composable
private fun PreviewStep1() {
    BookiiBookiiTheme {
        OnbStepLayout(currentStep = 1, isNextEnabled = false, onBack = {}, onNext = {}) {
            OnbStep1Content(
                vm = OnbViewModel(),
                state = OnbState(),
                nicknameCheckState = NicknameCheckState.Idle,
                imageUploadState = ProfileImageUploadState.Idle,
                onOpenCamera = {},
                onOpenGallery = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Step 2 - 인생 책")
@Composable
private fun PreviewStep2() {
    BookiiBookiiTheme {
        OnbStepLayout(currentStep = 2, isNextEnabled = false, onBack = {}, onNext = {}) {
            OnbStep2Content(vm = OnbViewModel(), state = OnbState())
        }
    }
}

@Preview(showBackground = true, name = "Step 3 - 기록 방식")
@Composable
private fun PreviewStep3() {
    BookiiBookiiTheme {
        OnbStepLayout(currentStep = 3, isNextEnabled = false, onBack = {}, onNext = {}) {
            OnbStep3Content(vm = OnbViewModel(), state = OnbState())
        }
    }
}

@Preview(showBackground = true, name = "Step 4 - 한 문장")
@Composable
private fun PreviewStep4() {
    BookiiBookiiTheme {
        OnbStepLayout(currentStep = 4, isNextEnabled = true, onBack = {}, onNext = {}) {
            OnbStep4Content(vm = OnbViewModel(), state = OnbState())
        }
    }
}
