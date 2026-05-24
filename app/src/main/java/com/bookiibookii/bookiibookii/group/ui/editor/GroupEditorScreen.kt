package com.bookiibookii.bookiibookii.group.ui.editor

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.heightIn
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.data.model.group.BookItem
import com.bookiibookii.bookiibookii.group.model.ExchangeType
import com.bookiibookii.bookiibookii.group.model.GroupEditorUiState
import com.bookiibookii.bookiibookii.group.model.ReadingStyle
import com.bookiibookii.bookiibookii.group.model.SelectablePlace
import com.bookiibookii.bookiibookii.group.ui.component.BookSearchDropdown
import com.bookiibookii.bookiibookii.group.vm.GroupEditorViewModel
import com.bookiibookii.bookiibookii.ui.component.AddressButton
import com.bookiibookii.bookiibookii.ui.component.FooterButton
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

// 그룹 생성/수정 화면
@Composable
fun GroupEditorRoute(
    onBack: () -> Unit,
    onCreated: (Long?) -> Unit,
    viewModel: GroupEditorViewModel = viewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var submitError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is GroupEditorViewModel.Event.Created -> onCreated(event.groupId)
                is GroupEditorViewModel.Event.ShowError -> submitError = event.message
            }
        }
    }

    GroupEditorScreen(
        uiState = uiState,
        onBookSearchQueryChange = viewModel::onBookSearchQueryChange,
        onSearchBooks = viewModel::searchBooks,
        onClearBookSearch = viewModel::onClearBookSearch,
        onBookSelect = viewModel::onBookSelect,
        onGroupNameChange = viewModel::onGroupNameChange,
        onTradeTypeSelect = viewModel::onTradeTypeSelect,
        onPlaceSelect = viewModel::onPlaceSelect,
        onReadingPeriodSelect = viewModel::onReadingPeriodSelect,
        onRuleStyleSelect = viewModel::onRuleStyleSelect,
        onGroupCommentChange = viewModel::onGroupCommentChange,
        onAddCustomRule = viewModel::onAddCustomRule,
        onCustomRuleChange = viewModel::onCustomRuleChange,
        onRemoveCustomRule = viewModel::onRemoveCustomRule,
        onBack = onBack,
        onSubmit = {
            submitError = null
            viewModel.createGroup()
        },
        submitError = submitError,
    )
}

// 그룹 생성/수정 화면 (stateless: 상태·콜백을 파라미터로 받음)
@Composable
fun GroupEditorScreen(
    uiState: GroupEditorUiState,
    onBookSearchQueryChange: (String) -> Unit,
    onSearchBooks: () -> Unit,
    onClearBookSearch: () -> Unit,
    onBookSelect: (BookItem) -> Unit,
    onGroupNameChange: (String) -> Unit,
    onTradeTypeSelect: (ExchangeType) -> Unit,
    onPlaceSelect: (Long) -> Unit,
    onReadingPeriodSelect: (Int) -> Unit,
    onRuleStyleSelect: (ReadingStyle) -> Unit,
    onGroupCommentChange: (String) -> Unit,
    onAddCustomRule: () -> Unit,
    onCustomRuleChange: (Int, String) -> Unit,
    onRemoveCustomRule: (Int) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    submitError: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookiiBookiiTheme.colors.white),
    ) {
        GroupEditorHeader(
            title = "그룹 만들기",
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BookSearchSection(
                    query = uiState.bookSearchQuery,
                    onQueryChange = onBookSearchQueryChange,
                    onSearchClick = onSearchBooks,
                    onClearClick = onClearBookSearch,
                    results = uiState.bookSearchResults,
                    onBookSelect = onBookSelect,
                    error = uiState.bookSearchError,
                )
                GroupNameSection(
                    value = uiState.groupName,
                    onValueChange = onGroupNameChange,
                )
            }
            SectionDivider()
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ExchangeTypeSection(
                    selected = uiState.tradeType,
                    onSelect = onTradeTypeSelect,
                )
                uiState.tradeType?.let { type ->
                    AddressSection(
                        tradeType = type,
                        places = uiState.places,
                        selectedPlaceId = uiState.selectedPlaceId,
                        onPlaceSelect = onPlaceSelect,
                    )
                }
            }
            SectionDivider()
            ReadingPeriodSection(
                selectedIndex = uiState.readingPeriodIndex,
                onSelect = onReadingPeriodSelect,
            )
            SectionDivider()
            GroupRuleSection(
                selectedRule = uiState.ruleStyle,
                onRuleSelect = onRuleStyleSelect,
                customRules = uiState.customRules,
                onAddCustomRule = onAddCustomRule,
                onCustomRuleChange = onCustomRuleChange,
                onRemoveCustomRule = onRemoveCustomRule,
            )
            SectionDivider()
            GroupIntroSection(
                value = uiState.groupComment,
                onValueChange = onGroupCommentChange,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookiiBookiiTheme.colors.white)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (submitError != null) {
                Text(
                    text = submitError,
                    style = BookiiBookiiTheme.typography.regular14,
                    color = BookiiBookiiTheme.colors.uiPointRed,
                )
            }
            FooterButton(
                text = "그룹 만들기",
                onClick = onSubmit,
                enabled = uiState.canSubmit && !uiState.submitting,
            )
        }
    }
}

