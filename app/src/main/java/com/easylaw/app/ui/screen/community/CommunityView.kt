package com.easylaw.app.ui.screen.community

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.data.models.CommunityWriteModel
import com.easylaw.app.data.models.NaverNewsModel
import com.easylaw.app.data.models.NewsItem
import com.easylaw.app.ui.components.CommonDialog
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
    gotoDetail: (String) -> Unit,
) {
    val viewState by viewModel.communityState.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    onCategorySelected = { viewModel.onCategorySelected(it) },
                )
            }
            MonthlyAIInsightSection(
                viewState.keywords,
                viewState.aiErrorText,
                onBottomSheet = { keyword ->
                    viewModel.onBottomSheet(keyword)
                },
                showCounselorList = {
                    viewModel.showCounselorList()
                },
                showCounselor = viewState.showCounselor,
            )
            PullToRefreshBox(
                isRefreshing = viewState.isCommunityListLoading,
                onRefresh = { viewModel.onCategorySelected(viewState.selectedCategory) },
                modifier = Modifier.weight(1f),
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewState.communityList) { item ->
                        CommunityPostItem(
                            item = item,
                            gotoDetail = gotoDetail,
                        )
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
        if (viewState.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeBottomSheet() }, // 시트 닫기 요청 시 상태 변경
                sheetState = sheetState,
            ) {
                NewsListBottomSheetContent(
                    naverNewsItem = viewState.naverNewsItem,
                )
            }
        }
        if (viewState.isCommunityListLoading) {
            CommonIndicator(title = "잠시만 기다려주세요...")
        }
        if (viewState.aiErrorText.isNotEmpty()) {
            CommonDialog(
                title = "네이버 응답 에러",
                desc = viewState.aiErrorText,
                icon = Icons.Default.Close,
                onConfirm = { viewModel.closeShowDialog() },
            )
        }
    }
}

@Composable
fun MonthlyAIInsightSection(
    keyworkds: List<String>,
    aiErrorText: String,
    onBottomSheet: (String) -> Unit,
    showCounselorList: () -> Unit,
    showCounselor: Boolean,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        color = Color(0xFFF2F4F6),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            Text(
                text = "🤖   이달의 관심사",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191F28),
            )
            Spacer(modifier = Modifier.height(12.dp))
            AiKeywordSection(
                keywords = listOf("전세 보증금 반환 지연 대처법", "부당해고 부당징계 구제신청", "층간소음 누수 분쟁 해결"),
//                keywords = keyworkds,
                onBottomSheet = { keyworkd ->
                    onBottomSheet(keyworkd)
                },
            )
//            if (keyworkds.isEmpty()) {
//                DotLoadingText()
//
//            } else if (aiErrorText.isNotEmpty()) {
//                Text(
//                    text = aiErrorText, fontSize = 13.sp, color = Color(0xFF4E5968)
//                )
//            }

            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "⚖️  명예 상담사",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28),
                )
                Text(
                    text = if (!showCounselor) "전체보기" else "닫기",
                    fontSize = 13.sp,
                    color = Color(0xFF8B95A1),
                    modifier =
                        Modifier.clickable {
                            showCounselorList()
                        },
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (showCounselor) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 임시 데이터
                    val counselors = listOf("박변호사", "김노무사", "이법무사")
                    counselors.forEach { name ->
                        CounselorCard(name = name)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CommunityPostItem(
    item: CommunityWriteModel,
    gotoDetail: (String) -> Unit,
) {
    val formattedDate = Common.formatIsoDate(item.created_at)
    val commentCount = item.comments?.size ?: 0
    val isAttentionNeeded = commentCount >= 3

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    Log.d("id > ", item.id.toString())
                    gotoDetail(item.id.toString())
                }.padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.category,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3182F6),
            )

            Text(text = "·", color = Color(0xFFB0B8C1))

            Text(
                text = if (item.author.isNotEmpty()) "${item.author} 님" else "익명",
                fontSize = 12.sp,
                color = Color(0xFF4E5968),
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = Color(0xFF8B95A1),
            )
        }

        // 제목 영역
        Text(
            text = item.title,
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191F28),
        )

        // 내용 영역
        Text(
            text = item.content,
            modifier = Modifier.padding(top = 4.dp),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = Color(0xFF4E5968),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            val tintColor = if (isAttentionNeeded) Color(0xFFFF5F2E) else Color(0xFF8B95A1)

            Icon(
                imageVector = if (isAttentionNeeded) Icons.Filled.Whatshot else Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = tintColor,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$commentCount",
                fontSize = 13.sp,
                color = tintColor,
                fontWeight = if (isAttentionNeeded) FontWeight.Bold else FontWeight.Medium,
            )

            if (isAttentionNeeded) {
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    color = Color(0xFFFFF1ED),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        text = "많은 분이 답변을 했습니다.",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF5F2E),
                    )
                }
            }
        }
    }
}

