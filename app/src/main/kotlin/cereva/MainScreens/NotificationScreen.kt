package cereva.MainScreens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cereva.ui.theme.CerevaTheme
import com.fremanrobots.cereva.R

class FullScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val message = intent.getStringExtra("NOTIFICATION_MESSAGE") ?: "Default Message"

        setContent {
            CerevaTheme {
                FullScreenNotificationScreen(message = message)
            }
        }
    }
}

@Composable
fun FullScreenNotificationScreen(message: String) {
    // Custom font definition
    val customFont = FontFamily(Font(R.font.two)) // Replace with your actual font resource

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = message,
                color = Color.Black,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(fontFamily = customFont), // Apply custom font here
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FullScreenNotificationScreenPreview() {
    FullScreenNotificationScreen(message = "This is a preview notification")
}
