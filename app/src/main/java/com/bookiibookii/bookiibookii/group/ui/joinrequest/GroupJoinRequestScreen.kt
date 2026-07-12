package com.bookiibookii.bookiibookii.group.ui.joinrequest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.model.group.GroupAppItem
import com.bookiibookii.bookiibookii.error.ErrorActivity
import com.bookiibookii.bookiibookii.error.model.ErrorType
import com.bookiibookii.bookiibookii.group.model.ApplicationListUiState
import com.bookiibookii.bookiibookii.group.vm.JoinRequestViewModel
import com.bookiibookii.bookiibookii.ui.component.BookCover
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.component.LocalOnProfileClick
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 신청자 명단 화면 — VM 주입/상태 수집 (stateful)
@Composable
fun GroupJoinRequestRoute(
    groupId: Long,
    onBack: () -> Unit,
    onAccepted: () -> Unit = {},
    viewModel: JoinRequestViewModel = viewModel(),
) {
    val uiState by viewModel.applicationListState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // 진입 시 및 groupId 변경 시 명단 로드
    LaunchedEffect(groupId) {
        viewModel.loadApplicationList(groupId)
    }
    // 수락/거절 결과 처리 — 성공 시 토스트(공통 디자인), 실패 시 에러 토스트
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is JoinRequestViewModel.Event.ApplicationUpdated -> {
                    val verb = if (event.status == "ACCEPTED") "수락" else "거절"
                    context.showCustomToast(
                        "${event.applicantName} 님의 요청을 ${verb}했어요",
                        isSuccess = true,
                    )
                    // 수락 시: 상세 화면을 건너뛰고 그 전 화면으로 나가 재조회 (거절은 머무름)
                    if (event.status == "ACCEPTED") onAccepted()
                }
                is JoinRequestViewModel.Event.ShowError ->
                    context.showCustomToast(event.message, isSuccess = false)
                is JoinRequestViewModel.Event.NotFound ->
                    context.startActivity(ErrorActivity.newIntent(context, ErrorType.GROUP_DELETED))
                is JoinRequestViewModel.Event.Applied,
                is JoinRequestViewModel.Event.Canceled,
                is JoinRequestViewModel.Event.AddressReady,
                is JoinRequestViewModel.Event.AddressMissing -> Unit
            }
        }
    }
    GroupJoinRequestScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = { viewModel.loadApplicationList(groupId) },
        onAccept = { applyId, name ->
            viewModel.updateApplicationStatus(applyId, "ACCEPTED", name, groupId)
        },
        onReject = { applyId, name ->
            viewModel.updateApplicationStatus(applyId, "REJECTED", name, groupId)
        },
    )
}

@Composable
fun GroupJoinRequestScreen(
    uiState: ApplicationListUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAccept: (applyId: Long, applicantName: String) -> Unit,
    onReject: (applyId: Long, applicantName: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        GroupJoinRequestHeader(
            requestCount = uiState.totalCount,
            onBack = onBack,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.loading && uiState.items.isEmpty() -> {
                    CircularProgressIndicator(color = BookiiBookiiTheme.colors.uiMain)
                }

                uiState.error != null && uiState.items.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = uiState.error,
                            style = BookiiBookiiTheme.typography.regular14,
                            color = BookiiBookiiTheme.colors.grey500,
                        )
                        Text(
                            text = "다시 시도",
                            style = BookiiBookiiTheme.typography.medium14,
                            color = BookiiBookiiTheme.colors.uiMain,
                            modifier = Modifier.clickable(onClick = onRetry),
                        )
                    }
                }

                uiState.items.isEmpty() -> {
                    Text(
                        text = "아직 신청자가 없어요",
                        style = BookiiBookiiTheme.typography.regular15,
                        color = BookiiBookiiTheme.colors.grey500,
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        uiState.items.forEach { item ->
                            JoinRequestCard(
                                item = item,
                                onAccept = onAccept,
                                onReject = onReject,
                            )
                        }
                    }
                }
            }
        }
    }
}

// 헤더 — "참여 요청 관리 (N)" + 뒤로가기
@Composable
private fun GroupJoinRequestHeader(
    requestCount: Int,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
        ) {
            BookiiBackButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart),
                tint = BookiiBookiiTheme.colors.black,
            )
            Text(
                text = "참여 요청 관리 ($requestCount)",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        HorizontalDivider(thickness = 1.dp, color = BookiiBookiiTheme.colors.grey200)
    }
}

// 신청자 카드 한 장
@Composable
private fun JoinRequestCard(
    item: GroupAppItem,
    onAccept: (applyId: Long, applicantName: String) -> Unit,
    onReject: (applyId: Long, applicantName: String) -> Unit,
) {
    val applyId = item.applicationId
    val applicantName = item.name.orEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round16)
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        JoinRequestUserRow(
            nickname = applicantName,
            date = item.createdAt.orEmpty(),
            profileImageUrl = item.profileImageUrl,
        )
        JoinRequestMessage(message = item.applyMsg.orEmpty())
        JoinRequestBookRow(
            title = item.bookTitle.orEmpty(),
            author = item.bookAuthor.orEmpty(),
            bookImage = item.bookImage,
        )
        JoinRequestActions(
            onAccept = { applyId?.let { onAccept(it, applicantName) } },
            onReject = { applyId?.let { onReject(it, applicantName) } },
        )
    }
}