@Composable
fun DotLoadingText() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotCount by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 4,
        typeConverter = Int.VectorConverter,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "dotCount",
    )

    val dots = ".".repeat(dotCount)

    Text(
        text = "AI가 게시글을 분석하고 있어요$dots",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF333D4B),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiKeywordSection(
    keywords: List<String>,
    onBottomSheet: (String) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 16.dp),
        // 상하 여백으로 입체감 확보
        horizontalArrangement = Arrangement.spacedBy(8.dp), // 간격을 padding 대신 배치로 조절
    ) {
        keywords.forEach { keyword ->
            Surface(
                onClick = { onBottomSheet(keyword) },
                shape = RoundedCornerShape(100.dp),
                color = Color(0xFFF2F8FF), // 아주 연한 푸른빛 배경
                border = BorderStroke(1.dp, Color(0xFFD0E3FF)), // 부드러운 푸른색 테두리
                shadowElevation = 2.dp, // 🌟 미세한 그림자로 "떠 있는" 느낌 추가
                modifier = Modifier.height(40.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    // 반짝이는 효과를 주는 아이콘으로 변경
                    Icon(
                        imageVector = Icons.Default.AutoAwesome, // 🌟 'AI 분석' 느낌을 주는 아이콘
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF3182F6),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = keyword,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B64D1), // 조금 더 깊은 블루
                        letterSpacing = (-0.3).sp, // 자간을 좁혀 응축된 느낌
                    )
                }
            }
        }
    }
}

@Composable
fun NewsListBottomSheetContent(naverNewsItem: NaverNewsModel) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .heightIn(min = 400.dp, max = 600.dp), // 적절한 높이 제한
    ) {
        Text(
            text = "추천 뉴스",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 10.dp, bottom = 20.dp),
        )

        if (naverNewsItem.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("관련 뉴스를 찾을 수 없습니다.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(naverNewsItem.items) { news ->
                    NewsItemRow(news)
                    HorizontalDivider(color = Color(0xFFF2F4F6), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun NewsItemRow(news: NewsItem) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { },
    ) {
        Text(
            text = news.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191F28),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = news.description,
            fontSize = 14.sp,
            color = Color(0xFF4E5968),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 20.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = news.pubDate,
            fontSize = 12.sp,
            color = Color(0xFF8B95A1),
        )
    }
}

@Composable
fun CounselorCard(name: String) {
    Surface(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF2F4F6)),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(Color(0xFFF2F8FF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = name.take(1), color = Color(0xFF3182F6), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Text(
                text = "답변 120+회",
                fontSize = 11.sp,
                color = Color(0xFF3182F6),
                modifier = Modifier.padding(top = 2.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // '상담하기' 버튼 느낌의 작은 칩
//            Surface(
//                shape = RoundedCornerShape(8.dp),
//                color = Color(0xFFF2F4F6),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(
//                    text = "프로필",
//                    fontSize = 11.sp,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(vertical = 4.dp),
//                    fontWeight = FontWeight.Medium
//                )
//            }
        }
    }
}
