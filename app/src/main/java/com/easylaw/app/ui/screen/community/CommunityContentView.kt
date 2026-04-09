package com.easylaw.app.ui.screen.community

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.easylaw.app.data.models.common.FileUploadModel
import com.easylaw.app.data.models.common.TemplateFieldModel
import com.easylaw.app.ui.components.CommonDialog
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.ui.components.CommonPreview
import com.easylaw.app.ui.screen.community.components.BtnLikeShare
import com.easylaw.app.ui.screen.community.components.CommentSheetContent
import com.easylaw.app.ui.screen.community.components.CommunityDeleteDialog
import com.easylaw.app.viewModel.community.CommunityDetailViewModel
import com.easylaw.app.viewModel.community.CommunityDetailViewState

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityContentView(
    modifier: Modifier = Modifier,
    viewModel: CommunityDetailViewModel,
    goBack: () -> Unit,
    goUpdate: (Long) -> Unit,
    navController: NavHostController,
) {
    val viewState by viewModel.communityDetailViewState.collectAsState()
    val contents = viewState.communityDetail
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(viewState.isCommunityDeleted) {
        if (viewState.isCommunityDeleted) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
            goBack()
            viewModel.consumeDeleteEvent() // 뒤로 가기 후 상태 초기화
        }
    }

    val refreshSignal =
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("refresh")
            ?.observeAsState()
    LaunchedEffect(refreshSignal?.value) {
        if (refreshSignal?.value == true) {
//            viewModel.refreshCommunityDetail()
            viewModel.loadAllDetailData()
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh")
        }
    }

    BackHandler {
        if (viewState.isReadOnly) (context as? Activity)?.finish() else goBack()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB)),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .height(56.dp)
                                .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!viewState.isReadOnly) {
                            IconButton(onClick = { goBack() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = Color(0xFF191F28),
                                )
                            }
                        }
                        Text(
                            text = "게시글 상세보기",
                            style =
                                TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF191F28),
                                ),
                        )
                    }
                }
            },
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                Spacer(
                    modifier = Modifier.height(10.dp),
                )
                if (contents != null) {
                    SectionOne(
                        category = contents.category,
                        title = contents.title,
                        userId = viewState.userState.id,
                        communityUserId = contents.user_id ?: "",
                        goUpdate = { goUpdate(contents.id ?: 0L) },
                        communityDelete = { viewModel.communityDelete() },
                        saveCommunityPdf = { viewModel.saveCommunityPdf(context) },
                        isReadOnly = viewState.isReadOnly,
                    )
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 34.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF3182F6),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "본문 내용",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191F28),
                        )
                    }
                    SectionTwo(
                        content = contents.content,
                    )
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 34.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF3182F6),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "추가 내용",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191F28),
                        )
                    }
                    SectionExtra(
                        categoryField = viewState.categoryField,
                        extraData = contents.extraData,
                    )
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 34.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = Color(0xFF3182F6),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "첨부된 사진 ${contents.images.size}/3",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B95A1),
                        )
                    }
                    SectionThree(
                        images = contents.images,
                        onImagePreview = { viewModel.onImagePreview(it) },
                    )
                    SectionFour(
                        clickLike = { if (!viewState.isReadOnly) viewModel.clickLike(contents.id ?: 0L) },
                        isLiked = viewState.isLiked,
                        likeCount = viewState.likeCount,
                        clickShare = { if (!viewState.isReadOnly) viewModel.clickShare(context, contents) },
                    )

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 34.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color(0xFF3182F6),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "댓글",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B95A1),
                        )
                    }
                    SectionFive(
                        viewState = viewState,
                        viewModel = viewModel,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
        if (viewState.showCommentSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeShowCommentSheet() },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = {
                    Surface(
                        modifier =
                            Modifier
                                .padding(vertical = 12.dp)
                                .width(40.dp)
                                .height(4.dp),
                        color = Color(0xFFD1D6DB),
                        shape = RoundedCornerShape(2.dp),
                    ) {}
                },
            ) {
                BackHandler(enabled = viewState.isReplyMode) { viewModel.closeCommentReply() }

                // [댓글/답글 통합 레이아웃]
                CommentSheetContent(
                    viewState = viewState,
                    viewModel = viewModel,
                )
            }
        }
        if (viewState.isDeleteMode || viewState.isCommunityDeleteMode) {
            CommunityDeleteDialog(
                title = if (viewState.isDeleteMode) "댓글을 삭제할까요?" else "게시글을 삭제할까요?",
                inputText = if (viewState.isDeleteMode) viewState.deleteInputText else viewState.deleteCommunityInputText,
                onValueChange = { if (viewState.isDeleteMode) viewModel.onDeleteValueChanged(it) else viewModel.onCommunityDeleteValueChanged(it) },
                onCancel = { if (viewState.isDeleteMode) viewModel.cancelDeleteMode() else viewModel.cancelCommunityDeleteMode() },
                onConfirm = {
                    if (viewState.isDeleteMode) {
                        viewState.deleteCommentId?.let { id -> viewModel.deleteComment(id, viewState.isReplyMode, viewState.isSelectedReplyed) }
                    } else {
                        viewModel.deleteCommunity(viewState.communityDetail?.id ?: 0L)
                    }
                },
            )
        }
        if (viewState.previewImage?.isNotEmpty() == true) {
            CommonPreview(previewImage = viewState.previewImage ?: "", clickable = { viewModel.onImagePreviewDismissed() })
        }
        if (viewState.isLoading) CommonIndicator(title = "잠시만 기다려주세요...")
        if (viewState.successDownLoad) {
            CommonDialog(
                title = "다운로드 완료",
                desc = "파일을 다운로드했습니다,",
                icon = Icons.Default.Done,
                onConfirm = { viewModel.closeShowDialog() },
            )
        }
        if (viewState.errorDownText.isNotEmpty()) {
            CommonDialog(
                title = "다운로드 실패",
                desc = viewState.errorDownText,
                icon = Icons.Default.Error,
                onConfirm = { viewModel.closeShowDialog() },
            )
        }
    }
}

