package com.easylaw.app.ui.screen.lawyers

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.easylaw.app.data.models.common.FileUploadModel
import com.easylaw.app.ui.components.CategoryDropDown
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.ui.components.CommonPreview
import com.easylaw.app.ui.components.CommonScreen
import com.easylaw.app.ui.components.CommonTextField
import com.easylaw.app.ui.components.getPdfBitmap
import com.easylaw.app.ui.components.getVideoBitmap
import com.easylaw.app.viewModel.lawyers.ReserveDetailViewModel
import com.easylaw.app.viewModel.lawyers.ReserveDetailViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveDetailView(
    modifier: Modifier,
    viewModel: ReserveDetailViewModel,
    goBack: () -> Unit,
    navController: NavHostController,
) {
    val viewState by viewModel.reserveDetailViewState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    Color(0xFFF9FAFB),
                ).statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        Scaffold { innerPadding ->
            CommonScreen(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                content = {
                    item { ReserveDetailUserInfoSection(state = viewState) }
                    item { ReserveDetailItemSection(state = viewState, viewModel = viewModel, context = context) }
//                   item { Spacer(modifier = Modifier.height(20.dp)) }
                },
            )
        }

        if (viewState.isLoading) {
            CommonIndicator(title = "잠시만 기다려주세요...")
        }
        if (viewState.previewImage.previewImage.isNotEmpty()) {
            CommonPreview(
                previewImage = viewState.previewImage.previewImage,
                mineType = viewState.previewImage.mineType,
                clickable = { viewModel.onImagePreviewDismissed() },
            )
        }
    }
}

@Composable
fun ReserveDetailUserInfoSection(state: ReserveDetailViewState) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp), // 전체 여백 조절
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 14.dp, bottom = 12.dp, start = 4.dp),
        ) {
            // 좌측 파란색 포인트 바
            Box(
                modifier =
                    Modifier
                        .size(4.dp, 16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF3182F6)),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "인적 사항",
                style =
                    TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191F28), // 토스 그레이 900
                        letterSpacing = (-0.3).sp,
                    ),
            )
        }

        // 2. 인적사항 카드 컨테이너
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFF2F4F6)), // 아주 연한 테두리
            shadowElevation = 0.dp, // 깔끔함을 위해 그림자 대신 테두리 강조 (원하시면 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp), // 필드 간 간격
            ) {
                // 성함 필드
                CommonTextField(
                    title = "성함",
                    value = state.reserveItem.userName,
                    onValueChange = {},
                    placeholder = "이름을 불러오는 중...",
                    isRequire = true,
                    isReadOnly = true,
                    // 만약 수정 불가라면 힌트를 주거나 ReadOnly 처리 제안
                )

                // 얇은 구분선
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFF9FAFB)),
                )

                // 이메일 필드
                CommonTextField(
                    title = "이메일",
                    value = state.reserveItem.userEmail,
                    onValueChange = {},
                    placeholder = "이메일을 불러오는 중...",
                    isRequire = true,
                    isReadOnly = true,
                )
            }
        }
    }
}

@Composable
fun ReserveDetailItemSection(
    state: ReserveDetailViewState,
    viewModel: ReserveDetailViewModel,
    context: Context,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp), // 전체 여백 조절
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 14.dp, bottom = 12.dp, start = 4.dp),
        ) {
            // 좌측 파란색 포인트 바
            Box(
                modifier =
                    Modifier
                        .size(4.dp, 16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF3182F6)),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "상세 항목",
                style =
                    TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191F28), // 토스 그레이 900
                        letterSpacing = (-0.3).sp,
                    ),
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFF2F4F6)),
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
//                verticalArrangement = Arrangement.spacedBy(10.dp) // 필드 간 간격
            ) {
                CategoryDropDown(
                    title = "상담 분야 선택",
                    desc = "상담 분야를 선택해주세요",
                    onExpandedChange = { },
                    enabled = false,
                    expanded = false,
                    categories = emptyList(),
                    onValueChange = {
                    },
                    selectedCategory = state.reserveItem.selectedCategoryName,
                    onClick = {
                    },
                )
                Spacer(modifier = Modifier.height(20.dp))
                CommonTextField(
                    title = "상담 제목",
                    value = state.reserveItem.detailTitle,
                    onValueChange = {
                    },
                    placeholder = "제목을 입력하세요.",
                    isRequire = true,
                    isReadOnly = true,
                )

                CommonTextField(
                    title = "구체적인 내용",
                    value = state.reserveItem.detailContent,
                    onValueChange = {
                    },
                    placeholder = "구체적인 내용을 입력해주세요.",
                    isRequire = true,
                    isReadOnly = true,
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFF9FAFB)),
                )
                Spacer(modifier = Modifier.height(20.dp))

                FileListDetailSection(
                    state = state,
                    viewModel = viewModel,
                    context = context,
                )
            }
        }
    }
}

@Composable
fun FileListDetailSection(
    state: ReserveDetailViewState,
    viewModel: ReserveDetailViewModel,
    context: Context,
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(385.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        val fileList = state.reserveItem.uploadFileList
        if (fileList.isNotEmpty()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp), // 파일 간 간격
            ) {
                fileList.forEach { image ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8F9FA)) // 아주 연한 배경색으로 구분감 주기
                                .clickable {
//                                    viewModel.onFilePreview(image = image)
                                    viewModel.downloadFile(
                                        context = context,
                                        url = image.uri,
                                        fileName = image.name,
                                    )
                                }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f), // 남은 공간 다 차지
                        ) {
                            Text(
                                text = image.name,
                                style =
                                    TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333D4B),
                                    ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis, // 너무 길면 ... 처리
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = image.dataSize,
                                style =
                                    TextStyle(
                                        fontSize = 12.sp,
                                        color = Color(0xFF8B95A1),
                                    ),
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier =
                                Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF2F4F6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            val icon =
                                when {
                                    image.mimeType.contains("image") -> Icons.Default.Image
                                    image.mimeType.contains("pdf") -> Icons.Default.PictureAsPdf
                                    image.mimeType.contains("video") -> Icons.Default.VideoLibrary
                                    else -> Icons.Default.InsertDriveFile
                                }

                            // 2. 컬러 결정 로직 (포인트 컬러)
                            val iconColor =
                                when {
                                    image.mimeType.contains("image") -> Color(0xFFFF9500) // 주황색
                                    image.mimeType.contains("pdf") -> Color(0xFFF04452) // 토스 레드 (빨간색)
                                    image.mimeType.contains("video") -> Color(0xFF3182F6) // 토스 블루 (파란색)
                                    else -> Color(0xFF8B95A1) // 기본 회색
                                }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp),
                                tint = iconColor,
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.ImageNotSupported, // 또는 가벼운 사진 아이콘
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFFD1D6DB), // 아주 연한 회색
                )
                Text(
                    "첨부된 파일이 없습니다.",
                    style =
                        TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFADB5BD), // 가독성은 있지만 강조되지 않는 회색
                            letterSpacing = (-0.3).sp, // 자간 살짝 좁히기
                        ),
                )
            }
        }
    }
}

@Composable
fun fileToBitmapDetail(
    context: Context,
    image: FileUploadModel,
): Bitmap? {
    val fileBitmap =
        remember(image.uri) {
            if (image.mimeType.contains("pdf")) {
                getPdfBitmap(context, Uri.parse(image.uri), 0) // 0번 페이지
            } else if (image.mimeType.contains("video")) {
                getVideoBitmap(context, image.uri)
            } else {
                null
            }
        }
    return fileBitmap
}
