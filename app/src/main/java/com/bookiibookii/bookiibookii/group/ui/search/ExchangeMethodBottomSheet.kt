package com.bookiibookii.bookiibookii.group.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetChip
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.component.bottomSheetTopShadow
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 교환 방식 필터 바텀시트
// API tradeTypes: 0=전체([]), 1=직접 교환([DIRECT]), 2=택배 교환([DELIVERY])
@Composable
fun ExchangeMethodBottomSheet(
    onApply: (List<String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    initialTradeTypes: List<String> = emptyList(),
) {
    val sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val options = listOf("전체", "직접 교환", "택배 교환")
    var selectedIndex by remember {
        mutableIntStateOf(
            when {
                "DIRECT" in initialTradeTypes -> 1
                "DELIVERY" in initialTradeTypes -> 2
                else -> 0
            }
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .bottomSheetTopShadow(cornerRadius = 20.dp)
            .background(color = BookiiBookiiTheme.colors.white, shape = sheetShape)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 44.dp, height = 4.dp)
                    .background(
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = BookiiBookiiTheme.shape.round50,
                    ),
            )
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "교환 방식",
                    style = BookiiBookiiTheme.typography.semibold20,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = options[selectedIndex],
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.uiMain,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            options.forEachIndexed { index, label ->
                BottomSheetChip(
                    text = label,
                    selected = index == selectedIndex,
                    onClick = { selectedIndex = index },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BottomSheetTwoBtnShort(
                text = "취소",
                style = BottomSheetBtnStyle.White,
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            )
            BottomSheetTwoBtnShort(
                text = "적용",
                style = BottomSheetBtnStyle.Dark,
                onClick = {
                    onApply(
                        when (selectedIndex) {
                            1 -> listOf("DIRECT")
                            2 -> listOf("DELIVERY")
                            else -> emptyList()
                        }
                    )
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun ExchangeMethodBottomSheetPreview() {
    BookiiPreview {
        ExchangeMethodBottomSheet(onApply = {}, onCancel = {})
    }
}
