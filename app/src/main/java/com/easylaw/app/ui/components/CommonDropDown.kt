package com.easylaw.app.ui.components
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.data.models.common.CategoryModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropDown(
    title: String,
    desc: String = "",
    onExpandedChange: () -> Unit,
    expanded: Boolean = false,
    enabled: Boolean = true,
    categories: List<CategoryModel>,
    onValueChange: (String) -> Unit,
    selectedCategory: String,
    onClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(text = title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191F28)))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "*",
                color = Color.Red,
            )
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { onExpandedChange() },
        ) {
            OutlinedTextField(
                value = selectedCategory.takeIf { it.isNotEmpty() } ?: desc,
                onValueChange = { categoryName ->
                    onValueChange(categoryName)
                },
                enabled = enabled,
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3182F6),
                        unfocusedBorderColor = Color(0xFFF2F4F6),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                shape = RoundedCornerShape(16.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    Log.d("드롭다운 클릭", "치ㅑ차!!!!!!!")
                    onExpandedChange()
                },
                modifier = Modifier.background(Color.White),
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = category.name,
                                style = TextStyle(fontSize = 16.sp, color = Color(0xFF4E5968)),
                            )
                        },
                        onClick = {
                            onClick(category.name)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}
