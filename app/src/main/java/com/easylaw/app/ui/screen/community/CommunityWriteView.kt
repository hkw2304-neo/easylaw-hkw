package com.easylaw.app.ui.screen.community

import CommonImageButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.easylaw.app.ui.components.CommonButton
import com.easylaw.app.ui.components.CommonDialog
import com.easylaw.app.ui.components.CommonFilterCategory
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.ui.components.CommonPreview
import com.easylaw.app.ui.components.CommonScreen
import com.easylaw.app.ui.components.CommonTextField
import com.easylaw.app.viewModel.community.CommunityWriteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityWriteView(
    modifier: Modifier = Modifier,
    viewModel: CommunityWriteViewModel,
    goBack: () -> Unit,
    navController: NavHostController,
) {
    val viewState by viewModel.commnuityWriteViewState.collectAsState()

    /*
        갤러리 실행기
        1. launch : 클릭 시 앱은 잠시 멈추고 시스템 갤러리가 화면을 덮는다.
        2. 사용자가 한 장 고르면 해당 주소를 가지고 앱으로 복귀
        3. 콜백 실행
        4. 저장

     */
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            uri?.let {
                viewModel.onImageAdded(it.toString())
            }
        }

    LaunchedEffect(viewState.isGoBack) {
        if (viewState.isGoBack) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            goBack()
        }
    }
    Box(
        modifier = modifier.fillMaxSize().background(Color(0xFFF9FAFB)),
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
                        text = "작성 완료",
                        isEnable =
                            if (
                                viewState.communityWriteTitleField.isNotEmpty() &&
                                viewState.communityWriteContentField.isNotEmpty()
                            ) {
                                true
                            } else {
                                false
                            },
                        onClick = {
                            // 디비 연동
                            viewModel.writeCommunity()
                        },
                        color = Color(0xFF3182F6),
                        icon = Icons.Default.Check,
                    )
                }
            },
            containerColor = Color(0xFFF9FAFB),
        ) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding),
            ) {
                CommonScreen(
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        item {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 14.dp, bottom = 12.dp, start = 4.dp),
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(4.dp, 16.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color(0xFF3182F6)),
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "게시판 글쓰기",
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
                                    border = BorderStroke(1.dp, Color(0xFFF2F4F6)), // 아주 연한 테두리
                                    shadowElevation = 0.dp,
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Text(
                                            text = "어떤 분야의 고민인가요?",
                                            style =
                                                TextStyle(
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF4E5968),
                                                    fontWeight = FontWeight.Medium,
                                                ),
                                            modifier = Modifier.padding(vertical = 12.dp),
                                        )
                                        CommonFilterCategory(
                                            category = viewState.categoryList,
                                            selectedCategory = viewState.selectedCategory,
                                            onCategorySelected = { it ->
                                                viewModel.onCategorySelected(it)
                                            },
                                            updateTemplateField = { viewModel.updateTemplateField() },
                                        )
                                        CommonTextField(
                                            title = "제목",
                                            value = viewState.communityWriteTitleField,
                                            placeholder = "제목을 입력해주세요.",
                                            onValueChange = { viewModel.onTitleFieldChanged(it) },
                                            isRequire = true,
                                        )
                                        CommonTextField(
                                            title = "본문",
                                            value = viewState.communityWriteContentField,
                                            placeholder = "본문을 입력해주세요.",
                                            singleLine = false,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(300.dp),
                                            onValueChange = { viewModel.onContentFieldChanged(it) },
                                            isRequire = true,
                                        )

                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color(0xFFF9FAFB))
                                                    .padding(16.dp),
                                        ) {
                                            Column {
                                                Text(
                                                    text = "첨부된 사진 ${viewState.selectedImages.size}/3",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF8B95A1),
                                                    modifier = Modifier.padding(bottom = 12.dp),
                                                )
                                                LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) {
                                                    item {
                                                        CommonImageButton(
                                                            image = Icons.Default.AddPhotoAlternate,
                                                            desc = "사진 추가",
                                                            onClick = {
                                                                if (viewState.selectedImages.size < 3) {
                                                                    galleryLauncher.launch("image/*")
                                                                } else {
                                                                    viewModel.onShowDialog()
                                                                }
                                                            },
                                                        )
                                                    }

                                                    items(viewState.selectedImages) { imageUri ->
                                                        Box(
                                                            modifier =
                                                                Modifier
                                                                    .size(56.dp)
                                                                    .clickable { viewModel.onImagePreview(imageUri) }
                                                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                                        ) {
                                                            AsyncImage(
                                                                model = imageUri,
                                                                contentDescription = null,
                                                                modifier =
                                                                    Modifier
                                                                        .fillMaxSize()
                                                                        .clip(RoundedCornerShape(12.dp)),
                                                                contentScale = ContentScale.Crop,
                                                            )

                                                            Box(
                                                                modifier =
                                                                    Modifier
                                                                        .align(Alignment.TopEnd)
                                                                        .offset(x = 4.dp, y = (-4).dp)
                                                                        .size(18.dp)
                                                                        .background(Color(0xFF4E5968), CircleShape)
                                                                        .clickable { viewModel.removeSelectedImage(imageUri) },
                                                                contentAlignment = Alignment.Center,
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = "삭제",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(10.dp),
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (viewState.categoryField.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                thickness = 1.dp,
                                                color = Color(0xFFF2F4F6),
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                "선택사항",
                                                style =
                                                    TextStyle(
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF8B95A1), // 약간 흐린 회색 (중요도가 낮음을 표시)
                                                        letterSpacing = (-0.3).sp, // 자간을 살짝 좁혀서 세련되게
                                                    ),
                                                modifier = Modifier.padding(bottom = 8.dp),
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            viewState.categoryField.forEach { item ->

                                                CommonTextField(
                                                    title = item.label,
                                                    value = viewState.selectedTextFields[item.id] ?: "",
                                                    placeholder = "${item.label}을 입력해주세요.",
                                                    singleLine = false,
                                                    onValueChange = { value ->
                                                        viewModel.onContentSelectedFieldChanged(
                                                            content = value,
                                                            id = item.id,
                                                        )
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
        if (viewState.previewImage?.isNotEmpty() ?: false) {
            CommonPreview(
                previewImage = viewState.previewImage ?: "",
                clickable = { viewModel.onImagePreviewDismissed() },
            )
        }
        if (viewState.isShowDialog) {
            CommonDialog(
                title = "사진 추가 제한",
                desc = "사진은 최대 3장까지만 등록할 수 있습니다.",
                icon = Icons.Default.Close,
                onConfirm = { viewModel.closeShowDialog() },
            )
        }
        if (viewState.isWriteLoading) {
            CommonIndicator(title = "잠시만 기다려주세요...")
        }
        if (viewState.isWriteSuccess) {
            CommonDialog(
                title = "게시글 등록 완료",
                desc = "등록이 완료되었습니다..",
                icon = Icons.Default.CloudCircle,
                onConfirm = { viewModel.closeWriteShowDialog() },
            )
        }
        if (viewState.isErrorMsg.isNotEmpty()) {
            CommonDialog(
                title = "게시글 등록 실패",
                desc = viewState.isErrorMsg,
                icon = Icons.Default.Error,
                onConfirm = { viewModel.closeWriteShowDialog() },
            )
        }
    }
}
