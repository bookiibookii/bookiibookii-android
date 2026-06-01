package com.bookiibookii.bookiibookii.tracker.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressUpdateReqDTO
import com.bookiibookii.bookiibookii.tracker.model.TrackerAction
import com.bookiibookii.bookiibookii.tracker.model.TrackerProfileItem
import com.bookiibookii.bookiibookii.tracker.model.TrackerStepLabelStyle
import com.bookiibookii.bookiibookii.tracker.model.toDisplay
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerDetailContent
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
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStep
import com.bookiibookii.bookiibookii.tracker.ui.detail.component.TrackerStepStatus
import com.bookiibookii.bookiibookii.tracker.vm.TrackerDetailViewModel
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview

@Composable
fun TrackerDetailRoute(
    groupId: Long,
    onBackClick: () -> Unit,
    onNavigateBookReview: () -> Unit,
    onNavigatePartnerReview: () -> Unit,
    onNavigateComment: (title: String) -> Unit = {},
    viewModel: TrackerDetailViewModel = viewModel(
        factory = TrackerDetailViewModel.factory(groupId)
    ),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val deliveryAddress by viewModel.deliveryAddress.collectAsStateWithLifecycle()
    val partnerDelivery by viewModel.partnerDelivery.collectAsStateWithLifecycle()
    val meetingPlace by viewModel.meetingPlace.collectAsStateWithLifecycle()
    val meetingInfo by viewModel.meetingInfo.collectAsStateWithLifecycle()
    var showProgressDialog by rememberSaveable { mutableStateOf(false) }
    var showTrackingDialog by rememberSaveable { mutableStateOf(false) }
    var showDeliveryInfoDialog by rememberSaveable { mutableStateOf(false) }
    var showDeliveryEditDialog by rememberSaveable { mutableStateOf(false) }
    // 운송장 정보 확인 다이얼로그(상대방 운송장) / 책 수령 확인 다이얼로그
    var showShippingConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showReceiveConfirmDialog by rememberSaveable { mutableStateOf(false) }
    // 약속 잡기 단계: 0=없음, 1=일시(1/3), 2=장소(2/3), 3=확인(3/3)
    var meetingStep by rememberSaveable { mutableStateOf(0) }
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
        steps = uiState.steps,
        onBackClick = onBackClick,
        onMessageClick = { onNavigateComment(uiState.groupName) },
        // TODO: 더보기 placeholder
        onMoreClick = {},
        onSecondaryActionClick = {
            dispatchAction(
                action = uiState.secondaryAction,
                onRecordProgress = { showProgressDialog = true },
                onWriteBookReview = onNavigateBookReview,
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
            )
        },
        onPrimaryActionClick = {
            dispatchAction(
                action = uiState.primaryAction,
                onRecordProgress = { showProgressDialog = true },
                onWriteBookReview = onNavigateBookReview,
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
                showDeliveryEditDialog = true
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
            initialAddress = myAddress?.address.orEmpty(),
            initialAddressDetail = myAddress?.addressDetail.orEmpty(),
            onDismiss = {
                showDeliveryEditDialog = false
                viewModel.clearDeliveryAddress()
            },
            onConfirm = { newAddress, newDetail ->
                viewModel.updateMyDeliveryAddress(
                    DeliveryAddressUpdateReqDTO(
                        receiverName = myAddress?.receiverName.orEmpty(),
                        phoneNumber = myAddress?.phoneNumber.orEmpty(),
                        address = newAddress,
                        addressDetail = newDetail,
                        zipCode = myAddress?.zipCode.orEmpty(),
                    ),
                ) {
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
            onTrackingSearchClick = {}, // TODO: 배송 조회 이동 로직 보류
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
                meetingAddressDetail = ""
                viewModel.clearMeetingPlace()
            },
            onNextClick = { scheduledAt ->
                meetingScheduledAt = scheduledAt
                meetingStep = 2
            },
        )
        2 -> TrackerDirectMeetingPlaceDialog(
            address = meetingPlace?.address.orEmpty(),
            addressDetail = meetingAddressDetail,
            onAddressDetailChange = { meetingAddressDetail = it },
            onLoadMyPlaceClick = { viewModel.loadMyExchangePlace() },
            onDismiss = {
                meetingStep = 0
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
                meetingAddressDetail = ""
                viewModel.clearMeetingPlace()
            },
            onConfirmClick = {
                val place = meetingPlace
                if (place != null) {
                    viewModel.registerMeeting(
                        locationId = place.id,
                        addressDetail = meetingAddressDetail.ifBlank { null },
                        scheduledAt = meetingScheduledAt,
                    ) {
                        meetingStep = 0
                        meetingAddressDetail = ""
                        viewModel.clearMeetingPlace()
                    }
                }
            },
        )
    }
    // 약속 확인(조회)
    val meeting = meetingInfo
    if (showMeetingInfoDialog && meeting != null) {
        TrackerDirectMeetingInfoDialog(
            scheduledAt = meeting.scheduledAt.orEmpty(),
            address = meeting.location?.address.orEmpty(),
            addressDetail = meeting.location?.addressDetail.orEmpty(),
            onDismiss = {
                showMeetingInfoDialog = false
                viewModel.clearMeeting()
            },
            onPreviousClick = {
                showMeetingInfoDialog = false
                viewModel.clearMeeting()
            },
            onConfirmClick = {
                showMeetingInfoDialog = false
                viewModel.clearMeeting()
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
            onReportClick = {}, // TODO: 신고하기 이동 로직 보류
            onGoToCommentsClick = {}, // TODO: 댓글 바로가기 이동 로직 보류
        )
    }
}

private inline fun dispatchAction(
    action: TrackerAction,
    onRecordProgress: () -> Unit,
    onWriteBookReview: () -> Unit,
    onRegisterTrackingNumber: () -> Unit,
    onCheckDeliveryInfo: () -> Unit,
    onRegisterMeeting: () -> Unit,
    onGoToComments: () -> Unit,
    onCheckMeeting: () -> Unit,
    onConfirmExchange: () -> Unit,
    onWritePartnerReview: () -> Unit,
    onCheckShippingInfo: () -> Unit,
    onConfirmReceive: () -> Unit,
) {
    when (action) {
        TrackerAction.RecordProgress -> onRecordProgress()
        TrackerAction.WriteBookReview -> onWriteBookReview()
        TrackerAction.RegisterTrackingNumber -> onRegisterTrackingNumber()
        TrackerAction.CheckDeliveryInfo -> onCheckDeliveryInfo()
        TrackerAction.RegisterMeeting -> onRegisterMeeting()
        TrackerAction.GoToComments -> onGoToComments()
        TrackerAction.CheckMeeting -> onCheckMeeting()
        TrackerAction.ConfirmExchange -> onConfirmExchange()
        TrackerAction.WritePartnerReview -> onWritePartnerReview()
        TrackerAction.CheckShippingInfo -> onCheckShippingInfo()
        TrackerAction.ConfirmReceive -> onConfirmReceive()
        TrackerAction.WriteReadingCard -> Unit // TODO: 독서카드 작성 화면 연결 보류
        TrackerAction.None -> Unit
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
    onBackClick: () -> Unit,
    onMessageClick: () -> Unit,
    onMoreClick: () -> Unit,
    onSecondaryActionClick: () -> Unit,
    onPrimaryActionClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        onBackClick = onBackClick,
        onMessageClick = onMessageClick,
        onMoreClick = onMoreClick,
        onSecondaryActionClick = onSecondaryActionClick,
        onPrimaryActionClick = onPrimaryActionClick,
        modifier = modifier,
    )
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun TrackerDetailScreenPreview() {
    BookiiPreview {
        TrackerDetailScreen(
            groupName = "김영하 도장깨기 하실 분",
            dDay = "D-2",
            statusLabel = "살인자의 기억법 · 후기 작성",
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
            onBackClick = {},
            onMessageClick = {},
            onMoreClick = {},
            onSecondaryActionClick = {},
            onPrimaryActionClick = {},
        )
    }
}