@Composable
private fun GroupEditorHeader(
    title: String,
    onBack: () -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(BookiiBookiiTheme.colors.white)
                .padding(horizontal = 16.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = BookiiBookiiTheme.colors.grey900,
                )
            }
            Text(
                text = title,
                style = BookiiBookiiTheme.typography.medium20,
                color = BookiiBookiiTheme.colors.grey900,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BookiiBookiiTheme.colors.grey200),
        )
    }
}

// 입력 섹션 공통 라벨
@Composable
private fun FieldLabel(
    text: String,
    required: Boolean = false,
) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = BookiiBookiiTheme.typography.medium16,
            color = BookiiBookiiTheme.colors.grey900,
        )
        if (required) {
            Text(
                text = "*",
                style = BookiiBookiiTheme.typography.medium16,
                color = BookiiBookiiTheme.colors.uiMain,
            )
        }
    }
}

// 도서 검색 섹션
@Composable
private fun BookSearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearClick: () -> Unit,
    results: List<BookItem>,
    onBookSelect: (BookItem) -> Unit,
    error: String? = null,
) {
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    Column {
        FieldLabel(text = "도서 검색", required = true)
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { fieldSize = it }
                    .clip(BookiiBookiiTheme.shape.round16)
                    .background(BookiiBookiiTheme.colors.white)
                    .border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey300,
                        shape = BookiiBookiiTheme.shape.round16,
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "검색",
                    tint = BookiiBookiiTheme.colors.grey500,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onSearchClick() },
                )
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = BookiiBookiiTheme.typography.regular16.copy(
                        color = BookiiBookiiTheme.colors.grey900,
                    ),
                    cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchClick() }),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = "어떤 책을 읽어볼까요?",
                                    style = BookiiBookiiTheme.typography.regular16,
                                    color = BookiiBookiiTheme.colors.grey500,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "초기화",
                    tint = BookiiBookiiTheme.colors.black,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onClearClick() },
                )
            }
            if (results.isNotEmpty()) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(
                        x = 0,
                        y = fieldSize.height + with(density) { 8.dp.roundToPx() },
                    ),
                    onDismissRequest = {},
                    properties = PopupProperties(focusable = false),
                ) {
                    BookSearchDropdown(
                        books = results,
                        onBookClick = onBookSelect,
                        modifier = Modifier
                            .width(with(density) { fieldSize.width.toDp() })
                            .heightIn(max = 320.dp),
                    )
                }
            }
        }
        if (error != null && results.isEmpty()) {
            Text(
                text = error,
                style = BookiiBookiiTheme.typography.regular14,
                color = BookiiBookiiTheme.colors.uiPointRed,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

// 그룹명 섹션
@Composable
private fun GroupNameSection(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column {
        FieldLabel(text = "그룹명", required = true)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = BookiiBookiiTheme.typography.regular16.copy(
                color = BookiiBookiiTheme.colors.grey900,
            ),
            cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
            modifier = Modifier
                .fillMaxWidth()
                .clip(BookiiBookiiTheme.shape.round16)
                .background(BookiiBookiiTheme.colors.white)
                .border(
                    width = 1.dp,
                    color = BookiiBookiiTheme.colors.grey300,
                    shape = BookiiBookiiTheme.shape.round16,
                )
                .padding(16.dp),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.weight(1f)) {  // placeholder 겹치기는 Box 유지
                        if (value.isEmpty()) {
                            Text(
                                text = "그룹명을 입력해주세요",
                                style = BookiiBookiiTheme.typography.regular16,
                                color = BookiiBookiiTheme.colors.grey500,
                            )
                        }
                        innerTextField()
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_x),
                        contentDescription = null,
                        tint = BookiiBookiiTheme.colors.black,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onValueChange("") },
                    )
                }
            },
        )
    }
}

