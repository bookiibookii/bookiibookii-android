package com.bookiibookii.bookiibookii.tracker.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.bookiibookii.bookiibookii.common.openExternalUrl
import com.bookiibookii.bookiibookii.common.openReportChannel
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingPlace
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.deliveryTrackingUrl
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.matchUserDeliveryId
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.toDeliveryAddressOption
import com.bookiibookii.bookiibookii.tracker.model.ReadingCardTarget
import com.bookiibookii.bookiibookii.tracker.model.TrackerAction
import com.bookiibookii.bookiibookii.tracker.model.TrackerCardModel
import com.bookiibookii.bookiibookii.tracker.model.TrackerMainUiState
import com.bookiibookii.bookiibookii.tracker.model.toDisplay
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerProgressRecordDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.TrackerDeliveryAddressEditDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.TrackerDeliveryInfoDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.TrackerDeliveryReceiveConfirmDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.TrackerDeliveryShippingConfirmDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.TrackerDeliveryTrackingNumberDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.direct.TrackerDirectExchangeConfirmDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.direct.TrackerDirectExchangeFailDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.direct.TrackerDirectMeetingConfirmDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.direct.TrackerDirectMeetingInfoDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.direct.TrackerDirectMeetingPlaceDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.direct.TrackerDirectMeetingTimeDialog
import com.bookiibookii.bookiibookii.tracker.model.TrackerNotificationItem
import com.bookiibookii.bookiibookii.tracker.model.TrackerProfileItem
import com.bookiibookii.bookiibookii.tracker.vm.TrackerMainViewModel
import com.bookiibookii.bookiibookii.ui.component.BookiiTopBar
import com.bookiibookii.bookiibookii.ui.component.CardButton
import com.bookiibookii.bookiibookii.ui.component.CardButtonStyle
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.delay

@Composable
private fun TrackerNoticeBanner(
    nickname: String,
    modifier: Modifier = Modifier,
) {
    val noticeText = buildAnnotatedString {
        withStyle(SpanStyle(color = BookiiBookiiTheme.colors.uiMain)) {
            append(nickname)
        }
        withStyle(SpanStyle(color = BookiiBookiiTheme.colors.grey900)) {
            append("님의\n교환독서 현황을 알려드려요.")
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white),
    ) {
        Text(
            text = noticeText,
            style = BookiiBookiiTheme.typography.regular24,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = BookiiBookiiTheme.colors.grey100,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerNoticeBannerPreview() {
    BookiiPreview {
        TrackerNoticeBanner(nickname = "sayo")
    }
}

@Composable
private fun TrackerNotificationCard(
    notifications: List<TrackerNotificationItem>,
    onItemClick: (groupId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (notifications.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { notifications.size })
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BookiiBookiiTheme.colors.white),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val item = notifications[page]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item.groupId) }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.dDay,
                    style = BookiiBookiiTheme.typography.semibold18,
                    color = BookiiBookiiTheme.colors.uiMainSub,
                )
                Text(
                    text = rememberBannerBody(item),
                    style = BookiiBookiiTheme.typography.regular18,
                )
                Text(
                    text = item.subText,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey400,
                )
            }
        }
        if (notifications.size > 1) {
            CarouselIndicator(
                total = notifications.size,
                current = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 24.dp),
            )
        }
    }
}

// 배너 placeholder 토큰. {remainingTime}이 있으면 1초마다 카운트다운한다.
private val BANNER_TOKEN_REGEX = Regex("""\{(nickname|bookTitle|remainingTime)\}""")

private const val SECONDS_PER_DAY = 86_400L

// 남은 시간 표기: 24시간 이상이면 "N일"(올림), 24시간 미만이면 HH:MM:SS 카운트다운
private fun formatRemainingTime(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    if (s >= SECONDS_PER_DAY) {
        val days = (s + SECONDS_PER_DAY - 1) / SECONDS_PER_DAY
        return "${days}일"
    }
    return "%02d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
}
    
// 조사 첫 글자 → (받침 있을 때 form, 받침 없을 때 form)
private val JOSA_FORMS = mapOf(
    '을' to ('을' to '를'), '를' to ('을' to '를'),
    '은' to ('은' to '는'), '는' to ('은' to '는'),
    '이' to ('이' to '가'), '가' to ('이' to '가'),
    '과' to ('과' to '와'), '와' to ('과' to '와'),
)

