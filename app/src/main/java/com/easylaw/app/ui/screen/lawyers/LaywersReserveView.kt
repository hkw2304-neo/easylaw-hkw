package com.easylaw.app.ui.screen.lawyers

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.easylaw.app.data.models.common.FileUploadModel
import com.easylaw.app.ui.components.CategoryDropDown
import com.easylaw.app.ui.components.CommonButton
import com.easylaw.app.ui.components.CommonDialog
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.ui.components.CommonPreview
import com.easylaw.app.ui.components.CommonScreen
import com.easylaw.app.ui.components.CommonTextField
import com.easylaw.app.ui.components.getPdfBitmap
import com.easylaw.app.ui.components.getVideoBitmap
import com.easylaw.app.viewModel.lawyers.LawyersReserveInputType
import com.easylaw.app.viewModel.lawyers.LawyersReserveViewModel
import com.easylaw.app.viewModel.lawyers.LawyersReserveViewState
import kotlin.text.isNotEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LasywersReserveView(
    modifier: Modifier,
    viewModel: LawyersReserveViewModel,
    goBack: () -> Unit,
    navController: NavHostController,
) {
    val viewState by viewModel.lawyersReserveViewState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    val docsLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(), // 또는 GetMultipleContents() (여러 장일 때)
        ) { uri: Uri? ->
            uri?.let {
                viewModel.onFileSelected(context, it.toString())
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(),
        ) { uris: List<Uri> ->
            uris.forEach { uri ->
                viewModel.onFileSelected(context, uri.toString())
            }
        }

    LaunchedEffect(
        viewState.isGoBack,
    ) {
        if (viewState.isGoBack) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("refresh", true)

            goBack()
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
        Scaffold(
            bottomBar = {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    CommonButton(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        text = "상담 신청",
                        isEnable =
                            if (
                                viewState.lawyersReserveInputForm.detailTitle.isNotEmpty() &&
                                viewState.lawyersReserveInputForm.detailContent.isNotEmpty() &&
                                viewState.lawyersReserveInputForm.selectedCategoryName.isNotEmpty()
                            ) {
                                true
                            } else {
                                false
                            },
                        onClick = {
                            viewModel.onClickReserve(
                                context = context,
                            )
                        },
                        color = Color(0xFF3182F6),
                        icon = Icons.Default.Check,
                    )
                }
            },
            containerColor = Color(0xFFF9FAFB),
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                CommonScreen(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    content = {
                        item { ReserveUserInfoSection(state = viewState) }
                        item { ReserveDetailSection(state = viewState, viewModel = viewModel, context = context) }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    },
                )
            }
        }

        if (viewState.isLoading) {
            CommonIndicator(title = "잠시만 기다려주세요...")
        }
        if (viewState.isWriteSuccess) {
            CommonDialog(
                title = "상담 등록 완료",
                desc = "등록이 완료되었습니다..",
                icon = Icons.Default.CloudCircle,
                onConfirm = { viewModel.closeShowDialog() },
            )
        }
        if (viewState.isErrorMsg.isNotEmpty()) {
            CommonDialog(
                title = "상담 등록 실패",
                desc = viewState.isErrorMsg,
                icon = Icons.Default.Error,
                onConfirm = { viewModel.closeShowDialog() },
            )
        }
        if (viewState.isShowSelectFiles) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.closeSelectFiles()
                },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }, // 상단 바 보이기
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding() // 네비게이션 바 영역 확보
                            .padding(bottom = 24.dp),
                ) {
                    SelectMenuItem(
                        icon = Icons.Default.PhotoLibrary,
                        title = "앨범에서 선택",
                        onClick = {
                            viewModel.closeSelectFiles()
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
                            )
                        },
                    )

                    Divider(color = Color(0xFFF2F4F6), thickness = 1.dp)

                    SelectMenuItem(
                        icon = Icons.Default.Folder,
                        title = "파일에서 선택",
                        onClick = {
                            viewModel.closeSelectFiles()
                            docsLauncher.launch("*/*")
                        },
                    )
                }
            }
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
fun ReserveUserInfoSection(state: LawyersReserveViewState) {
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
                    value = state.lawyersReserveInputForm.userName,
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
                    value = state.lawyersReserveInputForm.userEmail,
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
fun ReserveDetailSection(
    state: LawyersReserveViewState,
    viewModel: LawyersReserveViewModel,
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
                    onExpandedChange = { viewModel.onExpandedChange() },
                    expanded = state.categoryExpanded,
                    categories = state.categoryList,
                    onValueChange = { categoryName ->
                        viewModel.onValueChange(categoryName)
                    },
                    selectedCategory = state.lawyersReserveInputForm.selectedCategoryName,
                    onClick = { categoryName ->
                        viewModel.onClick(categoryName)
                    },
                )
                Spacer(modifier = Modifier.height(20.dp))
                // 얇은 구분선
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(1.dp)
//                        .background(Color(0xFFF9FAFB))
//                )

                CommonTextField(
                    title = "상담 제목",
                    value = state.lawyersReserveInputForm.detailTitle,
                    onValueChange = { detailTitle ->
                        viewModel.onValueChangeInput(value = detailTitle, type = LawyersReserveInputType.Title)
                    },
                    placeholder = "제목을 입력하세요.",
                    isRequire = true,
//                    isReadOnly = true
                )

                CommonTextField(
                    title = "구체적인 내용",
                    value = state.lawyersReserveInputForm.detailContent,
                    onValueChange = { detailContent ->
                        viewModel.onValueChangeInput(value = detailContent, type = LawyersReserveInputType.Content)
                    },
                    placeholder = "구체적인 내용을 입력해주세요.",
                    isRequire = true,
//                    isReadOnly = true
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFF9FAFB)),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "첨부파일",
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF000000),
                                letterSpacing = 0.5.sp,
                            ),
                    )
                    CommonButton(
                        modifier = Modifier.width(70.dp),
                        onClick = { viewModel.showSelectFiles() },
                        icon = Icons.Default.AttachFile,
                        color = Color(0xFF3182F6),
                        text = "",
                        isEnable = true,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                FileListSection(
                    state = state,
                    viewModel = viewModel,
                    context = context,
                )
            }
        }
    }
}

@Composable
fun FileListSection(
    state: LawyersReserveViewState,
    viewModel: LawyersReserveViewModel,
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
        val fileList = state.lawyersReserveInputForm.uploadFileList
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

                    val bitmapThumbnail = fileToBitmap(context, image)

                    // 하나의 파일 아이템 행(Row)
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8F9FA)) // 아주 연한 배경색으로 구분감 주기
                                .clickable {
                                    viewModel.onFilePreview(image = image)
                                }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 1️⃣ 좌측: 파일 정보 (이름, 용량)
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

                        // 2️⃣ 우측: 썸네일 및 삭제 버튼 영역
                        Box(
                            modifier = Modifier.size(56.dp),
                        ) {
                            // 썸네일 (이미지 or PDF 비트맵)
                            if (bitmapThumbnail != null) {
                                Image(
                                    bitmap = bitmapThumbnail.asImageBitmap(),
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                AsyncImage(
                                    model = image.uri,
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            }

                            Box(
                                modifier =
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-6).dp)
                                        .size(20.dp)
                                        .background(Color(0xFF4E5968), CircleShape)
                                        .clickable { viewModel.removeSelectedFile(image.uri) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "삭제",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp),
                                )
                            }
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
fun SelectMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF4E5968),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF191F28),
                ),
        )
    }
}

@Composable
fun fileToBitmap(
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
