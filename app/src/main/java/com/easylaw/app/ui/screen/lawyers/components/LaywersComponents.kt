package com.easylaw.app.ui.screen.lawyers.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.easylaw.app.data.models.lawer.LawyersModel
import com.easylaw.app.ui.components.CommonDataGrid
import com.easylaw.app.ui.components.TableCell

@Composable
fun LaywersDialog(
    title: String,
    desc: String? = null,
    icon: ImageVector? = null,
    onDismiss: () -> Unit,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    confirmText: String,
    lawyersList: List<LawyersModel>,
    onGridCellClick: (LawyersModel) -> Unit,
    selectedSet: Set<LawyersModel>,
    toggleSelectedTotalChecked: () -> Unit,
) {
    val columnWidth = 110.dp

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
        ) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (icon != null) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color(0xFF3182F6),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                        Text(
                            text = title,
                            style =
                                TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF191F28),
                                ),
                        )
                    }
                },
                bottomBar = {
                    // 하단에 고정될 버튼 영역
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(bottom = 20.dp)
                                .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        if (desc != null) {
                            //                        Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = desc,
                                textAlign = TextAlign.Center,
                                style =
                                    TextStyle(
                                        fontSize = 15.sp,
                                        color = Color(0xFF4E5968),
                                        lineHeight = 22.sp,
                                    ),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (dismissText != null) {
                                Button(
                                    onClick = onDismiss,
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(54.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFF2F4F6),
                                            contentColor = Color(0xFF4E5968),
                                        ),
                                ) {
                                    Text(text = dismissText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Button(
                                onClick = onConfirm,
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3182F6)),
                            ) {
                                Text(text = confirmText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                },
            ) { innerPadding ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(innerPadding)
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .padding(24.dp)
                                .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CommonDataGrid(
                            items = lawyersList,
                            selectedSet = selectedSet,
                            headers = listOf("순서", "이름", "전문분야", "기수", "출신대학", "경력", "지역"),
                            onItemClick = { lawyer -> onGridCellClick(lawyer) },
                            onTotalClick = { toggleSelectedTotalChecked() },
                        ) { index, lawyer ->
                            // 여기서 각 행의 셀 내용만 정의해주면 끝!
                            TableCell((index + 1).toString(), columnWidth)
                            TableCell(lawyer.name, columnWidth)
                            TableCell(lawyer.specialty, columnWidth)
                            TableCell(lawyer.barExamRound, columnWidth)
                            TableCell(lawyer.university, columnWidth)
                            TableCell("${lawyer.careerYears}년", columnWidth)
                            TableCell(lawyer.officeLocation, columnWidth)
//                            TableCell(lawyer.officeLocation, columnWidth)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LawyerHorizontalDetailItem(
    lawyer: LawyersModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFE8F3FF) else Color(0xFFF9FAFB),
        border =
            BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF3182F6) else Color(0xFFE5E8EB),
            ),
        modifier = modifier.width(280.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = lawyer.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${lawyer.gender} / ${lawyer.age}세", fontSize = 13.sp, color = Color(0xFF8B95A1))
            }

            HorizontalDivider(color = Color(0xFFF2F4F6))

            DetailRow(label = "⚖️ 전문분야", value = lawyer.specialty)
            DetailRow(label = "🎓 출신대학", value = lawyer.university)
            DetailRow(label = "📜 기수", value = lawyer.barExamRound)
            DetailRow(label = "💼 경력", value = "${lawyer.careerYears}년")
            DetailRow(label = "📍 위치", value = lawyer.officeLocation)
            DetailRow(label = "📞 연락처", value = lawyer.phoneNumber)
            DetailRow(label = "📧 이메일", value = lawyer.email)

            if (lawyer.isActive) {
                Text(
                    text = "● 활동 중",
                    color = Color(0xFF2DCA72),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// 출력 1 아이템
@Composable
fun DetailRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF4E5968))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF191F28))
    }
}
