package com.easylaw.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class) // stickyHeader 사용을 위함
@Composable
fun <T> CommonDataGrid(
    modifier: Modifier = Modifier,
    items: List<T>,
    headers: List<String>,
    columnWidth: Dp = 110.dp,
    selectedSet: Set<T> = emptySet(),
    onItemClick: (T) -> Unit,
    onTotalClick: () -> Unit,
    itemContent: @Composable RowScope.(index: Int, item: T) -> Unit,
) {
    val horizontalScrollState = rememberScrollState()
    val isTotalChecked = items.isNotEmpty() && items.size == selectedSet.size

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E8EB)),
    ) {
        // 전체를 가로 스크롤로 감쌉니다.
        Box(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // 1. 고정 헤더 영역 (stickyHeader)
                stickyHeader {
                    Column(modifier = Modifier.background(Color.White)) {
                        Row(
                            modifier = Modifier.background(Color(0xFFF9FAFB)),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // 전체 선택 체크박스
                            Box(
                                modifier = Modifier.width(50.dp).padding(8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Checkbox(
                                    checked = isTotalChecked,
                                    onCheckedChange = { onTotalClick() },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3182F6)),
                                )
                            }

                            // 헤더 텍스트들
                            headers.forEach { headerTitle ->
                                TableCell(headerTitle, columnWidth, isHeader = true)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFE5E8EB))
                    }
                }

                // 2. 데이터 바디 영역 (Lazy 연산)
                itemsIndexed(items) { index, item ->
                    val isSelected = selectedSet.contains(item)

                    Row(
                        modifier =
                            Modifier
                                .background(if (isSelected) Color(0xFFE8F3FF) else Color.White)
                                .clickable { onItemClick(item) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 개별 체크박스
                        Box(
                            modifier = Modifier.width(50.dp).padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onItemClick(item) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3182F6)),
                            )
                        }

                        // 외부에서 넘겨받은 셀 내용들을 여기서 그려줌
                        itemContent(index, item)
                    }
                    HorizontalDivider(color = Color(0xFFF2F4F6))
                }
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
) {
    Text(
        text = text,
        modifier =
            Modifier
                .width(width)
                .padding(vertical = 14.dp, horizontal = 8.dp),
        style =
            TextStyle(
                fontSize = if (isHeader) 13.sp else 13.sp,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                color = if (isHeader) Color(0xFF191F28) else Color(0xFF4E5968),
                textAlign = TextAlign.Center,
            ),
        maxLines = 1,
    )
}
