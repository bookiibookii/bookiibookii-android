package com.bookiibookii.bookiibookii.group.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.bookiibookii.bookiibookii.ui.component.BottomSheetBtnStyle
import com.bookiibookii.bookiibookii.ui.component.BottomSheetChip
import com.bookiibookii.bookiibookii.ui.component.BottomSheetTwoBtnShort
import com.bookiibookii.bookiibookii.ui.component.bottomSheetTopShadow
import com.bookiibookii.bookiibookii.ui.preview.BookiiPreview
import com.bookiibookii.bookiibookii.ui.theme.BookiiBookiiTheme

private const val NATIONWIDE = "전국"
private const val ALL = "전체"

private data class City(val name: String, val districts: List<String>)

private val cities = listOf(
    City("서울", listOf(ALL, "강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구", "노원구", "도봉구", "동대문구", "동작구", "마포구", "서대문구", "서초구", "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구", "중구", "중랑구")),
    City("경기", listOf(ALL, "수원시", "성남시", "의정부시", "안양시", "부천시", "광명시", "평택시", "동두천시", "안산시", "고양시", "과천시", "구리시", "남양주시", "오산시", "시흥시", "군포시", "의왕시", "하남시", "용인시", "파주시", "이천시", "안성시", "김포시", "화성시", "광주시", "양주시", "포천시", "여주시", "연천군", "가평군", "양평군")),
    City("인천", listOf(ALL, "계양구", "미추홀구", "남동구", "동구", "부평구", "서구", "연수구", "중구", "강화군·옹진군")),
    City("대전", listOf(ALL, "대덕구", "동구", "서구", "유성구", "중구")),
    City("대구", listOf(ALL, "남구", "달서구", "동구", "북구", "서구", "수성구", "중구", "달성군", "군위군")),
    City("광주", listOf(ALL, "광산구", "남구", "동구", "북구", "서구")),
    City("울산", listOf(ALL, "남구", "동구", "북구", "중구", "울주군")),
    City("부산", listOf(ALL, "강서구", "금정구", "남구", "동구", "동래구", "부산진구", "북구", "사상구", "사하구", "서구", "수영구", "연제구", "영도구", "중구", "해운대구", "기장군")),
    City("세종", listOf(ALL, "세종특별자치시")),
    City("강원", listOf(ALL, "춘천시", "원주시", "강릉시", "동해시", "태백시", "속초시", "삼척시", "홍천군", "횡성군", "영월군", "평창군", "정선군", "철원군", "화천군", "양구군", "인제군", "고성군", "양양군")),
    City("충북", listOf(ALL, "청주시", "충주시", "제천시", "보은군", "옥천군", "영동군", "증평군", "진천군", "괴산군", "음성군", "단양군")),
    City("충남", listOf(ALL, "천안시", "공주시", "보령시", "아산시", "서산시", "논산시", "계룡시", "당진시", "금산군", "부여군", "서천군", "청양군", "홍성군", "예산군", "태안군")),
    City("전북", listOf(ALL, "전주시", "군산시", "익산시", "정읍시", "남원시", "김제시", "완주군", "진안군", "무주군", "장수군", "임실군", "순창군", "고창군", "부안군")),
    City("전남", listOf(ALL, "목포시", "여수시", "순천시", "나주시", "광양시", "담양군", "곡성군", "구례군", "고흥군", "보성군", "화순군", "장흥군", "강진군", "해남군", "영암군", "무안군", "함평군", "영광군", "장성군", "완도군", "진도군", "신안군")),
    City("경북", listOf(ALL, "포항시", "경주시", "김천시", "안동시", "구미시", "영주시", "영천시", "상주시", "문경시", "경산시", "의성군", "청송군", "영양군", "영덕군", "청도군", "고령군", "성주군", "칠곡군", "예천군", "봉화군", "울진군", "울릉군")),
    City("경남", listOf(ALL, "창원시", "진주시", "통영시", "사천시", "김해시", "밀양시", "거제시", "양산시", "의령군", "함안군", "창녕군", "고성군", "남해군", "하동군", "산청군", "함양군", "거창군", "합천군")),
    City("제주", listOf(ALL, "제주시", "서귀포시")),
)

// 구역 칩 토글: "전체"는 구체 구역과 상호배타, 선택은 항상 1개 이상 유지
private fun toggleDistrict(current: Set<String>, district: String): Set<String> {
    if (district == ALL) return setOf(ALL)
    val base = current - ALL
    val next = if (district in base) base - district else base + district
    return if (next.isEmpty()) setOf(ALL) else next
}

//   NATIONWIDE / 도시 미선택 -> [] (전체)
//   도시 + ALL -> (city 단일)
//   도시 + 특정 구 다중 -> (city + 구)
private fun toRegionTokens(
    selectedRegion: String,
    selectedDistricts: Set<String>,
    city: City?,
): List<String> {
    if (selectedRegion == NATIONWIDE || city == null) return emptyList()
    if (selectedDistricts.contains(ALL) || selectedDistricts.isEmpty()) return listOf(city.name)
    return city.districts
        .filter { it != ALL && it in selectedDistricts }
        .map { "${city.name} $it" }
}

