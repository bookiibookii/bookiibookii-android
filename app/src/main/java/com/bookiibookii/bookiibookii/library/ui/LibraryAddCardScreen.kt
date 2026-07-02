package com.bookiibookii.bookiibookii.library.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import com.bookiibookii.bookiibookii.common.stripBookSubtitle
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import androidx.compose.ui.platform.LocalContext
import com.bookiibookii.bookiibookii.common.showCustomToast
import com.bookiibookii.bookiibookii.onboarding.login.TokenManager
import com.bookiibookii.bookiibookii.ui.component.BookiiBackButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AddCardMode { TEXT, PHOTO }

private const val QUOTE_MAX = 140
private const val TEXT_MEMO_MAX = 110
private const val PHOTO_MEMO_MAX = 110

@Composable
fun LibraryAddCardRoute(
    mode: AddCardMode,
    memberBookId: Int,
    cardId: Long,
    initialQuote: String,
    initialPage: String,
    initialMemo: String,
    initialImageUrl: String?,
    initialS3Key: String?,
    bookTitle: String,
    totalPages: Int,
    onBackClick: () -> Unit,
    onSaved: (isEdit: Boolean) -> Unit,
    viewModel: com.bookiibookii.bookiibookii.library.vm.LibraryAddCardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isEdit = cardId != -1L
    val uiState by viewModel.uiState.collectAsState()

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val pickImageLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let { selectedImageUri = it } }

    val takePhotoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicture(),
    ) { success -> if (success) cameraImageUri?.let { selectedImageUri = it } }

    fun launchCamera() {
        val uri = try {
            val cameraDir = java.io.File(context.cacheDir, "camera").apply { mkdirs() }
            val file = java.io.File(cameraDir, "card_${System.currentTimeMillis()}.jpg")
            androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
        if (uri == null) {
            context.showCustomToast("카메라를 실행할 수 없습니다.", false)
            return
        }
        cameraImageUri = uri
        takePhotoLauncher.launch(uri)
    }

    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) launchCamera() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is com.bookiibookii.bookiibookii.library.vm.LibraryAddCardViewModel.AddCardEvent.Success -> {
                    context.showCustomToast(if (isEdit) "독서카드가 수정되었습니다." else "독서카드가 등록되었습니다.", true)
                    onSaved(isEdit)
                }
                is com.bookiibookii.bookiibookii.library.vm.LibraryAddCardViewModel.AddCardEvent.Error -> {
                    context.showCustomToast(event.message, false)
                }
            }
        }
    }

    LibraryAddCardScreen(
        mode = mode,
        selectedImageUri = selectedImageUri,
        isEdit = isEdit,
        initialQuote = initialQuote,
        initialPage = initialPage,
        initialMemo = initialMemo,
        initialImageUrl = initialImageUrl,
        bookTitle = bookTitle,
        totalPages = totalPages.takeIf { it > 0 },
        onImagePick = { pickImageLauncher.launch("image/*") },
        onImageCapture = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
        onBackClick = onBackClick,
        isLoading = uiState.isLoading,
        onSubmit = { page, quotation, memo ->
            if (memberBookId == -1) {
                context.showCustomToast("책 정보를 찾을 수 없습니다.", false)
                return@LibraryAddCardScreen
            }
            if (isEdit) {
                viewModel.updateCard(
                    cardId = cardId,
                    memberBookId = memberBookId,
                    mode = mode,
                    page = page,
                    quotation = quotation,
                    memo = memo,
                    newImageUri = selectedImageUri,
                    existingS3Key = initialS3Key,
                    contentResolver = context.contentResolver,
                )
            } else {
                viewModel.createCard(
                    memberBookId = memberBookId,
                    mode = mode,
                    page = page,
                    quotation = quotation,
                    memo = memo,
                    imageUri = selectedImageUri,
                    contentResolver = context.contentResolver,
                )
            }
        },
    )
}

