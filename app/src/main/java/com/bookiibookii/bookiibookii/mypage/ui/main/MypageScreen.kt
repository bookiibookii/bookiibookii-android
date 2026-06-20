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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.data.model.mypage.BookReviewSummaryDto
import com.bookiibookii.bookiibookii.data.model.mypage.ReceivedMemberReviewDto
import com.bookiibookii.bookiibookii.data.model.mypage.UserBookDto
import com.bookiibookii.bookiibookii.data.model.mypage.UserProfileResDTO
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 라우트 진입점 — 구 MypageFragment의 onCreateView/onViewCreated 로직을 그대로 이식.
// MypageViewModel은 여러 화면(메인/프로필수정/후기/탈퇴)에서 공유되므로(구 activityViewModels())
// 호출자(MypageNavHost)가 단일 인스턴스를 만들어 파라미터로 넘겨준다.
@Composable
fun MypageMainRoute(
    viewModel: com.bookiibookii.bookiibookii.mypage.vm.MypageViewModel,
    onBackClick: () -> Unit,
    onSettingClick: () -> Unit,
    onProfileSettingClick: () -> Unit,
    onAddressManagementClick: () -> Unit,
    onBookshelfClick: () -> Unit,
    onWrittenReviewClick: () -> Unit,
    onReceivedReviewClick: () -> Unit,
    onInstagramShareClick: () -> Unit,
) {
    val profile by viewModel.profileData.observeAsState()

    // 구 Fragment의 onViewCreated()처럼 화면이 다시 보일 때마다(최초 진입 포함) 재조회
    // (replace+addToBackStack 구조라 마이페이지로 복귀할 때마다 onViewCreated가 다시 호출되던 것과 동일)
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.fetchMypageData()
    }

    MypageScreen(
        profile = profile,
        onSaveIntroduction = { viewModel.updateIntroduction(it) },
        onBackClick = onBackClick,
        onSettingClick = onSettingClick,
        onProfileSettingClick = onProfileSettingClick,
        onAddressManagementClick = onAddressManagementClick,
        onBookshelfClick = onBookshelfClick,
        onWrittenReviewClick = onWrittenReviewClick,
        onReceivedReviewClick = onReceivedReviewClick,
        onInstagramShareClick = onInstagramShareClick,
    )
}

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
            BookiiBackButton(onClick = onBackClick)
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
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 1.dp)
    }
}

private val previewProfile = UserProfileResDTO(
    userId = 1L,
    profileImageUrl = null,
    nickname = "부키",
    introduction = "매일 한 챕터씩 읽는 중입니다.",
    userBooks = listOf(
        UserBookDto(title = "데미안", auth = "헤르만 헤세", image = null),
        UserBookDto(title = "1984", auth = "조지 오웰", image = null),
        UserBookDto(title = "작별인사", auth = "김영하", image = null),
    ),
    bookReviewCount = 3,
    recentBookReviews = listOf(
        BookReviewSummaryDto(
            bookTitle = "데미안",
            bookAuthor = "헤르만 헤세",
            tradeType = "DELIVERY",
            rating = 4.0,
            comment = "성장에 대해 다시 생각하게 한 책.",
            reviewDate = "2026. 05. 01.",
        ),
    ),
    boomUpCount = 5,
    recentReceivedReviews = listOf(
        ReceivedMemberReviewDto(
            reviewerNickname = "noshel",
            reviewerProfileUrl = null,
            reaction = "BOOM_UP",
            comment = "교환 매너가 좋았어요!",
            createdAt = "2026. 05. 02.",
        ),
    ),
)

@Preview(name = "마이페이지 - 데이터", showBackground = true, widthDp = 412, heightDp = 1100)
@Composable
fun MypageScreenPreview() {
    BookiiBookiiTheme {
        MypageScreen(profile = previewProfile)
    }
}

@Preview(name = "마이페이지 - 빈 상태", showBackground = true, widthDp = 412)
@Composable
fun MypageScreenEmptyPreview() {
    BookiiBookiiTheme {
        MypageScreen()
    }
}
