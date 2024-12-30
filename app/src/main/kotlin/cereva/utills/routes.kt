package cereva.utills

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cereva.MainScreens.DetailScreen
import cereva.MainScreens.HomePage

@Composable
fun MyAppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current  // Access the context here

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("home") {
            HomePage(navController, context = context)  // Pass context to HomePage
        }
        composable("detail") {
            DetailScreen()  // Your detail screen
        }
    }
}