// 유저 헤더 (프로필 + 닉네임 + 날짜)
@Composable
private fun JoinRequestUserRow(
    nickname: String,
    date: String,
    profileImageUrl: String?,
) {
    val onProfileClick = LocalOnProfileClick.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfilePlaceholder(
            modifier = Modifier.size(48.dp),
            imageUrl = profileImageUrl,
            onClick = { onProfileClick(nickname) },
        )
        Column {
            Text(
                text = nickname,
                style = BookiiBookiiTheme.typography.medium15,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Text(
                text = date,
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey400,
            )
        }
    }
}

// 신청 메시지 박스
@Composable
private fun JoinRequestMessage(message: String) {
    Text(
        text = message,
        style = BookiiBookiiTheme.typography.regular15,
        color = BookiiBookiiTheme.colors.grey600,
        modifier = Modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round16)
            .background(BookiiBookiiTheme.colors.grey100)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    )
}

// 신청자가 교환하려는 책 정보 (표지 + 제목 + 저자)
// genre 필드는 응답 스키마에 없어 표시하지 않음
@Composable
private fun JoinRequestBookRow(
    title: String,
    author: String,
    bookImage: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BookCover(
            modifier = Modifier
                .width(48.dp)
                .height(60.dp),
            imageUrl = bookImage,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title.stripBookSubtitle(),
                style = BookiiBookiiTheme.typography.medium15,
                color = BookiiBookiiTheme.colors.grey800,
            )
            Text(
                text = author,
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey600,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

// 액션 버튼 (거절/수락) — PATCH 연결은 후속
@Composable
private fun JoinRequestActions(
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BottomSheetTwoBtnShort(
            text = "거절",
            style = BottomSheetBtnStyle.White,
            onClick = onReject,
            modifier = Modifier.weight(1f),
        )
        BottomSheetTwoBtnShort(
            text = "수락",
            style = BottomSheetBtnStyle.Orange,
            onClick = onAccept,
            modifier = Modifier.weight(1f),
        )
    }
}

// 수락 확인 다이얼로그 (PATCH 연결 시 사용 예정)
@Composable
private fun JoinRequestAcceptDialog(nickname: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "참여 요청 수락",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey100),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "닫기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = "$nickname 님의 그룹 참여 요청을 수락하시겠습니까? 수락 즉시 그룹이 시작됩니다.",
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier.padding(top = 24.dp),
        )
        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BottomSheetTwoBtnShort(
                text = "취소",
                style = BottomSheetBtnStyle.White,
                textStyle = BookiiBookiiTheme.typography.regular16,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            BottomSheetTwoBtnShort(
                text = "수락",
                style = BottomSheetBtnStyle.Orange,
                textStyle = BookiiBookiiTheme.typography.regular16,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// 거절 확인 다이얼로그 (PATCH 연결 시 사용 예정)
@Composable
private fun JoinRequestRejectDialog(nickname: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "참여 요청 거절",
                style = BookiiBookiiTheme.typography.bold24,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BookiiBookiiTheme.colors.grey100),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "닫기",
                    tint = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = "$nickname 님의 그룹 참여 요청을 거절하시겠습니까? 상대방에게 거절 알림이 발송됩니다.",
            style = BookiiBookiiTheme.typography.regular16,
            color = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier.padding(top = 24.dp),
        )
        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BottomSheetTwoBtnShort(
                text = "취소",
                style = BottomSheetBtnStyle.White,
                textStyle = BookiiBookiiTheme.typography.regular16,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            BottomSheetTwoBtnShort(
                text = "거절",
                style = BottomSheetBtnStyle.Red,
                textStyle = BookiiBookiiTheme.typography.regular16,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// 샘플 데이터 — 미리보기 전용
private val previewItems = listOf(
    GroupAppItem(
        applicationId = 1,
        user = 101,
        name = "kanghunsim",
        profileImageUrl = null,
        createdAt = "2025. 12. 05.",
        applyMsg = "저 너무 참여하고 싶은데 혹시 3일만 이따가 시작해도 될까요 ~~ㅠㅠ??",
        bookTitle = "사요가 바로 살인자",
        bookAuthor = "장우영",
        bookImage = null,
    ),
    GroupAppItem(
        applicationId = 2,
        user = 102,
        name = "minjiyoon",
        profileImageUrl = null,
        createdAt = "2025. 12. 06.",
        applyMsg = "이번 프로젝트에 함께할 수 있어서 정말 기대돼요!",
        bookTitle = "함께라면 무엇이든 가능해요",
        bookAuthor = "장우영",
        bookImage = null,
    ),
)

@Preview
@Composable
private fun GroupJoinRequestScreenPreview() {
    BookiiPreview {
        GroupJoinRequestScreen(
            uiState = ApplicationListUiState(
                items = previewItems,
                totalCount = previewItems.size,
            ),
            onBack = {},
            onRetry = {},
            onAccept = { _, _ -> },
            onReject = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun GroupJoinRequestScreenEmptyPreview() {
    BookiiPreview {
        GroupJoinRequestScreen(
            uiState = ApplicationListUiState(items = emptyList(), totalCount = 0),
            onBack = {},
            onRetry = {},
            onAccept = { _, _ -> },
            onReject = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun JoinRequestAcceptDialogPreview() {
    BookiiPreview {
        Column(
            modifier = Modifier
                .background(BookiiBookiiTheme.colors.uiBg)
                .padding(24.dp),
        ) {
            JoinRequestAcceptDialog(nickname = "kanghunsim")
        }
    }
}

@Preview
@Composable
private fun JoinRequestRejectDialogPreview() {
    BookiiPreview {
        Column(
            modifier = Modifier
                .background(BookiiBookiiTheme.colors.uiBg)
                .padding(24.dp),
        ) {
            JoinRequestRejectDialog(nickname = "아아아아")
        }
    }
}
