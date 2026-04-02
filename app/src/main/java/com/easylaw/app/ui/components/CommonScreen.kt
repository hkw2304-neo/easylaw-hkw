package com.easylaw.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CommonScreen(
    modifier: Modifier,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(10.dp),
//        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content()
    }
}
