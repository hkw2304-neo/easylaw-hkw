package com.easylaw.app.ui.screen.community
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.easylaw.app.data.models.CommentModel
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.ui.components.CommonPreview
import com.easylaw.app.util.Common
import com.easylaw.app.viewmodel.CommunityDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailView(
    modifier: Modifier = Modifier,
    viewModel: CommunityDetailViewModel,
    goBack: () -> Unit,
) {
    val viewState by viewModel.communityDetailViewState.collectAsState()
    val scrollState = rememberScrollState()
    val contents = viewState.communityDetail
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.White,
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(scrollState),
            ) {
                if (contents != null) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Surface(
                        color = Color(0xFFE8F3FF),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = contents.category,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3182F6),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = contents.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 36.sp,
                        color = Color(0xFF1B2128),
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF2F4F6),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            InfoRow(
                                label = "작성자",
                                value = if (contents.author.isNotEmpty()) "${contents.author} 님" else "익명",
                                valueColor = Color(0xFF3182F6),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow(
                                label = "작성일",
                                value = Common.formatIsoDate(contents.created_at),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier
                                    .width(4.dp)
                                    .height(16.dp)
                                    .background(Color(0xFF3182F6), RoundedCornerShape(2.dp)),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "본문 내용",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191F28),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF2F4F6),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E8EB)),
                    ) {
                        Text(
                            text = contents.content,
                            modifier =
                                Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                            fontSize = 17.sp,
                            lineHeight = 28.sp,
                            color = Color(0xFF333D4B),
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

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
                                text = "첨부된 사진 ${contents.images.size}/3",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B95A1),
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                items(contents.images) { imageUri ->
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
                                            onSuccess = { Log.d("Coil", "로드 성공: $imageUri") },
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
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, Color(0xFFF2F4F6)),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            InteractionItem(
                                icon = Icons.Default.FavoriteBorder,
                                label = "추천하기",
                                tint = Color(0xFFFF4D4D),
                            )
                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFFF2F4F6)))
                            InteractionItem(
                                icon = Icons.Default.Share,
                                label = "공유하기",
                                tint = Color(0xFF3182F6),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush =
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFF9FAFB), Color(0xFFECEFF1)),
                                        ),
                                ).border(1.dp, Color(0xFFE5E8EB), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF3182F6).copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF3182F6),
                                    modifier = Modifier.padding(6.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "커뮤니티 가이드라인",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF191F28),
                                )
                                Text(
                                    text = "깨끗한 커뮤니티를 위해 비방글은 삼가주세요.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4E5968),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.onShowCommentSheet()
                                },
                        color = Color(0xFFF2F4F6),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "댓글",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF191F28),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${contents.comments.size}",
                                        fontSize = 14.sp,
                                        color = Color(0xFF4E5968),
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF8B95A1),
                                )
                            }

                            if (contents.comments.isNotEmpty()) {
                                // 최신 댓글 먼저 출력
                                val lastComment = contents.comments.last()
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF3182F6),
                                        modifier = Modifier.size(24.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = lastComment.author.take(1),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = lastComment.content,
                                        fontSize = 14.sp,
                                        color = Color(0xFF333D4B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "첫 번째 댓글을 남겨보세요.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF8B95A1),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (viewState.showCommentSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.closeShowCommentSheet()
                },
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
                Column(
                    modifier =
                        Modifier
                            .fillMaxHeight(0.9f)
                            .navigationBarsPadding()
                            .imePadding(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("댓글", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    HorizontalDivider(color = Color(0xFFF2F4F6))

                    LazyColumn(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                    ) {
                        items(contents?.comments?.reversed() ?: emptyList()) { comment ->
                            CommentItem(comment)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 16.dp,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = viewState.commentInput,
                                onValueChange = { viewModel.onValueChanged(it) },
                                placeholder = { Text("댓글 추가...", fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF3182F6),
                                        unfocusedBorderColor = Color(0xFFE5E8EB),
                                        focusedContainerColor = Color(0xFFF9FAFB),
                                        unfocusedContainerColor = Color(0xFFF9FAFB),
                                    ),
                                maxLines = 3,
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = {
                                    viewModel.sendComment(viewState.commentInput)
                                },
                                enabled = viewState.commentInput.isNotBlank(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "전송",
                                    tint = if (viewState.commentInput.isNotBlank()) Color(0xFF3182F6) else Color(0xFFD1D6DB),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (viewState.previewImage?.isNotEmpty() ?: false) {
            CommonPreview(
                previewImage = viewState.previewImage ?: "",
                clickable = { viewModel.onImagePreviewDismissed() },
            )
        }

        if (viewState.isLoading) {
            CommonIndicator(title = "잠시만 기다려주세요...")
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF4E5968),
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(60.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF8B95A1),
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
    }
}

@Composable
fun InteractionItem(
    icon: ImageVector,
    label: String,
    tint: Color,
) {
    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4E5968),
        )
    }
}

@Composable
fun CommentItem(comment: CommentModel) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFE5E8EB),
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = comment.author.take(1),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E5968),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 작성자 이름
                Text(
                    text = comment.author,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = comment.created_at,
                    fontSize = 11.sp,
                    color = Color(0xFF8B95A1),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF333D4B),
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFF8B95A1),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "답글", fontSize = 12.sp, color = Color(0xFF4E5968))
            }
        }
    }
}
