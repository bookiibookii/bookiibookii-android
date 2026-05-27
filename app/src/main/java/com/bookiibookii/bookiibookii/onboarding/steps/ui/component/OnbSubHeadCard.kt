package com.bookiibookii.bookiibookii.onboarding.steps.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun OnbSubHeadCard(
    title: String,
    description: String,
    label: String? = null,
    secondLabel: String? = null,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .background(colors.white)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (label != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(label, style = typography.medium12, color = colors.uiMain)
                    if (secondLabel != null) {
                        Text(secondLabel, style = typography.regular12, color = colors.grey400)
                    }
                }
            }
            Text(title, style = typography.medium24, color = colors.grey900)
        }
        Text(description, style = typography.regular16, color = colors.grey500)
    }
}

// ─── 프리뷰 ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "OnbSubHeadCard - label 없음 (Step1)")
@Composable
private fun PreviewNoLabel() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OnbSubHeadCard(
                title = "만나서 반가워요!",
                description = "부키부키에서 사용할 정보를 알려주세요"
            )
        }
    }
}

@Preview(showBackground = true, name = "OnbSubHeadCard - 필수")
@Composable
private fun PreviewRequired() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OnbSubHeadCard(
                label = "필수",
                title = "나의 인생 책을 알려주세요",
                description = "가장 좋아하는 책을 최대 3권 선택해주세요."
            )
        }
    }
}

@Preview(showBackground = true, name = "OnbSubHeadCard - 필수 + 다중선택")
@Composable
private fun PreviewMultiSelect() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OnbSubHeadCard(
                label = "필수",
                secondLabel = "다중선택 가능",
                title = "독서 기록 방식을 알려주세요",
                description = "평소에 책을 어떻게 기록하시나요?"
            )
        }
    }
}

@Preview(showBackground = true, name = "OnbSubHeadCard - 선택")
@Composable
private fun PreviewOptional() {
    BookiiBookiiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OnbSubHeadCard(
                label = "선택",
                title = "한 문장으로 나를 소개해주세요",
                description = "다른 독서가들에게 나를 어떻게 소개하고 싶으신가요?"
            )
        }
    }
}
