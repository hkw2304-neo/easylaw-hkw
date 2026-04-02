package com.easylaw.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.load
import com.github.chrisbanes.photoview.PhotoView

enum class MineType {
    IMAGE,
    PDF,
    VIDEO,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommonPreview(
    previewImage: String,
    mineType: MineType = MineType.IMAGE,
    clickable: () -> Unit,
) {
    if (previewImage.isEmpty()) return

    val context = LocalContext.current

    // 1. 전체 페이지 수 계산
    val pageCount =
        remember(previewImage) {
            if (mineType == MineType.PDF) getPdfPageCount(context, Uri.parse(previewImage)) else 1
        }

    val pagerState = rememberPagerState(pageCount = { pageCount })

    Dialog(
        onDismissRequest = { clickable() },
        properties = DialogProperties(usePlatformDefaultWidth = false), // 너비를 더 넓게 쓰기 위해 false 권장
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.95f) // 화면의 95% 사용
                    .height(550.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
        ) {
            if (mineType == MineType.VIDEO) {
                VideoPlayerView(uri = previewImage)
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1, // 이전/다음 페이지 미리 로드
                ) { pageIndex ->
                    AndroidView(
                        factory = { ctx ->
                            PhotoView(ctx).apply {
                                // 초기 로드
                                setupContent(this, ctx, previewImage, mineType, pageIndex)
                            }
                        },
                        update = { photoView ->
                            // 페이지 변경 시 업데이트
                            setupContent(photoView, context, previewImage, mineType, pageIndex)
                        },
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                    )
                }
            }

            // 페이지 인디케이터 (PDF일 때만)
            if (mineType == MineType.PDF && pageCount > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / $pageCount",
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
            }

            // 닫기 버튼
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { clickable() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

// PhotoView 로직 분리
fun setupContent(
    photoView: PhotoView,
    context: Context,
    uri: String,
    type: MineType,
    index: Int,
) {
    if (type == MineType.PDF) {
        val bitmap = getPdfBitmap(context, Uri.parse(uri), index)
        if (bitmap != null) {
            photoView.setImageBitmap(bitmap)
        }
    } else {
        photoView.load(uri)
    }
}

fun getPdfPageCount(
    context: Context,
    uri: Uri,
): Int =
    try {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
            PdfRenderer(fd).use { it.pageCount }
        } ?: 1
    } catch (e: Exception) {
        Log.e("PdfError", "Page count error: ${e.message}")
        1
    }

fun getPdfBitmap(
    context: Context,
    uri: Uri,
    pageIndex: Int,
): Bitmap? {
    return try {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
            PdfRenderer(fd).use { renderer ->
                if (pageIndex >= renderer.pageCount) return null
                renderer.openPage(pageIndex).use { page ->
                    // 해상도를 위해 2배 확대 생성
                    val bitmap =
                        Bitmap.createBitmap(
                            (page.width * 2.0).toInt(),
                            (page.height * 2.0).toInt(),
                            Bitmap.Config.ARGB_8888,
                        )
                    // 배경을 하얀색으로 채우기 (PDF 투명도 방지)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            }
        }
    } catch (e: Exception) {
        Log.e("PdfError", "Render error: ${e.message}")
        null
    }
}

fun getVideoBitmap(
    context: Context,
    uri: String,
): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, Uri.parse(uri))
        // 0초 지점의 프레임을 가져옴 (OPTION_CLOSEST_SYNC는 가장 가까운 키프레임)
        retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } catch (e: Exception) {
        Log.e("VideoError", "썸네일 추출 실패: ${e.message}")
        null
    } finally {
        retriever.release()
    }
}

@Composable
fun VideoPlayerView(uri: String) {
    val context = LocalContext.current

    // 1. ExoPlayer 초기화
    val exoPlayer =
        remember {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse(uri))
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true // 다이얼로그 열리자마자 재생
            }
        }

    // 2. 화면에서 사라질 때 메모리 해제 (매우 중요!)
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 3. AndroidView를 통해 PlayerView 연결
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true // 재생/일시정지 버튼 보이기
                setBackgroundColor(android.graphics.Color.BLACK) // 비디오 배경은 검은색이 국룰
            }
        },
        modifier =
            Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp)),
    )
}