// 교환 유형 섹션. 단일 선택
@Composable
private fun ExchangeTypeSection(
    selected: ExchangeType?,
    onSelect: (ExchangeType) -> Unit,
) {
    Column {
        FieldLabel(text = "교환 유형", required = true)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ExchangeTypeCard(
                title = "택배 교환",
                description = "책을 택배로 교환해요",
                selected = selected == ExchangeType.DELIVERY,
                onClick = { onSelect(ExchangeType.DELIVERY) },
                modifier = Modifier.weight(1f),
            )
            ExchangeTypeCard(
                title = "직접 교환",
                description = "책을 직접 만나서 교환해요",
                selected = selected == ExchangeType.DIRECT,
                onClick = { onSelect(ExchangeType.DIRECT) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ExchangeTypeCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = BookiiBookiiTheme.shape.round20
    val containerColor = if (selected) {
        BookiiBookiiTheme.colors.uiMainPale
    } else {
        BookiiBookiiTheme.colors.white
    }
    val borderColor = if (selected) {
        BookiiBookiiTheme.colors.uiMain150
    } else {
        BookiiBookiiTheme.colors.grey200
    }
    val titleColor = if (selected) {
        BookiiBookiiTheme.colors.uiMain
    } else {
        BookiiBookiiTheme.colors.grey600
    }
    val descriptionColor = if (selected) {
        BookiiBookiiTheme.colors.uiMain
    } else {
        BookiiBookiiTheme.colors.grey500
    }
    Column(
        modifier = modifier
            .height(76.dp)
            .clip(shape)
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = title,
            style = BookiiBookiiTheme.typography.medium16,
            color = titleColor,
        )
        Text(
            text = description,
            style = BookiiBookiiTheme.typography.regular12,
            color = descriptionColor,
        )
    }
}

// 주소 섹션. 주소가 없으면 등록 안내, 있으면 카드 1~2개 중 선택
@Composable
private fun AddressSection(
    tradeType: ExchangeType,
    places: List<SelectablePlace>,
    selectedPlaceId: Long?,
    onPlaceSelect: (Long) -> Unit,
) {
    if (places.isEmpty()) {
        AddressEmpty(tradeType = tradeType)
    } else {
        AddressList(
            tradeType = tradeType,
            places = places,
            selectedPlaceId = selectedPlaceId,
            onPlaceSelect = onPlaceSelect,
        )
    }
}

private fun addressLabel(tradeType: ExchangeType) = when (tradeType) {
    ExchangeType.DIRECT -> "희망 교환 장소"
    ExchangeType.DELIVERY -> "배송지"
}

// 주소 미등록: 등록 안내 placeholder + 주소지 관리 링크
@Composable
private fun AddressEmpty(tradeType: ExchangeType) {
    val placeholder = when (tradeType) {
        ExchangeType.DIRECT -> "희망 교환 장소를 등록해주세요"
        ExchangeType.DELIVERY -> "배송지를 등록해주세요"
    }
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            FieldLabel(text = addressLabel(tradeType), required = true)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(BookiiBookiiTheme.shape.round16)
                    .background(BookiiBookiiTheme.colors.grey200)
                    .border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey300,
                        shape = BookiiBookiiTheme.shape.round16,
                    )
                    .clickable { /* TODO: 주소 관리 화면 이동 */ }
                    .padding(16.dp),
            ) {
                Text(
                    text = placeholder,
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey400,
                )
            }
        }
        Text(
            text = "주소지 관리",
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey400,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { /* TODO: 주소 관리 화면 이동 */ },
        )
    }
}

// 주소 등록됨: 카드 1~2개 중 하나 선택
@Composable
private fun AddressList(
    tradeType: ExchangeType,
    places: List<SelectablePlace>,
    selectedPlaceId: Long?,
    onPlaceSelect: (Long) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FieldLabel(text = addressLabel(tradeType), required = true)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            places.forEach { place ->
                AddressButton(
                    title = place.placeName,
                    address = place.address,
                    selected = place.id == selectedPlaceId,
                    onClick = { onPlaceSelect(place.id) },
                )
            }
        }
    }
}

