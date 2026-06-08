package com.bookiibookii.bookiibookii.notification.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bookiibookii.bookiibookii.notification.model.KeywordUiModel
import com.bookiibookii.bookiibookii.notification.ui.KeywordSettingScreen
import com.bookiibookii.bookiibookii.notification.ui.NotificationRoute

@Composable
fun NotificationNavHost(
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = NotificationDestinations.MAIN,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(NotificationDestinations.MAIN) {
            NotificationRoute(
                onBackClick = { if (!navController.popBackStack()) onExit() },
                onAddClick = { navController.navigate(NotificationDestinations.KEYWORD_SETTING) },
            )
        }
        composable(NotificationDestinations.KEYWORD_SETTING) {
            // TODO: 키워드 목록/등록/삭제는 추후 API 연동 — 현재는 로컬 상태로만 동작
            var keywordInput by rememberSaveable { mutableStateOf("") }
            val keywords = remember { mutableStateListOf<KeywordUiModel>() }
            var nextId by rememberSaveable { mutableStateOf(0L) }
            KeywordSettingScreen(
                keywordInput = keywordInput,
                keywords = keywords,
                onBackClick = { if (!navController.popBackStack()) onExit() },
                onKeywordInputChange = { keywordInput = it },
                onAddKeyword = {
                    val text = keywordInput.trim()
                    if (text.isNotEmpty() && keywords.none { it.keyword == text }) {
                        keywords.add(KeywordUiModel(id = nextId, keyword = text))
                        nextId += 1
                        keywordInput = ""
                    }
                },
                onDeleteKeyword = { keywords.remove(it) },
            )
        }
    }
}
