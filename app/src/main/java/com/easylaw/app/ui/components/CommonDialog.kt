package com.easylaw.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CommonDialog(
    title: String,
    desc: String,
    icon: ImageVector,
    confirmText: String = "확인",
    dismissText: String? = null, // null이면 버튼 하나만 노출
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape =
                androidx.compose.foundation.shape
                    .RoundedCornerShape(24.dp),
            color = Color.White,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(scrollState),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF3182F6),
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style =
                        TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191F28),
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(32.dp))

                // 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 1. 취소 버튼 (dismissText가 있을 때만 노출)
                    if (dismissText != null) {
                        androidx.compose.material3.Button(
                            onClick = onDismiss,
                            modifier =
                                Modifier
                                    .weight(1f) // 반반 차지
                                    .height(54.dp),
                            shape =
                                androidx.compose.foundation.shape
                                    .RoundedCornerShape(14.dp),
                            colors =
                                androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF2F4F6), // 연한 회색 배경
                                    contentColor = Color(0xFF4E5968), // 서브 텍스트 컬러
                                ),
                        ) {
                            Text(text = dismissText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // 2. 확인 버튼
                    androidx.compose.material3.Button(
                        onClick = onConfirm,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(54.dp),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(14.dp),
                        colors =
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3182F6),
                            ),
                    ) {
                        Text(text = confirmText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
