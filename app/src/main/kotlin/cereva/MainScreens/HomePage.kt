@file:OptIn(ExperimentalMaterial3Api::class)

package cereva.MainScreens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cereva.alarms.NotificationScheduler
import cereva.alarms.scheduleReminders
import cereva.alarms.showNotification
import cereva.utills.PreferencesManager

@Composable
fun HomePage(navController: NavController, context: Context) {
    var selectedDays by remember { mutableStateOf(listOf<String>()) }
    var frequency by remember { mutableStateOf(0) } // Initialize frequency with a default integer value
    var details by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(DialogType.None) }
    var intervals by remember { mutableStateOf(mutableListOf<Map<String, Any>>()) }

    // Preferences Manager instance
    val preferencesManager = PreferencesManager(context)

    // Fetch saved data from SharedPreferences if no new data
    val savedDays = preferencesManager.getSelectedDays()
    val savedFrequency = preferencesManager.getFrequency()
    val savedIntervals = preferencesManager.getIntervals()
    val category = preferencesManager.getCategory()

    // Centering all elements inside a column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(25.dp),
        verticalArrangement = Arrangement.Center, // Center all items vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center all items horizontally
    ) {
        // Heading text

        // Buttons for selecting days, interval, frequency, and navigating to the detail screen
        RoundedButton("Days") { isDialogOpen = DialogType.Days }
        Spacer(modifier = Modifier.height(24.dp)) // Add blank space at the top

        RoundedButton("Interval") { isDialogOpen = DialogType.Interval }
        Spacer(modifier = Modifier.height(24.dp)) // Add blank space at the top

        RoundedButton("Frequency") { isDialogOpen = DialogType.Frequency }
        Spacer(modifier = Modifier.height(24.dp)) // Add blank space at the top

        // Detail Screen Button styled the same as other buttons
        RoundedButton("Detail Screen") {
            navController.navigate("detail") // Navigation to Detail Screen
        }
        Spacer(modifier = Modifier.height(24.dp)) // Add blank space at the top

        // Save Reminder Button
        RoundedButton("Save Reminder") {
            Log.d("HomePage", "Selected Days: $selectedDays")
            Log.d("HomePage", "Intervals: $intervals")
            Log.d("HomePage", "Frequency: $frequency")

            // Check if new data exists, otherwise use saved data
            val daysToUse = if (selectedDays.isEmpty()) savedDays else selectedDays
            val intervalsToUse = if (intervals.isEmpty()) savedIntervals else intervals
            val frequencyToUse = if (frequency == 0) savedFrequency else frequency

            if ( intervalsToUse.isNotEmpty() ) {
                if (category == "Daily" && frequencyToUse >= 15 && daysToUse.isNotEmpty()) {
                    scheduleReminders(context, daysToUse, intervalsToUse, frequencyToUse)
                    Log.d("HomePage", "Selected Days to use: $daysToUse")
                    Log.d("HomePage", "Intervals to use : $intervalsToUse")
                    Log.d("HomePage", "Frequency to use : $frequencyToUse")
                    Toast.makeText(context, "Reminders Scheduled Suc cessfully", Toast.LENGTH_SHORT).show()
                }
                else {
                    // Initialize the NotificationScheduler
                    val notificationScheduler = NotificationScheduler(context)

                    // Schedule notifications
                    notificationScheduler.dailyscheduleNotifications()

                    // Log the selected days, intervals, and frequency
                    // Show success toast
                    Toast.makeText(context, "Reminders Scheduled Successfully for a day", Toast.LENGTH_SHORT).show()
                }


            }
            else if (daysToUse.isEmpty()) {
                Toast.makeText(context, "Please select days", Toast.LENGTH_SHORT).show()
            } else if (intervalsToUse.isEmpty()) {
                Toast.makeText(context, "Please select Interval", Toast.LENGTH_SHORT).show()
            } else if (frequencyToUse < 15) {
                Toast.makeText(context, "Frequency duration lesser than minimum allowed value", Toast.LENGTH_SHORT).show()
            }
        }

        // Dialogs to handle Day, Interval, Frequency selection
        when (isDialogOpen) {
            DialogType.Days -> DaySelectionDialog(
                context = context,
                onSave = { selectedDays = it },
                onDismiss = { isDialogOpen = DialogType.None }
            )
            DialogType.Interval -> EditIntervalsDialog(
                context = context,
                intervals = intervals,
                onSaveIntervals = {
                    Toast.makeText(context, "Intervals saved successfully", Toast.LENGTH_SHORT).show()
                    isDialogOpen = DialogType.None
                },
                onCancel = { isDialogOpen = DialogType.None }
            )
            DialogType.Frequency -> {
                val preferencesManager = PreferencesManager(context)
                FrequencySelectionScreen(
                    context = context,
                    preferencesManager = preferencesManager, // Pass the PreferencesManager
                    onDismiss = { isDialogOpen = DialogType.None }
                )
            }

            DialogType.None -> {}
            else -> {}
        }
    }
}



@Composable
fun RoundedButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .width(200.dp), // Same width for all buttons
        contentPadding = PaddingValues(12.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(color = Color.White, fontSize = 16.sp)
        )
    }
}

enum class DialogType {
    None, Days, Interval, Frequency, Details
}
