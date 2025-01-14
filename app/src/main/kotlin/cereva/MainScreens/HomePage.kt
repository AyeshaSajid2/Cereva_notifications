@file:OptIn(ExperimentalMaterial3Api::class)

package cereva.MainScreens

import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.darkColorScheme
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
import cereva.utills.PreferencesManager
import java.time.LocalDate
import java.time.ZoneId
import android.bluetooth.BluetoothAdapter
import cereva.bluetooth.BluetoothUtils
import android.os.Build
import android.provider.Settings
import cereva.services.KeepAliveService
import sendNotificationToSmartwatch

fun startKeepAliveService(context: Context) {
    val serviceIntent = Intent(context, KeepAliveService::class.java)  // Use context to create the Intent
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)  // Use context to call startForegroundService
    } else {
        context.startService(serviceIntent)  // Use context to call startService
    }
}


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
            .background(Color.White)
            .padding(25.dp),
        verticalArrangement = Arrangement.Center, // Center all items vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center all items horizontally
    ) {
        // Heading text

        // Buttons for selecting days, interval, frequency, and navigating to the detail screen

        RoundedButton("Slots") { isDialogOpen = DialogType.Interval }
        Spacer(modifier = Modifier.height(24.dp)) // Add blank space at the top

        RoundedButton("Frequency") { isDialogOpen = DialogType.Frequency }
        Spacer(modifier = Modifier.height(24.dp)) // Add blank space at the top
        // Buttons for selecting days, interval, frequency, and navigating to the detail screen

        RoundedButton("Days") { isDialogOpen = DialogType.Days }
        Spacer(modifier = Modifier.height(24.dp)) // Add blank space at the top

        // Detail Screen Button styled the same as other buttons
        RoundedButton("Preview") {
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
            val currentTime = System.currentTimeMillis()
            val index = 0
            val startTime = preferencesManager.getStartTime(index) // Replace this with the actual method to get the start time.
            Log.d("HomePage", "Current Time: $currentTime")
            Log.d("HomePage", "Start Time: $startTime")
           // val startTimeInMillis = startTime.atZone(zoneId).toInstant().toEpochMilli()

            // Ensure startTime is not null before comparing
                val startTimeMillis = startTime?.atDate(LocalDate.now()) // Combine LocalTime with current date
                    ?.atZone(ZoneId.systemDefault()) // Convert to ZonedDateTime
                    ?.toInstant() // Convert to Instant
                    ?.toEpochMilli() // Convert to milliseconds
                Log.d("HomePage", "Start Time IN MILIS : $startTimeMillis")

                if (intervalsToUse.isNotEmpty() ) {
                    if(startTimeMillis!! >= currentTime ){
                        if (category == "Weekly" && daysToUse.isNotEmpty()) {
                            scheduleReminders(context, daysToUse, intervalsToUse, frequencyToUse)
                            Log.d("HomePage", "Selected Days to use: $daysToUse")
                            Log.d("HomePage", "Intervals to use : $intervalsToUse")
                            Log.d("HomePage", "Frequency to use : $frequencyToUse")
                            Toast.makeText(context, "Reminders Scheduled Successfully", Toast.LENGTH_SHORT).show()
                        }
                        else if (frequencyToUse <= 10){
                            startKeepAliveService(context);
                            // Initialize the NotificationScheduler
                            val notificationScheduler = NotificationScheduler(context)

                            // Schedule notifications
                            notificationScheduler.dailyscheduleNotifications()

                            // Log the selected days, intervals, and frequency
                            // Show success toast
                            Toast.makeText(context, "Reminders Scheduled Successfully for a day with foreground activity", Toast.LENGTH_SHORT).show()

                        }
                        else if (frequencyToUse > 10)
                        {
                            startKeepAliveService(context);
                            // Initialize the NotificationScheduler
                            val notificationScheduler = NotificationScheduler(context)

                            // Schedule notifications
                            notificationScheduler.dailyscheduleNotifications()

                            // Log the selected days, intervals, and frequency
                            // Show success toast
                            Toast.makeText(context, "Reminders Scheduled Successfully for a day", Toast.LENGTH_SHORT).show()

                        }

                    }
                    else{
                        Toast.makeText(context, "Interval start time must be in future", Toast.LENGTH_SHORT).show()

                    }

                }
                else if (daysToUse.isEmpty()) {
                    Toast.makeText(context, "Please select days", Toast.LENGTH_SHORT).show()
                } else if (intervalsToUse.isEmpty()) {
                    Toast.makeText(context, "Please select Slots", Toast.LENGTH_SHORT).show()
                } else if (frequencyToUse < 15) {
                    Toast.makeText(context, "Frequency duration lesser than minimum allowed value", Toast.LENGTH_SHORT).show()
                }
            else {
                // Handle the case where startTime is null
                Toast.makeText(context, "Start time is invalid", Toast.LENGTH_SHORT).show()
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Add blank space at the top

        RoundedButton("Save Reminders to Smart Watch") {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                Toast.makeText(context, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show()
            } else if (!bluetoothAdapter.isEnabled) {
                // Navigate to Bluetooth settings
                Toast.makeText(context, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))  // Corrected reference
            } else {
                // Pair with smartwatch
                if (BluetoothUtils.pairWithSmartwatch(context, bluetoothAdapter)) {
                    Toast.makeText(context, "Smartwatch paired successfully", Toast.LENGTH_SHORT).show()
                    sendNotificationToSmartwatch(context, "This is your smartwatch notification!")

                } else {
                    Toast.makeText(context, "Failed to pair with smartwatch", Toast.LENGTH_SHORT).show()
                }
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
                    Toast.makeText(context, "Slots saved successfully", Toast.LENGTH_SHORT).show()
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
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFc9f2c7)),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .width(200.dp), // Same width for all buttons
        contentPadding = PaddingValues(12.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(color = Color.Black, fontSize = 16.sp)
        )
    }
}


enum class DialogType {
    None, Days, Interval, Frequency, Details
}