// 앞말(prevValue)의 마지막 글자 받침 유무에 따라 text 첫 글자의 조사를 보정
private fun correctJosa(text: String, prevValue: String?): String {
    if (prevValue.isNullOrEmpty() || text.isEmpty()) return text
    val forms = JOSA_FORMS[text[0]] ?: return text
    val code = prevValue.last().code
    val hasBatchim = code in 0xAC00..0xD7A3 && (code - 0xAC00) % 28 != 0
    val correct = if (hasBatchim) forms.first else forms.second
    return correct + text.substring(1)
}

// titleTemplate의 {bookTitle}/{nickname}/{remainingTime}를 실제 값으로 치환한
// API 재조회 없이 remainingSeconds를 매초 깎아 화면에서만 갱신
@Composable
private fun rememberBannerBody(item: TrackerNotificationItem): AnnotatedString {
    val hasTimer = remember(item.template) { item.template.contains("{remainingTime}") }
    var seconds by remember(item.groupId, item.remainingSeconds) {
        mutableStateOf(item.remainingSeconds)
    }
    LaunchedEffect(item.groupId, hasTimer) {
        if (!hasTimer) return@LaunchedEffect
        while (seconds > 0) {
            delay(1000)
            seconds -= 1
        }
    }
    val emphasis = BookiiBookiiTheme.colors.grey900
    val normal = BookiiBookiiTheme.colors.grey700
    return remember(item.template, item.nickname, item.bookTitle, seconds, emphasis, normal) {
        buildAnnotatedString {
            var last = 0
            var prevValue: String? = null  // 직전 토큰 값(조사 보정용)
            for (match in BANNER_TOKEN_REGEX.findAll(item.template)) {
                if (match.range.first > last) {
                    val literal = correctJosa(
                        item.template.substring(last, match.range.first),
                        prevValue,
                    )
                    withStyle(SpanStyle(color = normal)) { append(literal) }
                    prevValue = null  // 조사 보정은 토큰 바로 뒤 리터럴에만
                }
                val value = when (match.groupValues[1]) {
                    "nickname" -> item.nickname
                    "bookTitle" -> item.bookTitle
                    else -> formatRemainingTime(seconds)
                }
                withStyle(SpanStyle(color = emphasis)) { append(value) }
                prevValue = value
                last = match.range.last + 1
            }
            if (last < item.template.length) {
                val literal = correctJosa(item.template.substring(last), prevValue)
                withStyle(SpanStyle(color = normal)) { append(literal) }
            }
        }
    }
}

@Composable
private fun CarouselIndicator(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == current) {
                            BookiiBookiiTheme.colors.grey500
                        } else {
                            BookiiBookiiTheme.colors.grey200
                        },
                    ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerNotificationCardPreview() {
    BookiiPreview {
        TrackerNotificationCard(
            notifications = listOf(
                TrackerNotificationItem(
                    groupId = 1L,
                    dDay = "D-1",
                    template = "{nickname}님께 {bookTitle}을 발송해주세요",
                    nickname = "noshel",
                    bookTitle = "살인자의 기억법",
                    remainingSeconds = 0L,
                    subText = "책이 파손되지 않도록 꼼꼼히 포장해주세요",
                ),
                TrackerNotificationItem(
                    groupId = 2L,
                    dDay = "D-5",
                    template = "{nickname} 님과의 책 교환까지 {remainingTime} 남았어요",
                    nickname = "noshel",
                    bookTitle = "작별인사",
                    remainingSeconds = 3661L,
                    subText = "오늘 읽은 페이지를 기록해주세요",
                ),
                TrackerNotificationItem(
                    groupId = 3L,
                    dDay = "D-3",
                    template = "{bookTitle}을 읽고 후기를 남겨주세요",
                    nickname = "noshel",
                    bookTitle = "데미안",
                    remainingSeconds = 0L,
                    subText = "샘플 서브 텍스트",
                ),
            ),
            onItemClick = {},
        )
    }
}

@Composable
private fun TrackerCountBoard(
    total: Int,
    reading: Int,
    exchanging: Int,
    review: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round20)
            .background(BookiiBookiiTheme.colors.white)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CountColumn(label = "전체", count = total, modifier = Modifier.weight(1f))
        CountDivider()
        CountColumn(label = "읽는 중", count = reading, modifier = Modifier.weight(1f))
        CountDivider()
        CountColumn(label = "교환 중", count = exchanging, modifier = Modifier.weight(1f))
        CountDivider()
        CountColumn(label = "후기", count = review, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CountColumn(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey700,
        )
        Text(
            text = count.toString(),
            style = BookiiBookiiTheme.typography.regular24,
            color = if (count == 0) {
                BookiiBookiiTheme.colors.grey300
            } else {
                BookiiBookiiTheme.colors.grey900
            },
        )
    }
}

