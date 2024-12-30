package cereva.MainScreens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cereva.utills.PreferencesManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DetailScreen() {
    // Get the context using LocalContext.current
    val context = LocalContext.current

    // Initialize PreferencesManager with the context
    val preferencesManager = remember { PreferencesManager(context) }

    // Fetch selected days, frequency, and intervals using safe function calls
    val selectedDays = remember { preferencesManager.getSelectedDays() }
    val frequency = remember { preferencesManager.getFrequency() }
    val intervals = remember { fetchIntervals(preferencesManager) } // Safe call to get intervals

    // Log the values to debug
    Log.d("DetailScreen", "Selected Days: $selectedDays")
    Log.d("DetailScreen", "Frequency: $frequency")
    Log.d("DetailScreen", "Intervals: $intervals")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center) // Center the column vertically and horizontally
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center // Center the content inside the column
        ) {
            Spacer(modifier = Modifier.height(24.dp)) // Add blank space at the top

            // Days Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)), // Green color
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Days:",
                        style = TextStyle(fontSize = 20.sp, color = Color.White)
                    )
                    BasicText(
                        text = if (selectedDays.isNotEmpty()) selectedDays.joinToString(", ") else "No days selected",
                        style = TextStyle(fontSize = 16.sp, color = Color.White)
                    )
                }
            }

            // Frequency Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)), // Green color
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Frequency:",
                        style = TextStyle(fontSize = 20.sp, color = Color.White)
                    )
                    BasicText(
                        text = "$frequency Minutes",
                        style = TextStyle(fontSize = 16.sp, color = Color.White)
                    )
                }
            }

            // Display Intervals (with safe data)
            intervals.forEachIndexed { index, interval ->
                val startTimeString = interval["start"]?.toString() ?: return@forEachIndexed
                val endTimeString = interval["end"]?.toString() ?: return@forEachIndexed

                // Format and display time slots in safe way
                val formattedSlot = formatInterval(startTimeString, endTimeString)
                if (formattedSlot != null) {
                    // Slot Display Container
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)), // Green color
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Slot ${index + 1}:",
                                style = TextStyle(fontSize = 20.sp, color = Color.White)
                            )
                            BasicText(
                                text = formattedSlot,
                                style = TextStyle(fontSize = 16.sp, color = Color.White)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Safe function to fetch intervals
fun fetchIntervals(preferencesManager: PreferencesManager): List<Map<String, Any>> {
    return try {
        // Assuming your method fetches intervals and returns them
        preferencesManager.getIntervals()
    } catch (e: Exception) {
        Log.e("DetailScreen", "Error fetching intervals: ${e.message}", e)
        emptyList() // Return empty list in case of error
    }
}

// Safe function to format the interval
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