// 예상 독서 기간 섹션
@Composable
private fun ReadingPeriodSection(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val periods = GroupEditorUiState.PERIODS
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "예상 독서 기간",
                style = BookiiBookiiTheme.typography.medium16,
                color = BookiiBookiiTheme.colors.grey900,
            )
            Text(
                text = "${periods[selectedIndex]}일",
                style = BookiiBookiiTheme.typography.regular16,
                color = BookiiBookiiTheme.colors.uiMain,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReadingPeriodTrack(
                count = periods.size,
                selectedIndex = selectedIndex,
                onSelect = onSelect,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                periods.forEachIndexed { index, days ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = "${days}일",
                        style = if (isSelected) {
                            BookiiBookiiTheme.typography.medium14
                        } else {
                            BookiiBookiiTheme.typography.regular14
                        },
                        color = if (isSelected) {
                            BookiiBookiiTheme.colors.uiMain
                        } else {
                            BookiiBookiiTheme.colors.grey400
                        },
                        modifier = Modifier.clickable { onSelect(index) },
                    )
                }
            }
        }
    }
}

// 트랙 선택 지점까지 주황 채움 + 균등 배치 점. 점 탭 시 선택
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun ReadingPeriodTrack(
    count: Int,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val dotSize = 16.dp
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(dotSize),
    ) {
        val trackLength = maxWidth - dotSize
        val fraction = if (count <= 1) {
            0f
        } else {
            selectedIndex.toFloat() / (count - 1)
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = dotSize / 2)
                .fillMaxWidth()
                .height(6.dp)
                .background(BookiiBookiiTheme.colors.grey100),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = dotSize / 2)
                .width(trackLength * fraction)
                .height(6.dp)
                .background(BookiiBookiiTheme.colors.uiMain),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(count) { index ->
                val isSelected = index == selectedIndex
                val isFilled = index <= selectedIndex
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(BookiiBookiiTheme.shape.round50)
                        .background(
                            if (isFilled) {
                                BookiiBookiiTheme.colors.uiMain
                            } else {
                                BookiiBookiiTheme.colors.grey100
                            },
                        )
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(BookiiBookiiTheme.shape.round50)
                                .background(BookiiBookiiTheme.colors.white),
                        )
                    }
                }
            }
        }
    }
}

// 그룹 규칙 섹션
@Composable
private fun GroupRuleSection(
    selectedRule: ReadingStyle?,
    onRuleSelect: (ReadingStyle) -> Unit,
    customRules: List<String>,
    onAddCustomRule: () -> Unit,
    onCustomRuleChange: (Int, String) -> Unit,
    onRemoveCustomRule: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }
    val listShape = BookiiBookiiTheme.shape.round24
    val density = LocalDensity.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel(text = "그룹 규칙", required = true)
        // 드롭다운 필드 + 펼침 리스트 Popup
        Box(modifier = Modifier.fillMaxWidth()) {
            // 독서 스타일 드롭다운 (탭 시 펼침, 바깥 탭/뒤로가기로 닫힘)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .onSizeChanged { fieldSize = it }
                    .clip(BookiiBookiiTheme.shape.round20)
                    .background(BookiiBookiiTheme.colors.white)
                    .border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = BookiiBookiiTheme.shape.round20,
                    )
                    .clickable(enabled = !expanded) { expanded = true }
                    .padding(start = 20.dp, end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedRule?.label ?: "독서 스타일을 선택해주세요",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = if (selectedRule != null) {
                        BookiiBookiiTheme.colors.grey900
                    } else {
                        BookiiBookiiTheme.colors.grey400
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron),
                    contentDescription = null,
                    tint = BookiiBookiiTheme.colors.grey500,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (expanded) 90f else -90f),
                )
            }
            if (expanded) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(
                        x = 0,
                        y = fieldSize.height + with(density) { 8.dp.roundToPx() },
                    ),
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(focusable = true),
                ) {
                    Column(
                        modifier = Modifier
                            .width(with(density) { fieldSize.width.toDp() })
                            .shadow(elevation = 4.dp, shape = listShape)
                            .clip(listShape)
                            .background(BookiiBookiiTheme.colors.white)
                            .border(
                                width = 1.dp,
                                color = BookiiBookiiTheme.colors.grey200,
                                shape = listShape,
                            )
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        val styles = ReadingStyle.values()
                        styles.forEachIndexed { index, style ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onRuleSelect(style)
                                        expanded = false
                                    }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(BookiiBookiiTheme.shape.round50)
                                        .background(BookiiBookiiTheme.colors.uiMainPale),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(style.iconRes),
                                        contentDescription = null,
                                        tint = BookiiBookiiTheme.colors.uiMain,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Text(
                                    text = style.label,
                                    style = BookiiBookiiTheme.typography.regular14,
                                    color = BookiiBookiiTheme.colors.grey900,
                                )
                            }
                            if (index < styles.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(BookiiBookiiTheme.colors.grey100),
                                )
                            }
                        }
                    }
                }
            }
        }
        customRules.forEachIndexed { index, rule ->
            CustomRuleRow(
                value = rule,
                onValueChange = { onCustomRuleChange(index, it) },
                onRemove = { onRemoveCustomRule(index) },
            )
        }
        // 규칙 추가 버튼 (커스텀 규칙 4개면 숨김)
        if (customRules.size < GroupEditorUiState.MAX_CUSTOM_RULES) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(BookiiBookiiTheme.shape.round20)
                    .background(BookiiBookiiTheme.colors.grey100)
                    .border(
                        width = 1.dp,
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = BookiiBookiiTheme.shape.round20,
                    )
                    .clickable { onAddCustomRule() }
                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = null,
                    tint = BookiiBookiiTheme.colors.grey600,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "규칙 추가",
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.grey600,
                )
            }
        }
        Text(
            text = "최소 1개, 최대 5개까지 입력 가능합니다",
            style = BookiiBookiiTheme.typography.regular14,
            color = BookiiBookiiTheme.colors.grey500,
        )
    }
}