//   빈 리스트 -> NATIONWIDE + ALL
//   ["서울"] -> 서울 + ALL
//   ["서울 강남구", "서울 송파구"] → 서울 + {강남구, 송파구}
private fun fromRegionTokens(tokens: List<String>): Pair<String, Set<String>> {
    if (tokens.isEmpty()) return NATIONWIDE to setOf(ALL)
    val first = tokens.first().substringBefore(' ')
    val city = cities.firstOrNull { it.name == first } ?: return NATIONWIDE to setOf(ALL)
    val districts = tokens
        .mapNotNull { it.removePrefix(city.name).trim().ifBlank { null } }
        .toSet()
    return if (districts.isEmpty()) city.name to setOf(ALL) else city.name to districts
}

private fun headSummary(
    selectedRegion: String,
    city: City?,
    selectedDistricts: Set<String>,
): String {
    if (selectedRegion == NATIONWIDE || city == null) return NATIONWIDE
    if (selectedDistricts.contains(ALL) || selectedDistricts.isEmpty()) return "${city.name} | $ALL"
    val ordered = city.districts.filter { it != ALL && it in selectedDistricts }
    val body = if (ordered.size <= 3) {
        ordered.joinToString(" · ")
    } else {
        ordered.take(3).joinToString(" · ") + " 외 ${ordered.size - 3}개"
    }
    return "${city.name} | $body"
}

// 지역 필터 칩 라벨. 비어있으면 호출 측에서 기본 라벨 사용
//   ["서울"] -> "서울 전체", ["서울 동작구"] -> "서울 동작구"
//   ["서울 동작구", ...] -> "서울 동작구 외 N"
internal fun regionChipLabel(regions: List<String>): String {
    val first = regions.first()
    return when {
        regions.size > 1 -> "$first 외 ${regions.size - 1}"
        !first.contains(' ') -> "$first 전체"
        else -> first
    }
}

// 지역 필터 바텀시트
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegionBottomSheet(
    initialRegions: List<String>,
    onApply: (List<String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val (initialCityName, initialDistricts) = remember(initialRegions) {
        fromRegionTokens(initialRegions)
    }
    var selectedRegion by remember { mutableStateOf(initialCityName) }
    var selectedDistricts by remember { mutableStateOf(initialDistricts) }

    val currentCity = cities.firstOrNull { it.name == selectedRegion }

    val blockSheetDragConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset = available

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity = available
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) { detectVerticalDragGestures { _, _ -> } }
            .bottomSheetTopShadow(cornerRadius = 20.dp)
            .background(color = BookiiBookiiTheme.colors.white, shape = sheetShape)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 44.dp, height = 4.dp)
                    .background(
                        color = BookiiBookiiTheme.colors.grey200,
                        shape = BookiiBookiiTheme.shape.round50,
                    ),
            )
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "지역",
                    style = BookiiBookiiTheme.typography.semibold20,
                    color = BookiiBookiiTheme.colors.grey900,
                )
                Text(
                    text = headSummary(selectedRegion, currentCity, selectedDistricts),
                    style = BookiiBookiiTheme.typography.regular16,
                    color = BookiiBookiiTheme.colors.uiMain,
                    maxLines = 1,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .nestedScroll(blockSheetDragConnection),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                RegionItem(
                    text = NATIONWIDE,
                    selected = selectedRegion == NATIONWIDE,
                    onClick = { selectedRegion = NATIONWIDE },
                )
                cities.forEach { city ->
                    RegionItem(
                        text = city.name,
                        selected = selectedRegion == city.name,
                        onClick = {
                            selectedRegion = city.name
                            selectedDistricts = setOf(ALL)
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
            ) {
                if (currentCity == null) {
                    BottomSheetChip(text = NATIONWIDE, selected = true, onClick = {})
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 4,
                    ) {
                        currentCity.districts.forEach { district ->
                            BottomSheetChip(
                                text = district,
                                selected = district in selectedDistricts,
                                onClick = {
                                    selectedDistricts = toggleDistrict(selectedDistricts, district)
                                },
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BottomSheetTwoBtnShort(
                text = "취소",
                style = BottomSheetBtnStyle.White,
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            )
            BottomSheetTwoBtnShort(
                text = "적용",
                style = BottomSheetBtnStyle.Dark,
                onClick = {
                    onApply(toRegionTokens(selectedRegion, selectedDistricts, currentCity))
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// 좌측 지역 목록 아이템. 우측 라인 구분선 덧칠
@Composable
private fun RegionItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lineColor = if (selected) {
        BookiiBookiiTheme.colors.uiMain
    } else {
        BookiiBookiiTheme.colors.grey200
    }
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .drawBehind {
                val x = size.width
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(start = 4.dp, end = 28.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Text(
            text = text,
            style = if (selected) {
                BookiiBookiiTheme.typography.medium16
            } else {
                BookiiBookiiTheme.typography.regular16
            },
            color = if (selected) {
                BookiiBookiiTheme.colors.uiMain
            } else {
                BookiiBookiiTheme.colors.grey900
            },
            maxLines = 1,
        )
    }
}

@Preview(widthDp = 412, showBackground = true)
@Composable
private fun RegionBottomSheetPreview() {
    BookiiPreview {
        RegionBottomSheet(
            initialRegions = emptyList(),
            onApply = {},
            onCancel = {},
        )
    }
}
