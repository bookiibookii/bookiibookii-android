package com.bookiibookii.bookiibookii.onboarding.steps.ui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.steps.OnbViewModel
import com.bookiibookii.bookiibookii.onboarding.steps.model.OnbState
import com.bookiibookii.bookiibookii.onboarding.steps.ui.component.OnbSubHeadCard
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private const val MAX_INTRO_LENGTH = 50

@Composable
internal fun OnbStep4Content(vm: OnbViewModel, state: OnbState) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        OnbSubHeadCard(
            label = stringResource(R.string.onb_label_optional),
            title = stringResource(R.string.onb_step4_title),
            description = stringResource(R.string.onb_step4_desc)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = state.selfIntro,
                onValueChange = { new ->
                    if (new.length <= MAX_INTRO_LENGTH) vm.setSelfIntro(new)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, colors.grey300, RoundedCornerShape(16.dp))
                    .background(colors.white)
                    .padding(16.dp),
                textStyle = typography.regular15.copy(color = colors.grey900),
                cursorBrush = SolidColor(colors.uiMain),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (state.selfIntro.isEmpty()) {
                            Text(
                                text = stringResource(R.string.onb_step4_hint),
                                style = typography.regular15,
                                color = colors.grey500
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Text(
                text = "${state.selfIntro.length}/$MAX_INTRO_LENGTH",
                style = typography.regular12,
                color = colors.grey500,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Step 4 - 입력 전")
@Composable
private fun OnbStep4ContentEmptyPreview() {
    BookiiBookiiTheme {
        OnbStep4Content(vm = OnbViewModel(), state = OnbState())
    }
}

@Preview(showBackground = true, name = "Step 4 - 입력 후")
@Composable
private fun OnbStep4ContentFilledPreview() {
    BookiiBookiiTheme {
        OnbStep4Content(
            vm = OnbViewModel(),
            state = OnbState(selfIntro = "역시나 누군가를 사랑하고 사랑해야 할 당신을 위해")
        )
    }
}
