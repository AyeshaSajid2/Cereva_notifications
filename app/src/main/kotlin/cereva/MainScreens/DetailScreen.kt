package cereva.MainScreens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cereva.utills.PreferencesManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import cereva.alarms.NotificationScheduler
import cereva.cancelation.cancelWeeklyReminders
import cereva.ui.theme.DeepGreen

@Composable
fun DetailScreen() {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val selectedDays = remember { preferencesManager.getSelectedDays() }
    val frequency = remember { preferencesManager.getFrequency() }
    val category = remember { preferencesManager.getCategory() }
    val intervals = remember { fetchIntervals(preferencesManager) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 26.dp)
    ) {
        // Top-right button
        Spacer(modifier = Modifier.height(24.dp)) // Add blank space at the top

        Column {
            Spacer(modifier = Modifier.height(20.dp)) // Add blank space above the button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {

                        if (category == "Weekly") {
                            cancelWeeklyReminders(context)
                            Log.d("HomePage", "Selected Days to use:")
                            Toast.makeText(context, "Weekly Reminders Cancelled Successfully", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            val notificationScheduler = NotificationScheduler(context)

                            // Schedule notifications
                            notificationScheduler.cancelDailyScheduledNotifications()

                            // Log the selected days, intervals, and frequency
                            // Show success toast
                            Toast.makeText(context, "Daily Reminders Canceled Successfully", Toast.LENGTH_SHORT).show()

                            Log.d("DetailScreen", "Cancel Notifications clicked")
                        }


                        },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification Icon",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp) // Adjust size as needed
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                    Text(
                        text = "Stop Notifications",
                        style = TextStyle(fontSize = 14.sp, color = Color.White)
                    )
                }
            }
        }

        // Main content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Days Container
            InfoCard(title = "Days:", content = if (selectedDays.isNotEmpty()) selectedDays.joinToString(", ") else "No days selected")

            // Frequency Container
            InfoCard(title = "Frequency:", content = "$frequency Minutes")

            // Category Container
            InfoCard(title = "Category:", content = category)

            // Display Intervals
            intervals.forEachIndexed { index, interval ->
                val startTimeString = interval["start"]?.toString() ?: return@forEachIndexed
                val endTimeString = interval["end"]?.toString() ?: return@forEachIndexed
                val formattedSlot = formatInterval(startTimeString, endTimeString)
                if (formattedSlot != null) {
                    InfoCard(title = "Slot ${index + 1}:", content = formattedSlot)
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFc9f2c7)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = TextStyle(fontSize = 20.sp, color = Color.Black))
            BasicText(text = content, style = TextStyle(fontSize = 16.sp, color = Color.Black))
        }
    }
}

fun fetchIntervals(preferencesManager: PreferencesManager): List<Map<String, Any>> {
    return try {
        preferencesManager.getIntervals()
    } catch (e: Exception) {
        Log.e("DetailScreen", "Error fetching intervals: ${e.message}", e)
        emptyList()
    }
}

fun formatInterval(startTimeString: String, endTimeString: String): String? {
    return try {
        val startTime = LocalTime.parse(startTimeString)
        val endTime = LocalTime.parse(endTimeString)
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        "Start: ${startTime.format(timeFormatter)}, End: ${endTime.format(timeFormatter)}"
    } catch (e: Exception) {
        Log.e("DetailScreen", "Error formatting time: $startTimeString or $endTimeString", e)
        null
    }
}