@Composable
fun SectionOne(
    category: String,
    title: String,
    userId: String,
    goUpdate: () -> Unit,
    communityUserId: String,
    communityDelete: () -> Unit,
    saveCommunityPdf: () -> Unit,
    isReadOnly: Boolean = false,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth() // 가로 꽉 차게
                .padding(horizontal = 24.dp),
        // 이미지처럼 양옆 여백
        shape = RoundedCornerShape(20.dp), // 둥근 모서리 (이미지 스타일)
        color = Color.White, // 배경은 흰색
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(24.dp), // 박스 내부 안쪽 여백
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "[$category]",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3182F6),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
//                    maxLines = 1,
                    modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                )
                if (!isReadOnly && userId == communityUserId) {
                    Row(
//                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "저장",
                            style =
                                TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold, // 약간 두껍게
                                    color = Color(0xFF3182F6), // 토스 대표 블루색
                                ),
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(8.dp)) // 클릭 영역도 둥글게
                                    .clickable {
                                        saveCommunityPdf()
                                    },
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier =
                                Modifier
                                    .width(1.dp)
                                    .height(14.dp)
                                    .background(Color(0xFFE5E8EB)),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "수정",
                            style =
                                TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold, // 약간 두껍게
                                    color = Color(0xFF3182F6), // 토스 대표 블루색
                                ),
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(8.dp)) // 클릭 영역도 둥글게
                                    .clickable {
                                        goUpdate()
                                    },
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier =
                                Modifier
                                    .width(1.dp)
                                    .height(14.dp)
                                    .background(Color(0xFFE5E8EB)),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "삭제",
                            style =
                                TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF04452),
                                ),
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        communityDelete()
                                    },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTwo(content: String) {
    val scrollState = rememberScrollState()

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(220.dp) // 가로 꽉 차게
                .padding(horizontal = 24.dp),
        // 이미지처럼 양옆 여백
        shape = RoundedCornerShape(20.dp), // 둥근 모서리 (이미지 스타일)
        color = Color.White,
//        color = Color(0xFFF2F4F6),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
        ) {
            Text(
                text = content,
                modifier =
                    Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                fontSize = 17.sp,
                lineHeight = 28.sp,
                color = Color(0xFF333D4B),
            )
        }
    }
}

@Composable
fun SectionExtra(
    categoryField: List<TemplateFieldModel>,
    extraData: Map<String, String>,
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(220.dp) // 가로 꽉 차게
                .padding(horizontal = 24.dp),
        // 이미지처럼 양옆 여백
        shape = RoundedCornerShape(20.dp), // 둥근 모서리 (이미지 스타일)
        color = Color.White,
//        color = Color(0xFFF2F4F6),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
        ) {
            extraData.forEach { item ->

                val matchedField = categoryField.find { it.id == item.key }
                val displayLabel = matchedField?.label ?: ""

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = displayLabel,
                        style =
                            TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8B95A1), // 토스 그레이
                            ),
                    )
                }
                Text(
                    text = " - ${item.value}",
                    style =
                        TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333D4B), // 진한 회색
                        ),
                    modifier = Modifier.padding(start = 5.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SectionThree(
    images: List<FileUploadModel>,
    onImagePreview: (String) -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(85.dp)
                .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        if (images.isNotEmpty()) {
            Column {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                ) {
                    items(images) { item ->
                        Box(
                            modifier =
                                Modifier
                                    .size(56.dp)
                                    .clickable { onImagePreview(item.uri) }
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                        ) {
                            AsyncImage(
                                model = item.uri,
                                contentDescription = null,
                                onSuccess = { Log.d("Coil", "로드 성공: $item") },
                                onError = { error -> Log.e("Coil", "로드 실패: ${error.result.throwable.message}") }, // 여기서 에러 확인!
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
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
                    "첨부된 이미지가 없습니다.",
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
fun SectionFour(
    isLiked: Boolean,
    likeCount: Int,
    clickLike: (() -> Unit)? = null,
    clickShare: (() -> Unit)? = null,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(85.dp)
                .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Column(
//            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                clickLike?.invoke()
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    BtnLikeShare(
                        icon = if (isLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        label = "추천하기",
                        tint = if (isLiked) Color(0xFF3182F6) else Color.Gray,
                        likeCount = likeCount,
                        isLiked = isLiked,
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color(0xFFF2F4F6)),
                )

                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clickable {
                                clickShare?.invoke()
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    BtnLikeShare(
                        icon = Icons.Default.Share,
                        label = "공유하기",
                        tint = Color(0xFF3182F6),
                    )
                }
            }
        }
    }
}

@Composable
fun SectionFive(
    viewState: CommunityDetailViewState,
    viewModel: CommunityDetailViewModel,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(65.dp)
                .padding(horizontal = 24.dp)
                .clickable { viewModel.onShowCommentSheet() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = Color(0xFF3182F6),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${viewState.communityComments.size}개의 댓글이 달렸습니다.",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E5968)),
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFB0B8C1))
        }
    }
}
