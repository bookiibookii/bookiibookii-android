package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun LibraryCardPreviewDialog(
    mode: AddCardMode,
    bookTitle: String,
    username: String,
    quote: String,
    memo: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BookiiBookiiTheme.colors.white),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(BookiiBookiiTheme.colors.uiMain)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(painter = painterResource(R.drawable.ic_book), contentDescription = null, tint = BookiiBookiiTheme.colors.white, modifier = Modifier.size(14.dp))
                        Text(text = bookTitle, style = BookiiBookiiTheme.typography.semibold12, color = BookiiBookiiTheme.colors.white)
                    }
                }

                when (mode) {
                    AddCardMode.TEXT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFF4E18), Color(0xFFFF7618), Color(0xFFFFC9A4)),
                                        start = Offset(0f, Float.POSITIVE_INFINITY),
                                        end = Offset(Float.POSITIVE_INFINITY, 0f),
                                    )
                                )
                                .padding(16.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(painter = painterResource(R.drawable.ic_quote), contentDescription = null, tint = BookiiBookiiTheme.colors.white, modifier = Modifier.size(20.dp))
                                Text(text = "\"${quote.ifBlank { "인용구를 입력해주세요." }}\"", style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.white)
                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Column {
                                Text(text = memo.ifBlank { "메모를 입력해주세요." }, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey800)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "by. $username", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey500, modifier = Modifier.align(Alignment.End))
                            }
                        }
                    }
                    AddCardMode.PHOTO -> {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(BookiiBookiiTheme.colors.grey300), contentAlignment = Alignment.Center) {
                            Text(text = "사진 영역", style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey500)
                        }
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Column {
                                Text(text = memo.ifBlank { "메모를 입력해주세요." }, style = BookiiBookiiTheme.typography.regular14, color = BookiiBookiiTheme.colors.grey800)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "by. $username", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey500, modifier = Modifier.align(Alignment.End))
                            }
                        }
                    }
                }
            }
        }
    }
}
