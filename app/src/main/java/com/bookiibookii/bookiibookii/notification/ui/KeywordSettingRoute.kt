package com.bookiibookii.bookiibookii.notification.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookiibookii.bookiibookii.ui.component.showCustomToast
import com.bookiibookii.bookiibookii.notification.vm.KeywordSettingViewModel

private const val MAX_KEYWORD_COUNT = 10
private const val MAX_KEYWORD_LENGTH = 10

@Composable
fun KeywordSettingRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KeywordSettingViewModel = viewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    KeywordSettingScreen(
        keywordInput = uiState.keywordInput,
        keywords = uiState.keywords,
        onBackClick = onBackClick,
        onKeywordInputChange = viewModel::onInputChange,
        onAddKeyword = {
            val input = uiState.keywordInput.trim()
            when {
                input.isEmpty() -> Unit
                input.length > MAX_KEYWORD_LENGTH ->
                    context.showCustomToast("키워드는 최대 ${MAX_KEYWORD_LENGTH}자까지 입력할 수 있어요.", false)
                uiState.keywords.size >= MAX_KEYWORD_COUNT ->
                    context.showCustomToast("키워드는 최대 ${MAX_KEYWORD_COUNT}개까지만 등록 가능합니다.", false)
                uiState.keywords.any { it.keyword == input } ->
                    context.showCustomToast("이미 등록된 키워드입니다.", false)
                else -> viewModel.addKeyword(input) {
                    context.showCustomToast("키워드가 등록되었습니다", true)
                }
            }
        },
        onDeleteKeyword = viewModel::deleteKeyword,
        maxCount = MAX_KEYWORD_COUNT,
        modifier = modifier,
    )
}
