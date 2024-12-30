package cereva.utills

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import cereva.ui.theme.MediumGreen
import com.fremanrobots.cereva.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var showRipple by remember { mutableStateOf(false) }
    var showIcon by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showRipple = true
        delay(3000)  // Show ripple for 3 seconds
        showRipple = false
        showIcon = true
        delay(3000)  // Show icon for 3 seconds
        showIcon = false
        showText = true
        delay(3000)  // Show text for 3 seconds

        // Directly navigate to the home screen
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showRipple,
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(1000))
        ) {
            Box(modifier = Modifier.padding(top = 250.dp))
            {
                Text(
                    text = "Cereva",
                    color = MediumGreen,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(
            visible = showIcon,
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(1000))
        ) {
            Box(modifier = Modifier.padding(bottom = 200.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo3),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(300.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showText,
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(1000))
        ) {
            Box(modifier = Modifier.padding(top = 150.dp)) {
                Text(
                    text = "Cerebral Co-Pilot",
                    color = Color.Green,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