// 커스텀 규칙 입력 행
@Composable
private fun CustomRuleRow(
    value: String,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(BookiiBookiiTheme.shape.round20)
            .background(BookiiBookiiTheme.colors.white)
            .border(
                width = 1.dp,
                color = BookiiBookiiTheme.colors.grey200,
                shape = BookiiBookiiTheme.shape.round20,
            )
            .padding(start = 20.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = BookiiBookiiTheme.typography.regular16.copy(
                color = BookiiBookiiTheme.colors.grey900,
            ),
            cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "독서 스타일을 입력해주세요",
                            style = BookiiBookiiTheme.typography.regular16,
                            color = BookiiBookiiTheme.colors.grey400,
                        )
                    }
                    innerTextField()
                }
            },
        )
        Icon(
            painter = painterResource(R.drawable.ic_x),
            contentDescription = "규칙 삭제",
            tint = BookiiBookiiTheme.colors.grey500,
            modifier = Modifier
                .size(24.dp)
                .clickable { onRemove() },
        )
    }
}

// 그룹 소개 섹션
@Composable
private fun GroupIntroSection(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column {
        FieldLabel(text = "그룹 소개")
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = BookiiBookiiTheme.typography.regular16.copy(
                color = BookiiBookiiTheme.colors.grey900,
            ),
            cursorBrush = SolidColor(BookiiBookiiTheme.colors.uiMain),
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .clip(BookiiBookiiTheme.shape.round20)
                .background(BookiiBookiiTheme.colors.white)
                .border(
                    width = 1.dp,
                    color = BookiiBookiiTheme.colors.grey200,
                    shape = BookiiBookiiTheme.shape.round20,
                )
                .padding(20.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "게스트가 꼭 지켜야 할 규칙을 적어주세요.",
                            style = BookiiBookiiTheme.typography.regular16,
                            color = BookiiBookiiTheme.colors.grey500,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

// 섹션 구분선
@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BookiiBookiiTheme.colors.grey100),
    )
}

@Preview(widthDp = 412, heightDp = 917, showBackground = true)
@Composable
private fun GroupEditorScreenPreview() {
    BookiiPreview {
        GroupEditorScreen(
            uiState = GroupEditorUiState(),
            onBookSearchQueryChange = {},
            onSearchBooks = {},
            onClearBookSearch = {},
            onBookSelect = {},
            onGroupNameChange = {},
            onTradeTypeSelect = {},
            onPlaceSelect = {},
            onReadingPeriodSelect = {},
            onRuleStyleSelect = {},
            onGroupCommentChange = {},
            onAddCustomRule = {},
            onCustomRuleChange = { _, _ -> },
            onRemoveCustomRule = {},
            onBack = {},
            onSubmit = {},
        )
    }
}

// BookSearchSection 에러 상태 Preview
@Preview(widthDp = 360, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BookSearchSectionWithErrorPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            BookSearchSection(
                query = "살인자",
                onQueryChange = {},
                onSearchClick = {},
                onClearClick = {},
                results = emptyList(),
                onBookSelect = {},
                error = "네트워크 오류가 발생했어요",
            )
        }
    }
}
