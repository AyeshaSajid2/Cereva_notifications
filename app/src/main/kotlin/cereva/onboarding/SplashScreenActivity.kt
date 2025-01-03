package cereva.utills

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
import androidx.navigation.NavController
import cereva.ui.theme.DarkGreen
import cereva.ui.theme.MediumGreen
import com.fremanrobots.cereva.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var showCerevaAndIcon by remember { mutableStateOf(true) }
    var showCoPilotText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(3000)  // Show "Cereva" and icon for 3 seconds
        showCerevaAndIcon = false
        showCoPilotText = true
        delay(3000)  // Show "Cerebral Co-Pilot" text for 3 seconds

        // Navigate to the home screen after delay
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
        // "Cereva" text and icon visible for 3 seconds
        AnimatedVisibility(
            visible = showCerevaAndIcon,
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(1000))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.logo3),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(300.dp)
                )
                Text(
                    text = "Cereva",
                    color = MediumGreen,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        // "Cerebral Co-Pilot" text visible for 3 seconds after "Cereva" and icon disappear
        AnimatedVisibility(
            visible = showCoPilotText,
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(1000))
        ) {
            Box(modifier = Modifier.padding(top = 150.dp)) {
                Text(
                    text = "Cerebral Co-Pilot",
                    color = DarkGreen,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

