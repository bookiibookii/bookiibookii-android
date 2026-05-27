package com.bookiibookii.bookiibookii.onboarding.steps.ui.content

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.onboarding.steps.OnbViewModel
import com.bookiibookii.bookiibookii.onboarding.steps.model.NicknameCheckState
import com.bookiibookii.bookiibookii.onboarding.steps.model.OnbState
import com.bookiibookii.bookiibookii.onboarding.steps.model.ProfileImageUploadState
import com.bookiibookii.bookiibookii.onboarding.steps.ui.component.BirthdatePickerBottomSheet
import com.bookiibookii.bookiibookii.onboarding.steps.ui.component.OnbSubHeadCard
import com.bookiibookii.bookiibookii.onboarding.steps.ui.component.ProfilePhotoBottomSheet
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private object SquircleShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val s = size.width / 128f
        val path = Path().apply {
            moveTo(0f, 64f * s)
            cubicTo(0f, 11.296f * s, 11.296f * s, 0f, 64f * s, 0f)
            cubicTo(116.704f * s, 0f, size.width, 11.296f * s, size.width, 64f * s)
            cubicTo(size.width, 116.704f * s, 116.704f * s, size.height, 64f * s, size.height)
            cubicTo(11.296f * s, size.height, 0f, 116.704f * s, 0f, 64f * s)
            close()
        }
        return Outline.Generic(path)
    }
}

private val nicknameAllowedCharRegex = Regex("[가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9._\\-_/()\\[\\]:!?]")
private val nicknameAllowedRegex = Regex("^[가-힣A-Za-z0-9._\\-_/()\\[\\]:!?]+$")

private fun validateNicknameInput(nickname: String): Boolean {
    if (nickname.isBlank() || nickname.length > 10) return false
    if (nickname.any { it.isWhitespace() || Character.isSurrogate(it) }) return false
    return nicknameAllowedRegex.matches(nickname)
}

