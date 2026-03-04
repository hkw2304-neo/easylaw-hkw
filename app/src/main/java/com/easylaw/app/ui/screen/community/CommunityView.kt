package com.easylaw.app.ui.screen.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.data.models.CommunityWriteModel
import com.easylaw.app.ui.components.CommonFilterCategory
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.util.Common
import com.easylaw.app.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityView(
    modifier: Modifier,
    viewModel: CommunityViewModel,
    communityWrite: () -> Unit,
) {
    val viewState by viewModel.communityState.collectAsState()

    // LaunchedEffect : 화면이 재생성됨에 따라 실행
//    LaunchedEffect(Unit) {
//        viewModel.loadCommunityLists()
//    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "커뮤니티",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Text(
                    text = "나와 비슷한 상황의 사람들과 이야기를 나눠보세요.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                )

                Spacer(modifier = Modifier.height(10.dp))
                CommonFilterCategory(
                    category = viewState.categoryList,
                    selectedCategory = viewState.selectedCategory,
                    onCategorySelected = { it ->
                        viewModel.onCategorySelected(it)
                    },
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            PullToRefreshBox(
                isRefreshing = viewState.isCommunityListLoading,
                onRefresh = {
                    viewModel.onCategorySelected(viewState.selectedCategory)
                },
                modifier = Modifier.weight(1f),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(viewState.communityList) { item ->
                        CommunityPostItem(item = item)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { communityWrite() },
            containerColor = Color(0xFF2196F3),
            contentColor = Color.White,
            shape = CircleShape,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "글쓰기")
        }
        if (viewState.isCommunityListLoading) {
            CommonIndicator(title = "불러오는 중입니다...")
        }
    }
}

@Composable
fun CommunityPostItem(item: CommunityWriteModel) {
    val formattedDate = Common.formatIsoDate(item.created_at)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {}
                .padding(horizontal = 24.dp, vertical = 10.dp), // 시원한 여백
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.category,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3182F6), // 토스 블루
            )

            Text(text = "·", color = Color(0xFFB0B8C1))

            Text(
                text = if (item.author.isNotEmpty()) "${item.author} 님" else "익명",
                fontSize = 12.sp,
                color = Color(0xFF4E5968), // 토스 그레이 600
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = Color(0xFF8B95A1),
            )
        }

        Text(
            text = item.title,
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191F28),
        )

        Text(
            text = item.content,
            modifier = Modifier.padding(top = 4.dp),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = Color(0xFF4E5968),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
