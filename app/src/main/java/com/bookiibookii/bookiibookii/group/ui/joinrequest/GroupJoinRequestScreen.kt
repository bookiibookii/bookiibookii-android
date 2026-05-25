package com.bookiibookii.bookiibookii.group.ui.joinrequest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.component.ProfilePlaceholder
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private const val SAMPLE_BOOK_TITLE = "자연과 함께하는 삶"

private data class JoinRequestItem(
    val nickname: String,
    val date: String,
    val message: String,
    val bookTitle: String,
    val bookAuthor: String,
    val bookGenre: String,
)

private val sampleJoinRequests = listOf(
    JoinRequestItem(
        nickname = "kanghunsim",
        date = "2025. 12. 05.",
        message = "저 너무 참여하고 싶은데 혹시 3일만 이따가 시작해도 될까요 ~~ㅠㅠ??",
        bookTitle = "사요가 바로 살인자",
        bookAuthor = "장우영",
        bookGenre = "소설",
    ),
    JoinRequestItem(
        nickname = "minjiyoon",
        date = "2025. 12. 06.",
        message = "이번 프로젝트에 함께할 수 있어서 정말 기대돼요!",
        bookTitle = "함께라면 무엇이든 가능해요",
        bookAuthor = "장우영",
        bookGenre = "에세이",
    ),
    JoinRequestItem(
        nickname = "jihunpark",
        date = "2025. 12. 07.",
        message = "일정 때문에 조금 늦을 것 같아요, 양해 부탁드립니다.",
        bookTitle = "데미안",
        bookAuthor = "헤르만 헤세",
        bookGenre = "소설",
    ),
)

@Composable
fun GroupJoinRequestScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        GroupJoinRequestHeader(
            requestCount = sampleJoinRequests.size,
            bookTitle = SAMPLE_BOOK_TITLE,
        )
        if (sampleJoinRequests.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                sampleJoinRequests.forEach { item ->
                    JoinRequestCard(item = item)
                }
            }
        }
    }
}

// 헤더
@Composable
private fun GroupJoinRequestHeader(
    requestCount: Int,
    bookTitle: String,
) {
    Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = BookiiBookiiTheme.colors.black,
                )
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "참여 요청 관리 ($requestCount)",
                    style = BookiiBookiiTheme.typography.medium20,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = bookTitle,
                    style = BookiiBookiiTheme.typography.regular12,
                    color = BookiiBookiiTheme.colors.grey500,
                )
            }
        }
    }
}

// 참여 요청 카드 한 장
@Composable
private fun JoinRequestCard(item: JoinRequestItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round16)
            .background(BookiiBookiiTheme.colors.white)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        JoinRequestUserRow(nickname = item.nickname, date = item.date)
        JoinRequestMessage(message = item.message)
        JoinRequestBookRow(
            title = item.bookTitle,
            author = item.bookAuthor,
            genre = item.bookGenre,
        )
        JoinRequestActions()
    }
}

// 유저 헤더 (프로필 + 닉네임 + 날짜)
@Composable
private fun JoinRequestUserRow(nickname: String, date: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 프로필 이미지 — JoinRequestItem에 profileImageUrl 추가 시 imageUrl 전달 (VM 연결 후속 작업)
        ProfilePlaceholder(modifier = Modifier.size(48.dp))
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

// 도서 정보 (표지 + 제목 + 저자(장르))
@Composable
private fun JoinRequestBookRow(title: String, author: String, genre: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 60.dp)
                .clip(BookiiBookiiTheme.shape.round8)
                .background(BookiiBookiiTheme.colors.grey200),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = BookiiBookiiTheme.typography.medium15,
                color = BookiiBookiiTheme.colors.grey800,
            )
            Text(
                text = "$author($genre)",
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.grey600,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// 액션 버튼 (거절 + 수락)
@Composable
private fun JoinRequestActions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BottomSheetTwoBtnShort(
            text = "거절",
            style = BottomSheetBtnStyle.White,
            onClick = {},
            modifier = Modifier.weight(1f),
        )
        BottomSheetTwoBtnShort(
            text = "수락",
            style = BottomSheetBtnStyle.Orange,
            onClick = {},
            modifier = Modifier.weight(1f),
        )
    }
}

// 수락 확인 다이얼로그
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

// 수락 확인 다이얼로그
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


@Preview
@Composable
private fun GroupJoinRequestScreenPreview() {
    BookiiPreview {
        GroupJoinRequestScreen()
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

@Preview
@Composable
private fun GroupJoinRequestScreenEmptyPreview() {
    BookiiPreview {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BookiiBookiiTheme.colors.uiBg),
        ) {
            GroupJoinRequestHeader(
                requestCount = 0,
                bookTitle = SAMPLE_BOOK_TITLE,
            )
        }
    }
}
