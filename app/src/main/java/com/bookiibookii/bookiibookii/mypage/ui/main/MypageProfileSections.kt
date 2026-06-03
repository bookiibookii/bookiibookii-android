package com.bookiibookii.bookiibookii.mypage.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.UserBookDto
import com.bookiibookii.bookiibookii.mypage.ui.detail.verticalRotation
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
internal fun ProfileSection(
    nickname: String,
    profileImageUrl: String? = null,
    onProfileSettingClick: () -> Unit = {},
    onAddressManagementClick: () -> Unit = {},
    onProfileShareClick: () -> Unit = {},
) {
    val btnShape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfilePlaceholder(modifier = Modifier.size(52.dp), imageUrl = profileImageUrl)
            Text(
                text = nickname,
                style = BookiiBookiiTheme.typography.semibold20,
                color = BookiiBookiiTheme.colors.grey900,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(btnShape)
                    .background(BookiiBookiiTheme.colors.white)
                    .border(1.dp, BookiiBookiiTheme.colors.grey200, btnShape)
                    .clickable(onClick = onProfileSettingClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "프로필 수정", style = BookiiBookiiTheme.typography.semibold15, color = BookiiBookiiTheme.colors.grey900, maxLines = 1)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(btnShape)
                    .background(BookiiBookiiTheme.colors.white)
                    .border(1.dp, BookiiBookiiTheme.colors.grey200, btnShape)
                    .clickable(onClick = onAddressManagementClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "주소지 관리", style = BookiiBookiiTheme.typography.semibold15, color = BookiiBookiiTheme.colors.grey900, maxLines = 1)
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(btnShape)
                    .background(BookiiBookiiTheme.colors.white)
                    .border(1.dp, BookiiBookiiTheme.colors.grey200, btnShape)
                    .clickable(onClick = onProfileShareClick)
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = "공유",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
internal fun MottoSection(
    motto: String,
    isEditing: Boolean,
    editText: String,
    onEditTextChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "나를 대표하는 문구",
                modifier = Modifier.weight(1f),
                style = BookiiBookiiTheme.typography.semibold16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BookiiBookiiTheme.colors.grey200)
                    .clickable(onClick = onEditClick)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(text = "수정", style = BookiiBookiiTheme.typography.medium11, color = BookiiBookiiTheme.colors.grey700)
            }
        }

        if (isEditing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.grey100)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { if (it.length <= 100) onEditTextChange(it) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    placeholder = {
                        Text(text = "나만의 인용구를 입력하세요...", style = BookiiBookiiTheme.typography.regular15, color = BookiiBookiiTheme.colors.grey500)
                    },
                    textStyle = BookiiBookiiTheme.typography.regular15.copy(color = BookiiBookiiTheme.colors.grey900),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    maxLines = 4,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "${editText.length}/100", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey500)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BookiiBookiiTheme.colors.grey200),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = BookiiBookiiTheme.colors.white),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(text = "취소", style = BookiiBookiiTheme.typography.regular15, color = BookiiBookiiTheme.colors.grey900)
                        }
                        Button(
                            onClick = onSaveClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BookiiBookiiTheme.colors.grey900),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(text = "저장", style = BookiiBookiiTheme.typography.regular15, color = BookiiBookiiTheme.colors.white)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.grey100)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_quote),
                    contentDescription = null,
                    tint = if (motto.isBlank()) BookiiBookiiTheme.colors.grey300 else BookiiBookiiTheme.colors.uiMain,
                    modifier = Modifier.size(28.dp),
                )
                if (motto.isBlank()) {
                    Text(text = "나를 대표하는 문구가 없어요", style = BookiiBookiiTheme.typography.regular15, color = BookiiBookiiTheme.colors.grey400)
                } else {
                    Text(text = motto, style = BookiiBookiiTheme.typography.medium15, color = BookiiBookiiTheme.colors.grey700)
                }
            }
        }
    }
}

@Composable
internal fun RepresentativeBooksSection(books: List<UserBookDto>, onArrowClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "나를 대표하는 책", style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BookiiBookiiTheme.colors.grey200, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(text = "${books.size}/7권", style = BookiiBookiiTheme.typography.medium11, color = BookiiBookiiTheme.colors.grey900)
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.size(24.dp).graphicsLayer { scaleX = -1f }.clickable { onArrowClick() },
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            val gap = 8.dp
            val itemWidth = (maxWidth - gap * 6) / 7
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.Bottom,
            ) {
                books.forEachIndexed { index, book ->
                    BookSpineItem(title = book.title, isOrange = index % 2 == 0, modifier = Modifier.width(itemWidth))
                }
            }
        }
    }
}

@Composable
private fun BookSpineItem(title: String, isOrange: Boolean, modifier: Modifier = Modifier) {
    val bgColor   = if (isOrange) BookiiBookiiTheme.colors.uiMain150 else BookiiBookiiTheme.colors.uiMainSubPale
    val textColor = if (isOrange) BookiiBookiiTheme.colors.uiMain     else BookiiBookiiTheme.colors.uiMainSub
    val archHeight = 15.dp

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .padding(top = archHeight / 2)
                .fillMaxWidth()
                .background(color = bgColor, shape = RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                .padding(top = 24.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = title, style = BookiiBookiiTheme.typography.medium16, color = textColor, maxLines = 1, modifier = Modifier.verticalRotation())
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(archHeight)) {
            val path = Path().apply {
                arcTo(rect = Rect(0f, 0f, size.width, size.height), startAngleDegrees = 180f, sweepAngleDegrees = 180f, forceMoveTo = true)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color = bgColor)
        }
    }
}