@Composable
fun OnbStep1Content(
    vm: OnbViewModel,
    state: OnbState,
    nicknameCheckState: NicknameCheckState,
    imageUploadState: ProfileImageUploadState,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,
) {
    val context = LocalContext.current
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    var showPhotoSheet by remember { mutableStateOf(false) }
    var showBirthdateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(imageUploadState) {
        when (val s = imageUploadState) {
            is ProfileImageUploadState.Success ->
                Toast.makeText(context, "프로필 이미지가 업로드되었습니다.", Toast.LENGTH_SHORT).show()
            is ProfileImageUploadState.Error ->
                Toast.makeText(context, s.message, Toast.LENGTH_SHORT).show()
            else -> {}
        }
    }

    val isCheckEnabled = validateNicknameInput(state.nickname) &&
            nicknameCheckState !is NicknameCheckState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        OnbSubHeadCard(
            title = "만나서 반가워요!",
            description = "부키부키에서 사용할 정보를 알려주세요"
        )

        ProfileImageSection(
            profileUri = state.profileUri,
            onEditClick = { showPhotoSheet = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        NicknameSection(
            nickname = state.nickname,
            onNicknameChange = { new ->
                val filtered = new
                    .filter { ch ->
                        !ch.isWhitespace() && !Character.isSurrogate(ch) &&
                                nicknameAllowedCharRegex.matches(ch.toString())
                    }
                    .take(10)
                vm.setNickname(filtered)
            },
            nicknameState = nicknameCheckState,
            isCheckEnabled = isCheckEnabled,
            onCheckClick = { vm.checkNickname(state.nickname.trim()) }
        )

        GenderSection(
            selectedGender = state.gender,
            onGenderSelected = { vm.setGender(it) }
        )

        BirthdateSection(
            birthdateText = state.birthdate?.replace("-", ".") ?: "0000.00.00",
            isBirthdateSet = state.birthdate != null,
            onClick = { showBirthdateSheet = true }
        )
    }

    if (showPhotoSheet) {
        ProfilePhotoBottomSheet(
            onCamera = { onOpenCamera(); showPhotoSheet = false },
            onGallery = { onOpenGallery(); showPhotoSheet = false },
            onDismiss = { showPhotoSheet = false }
        )
    }

    if (showBirthdateSheet) {
        BirthdatePickerBottomSheet(
            onDone = { year, month, day ->
                vm.setBirthdate("%04d-%02d-%02d".format(year, month, day))
                showBirthdateSheet = false
            },
            onDismiss = { showBirthdateSheet = false }
        )
    }
}

@Composable
internal fun ProfileImageSection(
    profileUri: Uri?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BookiiBookiiTheme.colors
    Box(modifier = modifier.size(128.dp)) {
        if (profileUri != null) {
            Box(modifier = Modifier.size(128.dp).clip(SquircleShape).background(colors.grey300)) {
                AsyncImage(
                    model = profileUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Image(
                painter = painterResource(R.drawable.ic_profile_placeholder),
                contentDescription = null,
                modifier = Modifier.size(128.dp),
                contentScale = ContentScale.FillBounds
            )
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(colors.grey600)
                .clickable(onClick = onEditClick)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_camera),
                contentDescription = "프로필 사진 변경",
                tint = colors.white,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun NicknameSection(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    nicknameState: NicknameCheckState,
    isCheckEnabled: Boolean,
    onCheckClick: () -> Unit,
) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    val (validationText, validationColor) = when (nicknameState) {
        is NicknameCheckState.Available -> nicknameState.message to colors.uiPointGreen200
        is NicknameCheckState.Duplicated -> nicknameState.message to colors.uiPointRed
        is NicknameCheckState.Error -> nicknameState.message to colors.uiPointRed
        else -> null to colors.uiPointRed
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("닉네임", style = typography.medium16, color = colors.grey900)
            Spacer(modifier = Modifier.width(4.dp))
            Text("*", style = typography.medium16, color = colors.uiMain)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(BookiiBookiiTheme.shape.round16)
                .border(1.dp, colors.grey300, BookiiBookiiTheme.shape.round16)
                .background(colors.white)
                .padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                textStyle = TextStyle(
                    fontFamily = BookiiBookiiTheme.typography.regular16.fontFamily,
                    fontSize = 16.sp,
                    color = colors.grey900
                ),
                cursorBrush = SolidColor(colors.grey900),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxHeight()) {
                        if (nickname.isEmpty()) {
                            Text("닉네임을 입력해주세요", style = typography.regular16, color = colors.grey500)
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            val checkBg = if (isCheckEnabled) colors.grey900 else colors.grey400
            val checkTextColor = if (isCheckEnabled) colors.white else colors.grey100
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(BookiiBookiiTheme.shape.round16)
                    .background(checkBg)
                    .clickable(enabled = isCheckEnabled, onClick = onCheckClick)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("중복 확인", style = typography.medium14, color = checkTextColor)
            }
        }
        Text(
            text = "한글, 영문, 숫자 공백 포함 10자 이내",
            style = typography.regular12,
            color = colors.grey500,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        if (validationText != null) {
            Text(
                text = validationText,
                style = typography.regular12,
                color = validationColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun GenderSection(selectedGender: String?, onGenderSelected: (String) -> Unit) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("성별", style = typography.medium16, color = colors.grey900)
            Spacer(modifier = Modifier.width(4.dp))
            Text("*", style = typography.medium16, color = colors.uiMain)
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenderButton("여성", selectedGender == "FEMALE", { onGenderSelected("FEMALE") }, Modifier.width(119.dp))
            GenderButton("남성", selectedGender == "MALE", { onGenderSelected("MALE") }, Modifier.width(119.dp))
            GenderButton("선택 안함", selectedGender == "none", { onGenderSelected("none") }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun GenderButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(BookiiBookiiTheme.shape.round16)
            .background(if (selected) colors.uiMainPale else colors.white)
            .border(1.dp, if (selected) colors.uiMain150 else colors.grey300, BookiiBookiiTheme.shape.round16)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = typography.regular15, color = if (selected) colors.uiMain else colors.grey500)
    }
}

@Composable
private fun BirthdateSection(birthdateText: String, isBirthdateSet: Boolean, onClick: () -> Unit) {
    val colors = BookiiBookiiTheme.colors
    val typography = BookiiBookiiTheme.typography

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("생년월일", style = typography.medium16, color = colors.grey900)
            Spacer(modifier = Modifier.width(4.dp))
            Text("*", style = typography.medium16, color = colors.uiMain)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BookiiBookiiTheme.shape.round16)
                .border(1.dp, colors.grey300, BookiiBookiiTheme.shape.round16)
                .background(colors.white)
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = birthdateText,
                style = typography.regular16,
                color = if (isBirthdateSet) colors.grey900 else colors.grey500,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(R.drawable.ic_calender),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─── 섹션 프리뷰 ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "ProfileImageSection - 기본")
@Composable
private fun ProfileImageSectionPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            ProfileImageSection(profileUri = null, onEditClick = {})
        }
    }
}

@Preview(showBackground = true, name = "NicknameSection - 기본")
@Composable
private fun NicknameSectionIdlePreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            NicknameSection(
                nickname = "",
                onNicknameChange = {},
                nicknameState = NicknameCheckState.Idle,
                isCheckEnabled = false,
                onCheckClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "NicknameSection - 입력 중")
@Composable
private fun NicknameSectionTypingPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            NicknameSection(
                nickname = "부키부키",
                onNicknameChange = {},
                nicknameState = NicknameCheckState.Idle,
                isCheckEnabled = true,
                onCheckClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "NicknameSection - 사용 가능")
@Composable
private fun NicknameSectionAvailablePreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            NicknameSection(
                nickname = "부키부키",
                onNicknameChange = {},
                nicknameState = NicknameCheckState.Available("사용 가능한 닉네임입니다."),
                isCheckEnabled = false,
                onCheckClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "NicknameSection - 중복")
@Composable
private fun NicknameSectionDuplicatedPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            NicknameSection(
                nickname = "부키부키",
                onNicknameChange = {},
                nicknameState = NicknameCheckState.Duplicated("이미 사용 중인 닉네임입니다."),
                isCheckEnabled = false,
                onCheckClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "GenderSection - 미선택")
@Composable
private fun GenderSectionNonePreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            GenderSection(selectedGender = null, onGenderSelected = {})
        }
    }
}

@Preview(showBackground = true, name = "GenderSection - 여성 선택")
@Composable
private fun GenderSectionSelectedPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            GenderSection(selectedGender = "FEMALE", onGenderSelected = {})
        }
    }
}

@Preview(showBackground = true, name = "BirthdateSection - 미입력")
@Composable
private fun BirthdateSectionEmptyPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            BirthdateSection(birthdateText = "0000.00.00", isBirthdateSet = false, onClick = {})
        }
    }
}

@Preview(showBackground = true, name = "BirthdateSection - 입력됨")
@Composable
private fun BirthdateSectionFilledPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            BirthdateSection(birthdateText = "1997.01.18", isBirthdateSet = true, onClick = {})
        }
    }
}

// ─── 전체 화면 프리뷰 ─────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Step 1 - 전체")
@Composable
private fun OnbStep1ContentPreview() {
    BookiiPreview {
        OnbStep1Content(
            vm = OnbViewModel(),
            state = OnbState(),
            nicknameCheckState = NicknameCheckState.Idle,
            imageUploadState = ProfileImageUploadState.Idle,
            onOpenCamera = {},
            onOpenGallery = {},
        )
    }
}
