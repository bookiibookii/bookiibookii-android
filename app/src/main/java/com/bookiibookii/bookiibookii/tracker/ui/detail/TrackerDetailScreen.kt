package com.bookiibookii.bookiibookii.tracker.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.openExternalUrl
import com.bookiibookii.bookiibookii.common.openReportChannel
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.deliveryTrackingUrl
import com.bookiibookii.bookiibookii.data.model.location.PlaceSearchResult
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingPlace
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.matchUserDeliveryId
import com.bookiibookii.bookiibookii.tracker.ui.detail.delivery.toDeliveryAddressOption
import com.bookiibookii.bookiibookii.tracker.model.ReadingCardTarget
import com.bookiibookii.bookiibookii.tracker.model.TrackerAction
import com.bookiibookii.bookiibookii.tracker.model.TrackerProfileItem
import com.bookiibookii.bookiibookii.tracker.model.TrackerStepLabelStyle
import com.bookiibookii.bookiibookii.tracker.model.toDisplay
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerDetailContent
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerProgressRecordDialog
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerReadingPeriodEditDialog
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
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStep
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStepStatus
import com.bookiibookii.bookiibookii.tracker.vm.TrackerDetailViewModel
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import java.time.LocalDate

@Composable
fun TrackerDetailRoute(
    groupId: Long,
    onBackClick: () -> Unit,
    onNavigateBookReview: (edit: Boolean) -> Unit,
    onNavigatePartnerReview: () -> Unit,
    onNavigateComment: (title: String) -> Unit = {},
    onNavigatePlaceSearch: () -> Unit = {},
    onNavigateLibraryDetail: (ReadingCardTarget) -> Unit = {},
    onNavigateLibrary: () -> Unit = {},
    selectedPlace: PlaceSearchResult? = null,
    onPlaceConsumed: () -> Unit = {},
    viewModel: TrackerDetailViewModel = viewModel(
        factory = TrackerDetailViewModel.factory(groupId)
    ),
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val deliveryAddress by viewModel.deliveryAddress.collectAsStateWithLifecycle()
    val savedDeliveries by viewModel.savedDeliveries.collectAsStateWithLifecycle()
    val partnerDelivery by viewModel.partnerDelivery.collectAsStateWithLifecycle()
    val meetingPlace by viewModel.meetingPlace.collectAsStateWithLifecycle()
    val meetingInfo by viewModel.meetingInfo.collectAsStateWithLifecycle()

    // 최초 진입은 VM init에서 이미 로드하므로 첫 ON_RESUME은 건너뛰고,
    // 하위 화면(서재/리뷰 등)에서 복귀할 때만 상세를 재조회한다. (rememberSaveable로 백스택 복귀 시에도 유지)
    var isFirstResume by rememberSaveable { mutableStateOf(true) }
    // 장소 검색 화면으로 이동 중이면 2/3 다이얼로그를 즉시 숨김(복귀 시 ON_RESUME에서 해제)
    var placeSearchPending by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        placeSearchPending = false
        if (isFirstResume) {
            isFirstResume = false
        } else {
            viewModel.load()
        }
    }

    // 장소 검색 화면에서 선택한 결과를 약속 장소로 반영 (복귀 시 step 2 다이얼로그 유지됨)
    LaunchedEffect(selectedPlace) {
        val place = selectedPlace ?: return@LaunchedEffect
        viewModel.setMeetingPlace(place)
        onPlaceConsumed()
    }
    var showProgressDialog by rememberSaveable { mutableStateOf(false) }
    // 독서 기간 수정 다이얼로그(더보기 > 독서 기간 수정)
    var showReadingPeriodDialog by rememberSaveable { mutableStateOf(false) }
    var showTrackingDialog by rememberSaveable { mutableStateOf(false) }
    var showDeliveryInfoDialog by rememberSaveable { mutableStateOf(false) }
    var showDeliveryEditDialog by rememberSaveable { mutableStateOf(false) }
    // 운송장 정보 확인 다이얼로그(상대방 운송장) / 책 수령 확인 다이얼로그
    var showShippingConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showReceiveConfirmDialog by rememberSaveable { mutableStateOf(false) }
    // 약속 잡기 단계: 0=없음, 1=일시(1/3), 2=장소(2/3), 3=확인(3/3)
    var meetingStep by rememberSaveable { mutableStateOf(0) }
    // 약속 수정 모드 — true면 3/3 확인에서 등록(POST) 대신 수정(PATCH) 호출
    var meetingEditMode by rememberSaveable { mutableStateOf(false) }
    // 1/3에서 고른 약속 일시 (raw ISO, 예: 2026-05-20T14:30:00)
    var meetingScheduledAt by rememberSaveable { mutableStateOf("") }
    // 2/3에서 입력한 상세주소 (사용자 직접 입력, 빈칸 시작)
    var meetingAddressDetail by rememberSaveable { mutableStateOf("") }
    // 약속 확인(조회) 다이얼로그 표시 여부
    var showMeetingInfoDialog by rememberSaveable { mutableStateOf(false) }
    // 교환 확인 다이얼로그 표시 여부
    var showExchangeConfirmDialog by rememberSaveable { mutableStateOf(false) }
    // 교환 실패 안내 다이얼로그 표시 여부
    var showExchangeFailDialog by rememberSaveable { mutableStateOf(false) }

    // 바텀 네비 표시는 TrackerNavHost에서 현재 라우트 기준으로 일괄 제어 (여기서 토글하지 않음)
    TrackerDetailScreen(
        groupName = uiState.groupName,
        dDay = uiState.dDay,
        statusLabel = uiState.statusLabel,
        currentStepLabel = uiState.currentStepLabel,
        currentStepLabelStyle = uiState.currentStepLabelStyle,
        currentStepPosition = uiState.currentStepPosition,
        myProfile = uiState.myProfile,
        partnerProfile = uiState.partnerProfile,
        exchangeLabel = uiState.exchangeLabel,
        secondaryActionLabel = uiState.secondaryAction.label,
        primaryActionLabel = uiState.primaryAction.label,
        // 파트너 약속 완료 대기(WAITING_PARTNER_MEETING_COMPLETE)면 교환 완료 버튼 비활성화
        primaryActionEnabled = uiState.primaryEnabled,
        // 약속 등록 대기 상태(WAITING_HOST_MEETING_REGISTER)면 비활성화
        secondaryActionEnabled = uiState.secondaryEnabled,
        // 교환 단계 이후엔 읽기 진행률 바·% 숨김
        showReadingProgress = uiState.showReadingProgress,
        steps = uiState.steps,
        isHost = uiState.isHost,
        onBackClick = onBackClick,
        onMessageClick = { onNavigateComment(uiState.groupName) },
        onEditPeriodClick = { showReadingPeriodDialog = true },
        onGoToLibraryClick = onNavigateLibrary,
        onReportClick = { context.openReportChannel() },
        onSecondaryActionClick = {
            dispatchAction(
                action = uiState.secondaryAction,
                onRecordProgress = { showProgressDialog = true },
                onWriteBookReview = { onNavigateBookReview(false) },
                onEditBookReview = { onNavigateBookReview(true) },
                onRegisterTrackingNumber = { showTrackingDialog = true },
                onCheckDeliveryInfo = {
                    viewModel.loadDeliveryAddress { showDeliveryInfoDialog = true }
                },
                onRegisterMeeting = { meetingStep = 1 },
                onGoToComments = { onNavigateComment(uiState.groupName) },
                onCheckMeeting = {
                    viewModel.loadMeeting { showMeetingInfoDialog = true }
                },
                onConfirmExchange = { showExchangeConfirmDialog = true },
                onWritePartnerReview = onNavigatePartnerReview,
                onCheckShippingInfo = {
                    viewModel.loadPartnerDelivery { showShippingConfirmDialog = true }
                },
                onConfirmReceive = { showReceiveConfirmDialog = true },
                onWriteReadingCard = {
                    viewModel.openReadingCard(groupId, uiState.myProfile.bookTitle) {
                        onNavigateLibraryDetail(it)
                    }
                },
            )
        },
        onPrimaryActionClick = {
            dispatchAction(
                action = uiState.primaryAction,
                onRecordProgress = { showProgressDialog = true },
                onWriteBookReview = { onNavigateBookReview(false) },
                onEditBookReview = { onNavigateBookReview(true) },
                onRegisterTrackingNumber = { showTrackingDialog = true },
                onCheckDeliveryInfo = {
                    viewModel.loadDeliveryAddress { showDeliveryInfoDialog = true }
                },
                onRegisterMeeting = { meetingStep = 1 },
                onGoToComments = { onNavigateComment(uiState.groupName) },
                onCheckMeeting = {
                    viewModel.loadMeeting { showMeetingInfoDialog = true }
                },
                onConfirmExchange = { showExchangeConfirmDialog = true },
                onWritePartnerReview = onNavigatePartnerReview,
                onCheckShippingInfo = {
                    viewModel.loadPartnerDelivery { showShippingConfirmDialog = true }
                },
                onConfirmReceive = { showReceiveConfirmDialog = true },
                onWriteReadingCard = {
                    viewModel.openReadingCard(groupId, uiState.myProfile.bookTitle) {
                        onNavigateLibraryDetail(it)
                    }
                },
            )
        },
    )
    if (showProgressDialog) {
        TrackerProgressRecordDialog(
            totalPages = uiState.myProfile.totalPages,
            onDismiss = { showProgressDialog = false },
            onConfirm = { currentPage -> viewModel.recordProgress(currentPage) },
        )
    }
    if (showReadingPeriodDialog) {
        // 기존 예정 종료일 = 오늘 + dDay
        val originalEndDate = uiState.dDayCount?.let { LocalDate.now().plusDays(it.toLong()) }
        TrackerReadingPeriodEditDialog(
            originalEndDate = originalEndDate,
            onConfirm = { newEndDate ->
                viewModel.updateReadingPeriod(newEndDate.toString()) {
                    showReadingPeriodDialog = false
                }
            },
            onDismiss = { showReadingPeriodDialog = false },
        )
    }
    if (showTrackingDialog) {
        TrackerDeliveryTrackingNumberDialog(
            onDismiss = { showTrackingDialog = false },
            onConfirm = { company, number -> viewModel.registerDelivery(company, number) },
        )
    }
    val addressData = deliveryAddress
    if (showDeliveryInfoDialog && addressData != null) {
        TrackerDeliveryInfoDialog(
            partnerNickname = uiState.partnerProfile.nickname,
            myAddress = addressData.myAddress.toDisplay(),
            partnerAddress = addressData.partnerAddress.toDisplay(),
            canEditMyAddress = addressData.canEditMyAddress == true,
            onDismiss = {
                showDeliveryInfoDialog = false
                viewModel.clearDeliveryAddress()
            },
            onEditClick = {
                showDeliveryInfoDialog = false
                viewModel.loadSavedDeliveries { showDeliveryEditDialog = true }
            },
            onConfirmClick = {
                showDeliveryInfoDialog = false
                viewModel.clearDeliveryAddress()
            },
        )
    }
    if (showDeliveryEditDialog) {
        val myAddress = deliveryAddress?.myAddress
        TrackerDeliveryAddressEditDialog(
            savedAddresses = savedDeliveries.map { it.toDeliveryAddressOption() },
            initialSelectedUserDeliveryId = savedDeliveries.matchUserDeliveryId(myAddress),
            onDismiss = {
                showDeliveryEditDialog = false
                viewModel.clearDeliveryAddress()
            },
            onConfirmSaved = { userDeliveryId ->
                viewModel.changeDeliveryAddressSaved(userDeliveryId) {
                    showDeliveryEditDialog = false
                    viewModel.clearDeliveryAddress()
                }
            },
            onConfirmDirect = { zipCode, address, addressDetail ->
                viewModel.changeDeliveryAddressDirect(zipCode, address, addressDetail) {
                    showDeliveryEditDialog = false
                    viewModel.clearDeliveryAddress()
                }
            },
        )
    }
    // 운송장 정보 확인 (상대방 운송장)
    val partner = partnerDelivery
    if (showShippingConfirmDialog && partner != null) {
        TrackerDeliveryShippingConfirmDialog(
            companyName = partner.deliveryCompanyName.orEmpty(),
            trackingNumber = partner.trackingNumber.orEmpty(),
            onDismiss = {
                showShippingConfirmDialog = false
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
                showShippingConfirmDialog = false
                viewModel.clearPartnerDelivery()
            },
        )
    }
    // 책 수령 확인 -> PATCH 수령 확인
    if (showReceiveConfirmDialog) {
        TrackerDeliveryReceiveConfirmDialog(
            onDismiss = { showReceiveConfirmDialog = false },
            onConfirmClick = {
                viewModel.confirmReceive { showReceiveConfirmDialog = false }
            },
        )
    }
    // 약속 잡기 1/3 → 2/3 → 3/3
    when (meetingStep) {
        1 -> TrackerDirectMeetingTimeDialog(
            onDismiss = {
                meetingStep = 0
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
                meetingStep = 0
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
                meetingStep = 0
                meetingEditMode = false
                meetingAddressDetail = ""
                viewModel.clearMeetingPlace()
            },
            onConfirmClick = {
                val place = meetingPlace
                if (place != null) {
                    val onDone = {
                        meetingStep = 0
                        meetingEditMode = false
                        meetingAddressDetail = ""
                        viewModel.clearMeetingPlace()
                    }
                    // 수정 모드면 PATCH(editMeeting), 아니면 등록 POST(registerMeeting)
                    if (meetingEditMode) {
                        viewModel.editMeeting(
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
    // 약속 확인(조회)
    val meeting = meetingInfo
    if (showMeetingInfoDialog && meeting != null) {
        TrackerDirectMeetingInfoDialog(
            scheduledAt = meeting.meetingAt.orEmpty(),
            address = meeting.location?.address.orEmpty(),
            addressDetail = meeting.addressDetail.orEmpty(),
            isHost = uiState.isHost,
            onDismiss = {
                showMeetingInfoDialog = false
                viewModel.clearMeeting()
            },
            onConfirmClick = {
                showMeetingInfoDialog = false
                viewModel.clearMeeting()
            },
            // 수정: 등록과 동일한 3스텝 흐름 재사용, 기존 장소/일시/상세주소 프리필 후
            // 마지막 확인에서 editMeeting(PATCH) 호출
            onEditClick = {
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
                showMeetingInfoDialog = false
                viewModel.clearMeeting()
                meetingEditMode = true
                meetingStep = 1
            },
        )
    }
    // 교환 확인 → "교환했어요"면 완료 PATCH, "못했어요"면 실패 안내
    if (showExchangeConfirmDialog) {
        TrackerDirectExchangeConfirmDialog(
            onDismiss = { showExchangeConfirmDialog = false },
            onNotYetClick = {
                showExchangeConfirmDialog = false
                showExchangeFailDialog = true
            },
            onConfirmClick = {
                viewModel.completeMeeting { showExchangeConfirmDialog = false }
            },
        )
    }
    // 교환 실패 안내
    if (showExchangeFailDialog) {
        TrackerDirectExchangeFailDialog(
            onDismiss = { showExchangeFailDialog = false },
            onReportClick = { context.openReportChannel() },
            onGoToCommentsClick = {
                showExchangeFailDialog = false
                onNavigateComment(uiState.groupName)
            },
        )
    }
}

private inline fun dispatchAction(
    action: TrackerAction,
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
        TrackerAction.CompleteExchange, TrackerAction.None -> Unit
    }
}

@Composable
fun TrackerDetailScreen(
    groupName: String,
    dDay: String,
    statusLabel: String,
    currentStepLabel: String,
    currentStepLabelStyle: TrackerStepLabelStyle,
    currentStepPosition: Int,
    myProfile: TrackerProfileItem,
    partnerProfile: TrackerProfileItem,
    exchangeLabel: String,
    secondaryActionLabel: String,
    primaryActionLabel: String,
    steps: List<TrackerStep>,
    isHost: Boolean,
    onBackClick: () -> Unit,
    onMessageClick: () -> Unit,
    onEditPeriodClick: () -> Unit,
    onGoToLibraryClick: () -> Unit,
    onReportClick: () -> Unit,
    onSecondaryActionClick: () -> Unit,
    onPrimaryActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryActionEnabled: Boolean = true,
    secondaryActionEnabled: Boolean = true,
    showReadingProgress: Boolean = true,
) {
    TrackerDetailContent(
        groupName = groupName,
        dDay = dDay,
        statusLabel = statusLabel,
        currentStepLabel = currentStepLabel,
        currentStepLabelStyle = currentStepLabelStyle,
        currentStepPosition = currentStepPosition,
        myProfile = myProfile,
        partnerProfile = partnerProfile,
        exchangeLabel = exchangeLabel,
        secondaryActionLabel = secondaryActionLabel,
        primaryActionLabel = primaryActionLabel,
        steps = steps,
        isHost = isHost,
        onBackClick = onBackClick,
        onMessageClick = onMessageClick,
        onEditPeriodClick = onEditPeriodClick,
        onGoToLibraryClick = onGoToLibraryClick,
        onReportClick = onReportClick,
        onSecondaryActionClick = onSecondaryActionClick,
        onPrimaryActionClick = onPrimaryActionClick,
        modifier = modifier,
        primaryActionEnabled = primaryActionEnabled,
        secondaryActionEnabled = secondaryActionEnabled,
        showReadingProgress = showReadingProgress,
    )
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun TrackerDetailScreenPreview() {
    BookiiPreview {
        TrackerDetailScreen(
            groupName = "김영하 도장깨기 하실 분",
            dDay = "D-2",
            statusLabel = "일이삼사오육칠팔구... · 후기 작성",
            currentStepLabel = "내 책 읽기",
            currentStepLabelStyle = TrackerStepLabelStyle.Main,
            currentStepPosition = 1,
            myProfile = TrackerProfileItem(
                nickname = "나",
                bookTitle = "살인자의 기억법",
                bookCoverUrl = null,
                profileImageUrl = null,
                progressPercent = 100,
                isOwnerBook = true,
            ),
            partnerProfile = TrackerProfileItem(
                nickname = "noshel",
                bookTitle = "작별인사",
                bookCoverUrl = null,
                profileImageUrl = null,
                progressPercent = 0,
                isOwnerBook = false,
            ),
            exchangeLabel = "직접 교환",
            secondaryActionLabel = "독서카드 작성",
            primaryActionLabel = "책 후기 작성",
            steps = listOf(
                TrackerStep(
                    title = "반납",
                    description = "파트너에게 책을 돌려보내주세요",
                    status = TrackerStepStatus.Pending,
                ),
                TrackerStep(
                    title = "파트너 책 읽기",
                    description = "작별인사를 읽고 진행률을 기록해주세요",
                    status = TrackerStepStatus.InProgress(chipText = "D-2"),
                ),
                TrackerStep(
                    title = "교환",
                    description = "파트너와 책을 교환해주세요",
                    status = TrackerStepStatus.Completed,
                ),
                TrackerStep(
                    title = "살인자의 기억법 읽기",
                    description = "독서카드를 작성하면 교환독서가 더 즐거워져요",
                    status = TrackerStepStatus.Completed,
                ),
            ),
            isHost = true,
            onBackClick = {},
            onMessageClick = {},
            onEditPeriodClick = {},
            onGoToLibraryClick = {},
            onReportClick = {},
            onSecondaryActionClick = {},
            onPrimaryActionClick = {},
        )
    }
}
