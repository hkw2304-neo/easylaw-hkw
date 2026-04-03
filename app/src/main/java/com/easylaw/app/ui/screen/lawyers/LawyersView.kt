package com.easylaw.app.ui.screen.lawyers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.easylaw.app.data.models.lawer.LaywersReserveReqModel
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.ui.components.CommonScreen
import com.easylaw.app.ui.screen.lawyers.components.LaywersDialog
import com.easylaw.app.util.Common
import com.easylaw.app.viewModel.lawyers.LawyersViewModel

@Composable
fun LawyersView(
    modifier: Modifier,
    viewModel: LawyersViewModel,
    goToReserve: (String) -> Unit,
    navController: NavHostController,
) {
    val viewState by viewModel.lawyersViewState.collectAsState()

    val refreshSignal =
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("refresh")
            ?.observeAsState()

    LaunchedEffect(refreshSignal?.value) {
        if (refreshSignal?.value == true) {
            viewModel.loadLawyers()
            // 한 번 불렀으면 신호 초기화 (중요)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh")
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    Color(0xFFF9FAFB),
                ),
    ) {
        CommonScreen(
            modifier = modifier,
            content = {
                stickyHeader {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    // 선형 그라데이션 (Linear Gradient)
                                    brush =
                                        Brush.verticalGradient(
                                            colors =
                                                listOf(
                                                    Color(0xFFFFFDF5),
                                                    Color(0xFFF7F1DE),
                                                ),
                                        ),
                                ).padding(20.dp),
                    ) {
                        SectionOne()
                    }
                }

                if (!viewState.isLoading && viewState.reserveList.isNotEmpty()) {
                    item {
                        ReserveCard(
                            items = viewState.reserveList,
                            showLaywersDialog = { viewModel.toggleLaywersDialog() },
                        )
                    }
                } else if (!viewState.isLoading && viewState.reserveList.isEmpty()) {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    // .fillParentAsState()
                                    .fillMaxWidth()
                                    .padding(vertical = 80.dp),
                            // 상하 여백을 줘서 중앙 느낌 유도
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description, // 또는 Outlined.Inbox 등
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color(0xFFD1D8DD), // 연한 회색으로 부담 없게
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "등록된 게시글이 없습니다.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E5968), // 토스 스타일의 진한 회색
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "첫 번째 게시글의 주인공이 되어보세요!",
                                    fontSize = 14.sp,
                                    color = Color(0xFF8B95A1), // 조금 더 연한 회색
                                )
                            }
                        }
                    }
                }
            },
        )
        FloatingActionButton(
            onClick = { goToReserve(viewState.userState.id) },
            containerColor = Color(0xFF2196F3),
            contentColor = Color.White,
            shape = CircleShape,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "상담신청")
        }

        if (viewState.isLoading) {
            CommonIndicator(title = "잠시만 기다려주세요...")
        }

        if (viewState.showLaywersDialog) {
            LaywersDialog(
                title = "변호사 명단",
                desc = "어느 분에게 요청을하시겠습니까?",
                icon = Icons.Default.PersonSearch,
                onConfirm = { },
                confirmText = "요청",
                onDismiss = { viewModel.toggleLaywersDialog() },
                dismissText = "닫기",
                lawyersList = viewState.laywersList,
                onGridCellClick = { currentId -> viewModel.onGridCellClick(currentId) },
                selectedSet = viewState.selectedIdSet,
                toggleSelectedTotalChecked = { viewModel.toggleSelectedTotalChecked() },
            )
        }
    }
}

@Composable
fun SectionOne() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "상담 신청",
                    style =
                        TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF191F28),
                            letterSpacing = (-0.5).sp,
                            shadow =
                                Shadow(
                                    color = Color.Black.copy(alpha = 0.05f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f,
                                ),
                        ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color(0xFFC4A484),
                    shape = RoundedCornerShape(100.dp),
                ) {
                    Text(
                        text = "분야별 전문 변호사",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFDF5),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "나의 상담 신청 내역",
                style =
                    TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFF4E5968),
                        fontWeight = FontWeight.Medium,
                    ),
            )
        }

        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, Color(0xFFF2F4F6)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Assignment, // 또는 대화 아이콘
                    contentDescription = null,
                    tint = Color(0xFFE8D5A1),
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
fun ReserveCard(
    items: List<LaywersReserveReqModel>,
    showLaywersDialog: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth(),
    ) {
        Spacer(
            modifier = Modifier.height(9.dp),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "신청 건수: ${items.size} 건",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333D4B),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusIndicator(color = Color(0xFFB0B8C1), text = "대기") // 회색
                Spacer(modifier = Modifier.width(8.dp))
                StatusIndicator(color = Color(0xFF2DB400), text = "접수") // 초록
                Spacer(modifier = Modifier.width(8.dp))
                StatusIndicator(color = Color(0xFF1B64DA), text = "완료") // 파랑
            }
        }

        items.forEach { item ->
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            showLaywersDialog()
                        },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFF2F4F6)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    // 1. 상단: 카테고리와 날짜
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Surface(
                            color = Color(0xFFF2F8FF),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                text = item.selectedCategoryName,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B64DA), // 신뢰감 있는 블루
                            )
                        }

                        Text(
                            text = Common.formatIsoDate(item.createdAt),
                            fontSize = 12.sp,
                            color = Color(0xFF8B95A1),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. 중단: 제목 (상담 요약)
                    Text(
                        text = item.detailTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191F28),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle, // Material Icons 추가 필요
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFB0B8C1), // 진행 상태 표시용 그린
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "상담 접수 대기",
                            fontSize = 12.sp,
                            color = Color(0xFF4E5968),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(
    color: Color,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // 작은 동그라미 원
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(color = color, shape = CircleShape),
        )
        Spacer(modifier = Modifier.width(4.dp))
        // 상태 텍스트
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color(0xFF8B95A1),
            fontWeight = FontWeight.Medium,
        )
    }
}