@Composable
private fun CountDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(48.dp)
            .background(BookiiBookiiTheme.colors.grey100),
    )
}

@Preview(showBackground = true)
@Composable
private fun TrackerCountBoardEmptyPreview() {
    BookiiPreview {
        TrackerCountBoard(
            total = 0,
            reading = 0,
            exchanging = 0,
            review = 0,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerCountBoardFilledPreview() {
    BookiiPreview {
        TrackerCountBoard(
            total = 3,
            reading = 2,
            exchanging = 1,
            review = 0,
        )
    }
}

@Composable
private fun TrackerEmptyCard(
    onCreateGroupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BookiiBookiiTheme.shape.round24)
            .background(BookiiBookiiTheme.colors.white)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "참여 중인 그룹이 없어요 😭\n읽고 싶은 책으로 그룹을 만들어보세요",
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey900,
            textAlign = TextAlign.Center,
        )
        CardButton(
            text = "그룹 만들기",
            style = CardButtonStyle.Main,
            onClick = onCreateGroupClick,
            modifier = Modifier.fillMaxWidth(),
            shape = BookiiBookiiTheme.shape.round20,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackerEmptyCardPreview() {
    BookiiPreview {
        TrackerEmptyCard(onCreateGroupClick = {})
    }
}

// stateful: VM 주입 + state 수집 + 카드 버튼 액션 분기
@Composable
fun TrackerMainRoute(
    onProfileClick: () -> Unit,
    onAlertClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onCardClick: (groupId: Long) -> Unit,
    onNavigateBookReview: (groupId: Long, edit: Boolean) -> Unit,
    onNavigatePartnerReview: (groupId: Long) -> Unit,
    onNavigateComment: (groupId: Long, title: String) -> Unit = { _, _ -> },
    onNavigatePlaceSearch: () -> Unit = {},
    onNavigateLibraryDetail: (ReadingCardTarget) -> Unit = {},
    selectedPlace: PlaceSearchResult? = null,
    onPlaceConsumed: () -> Unit = {},
    viewModel: TrackerMainViewModel = viewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    // 최초 진입은 VM init에서 이미 로드하므로 첫 ON_RESUME은 건너뛰고,
    // 상세 화면 등에서 복귀할 때만 목록을 재조회한다. (rememberSaveable로 백스택 복귀 시에도 유지)
    var isFirstResume by rememberSaveable { mutableStateOf(true) }
    // 장소 검색 화면으로 이동 중이면 2/3 다이얼로그를 즉시 숨김(복귀 시 ON_RESUME에서 해제)
    var placeSearchPending by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        placeSearchPending = false
        if (isFirstResume) {
            isFirstResume = false
        } else {
            viewModel.load()
            viewModel.fetchNotificationDot()
        }
    }
    var progressDialogGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    var trackingDialogGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deliveryInfoDialogGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deliveryEditDialogGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    // 운송장 정보 확인 / 책 수령 확인 다이얼로그
    var shippingConfirmGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    var receiveConfirmGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    // 약속 잡기(1=일시, 2=장소, 3=확인)
    var meetingDialogGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    var meetingStep by rememberSaveable { mutableStateOf(1) }
    // 약속 수정 모드 — true면 3/3 확인에서 등록(POST) 대신 수정(PATCH) 호출
    var meetingEditMode by rememberSaveable { mutableStateOf(false) }
    // 1/3에서 고른 약속 일시 (raw ISO, 예: 2026-05-20T14:30:00)
    var meetingScheduledAt by rememberSaveable { mutableStateOf("") }
    // 2/3에서 입력한 상세주소 (사용자 직접 입력, 빈칸 시작)
    var meetingAddressDetail by rememberSaveable { mutableStateOf("") }
    // 약속 확인(조회) 다이얼로그
    var meetingInfoDialogGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    // 교환 확인 다이얼로그
    var exchangeConfirmGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    // 교환 실패 안내 다이얼로그
    var exchangeFailGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    val deliveryAddress by viewModel.deliveryAddress.collectAsStateWithLifecycle()
    val savedDeliveries by viewModel.savedDeliveries.collectAsStateWithLifecycle()
    val partnerDelivery by viewModel.partnerDelivery.collectAsStateWithLifecycle()
    val meetingPlace by viewModel.meetingPlace.collectAsStateWithLifecycle()
    val meetingInfo by viewModel.meetingInfo.collectAsStateWithLifecycle()

    // 장소 검색 화면에서 선택한 결과를 약속 장소로 반영 (복귀 시 step 2 다이얼로그 유지됨)
    LaunchedEffect(selectedPlace) {
        val place = selectedPlace ?: return@LaunchedEffect
        viewModel.setMeetingPlace(place)
        onPlaceConsumed()
    }
    TrackerMainScreen(
        uiState = uiState,
        nickname = uiState.nickname,
        notifications = uiState.notifications,
        onProfileClick = onProfileClick,
        onAlertClick = onAlertClick,
        onCreateGroupClick = onCreateGroupClick,
        onCardClick = onCardClick,
        onPrimaryAction = { groupId ->
            val action = uiState.cards.firstOrNull { it.groupId == groupId }?.primaryAction
            dispatchAction(
                action = action,
                onRecordProgress = { progressDialogGroupId = groupId },
                onWriteBookReview = { onNavigateBookReview(groupId, false) },
                onEditBookReview = { onNavigateBookReview(groupId, true) },
                onRegisterTrackingNumber = { trackingDialogGroupId = groupId },
                onCheckDeliveryInfo = {
                    viewModel.loadDeliveryAddress(groupId) {
                        deliveryInfoDialogGroupId = groupId
                    }
                },
                onRegisterMeeting = {
                    meetingStep = 1
                    meetingDialogGroupId = groupId
                },
                onGoToComments = {
                    val title = uiState.cards.firstOrNull { it.groupId == groupId }?.groupName.orEmpty()
                    onNavigateComment(groupId, title)
                },
                onCheckMeeting = {
                    viewModel.loadMeeting(groupId) { meetingInfoDialogGroupId = groupId }
                },
                onConfirmExchange = { exchangeConfirmGroupId = groupId },
                onWritePartnerReview = { onNavigatePartnerReview(groupId) },
                onCheckShippingInfo = {
                    viewModel.loadPartnerDelivery(groupId) { shippingConfirmGroupId = groupId }
                },
                onConfirmReceive = { receiveConfirmGroupId = groupId },
                onWriteReadingCard = {
                    val bookTitle = uiState.cards.firstOrNull { it.groupId == groupId }?.bookTitle.orEmpty()
                    viewModel.openReadingCard(groupId, bookTitle) { onNavigateLibraryDetail(it) }
                },
            )
        },
        onSecondaryAction = { groupId ->
            val action = uiState.cards.firstOrNull { it.groupId == groupId }?.secondaryAction
            dispatchAction(
                action = action,
                onRecordProgress = { progressDialogGroupId = groupId },
                onWriteBookReview = { onNavigateBookReview(groupId, false) },
                onEditBookReview = { onNavigateBookReview(groupId, true) },
                onRegisterTrackingNumber = { trackingDialogGroupId = groupId },
                onCheckDeliveryInfo = {
                    viewModel.loadDeliveryAddress(groupId) {
                        deliveryInfoDialogGroupId = groupId
                    }
                },
                onRegisterMeeting = {
                    meetingStep = 1
                    meetingDialogGroupId = groupId
                },
                onGoToComments = {
                    val title = uiState.cards.firstOrNull { it.groupId == groupId }?.groupName.orEmpty()
                    onNavigateComment(groupId, title)
                },
                onCheckMeeting = {
                    viewModel.loadMeeting(groupId) { meetingInfoDialogGroupId = groupId }
                },
                onConfirmExchange = { exchangeConfirmGroupId = groupId },
                onWritePartnerReview = { onNavigatePartnerReview(groupId) },
                onCheckShippingInfo = {
                    viewModel.loadPartnerDelivery(groupId) { shippingConfirmGroupId = groupId }
                },
                onConfirmReceive = { receiveConfirmGroupId = groupId },
                onWriteReadingCard = {
                    val bookTitle = uiState.cards.firstOrNull { it.groupId == groupId }?.bookTitle.orEmpty()
                    viewModel.openReadingCard(groupId, bookTitle) { onNavigateLibraryDetail(it) }
                },
            )
        },
    )
    val openedGroupId = progressDialogGroupId
    if (openedGroupId != null) {
        val card = uiState.cards.firstOrNull { it.groupId == openedGroupId }
        TrackerProgressRecordDialog(
            totalPages = card?.left?.totalPages ?: 0,
            onDismiss = { progressDialogGroupId = null },
            onConfirm = { currentPage -> viewModel.recordProgress(openedGroupId, currentPage) },
        )
    }
    val trackingGroupId = trackingDialogGroupId
    if (trackingGroupId != null) {
        TrackerDeliveryTrackingNumberDialog(
            onDismiss = { trackingDialogGroupId = null },
            onConfirm = { company, number ->
                viewModel.registerDelivery(trackingGroupId, company, number)
            },
        )
    }
    val infoGroupId = deliveryInfoDialogGroupId
    val addressData = deliveryAddress
    if (infoGroupId != null && addressData != null) {
        val card = uiState.cards.firstOrNull { it.groupId == infoGroupId }
        TrackerDeliveryInfoDialog(
            partnerNickname = card?.right?.nickname.orEmpty(),
            myAddress = addressData.myAddress.toDisplay(),
            partnerAddress = addressData.partnerAddress.toDisplay(),
            canEditMyAddress = addressData.canEditMyAddress == true,
            onDismiss = {
                deliveryInfoDialogGroupId = null
                viewModel.clearDeliveryAddress()
            },
            onEditClick = {
                deliveryInfoDialogGroupId = null
                viewModel.loadSavedDeliveries { deliveryEditDialogGroupId = infoGroupId }
            },
            onConfirmClick = {
                deliveryInfoDialogGroupId = null
                viewModel.clearDeliveryAddress()
            },
        )
    }
    val editGroupId = deliveryEditDialogGroupId
    if (editGroupId != null) {
        val myAddress = deliveryAddress?.myAddress
        TrackerDeliveryAddressEditDialog(
            savedAddresses = savedDeliveries.map { it.toDeliveryAddressOption() },
            initialSelectedUserDeliveryId = savedDeliveries.matchUserDeliveryId(myAddress),
            onDismiss = {
                deliveryEditDialogGroupId = null
                viewModel.clearDeliveryAddress()
            },
            onConfirmSaved = { userDeliveryId ->
                viewModel.changeDeliveryAddressSaved(editGroupId, userDeliveryId) {
                    deliveryEditDialogGroupId = null
                    viewModel.clearDeliveryAddress()
                }
            },
            onConfirmDirect = { zipCode, address, addressDetail ->
                viewModel.changeDeliveryAddressDirect(editGroupId, zipCode, address, addressDetail) {
                    deliveryEditDialogGroupId = null
                    viewModel.clearDeliveryAddress()
                }
            },
        )
    }
    // 운송장 정보 확인 (상대방 운송장)
    val shippingGroupId = shippingConfirmGroupId
    val partner = partnerDelivery
    if (shippingGroupId != null && partner != null) {
        TrackerDeliveryShippingConfirmDialog(
            companyName = partner.deliveryCompanyName.orEmpty(),
            trackingNumber = partner.trackingNumber.orEmpty(),
            onDismiss = {
                shippingConfirmGroupId = null
                viewModel.clearPartnerDelivery()
            },
            onTrackingSearchClick = {
                val url = deliveryTrackingUrl(partner.deliveryCompany, partner.trackingNumber)
                if (url != null) {
                    context.openExternalUrl(url)
                } else {
                    context.showCustomToast("배송 조회를 지원하지 않는 택배사예요.", false)
                }
            },
            onConfirmClick = {
                shippingConfirmGroupId = null
                viewModel.clearPartnerDelivery()
            },
        )
    }
    // 책 수령 확인 → PATCH 수령 확인
    val receiveGroupId = receiveConfirmGroupId
    if (receiveGroupId != null) {
        TrackerDeliveryReceiveConfirmDialog(
            onDismiss = { receiveConfirmGroupId = null },
            onConfirmClick = {
                viewModel.confirmReceive(receiveGroupId) { receiveConfirmGroupId = null }
            },
        )
    }
    // 약속 잡기 1/3 → 2/3 → 3/3
    if (meetingDialogGroupId != null) {
        when (meetingStep) {
            1 -> TrackerDirectMeetingTimeDialog(
                onDismiss = {
                    meetingDialogGroupId = null
                    meetingEditMode = false
                    meetingAddressDetail = ""
                    viewModel.clearMeetingPlace()
                },
                onNextClick = { scheduledAt ->
                    meetingScheduledAt = scheduledAt
                    meetingStep = 2
                },
                // 수정 모드면 기존 일시 프리필
                initialScheduledAt = if (meetingEditMode) meetingScheduledAt else null,
            )
            2 -> if (!placeSearchPending) TrackerDirectMeetingPlaceDialog(
                address = meetingPlace?.address.orEmpty(),
                addressDetail = meetingAddressDetail,
                onAddressDetailChange = { meetingAddressDetail = it },
                onSearchClick = {
                    placeSearchPending = true
                    onNavigatePlaceSearch()
                },
                onLoadMyPlaceClick = { viewModel.loadMyExchangePlace() },
                onDismiss = {
                    meetingDialogGroupId = null
                    meetingEditMode = false
                    meetingAddressDetail = ""
                    viewModel.clearMeetingPlace()
                },
                onPreviousClick = { meetingStep = 1 },
                onNextClick = { meetingStep = 3 },
            )
            3 -> TrackerDirectMeetingConfirmDialog(
                scheduledAt = meetingScheduledAt,
                address = meetingPlace?.address.orEmpty(),
                addressDetail = meetingAddressDetail,
                onDismiss = {
                    meetingDialogGroupId = null
                    meetingEditMode = false
                    meetingAddressDetail = ""
                    viewModel.clearMeetingPlace()
                },
                onConfirmClick = {
                    val gid = meetingDialogGroupId
                    val place = meetingPlace
                    if (gid != null && place != null) {
                        val onDone = {
                            meetingDialogGroupId = null
                            meetingEditMode = false
                            meetingAddressDetail = ""
                            viewModel.clearMeetingPlace()
                        }
                        // 수정 모드면 PATCH(editMeeting), 아니면 등록 POST(registerMeeting)
                        if (meetingEditMode) {
                            viewModel.editMeeting(
                                groupId = gid,
                                placeName = place.placeName,
                                address = place.address,
                                zipCode = place.zipCode,
                                x = place.x,
                                y = place.y,
                                addressDetail = meetingAddressDetail.ifBlank { null },
                                scheduledAt = meetingScheduledAt,
                                onSuccess = onDone,
                            )
                        } else {
                            viewModel.registerMeeting(
                                groupId = gid,
                                placeName = place.placeName,
                                address = place.address,
                                zipCode = place.zipCode,
                                x = place.x,
                                y = place.y,
                                addressDetail = meetingAddressDetail.ifBlank { null },
                                scheduledAt = meetingScheduledAt,
                                onSuccess = onDone,
                            )
                        }
                    }
                },
            )
        }
    }
    // 약속 확인(조회)
    val meeting = meetingInfo
    if (meetingInfoDialogGroupId != null && meeting != null) {
        TrackerDirectMeetingInfoDialog(
            scheduledAt = meeting.meetingAt.orEmpty(),
            address = meeting.location?.address.orEmpty(),
            addressDetail = meeting.addressDetail.orEmpty(),
            isHost = uiState.cards.firstOrNull { it.groupId == meetingInfoDialogGroupId }?.isHost == true,
            onDismiss = {
                meetingInfoDialogGroupId = null
                viewModel.clearMeeting()
            },
            onConfirmClick = {
                meetingInfoDialogGroupId = null
                viewModel.clearMeeting()
            },
            // 수정: 등록과 동일한 3스텝 흐름 재사용, 기존 장소/일시/상세주소 프리필 후
            // 마지막 확인에서 editMeeting(PATCH) 호출
            onEditClick = {
                val gid = meetingInfoDialogGroupId
                val loc = meeting.location
                if (loc?.x != null && loc.y != null) {
                    viewModel.setMeetingPlace(
                        MeetingPlace(
                            placeName = loc.placeName.orEmpty(),
                            address = loc.address.orEmpty(),
                            zipCode = loc.zipCode,
                            x = loc.x,
                            y = loc.y,
                        )
                    )
                }
                meetingScheduledAt = meeting.meetingAt.orEmpty()
                meetingAddressDetail = meeting.addressDetail.orEmpty()
                meetingInfoDialogGroupId = null
                viewModel.clearMeeting()
                meetingEditMode = true
                meetingDialogGroupId = gid
                meetingStep = 1
            },
        )
    }
    // 교환 확인 → "교환했어요"면 완료 PATCH, "못했어요"면 실패 안내
    val exchangeGid = exchangeConfirmGroupId
    if (exchangeGid != null) {
        TrackerDirectExchangeConfirmDialog(
            onDismiss = { exchangeConfirmGroupId = null },
            onNotYetClick = {
                exchangeConfirmGroupId = null
                exchangeFailGroupId = exchangeGid
            },
            onConfirmClick = {
                viewModel.completeMeeting(exchangeGid) { exchangeConfirmGroupId = null }
            },
        )
    }
    // 교환 실패 안내
    val exchangeFailGid = exchangeFailGroupId
    if (exchangeFailGid != null) {
        TrackerDirectExchangeFailDialog(
            onDismiss = { exchangeFailGroupId = null },
            onReportClick = { context.openReportChannel() },
            onGoToCommentsClick = {
                val title = uiState.cards
                    .firstOrNull { it.groupId == exchangeFailGid }?.groupName.orEmpty()
                exchangeFailGroupId = null
                onNavigateComment(exchangeFailGid, title)
            },
        )
    }
}

private inline fun dispatchAction(
    action: TrackerAction?,
    onRecordProgress: () -> Unit,
    onWriteBookReview: () -> Unit,
    onEditBookReview: () -> Unit,
    onRegisterTrackingNumber: () -> Unit,
    onCheckDeliveryInfo: () -> Unit,
    onRegisterMeeting: () -> Unit,
    onGoToComments: () -> Unit,
    onCheckMeeting: () -> Unit,
    onConfirmExchange: () -> Unit,
    onWritePartnerReview: () -> Unit,
    onCheckShippingInfo: () -> Unit,
    onConfirmReceive: () -> Unit,
    onWriteReadingCard: () -> Unit,
) {
    when (action) {
        TrackerAction.RecordProgress -> onRecordProgress()
        TrackerAction.WriteBookReview -> onWriteBookReview()
        TrackerAction.EditBookReview -> onEditBookReview()
        TrackerAction.RegisterTrackingNumber -> onRegisterTrackingNumber()
        TrackerAction.CheckDeliveryInfo -> onCheckDeliveryInfo()
        TrackerAction.RegisterMeeting -> onRegisterMeeting()
        TrackerAction.GoToComments -> onGoToComments()
        TrackerAction.CheckMeeting -> onCheckMeeting()
        TrackerAction.ConfirmExchange -> onConfirmExchange()
        TrackerAction.WritePartnerReview -> onWritePartnerReview()
        TrackerAction.CheckShippingInfo -> onCheckShippingInfo()
        TrackerAction.ConfirmReceive -> onConfirmReceive()
        TrackerAction.WriteReadingCard -> onWriteReadingCard()
        // 교환 완료는 비활성 버튼이라 디스패치되지 않음
        TrackerAction.CompleteExchange, TrackerAction.None, null -> Unit
    }
}

@Composable
fun TrackerMainScreen(
    uiState: TrackerMainUiState,
    nickname: String,
    notifications: List<TrackerNotificationItem>,
    onProfileClick: () -> Unit,
    onAlertClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onCardClick: (groupId: Long) -> Unit,
    onPrimaryAction: (groupId: Long) -> Unit,
    onSecondaryAction: (groupId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg),
    ) {
        // 상단 고정 헤더 (스크롤 영역 밖)
        BookiiTopBar(
            title = "트래커",
            onProfileClick = onProfileClick,
            onNotificationClick = onAlertClick,
            hasNewNotification = uiState.hasNewNotification,
        )
        // 헤더 아래만 스크롤 (weight(1f)로 남은 공간 채움)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BookiiBookiiTheme.colors.white),
            ) {
                TrackerNoticeBanner(nickname = nickname)
                if (uiState.cards.isNotEmpty()) {
                    TrackerNotificationCard(
                        notifications = notifications,
                        onItemClick = onCardClick,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TrackerCountBoard(
                    total = uiState.totalCount,
                    reading = uiState.readingCount,
                    exchanging = uiState.exchangingCount,
                    review = uiState.reviewCount,
                )
                if (uiState.hasLoadedOnce && uiState.cards.isEmpty()) {
                    TrackerEmptyCard(onCreateGroupClick = onCreateGroupClick)
                } else if (uiState.cards.isNotEmpty()) {
                    uiState.cards.forEach { card ->
                        TrackerMainCard(
                            card = card,
                            onCardClick = { onCardClick(card.groupId) },
                            onPrimaryAction = { onPrimaryAction(card.groupId) },
                            onSecondaryAction = { onSecondaryAction(card.groupId) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(192.dp))
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun TrackerMainScreenEmptyPreview() {
    BookiiPreview {
        TrackerMainScreen(
            uiState = TrackerMainUiState(),
            nickname = "sayo",
            notifications = emptyList(),
            onProfileClick = {},
            onAlertClick = {},
            onCreateGroupClick = {},
            onCardClick = {},
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun TrackerMainScreenWithGroupsPreview() {
    BookiiPreview {
        val notifications = listOf(
            TrackerNotificationItem(
                groupId = 1L,
                dDay = "D-1",
                template = "{nickname}님께 {bookTitle}을 발송해주세요",
                nickname = "noshel",
                bookTitle = "살인자의 기억법",
                remainingSeconds = 0L,
                subText = "책이 파손되지 않도록 꼼꼼히 포장해주세요",
            ),
            TrackerNotificationItem(
                groupId = 2L,
                dDay = "D-5",
                template = "{bookTitle}의 진행률을 기록해보세요",
                nickname = "noshel",
                bookTitle = "작별인사",
                remainingSeconds = 0L,
                subText = "오늘 읽은 페이지를 기록해주세요",
            ),
            TrackerNotificationItem(
                groupId = 3L,
                dDay = "D-3",
                template = "{bookTitle}의 후기를 작성해주세요",
                nickname = "noshel",
                bookTitle = "데미안",
                remainingSeconds = 0L,
                subText = "이번 주말까지 작성을 권장드려요",
            ),
        )
        val groups = listOf(
            TrackerCardModel(
                groupId = 1L,
                groupName = "김영하 도장깨기 하실 분",
                displayBookTitle = "살인자의 기억법",
                bookTitle = "살인자의 기억법",
                progressLabel = "읽는 중",
                dDay = "D-5",
                left = TrackerProfileItem(
                    nickname = "나",
                    bookTitle = "살인자의 기억법",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 48,
                    isOwnerBook = true,
                ),
                right = TrackerProfileItem(
                    nickname = "noshel",
                    bookTitle = "작별인사",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 48,
                    isOwnerBook = false,
                ),
                primaryAction = TrackerAction.RecordProgress,
                secondaryAction = TrackerAction.WriteReadingCard,
            ),
            TrackerCardModel(
                groupId = 2L,
                groupName = "김영하 도장깨기 하실 분",
                displayBookTitle = "살인자의 기억법",
                bookTitle = "살인자의 기억법",
                progressLabel = "후기 작성",
                dDay = "D-3",
                left = TrackerProfileItem(
                    nickname = "나",
                    bookTitle = "살인자의 기억법",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 100,
                    isOwnerBook = true,
                ),
                right = TrackerProfileItem(
                    nickname = "noshel",
                    bookTitle = "작별인사",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 100,
                    isOwnerBook = false,
                ),
                primaryAction = TrackerAction.RecordProgress,
                secondaryAction = TrackerAction.WriteReadingCard,
            ),
            TrackerCardModel(
                groupId = 3L,
                groupName = "독서 모임 셋째",
                displayBookTitle = "데미안",
                bookTitle = "데미안",
                progressLabel = "교환 중",
                dDay = "D-7",
                left = TrackerProfileItem(
                    nickname = "나",
                    bookTitle = "데미안",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 20,
                    isOwnerBook = true,
                ),
                right = TrackerProfileItem(
                    nickname = "partner3",
                    bookTitle = "1984",
                    bookCoverUrl = null,
                    profileImageUrl = null,
                    progressPercent = 35,
                    isOwnerBook = false,
                ),
                primaryAction = TrackerAction.RecordProgress,
                secondaryAction = TrackerAction.WriteReadingCard,
            ),
        )
        TrackerMainScreen(
            uiState = TrackerMainUiState(
                cards = groups,
                totalCount = 3,
                readingCount = 2,
                exchangingCount = 1,
                reviewCount = 0,
            ),
            nickname = "sayo",
            notifications = notifications,
            onProfileClick = {},
            onAlertClick = {},
            onCreateGroupClick = {},
            onCardClick = {},
            onPrimaryAction = {},
            onSecondaryAction = {},
        )
    }
}
