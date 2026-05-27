package com.bookiibookii.bookiibookii.onboarding.steps.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.steps.OnbViewModel
import com.bookiibookii.bookiibookii.onboarding.steps.model.OnbState
import com.bookiibookii.bookiibookii.onboarding.steps.model.RecordMethod
import com.bookiibookii.bookiibookii.onboarding.steps.ui.component.OnbSubHeadCard
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun OnbStep3Content(vm: OnbViewModel, state: OnbState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OnbSubHeadCard(
            label = stringResource(R.string.onb_label_required),
            secondLabel = stringResource(R.string.onb_label_multi_select),
            title = stringResource(R.string.onb_step3_title),
            description = stringResource(R.string.onb_step3_desc)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RecordMethod.entries.forEach { method ->
                RecordMethodItem(
                    titleResId = method.titleResId,
                    iconRes = method.iconRes,
                    selected = state.recordMethods.contains(method),
                    onClick = { vm.toggleRecordMethod(method) }
                )
            }
            RecordMethodItem(
                titleResId = R.string.onb_step3_record_unknown,
                iconRes = R.drawable.ic_question,
                selected = state.isUnknownMethod,
                onClick = { vm.toggleUnknownMethod() }
            )
        }
    }
}

@Composable
private fun RecordMethodItem(
    titleResId: Int,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(BookiiBookiiTheme.shape.round16)
            .border(1.dp, if (selected) colors.uiMain150 else colors.grey200, BookiiBookiiTheme.shape.round16)
            .background(if (selected) colors.uiMainPale else colors.white)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (selected) colors.uiMain150 else colors.uiMainPale),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = colors.uiMain,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(titleResId),
            style = typography.regular15,
            color = if (selected) colors.uiMain else colors.grey900
        )
    }
}

// ─── 섹션 프리뷰 ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "RecordMethodItem - 미선택")
@Composable
private fun RecordMethodItemUnselectedPreview() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RecordMethodItem(
                titleResId = RecordMethod.MEMO.titleResId,
                iconRes = RecordMethod.MEMO.iconRes,
                selected = false,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "RecordMethodItem - 선택됨")
@Composable
private fun RecordMethodItemSelectedPreview() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RecordMethodItem(
                titleResId = RecordMethod.MEMO.titleResId,
                iconRes = RecordMethod.MEMO.iconRes,
                selected = true,
                onClick = {}
            )
        }
    }
}

// ─── 전체 화면 프리뷰 ─────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Step 3 - 전체")
@Composable
private fun OnbStep3ContentPreview() {
    BookiiBookiiTheme {
        OnbStep3Content(vm = OnbViewModel(), state = OnbState())
    }
}
