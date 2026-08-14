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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.common.openPrivacyPolicy
import com.bookiibookii.bookiibookii.common.openTermsOfService
import com.bookiibookii.bookiibookii.ui.component.showCustomToast
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview

@Composable
fun SettingRoute(
    onBackClick: () -> Unit,
    onNoticeClick: () -> Unit,
    onQuestionClick: () -> Unit,
    onWithdrawClick: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val prefs = remember { context.getSharedPreferences("bookii_prefs", android.content.Context.MODE_PRIVATE) }
    var pushEnabled by remember {
        mutableStateOf(prefs.getBoolean("push_notification_enabled", true))
    }

    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                prefs.edit().putBoolean("push_notification_enabled", true).apply()
                pushEnabled = true
                com.bookiibookii.bookiibookii.notification.fcm.FcmTokenRegistrar.registerCurrentToken(context)
            } else {
                context.showCustomToast("알림 권한이 거부되었습니다. 시스템 설정에서 직접 허용해주세요.", false)
            }
        },
    )

    SettingScreen(
        pushEnabled = pushEnabled,
        onPushToggle = { enabled ->
            if (enabled) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        prefs.edit().putBoolean("push_notification_enabled", true).apply()
                        pushEnabled = true
                        com.bookiibookii.bookiibookii.notification.fcm.FcmTokenRegistrar.registerCurrentToken(context)
                    } else {
                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    prefs.edit().putBoolean("push_notification_enabled", true).apply()
                    pushEnabled = true
                    com.bookiibookii.bookiibookii.notification.fcm.FcmTokenRegistrar.registerCurrentToken(context)
                }
            } else {
                prefs.edit().putBoolean("push_notification_enabled", false).apply()
                pushEnabled = false
                com.bookiibookii.bookiibookii.notification.fcm.FcmTokenRegistrar.deactivateCurrentToken {}
            }
        },
        onBackClick = onBackClick,
        onNoticeClick = onNoticeClick,
        onQuestionClick = onQuestionClick,
        onTermsClick = { context.openTermsOfService() },
        onPrivacyClick = { context.openPrivacyPolicy() },
        onWithdrawClick = onWithdrawClick,
        onLogoutClick = {
            val ctx = context.applicationContext
            com.bookiibookii.bookiibookii.notification.fcm.FcmTokenRegistrar.deactivateCurrentToken {
                com.bookiibookii.bookiibookii.onboarding.login.TokenManager.clear(ctx)
                val intent = android.content.Intent(ctx, com.bookiibookii.bookiibookii.onboarding.login.LoginActivity::class.java).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                ctx.startActivity(intent)
            }
        },
    )
}

@Composable
fun SettingScreen(
    pushEnabled: Boolean = true,
    onPushToggle: (Boolean) -> Unit = {},
    onBackClick: () -> Unit = {},
    onNoticeClick: () -> Unit = {},
    onQuestionClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(BookiiBookiiTheme.colors.uiBg)) {
        SettingTopBar(onBackClick = onBackClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingSectionHeader("알림")
            Spacer(modifier = Modifier.height(8.dp))
            PushNotificationCard(pushEnabled = pushEnabled, onToggle = onPushToggle)

            Spacer(modifier = Modifier.height(24.dp))
            SettingSectionHeader("고객센터")
            Spacer(modifier = Modifier.height(8.dp))
            IconSettingCard(
                iconRes = R.drawable.ic_alert,
                title = "공지사항",
                subtitle = "부키부키의 새로운 소식을 확인하세요!",
                onClick = onNoticeClick,
            )
            Spacer(modifier = Modifier.height(8.dp))
            IconSettingCard(
                iconRes = R.drawable.ic_info,
                title = "자주 묻는 질문 / 신고하기",
                subtitle = "문의 또는 불편사항이 있으신가요?",
                onClick = onQuestionClick,
            )

            Spacer(modifier = Modifier.height(24.dp))
            SettingSectionHeader("이용 약관")
            Spacer(modifier = Modifier.height(8.dp))
            TextSettingCard(title = "서비스 이용 약관", showChevron = true, onClick = onTermsClick)
            Spacer(modifier = Modifier.height(8.dp))
            TextSettingCard(title = "개인정보 처리 방침", showChevron = true, onClick = onPrivacyClick)

            Spacer(modifier = Modifier.height(24.dp))
            SettingSectionHeader("버전 정보")
            Spacer(modifier = Modifier.height(8.dp))
            TextSettingCard(title = "v1.0.0", textColor = BookiiBookiiTheme.colors.grey500)

            Spacer(modifier = Modifier.height(24.dp))
            SettingSectionHeader("계정 관리")
            Spacer(modifier = Modifier.height(8.dp))
            TextSettingCard(title = "로그아웃", onClick = { showLogoutDialog = true })
            Spacer(modifier = Modifier.height(8.dp))
            TextSettingCard(title = "회원 탈퇴", textColor = BookiiBookiiTheme.colors.uiPointRed, onClick = onWithdrawClick)

            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }

    if (showLogoutDialog) {
        BookiiDialog(
            title = "로그아웃",
            body = "로그아웃 하시겠어요?",
            confirmText = "로그아웃",
            confirmColor = BookiiBookiiTheme.colors.uiPointRed,
            onConfirm = {
                showLogoutDialog = false
                onLogoutClick()
            },
            cancelText = "취소",
            onDismiss = { showLogoutDialog = false },
        )
    }
}

@Composable
private fun SettingTopBar(onBackClick: () -> Unit) {
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
                text = "설정",
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
private fun SettingSectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = BookiiBookiiTheme.typography.semibold16, color = BookiiBookiiTheme.colors.grey900)
    }
}

@Composable
private fun PushNotificationCard(pushEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BookiiBookiiTheme.colors.white)
            .border(1.dp, BookiiBookiiTheme.colors.grey100, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_alert),
            contentDescription = null,
            tint = BookiiBookiiTheme.colors.uiMain,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text("푸시 알림 받기", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
            Text("서비스 알림을 받습니다.", style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey600)
        }
        BookiiToggle(checked = pushEnabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun IconSettingCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BookiiBookiiTheme.colors.white)
            .border(1.dp, BookiiBookiiTheme.colors.grey100, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = BookiiBookiiTheme.colors.uiMain,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
            Text(subtitle, style = BookiiBookiiTheme.typography.regular12, color = BookiiBookiiTheme.colors.grey600)
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron),
            contentDescription = null,
            tint = BookiiBookiiTheme.colors.grey900,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { scaleX = -1f },
        )
    }
}

@Composable
private fun TextSettingCard(
    title: String,
    showChevron: Boolean = false,
    textColor: Color = BookiiBookiiTheme.colors.grey900,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BookiiBookiiTheme.colors.white)
            .border(1.dp, BookiiBookiiTheme.colors.grey100, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = BookiiBookiiTheme.typography.medium16,
            color = textColor,
            modifier = Modifier.weight(1f),
        )
        if (showChevron) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { scaleX = -1f },
            )
        }
    }
}

@Composable
private fun BookiiToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = BookiiBookiiTheme.colors
    val thumbOffset by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (checked) 20f else 0f,
        label = "toggle",
    )
    Box(
        modifier = androidx.compose.ui.Modifier
            .width(52.dp)
            .height(32.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(if (checked) colors.uiMain else colors.grey300)
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            ) { onCheckedChange(!checked) },
    ) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .padding(4.dp)
                .size(24.dp)
                .offset(x = thumbOffset.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(androidx.compose.ui.graphics.Color.White),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    BookiiPreview {
        SettingScreen()
    }
}
