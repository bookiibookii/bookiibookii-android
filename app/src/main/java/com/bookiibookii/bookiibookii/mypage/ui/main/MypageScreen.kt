package com.bookiibookii.bookiibookii.mypage.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.mypage.UserProfileResDTO
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

@Composable
fun MypageScreen(
    profile: UserProfileResDTO? = null,
    onSaveIntroduction: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onSettingClick: () -> Unit = {},
    onBookshelfClick: () -> Unit = {},
    onWrittenReviewClick: () -> Unit = {},
    onReceivedReviewClick: () -> Unit = {},
    onProfileSettingClick: () -> Unit = {},
    onAddressManagementClick: () -> Unit = {},
    onInstagramShareClick: () -> Unit = {},
) {
    var isMottoEditing by remember { mutableStateOf(false) }
    var mottoInput by remember(profile?.introduction) { mutableStateOf(profile?.introduction ?: "") }
    var showShareDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
            Box(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
                MypTopBar(onBackClick = onBackClick, onSettingClick = onSettingClick)
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BookiiBookiiTheme.colors.white)
                        .padding(top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    ProfileSection(
                        nickname = profile?.nickname ?: "",
                        profileImageUrl = profile?.profileImageUrl,
                        onProfileSettingClick = onProfileSettingClick,
                        onAddressManagementClick = onAddressManagementClick,
                        onProfileShareClick = { showShareDialog = true },
                    )
                    MottoSection(
                        motto = profile?.introduction ?: "",
                        isEditing = isMottoEditing,
                        editText = mottoInput,
                        onEditTextChange = { mottoInput = it },
                        onEditClick = { mottoInput = profile?.introduction ?: ""; isMottoEditing = true },
                        onCancelClick = { isMottoEditing = false },
                        onSaveClick = { onSaveIntroduction(mottoInput); isMottoEditing = false },
                    )
                    RepresentativeBooksSection(
                        books = profile?.userBooks ?: emptyList(),
                        onArrowClick = onBookshelfClick,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                WrittenReviewsSection(
                    reviewCount = profile?.bookReviewCount ?: 0,
                    reviews = profile?.recentBookReviews,
                    onArrowClick = onWrittenReviewClick,
                )

                Spacer(modifier = Modifier.height(16.dp))
                ReceivedReviewsSection(
                    boomUpCount = profile?.boomUpCount ?: 0,
                    nickname = profile?.nickname ?: "",
                    reviews = profile?.recentReceivedReviews,
                    onArrowClick = onReceivedReviewClick,
                )

                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        if (showShareDialog) {
            ProfileShareDialog(
                name = profile?.nickname ?: "",
                motto = profile?.introduction ?: "",
                imageUrl = profile?.profileImageUrl,
                representativeBooks = profile?.userBooks ?: emptyList(),
                onDismiss = { showShareDialog = false },
                onInstagramClick = { showShareDialog = false; onInstagramShareClick() },
            )
        }
    }
}

@Composable
private fun MypTopBar(onBackClick: () -> Unit, onSettingClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(32.dp),
                )
            }
            Text(text = "마이페이지", style = BookiiBookiiTheme.typography.medium20, color = BookiiBookiiTheme.colors.grey900)
            IconButton(onClick = onSettingClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_gear),
                    contentDescription = "설정",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 0.5.dp)
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
fun MypageScreenPreview() {
    MypageScreen()
}
