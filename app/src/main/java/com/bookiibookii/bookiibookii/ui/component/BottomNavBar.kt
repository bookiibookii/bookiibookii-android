package com.bookiibookii.bookiibookii.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.R
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private val NavContainerShape = RoundedCornerShape(999.dp)
private val NavItemShape = RoundedCornerShape(999.dp)

@Composable
fun BottomNavBar(
    groupSelected: Boolean,
    trackerSelected: Boolean,
    librarySelected: Boolean,
    onGroupClick: () -> Unit,
    onTrackerClick: () -> Unit,
    onLibraryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = NavContainerShape)
            .background(BookiiBookiiTheme.colors.white, NavContainerShape)
            .border(1.dp, BookiiBookiiTheme.colors.grey100, NavContainerShape)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItem(
            iconRes = R.drawable.ic_group_32,
            label = "탐색",
            selected = groupSelected,
            onClick = onGroupClick,
        )
        BottomNavItem(
            iconRes = R.drawable.ic_tracker_32,
            label = "트래커",
            selected = trackerSelected,
            onClick = onTrackerClick,
        )
        BottomNavItem(
            iconRes = R.drawable.ic_book_32,
            label = "서재",
            selected = librarySelected,
            onClick = onLibraryClick,
        )
    }
}

@Composable
private fun BottomNavItem(
    iconRes: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(80.dp)
            .height(60.dp)
            .background(
                color = if (selected) BookiiBookiiTheme.colors.grey100 else BookiiBookiiTheme.colors.white,
                shape = NavItemShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
            if (!selected) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    style = BookiiBookiiTheme.typography.medium11,
                    color = BookiiBookiiTheme.colors.grey900,
                )
            }
        }
    }
}

@Preview(name = "바텀 내비게이션 - 탐색 선택", showBackground = true)
@Composable
private fun BottomNavBarGroupSelectedPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            BottomNavBar(
                groupSelected = true,
                trackerSelected = false,
                librarySelected = false,
                onGroupClick = {},
                onTrackerClick = {},
                onLibraryClick = {},
            )
        }
    }
}

@Preview(name = "바텀 내비게이션 - 트래커 선택", showBackground = true)
@Composable
private fun BottomNavBarTrackerSelectedPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            BottomNavBar(
                groupSelected = false,
                trackerSelected = true,
                librarySelected = false,
                onGroupClick = {},
                onTrackerClick = {},
                onLibraryClick = {},
            )
        }
    }
}

@Preview(name = "바텀 내비게이션 - 서재 선택", showBackground = true)
@Composable
private fun BottomNavBarLibrarySelectedPreview() {
    BookiiPreview {
        Box(modifier = Modifier.padding(16.dp)) {
            BottomNavBar(
                groupSelected = false,
                trackerSelected = false,
                librarySelected = true,
                onGroupClick = {},
                onTrackerClick = {},
                onLibraryClick = {},
            )
        }
    }
}