@Composable
fun LibraryAddCardScreen(
    mode: AddCardMode = AddCardMode.TEXT,
    selectedImageUri: android.net.Uri? = null,
    isEdit: Boolean = false,
    initialQuote: String = "",
    initialPage: String = "",
    initialMemo: String = "",
    initialImageUrl: String? = null,
    bookTitle: String = "",
    totalPages: Int? = null,
    onImagePick: () -> Unit = {},
    onImageCapture: () -> Unit = {},
    onBackClick: () -> Unit = {},
    isLoading: Boolean = false,
    onSubmit: (page: Int, quotation: String, memo: String) -> Unit = { _, _, _ -> },
) {
    var quote by remember { mutableStateOf(initialQuote) }
    var page by remember { mutableStateOf(initialPage) }
    var memo by remember { mutableStateOf(initialMemo) }

    fun clampPage(input: String): String {
        val n = input.toIntOrNull() ?: return input
        return if (totalPages != null && totalPages > 0 && n > totalPages) totalPages.toString() else input
    }

    val context = LocalContext.current
    val currentNickname = remember { TokenManager.getNickname(context) ?: "" }

    var quoteError by remember { mutableStateOf(false) }
    var pageError by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    val memoMax = if (mode == AddCardMode.TEXT) TEXT_MEMO_MAX else PHOTO_MEMO_MAX

    val hasChanges = if (mode == AddCardMode.TEXT) {
        quote != initialQuote || page != initialPage || memo != initialMemo
    } else {
        page != initialPage || memo != initialMemo || selectedImageUri != null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.uiBg)
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BookiiBackButton(onClick = onBackClick)
                Text(
                    text = if (isEdit) "독서카드 수정" else "독서카드 추가",
                    style = BookiiBookiiTheme.typography.medium20,
                    color = BookiiBookiiTheme.colors.grey900,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(40.dp))
            }
            HorizontalDivider(color = BookiiBookiiTheme.colors.grey200, thickness = 1.dp)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            when (mode) {
                AddCardMode.TEXT -> {
                    AddCardField(
                        label = "인용구",
                        required = true,
                        value = quote,
                        onValueChange = {
                            if (it.length <= QUOTE_MAX) {
                                quote = it
                                quoteError = false
                            }
                        },
                        placeholder = "인상 깊은 문장을 작성해주세요.",
                        maxLength = QUOTE_MAX,
                        errorText = if (quoteError) "인용구를 입력해주세요" else null,
                    )
                    AddCardField(
                        label = "페이지",
                        required = true,
                        value = page,
                        onValueChange = {
                            page = clampPage(it)
                            pageError = false
                        },
                        placeholder = "페이지를 입력해주세요.",
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        errorText = if (pageError) "페이지를 입력해주세요" else null,
                    )
                    AddCardField(
                        label = "메모",
                        required = false,
                        value = memo,
                        onValueChange = { if (it.length <= memoMax) memo = it },
                        placeholder = "메모를 입력해주세요.",
                        maxLength = memoMax,
                    )
                }
                AddCardMode.PHOTO -> {
                    var showPhotoSheet by remember { mutableStateOf(false) }
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(BookiiBookiiTheme.colors.grey200)
                                .border(1.dp, BookiiBookiiTheme.colors.grey300, RoundedCornerShape(20.dp))
                                .clickable { showPhotoSheet = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            val photoModel = selectedImageUri ?: initialImageUrl
                            if (photoModel != null) {
                                coil.compose.AsyncImage(
                                    model              = photoModel,
                                    contentDescription = null,
                                    contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp)
                                        .size(width = 32.dp, height = 33.dp)
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(BookiiBookiiTheme.colors.white)
                                        .border(1.dp, BookiiBookiiTheme.colors.grey200, RoundedCornerShape(30.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_edit),
                                        contentDescription = "사진 변경",
                                        tint = BookiiBookiiTheme.colors.grey500,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_upload),
                                        contentDescription = null,
                                        tint = BookiiBookiiTheme.colors.grey600,
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Text(
                                        text = "사진 업로드",
                                        style = BookiiBookiiTheme.typography.regular14,
                                        color = BookiiBookiiTheme.colors.grey600,
                                    )
                                }
                            }
                        }
                        if (showPhotoSheet) {
                            CardImagePickerBottomSheet(
                                onCamera  = { onImageCapture(); showPhotoSheet = false },
                                onGallery = { onImagePick();   showPhotoSheet = false },
                                onDismiss = { showPhotoSheet = false },
                            )
                        }
                    }
                    AddCardField(
                        label = "페이지",
                        required = true,
                        value = page,
                        onValueChange = {
                            page = clampPage(it)
                            pageError = false
                        },
                        placeholder = "페이지를 입력해주세요.",
                        singleLine = true,
                        keyboardType = KeyboardType.Number,
                        errorText = if (pageError) "페이지를 입력해주세요" else null,
                    )
                    AddCardField(
                        label = "메모",
                        required = false,
                        value = memo,
                        onValueChange = { if (it.length <= memoMax) memo = it },
                        placeholder = "메모를 입력해주세요.",
                        maxLength = memoMax,
                    )
                }
            }
        }

        val submitting = remember { mutableStateOf(false) }
        LaunchedEffect(isLoading) { if (!isLoading) submitting.value = false }

        val submit = submit@{
            if (submitting.value) return@submit
            var valid = true
            if (mode == AddCardMode.TEXT && quote.isBlank()) {
                quoteError = true
                valid = false
            }
            if (page.isBlank()) {
                pageError = true
                valid = false
            }
            if (valid) {
                submitting.value = true
                onSubmit(page.toIntOrNull() ?: 0, quote, memo)
            }
        }

        if (isEdit) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (hasChanges) BookiiBookiiTheme.colors.grey900 else BookiiBookiiTheme.colors.grey200)
                    .then(if (hasChanges) Modifier.clickable(onClick = submit) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "수정",
                    style = BookiiBookiiTheme.typography.medium16,
                    color = if (hasChanges) BookiiBookiiTheme.colors.white else BookiiBookiiTheme.colors.grey500,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BookiiBookiiTheme.colors.grey200)
                        .clickable { showPreview = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "미리보기",
                        style = BookiiBookiiTheme.typography.medium16,
                        color = BookiiBookiiTheme.colors.grey700,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BookiiBookiiTheme.colors.grey900)
                        .clickable(onClick = submit),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "등록하기",
                        style = BookiiBookiiTheme.typography.medium16,
                        color = BookiiBookiiTheme.colors.white,
                    )
                }
            }
        }
    }

    if (showPreview) {
        LibraryCardPreviewDialog(
            mode = mode,
            quote = quote,
            memo = memo,
            onDismiss = { showPreview = false },
            imageUri = selectedImageUri ?: initialImageUrl?.let(android.net.Uri::parse),
            bookTitle = bookTitle.stripBookSubtitle(),
            bookAuthor = currentNickname,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddCardField(
    label: String,
    required: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int? = null,
    errorText: String? = null,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.bringIntoViewRequester(bringIntoViewRequester)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = label, style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.grey900)
                if (required) {
                    Text(text = " *", style = BookiiBookiiTheme.typography.medium16, color = BookiiBookiiTheme.colors.uiPointRed)
                }
            }
            if (maxLength != null) {
                Text(
                    text = "${value.length}/$maxLength",
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.grey400,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = BookiiBookiiTheme.typography.regular15.copy(color = BookiiBookiiTheme.colors.grey900),
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (singleLine) Modifier.height(54.dp) else Modifier)
                .clip(RoundedCornerShape(20.dp))
                .background(BookiiBookiiTheme.colors.white)
                .border(
                    width = 1.dp,
                    color = if (errorText != null) BookiiBookiiTheme.colors.uiPointRed else BookiiBookiiTheme.colors.grey300,
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .onFocusEvent { state ->
                    if (state.isFocused) {
                        scope.launch {
                            delay(300)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = BookiiBookiiTheme.typography.regular15, color = BookiiBookiiTheme.colors.grey500)
                    }
                    inner()
                }
            },
        )
        if (errorText != null) {
            Text(
                text = errorText,
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.uiPointRed,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LibraryAddCardTextPreview() {
    BookiiPreview {
        LibraryAddCardScreen(mode = AddCardMode.TEXT)
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LibraryAddCardPhotoPreview() {
    BookiiPreview {
        LibraryAddCardScreen(mode = AddCardMode.PHOTO)
    }
}
