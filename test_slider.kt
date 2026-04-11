import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Test() {
    Slider(
        value = 0f,
        onValueChange = {},
        thumb = {
            Box(Modifier.size(16.dp).background(Color.Red, CircleShape))
        }
    )
}
