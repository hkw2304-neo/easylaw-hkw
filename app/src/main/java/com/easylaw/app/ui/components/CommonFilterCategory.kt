package com.easylaw.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.data.models.common.CategoryModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonFilterCategory(
    category: Map<String, CategoryModel>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    updateTemplateField: (() -> Unit)? = null,
) {
    val categoryKey = category.keys.toList()
    LazyRow(
//        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categoryKey) { itemKey ->
            val categoryName = category[itemKey]?.name ?: ""

//            Log.d("필터값",  "itemKey: ${ itemKey} / categoryName: ${categoryName} / selectedCategory: ${selectedCategory}")

            FilterChip(
                selected = (itemKey.trim() == selectedCategory.trim()),
                onClick = {
                    onCategorySelected(itemKey)
                    updateTemplateField?.invoke()
                },
                label = {
                    Text(
                        text = categoryName,
                        style =
                            TextStyle(
                                fontSize = 14.sp,
                                fontWeight = if (itemKey == selectedCategory) FontWeight.Bold else FontWeight.Medium,
                            ),
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors =
                    FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFFF2F4F6),
                        labelColor = Color(0xFF6B7684),
                        selectedContainerColor = Color(0xFFE8F3FF),
                        selectedLabelColor = Color(0xFF3182F6),
                    ),
                border = null,
                elevation = FilterChipDefaults.filterChipElevation(0.dp),
            )
        }
    }
}
