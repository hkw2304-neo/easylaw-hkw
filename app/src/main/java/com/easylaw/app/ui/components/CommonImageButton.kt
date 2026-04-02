import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CommonImageButton(
    onClick: () -> Unit,
    image: ImageVector,
    desc: String,
//    currentPhotoCount: Int,
//    maxPhotoCount: Int = 5
) {
    Box(
        modifier =
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF3182F6).copy(alpha = 0.1f))
                .clickable {
                    onClick()
                },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = image,
                contentDescription = desc,
                tint = Color(0xFF3182F6),
                modifier = Modifier.size(24.dp), // 아이콘 크기 살짝 키움
            )

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
