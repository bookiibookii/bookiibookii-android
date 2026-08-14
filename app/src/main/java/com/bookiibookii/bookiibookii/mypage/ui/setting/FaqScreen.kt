package com.bookiibookii.bookiibookii.mypage.ui.setting

import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.openReportChannel
import com.bookiibookii.bookiibookii.ui.component.showCustomToast
import com.bookiibookii.bookiibookii.data.model.mypage.FaqItem
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqRoute(
    onBackClick: () -> Unit,
    viewModel: com.bookiibookii.bookiibookii.mypage.vm.SettingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val faqItems by viewModel.faqItems.observeAsState(emptyList())

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.fetchFaq()
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is com.bookiibookii.bookiibookii.mypage.vm.SettingViewModel.Event.ShowToast ->
                    context.showCustomToast(event.message, event.isSuccess)
                is com.bookiibookii.bookiibookii.mypage.vm.SettingViewModel.Event.InquirySuccess ->
                    context.showCustomToast("문의가 접수되었습니다.", true)
                else -> Unit
            }
        }
    }

    FaqScreen(
        faqItems = faqItems,
        onBackClick = onBackClick,
        onPostInquiry = { title, content -> viewModel.postInquiry(title, content) },
        onInquiryClick = { context.openReportChannel() },
        onReportClick = { context.openReportChannel() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(
    faqItems: List<FaqItem> = emptyList(),
    onBackClick: () -> Unit = {},
    onPostInquiry: (title: String, content: String) -> Unit = { _, _ -> },
    onInquiryClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
) {
    var expandedIndex by remember { mutableStateOf<Int?>(0) }
    var showInquirySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
        FaqTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (faqItems.isEmpty()) {
                FaqEmptyCard()
            } else {
                faqItems.forEachIndexed { index, item ->
                    FaqItemCard(
                        item = item,
                        isExpanded = expandedIndex == index,
                        onToggle = { expandedIndex = if (expandedIndex == index) null else index },
                    )
                    if (index < faqItems.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.white)
                    .clickable { onInquiryClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text("1:1 문의", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.black)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BookiiBookiiTheme.colors.uiPointRed)
                    .clickable { onReportClick() },
                contentAlignment = Alignment.Center,
            ) {
                Text("신고", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.white)
            }
        }
    }

    if (showInquirySheet) {
        ModalBottomSheet(
            onDismissRequest = { showInquirySheet = false },
            sheetState = sheetState,
            containerColor = BookiiBookiiTheme.colors.white,
        ) {
            InquiryBottomSheetContent(
                onSubmit = { title, content ->
                    onPostInquiry(title, content)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showInquirySheet = false
                    }
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showInquirySheet = false
                    }
                },
            )
        }
    }
}

@Composable
private fun InquiryBottomSheetContent(
    onSubmit: (title: String, content: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val isEnabled = title.trim().isNotEmpty() && content.trim().isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "1:1 문의하기",
            style = BookiiBookiiTheme.typography.semibold20,
            color = BookiiBookiiTheme.colors.grey900,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "제목", style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.grey700)
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = BookiiBookiiTheme.typography.regular15.copy(color = BookiiBookiiTheme.colors.grey900),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BookiiBookiiTheme.colors.grey100)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = "제목을 입력해주세요",
                            style = BookiiBookiiTheme.typography.regular15,
                            color = BookiiBookiiTheme.colors.grey400,
                        )
                    }
                    innerTextField()
                },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "내용", style = BookiiBookiiTheme.typography.medium14, color = BookiiBookiiTheme.colors.grey700)
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                textStyle = BookiiBookiiTheme.typography.regular15.copy(color = BookiiBookiiTheme.colors.grey900),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BookiiBookiiTheme.colors.grey100)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.height(120.dp), contentAlignment = Alignment.TopStart) {
                        if (content.isEmpty()) {
                            Text(
                                text = "문의 내용을 입력해주세요",
                                style = BookiiBookiiTheme.typography.regular15,
                                color = BookiiBookiiTheme.colors.grey400,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isEnabled) BookiiBookiiTheme.colors.uiMain else BookiiBookiiTheme.colors.grey200)
                .clickable(enabled = isEnabled) { onSubmit(title.trim(), content.trim()) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "제출하기",
                style = BookiiBookiiTheme.typography.medium16,
                color = if (isEnabled) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey400,
            )
        }
    }
}

@Composable
private fun FaqTopBar(onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(BookiiBookiiTheme.colors.white)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookiiBackButton(onClick = onBackClick)
            Text(
                text = "자주 묻는 질문 / 신고하기",
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Box(modifier = Modifier.size(40.dp))
        }
        HorizontalDivider(color = BookiiBookiiTheme.colors.grey200)
    }
}

@Composable
private fun FaqEmptyCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BookiiBookiiTheme.colors.white)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "아직 등록된 질문이 없어요.",
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey900,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "문의사항 발생 시 문의 부탁드립니다.",
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey600,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FaqItemCard(item: FaqItem, isExpanded: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BookiiBookiiTheme.colors.white)
            .then(
                if (isExpanded) Modifier.border(1.dp, BookiiBookiiTheme.colors.uiMain, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable { onToggle() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Q.", style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.uiMain)
                Text(
                    text = item.question,
                    style = BookiiBookiiTheme.typography.semibold16,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = if (isExpanded) 90f else -90f },
            )
        }
        if (isExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("A.", style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.uiMain)
                Text(
                    text = item.answer,
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }
        }
    }
}

private val previewFaqItems = listOf(
    FaqItem(
        id = 1L,
        question = "책 상태가 좋지 않으면 교환이 거절될 수 있나요?",
        answer = "네, 책의 상태에 따라 교환이 제한될 수 있습니다. 낙서, 심한 훼손, 페이지 누락 등이 있는 경우 상대방이 교환을 거절할 권리가 있습니다.",
    ),
)

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun FaqScreenPreview() {
    BookiiPreview {
        FaqScreen(faqItems = previewFaqItems)
    }
}
