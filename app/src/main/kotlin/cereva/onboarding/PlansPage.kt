package cereva.onboarding

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import cereva.MainScreens.HomePage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.tooling.preview.Preview
import cereva.ui.theme.DarkGreen
import cereva.ui.theme.DeepGreen
import cereva.ui.theme.MediumGreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansPageScreen(navController: NavController, context: Context) {
    var isFreeTrial by remember { mutableStateOf(false) }
    var isMonthlySubscription by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White ), // Set the entire screen's background to white
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Choose Your Plan",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepGreen
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(Color.White), // Ensures the column area is also white
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Subscription Plans",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(20.dp))

            SubscriptionCard(
                title = "Free Trial - 7 Days",
                subtitle = "Get full access to all premium features for 7 days.",
                isSelected = isFreeTrial,
                onClick = {
                    isFreeTrial = true
                    isMonthlySubscription = false
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SubscriptionCard(
                title = "Monthly Subscription - €10/month",
                subtitle = "Unlock all premium features with a monthly subscription.",
                isSelected = isMonthlySubscription,
                onClick = {
                    isMonthlySubscription = true
                    isFreeTrial = false
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            TermsAndConditionsText(context)

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    when {
                        isFreeTrial -> {
                            Toast.makeText(context, "Free Trial Activated for 7 Days", Toast.LENGTH_SHORT).show()
                            navController.navigate("home")
                        }
                        isMonthlySubscription -> {
                            Toast.makeText(context, "Subscribed to Monthly Plan", Toast.LENGTH_SHORT).show()
                            navController.navigate("home")
                        }
                        else -> {
                            Toast.makeText(context, "Please select a subscription plan", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Continue",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SubscriptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MediumGreen

        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 20.dp else 0.dp
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = subtitle, color = Color.Gray)
            }
        }
    }

}

@Composable
fun TermsAndConditionsText(context: Context) {
    val annotatedText = buildAnnotatedString {
        append("By subscribing, you agree to our ")

        pushStringAnnotation(tag = "TERMS", annotation = "terms_and_conditions")
        withStyle(style = SpanStyle(color = DarkGreen, fontWeight = FontWeight.Bold)) {
            append("Terms & Conditions")
        }
        pop()

        append(" and ")

        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy_policy")
        withStyle(style = SpanStyle(color = DarkGreen, fontWeight = FontWeight.Bold)) {
            append("Privacy Policy")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        style = TextStyle(fontSize = 14.sp),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                .firstOrNull()?.let {
                    Toast.makeText(context, "Terms & Conditions clicked", Toast.LENGTH_SHORT).show()
                }
            annotatedText.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                .firstOrNull()?.let {
                    Toast.makeText(context, "Privacy Policy clicked", Toast.LENGTH_SHORT).show()
                }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PlansPageScreenPreview() {
    // Create a mock NavController
    val mockNavController = rememberNavController()

    // Use a default or mock context (LocalContext.current is safe for previews)
    val mockContext: Context = androidx.compose.ui.platform.LocalContext.current

    // Call the screen composable with the mock NavController and context
    PlansPageScreen(navController = mockNavController, context = mockContext)
}