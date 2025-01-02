package cereva.MainScreens

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.app.TimePickerDialog
import android.util.Log
import android.widget.TimePicker
import cereva.ui.theme.DarkGreen
import cereva.ui.theme.LightGreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditIntervalsDialog(
    context: Context,
    intervals: MutableList<Map<String, Any>>,
    onSaveIntervals: () -> Unit,
    onCancel: () -> Unit
) {
    val updatedIntervals by remember { mutableStateOf(intervals) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var selectedStartTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedEndTime by remember { mutableStateOf(LocalTime.now().plusHours(1)) }
    var intervalBeingEditedIndex by remember { mutableStateOf(-1) }


    // Time Picker Dialogs
    if (showStartTimePicker) {
        Toast.makeText(context, "Start time must be in the future", Toast.LENGTH_SHORT).show()

        val timePickerDialog = TimePickerDialog(
            context,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                selectedStartTime = LocalTime.of(hourOfDay, minute)
                showStartTimePicker = false
                showEndTimePicker = true
            },
            selectedStartTime.hour,
            selectedStartTime.minute,
            true
        )
        LaunchedEffect(Unit) {
            timePickerDialog.show()
        }
    }

    if (showEndTimePicker) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                selectedEndTime = LocalTime.of(hourOfDay, minute)
                showEndTimePicker = false
                if (intervalBeingEditedIndex == -1) {
                    // Adding new interval
                    if (updatedIntervals.size < 3) {
                        updatedIntervals.add(mapOf("start" to selectedStartTime, "end" to selectedEndTime))
                    } else {
                        Toast.makeText(context, "You can only add up to 3 intervals", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Editing existing interval
                    updatedIntervals[intervalBeingEditedIndex] = mapOf("start" to selectedStartTime, "end" to selectedEndTime)
                }
            },
            selectedEndTime.hour,
            selectedEndTime.minute,
            true
        )
        LaunchedEffect(Unit) {
            timePickerDialog.show()
        }
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("Save Intervals", color = DarkGreen)
        },
        text = {
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                // Displaying intervals
                updatedIntervals.forEachIndexed { index, interval ->
                    val start = interval["start"] as LocalTime
                    val end = interval["end"] as LocalTime
                    ListItem(
                        headlineContent = {
                            Text(
                                "Start: ${start.format(DateTimeFormatter.ofPattern("hh:mm a"))} - End: ${end.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                                color = Color.White
                            )
                        },
                        modifier = Modifier.clickable {
                            intervalBeingEditedIndex = index
                            selectedStartTime = start
                            selectedEndTime = end
                            showStartTimePicker = true
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Button to add interval
                Button(
                    onClick = {
                        if (updatedIntervals.size < 3) {
                            intervalBeingEditedIndex = -1
                            showStartTimePicker = true
                        } else {
                            Toast.makeText(context, "You can only add up to 3 intervals", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LightGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Interval", color = Color.Black)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (validateIntervals(updatedIntervals)) {
                    saveIntervalsToSharedPreferences(context, updatedIntervals)
                    onSaveIntervals()
                    Toast.makeText(context, "Intervals saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please ensure all intervals are valid", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Save", color = LightGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

// Function to validate intervals
@RequiresApi(Build.VERSION_CODES.O)
fun validateIntervals(intervals: List<Map<String, Any>>): Boolean {
    return intervals.all {
        val start = it["start"] as LocalTime
        val end = it["end"] as LocalTime
        start.isBefore(end)
    }
}

// Function to save intervals to SharedPreferences
@RequiresApi(Build.VERSION_CODES.O)
fun saveIntervalsToSharedPreferences(context: Context, intervals: List<Map<String, Any>>) {
    val sharedPreferences = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val savedIntervals = mutableListOf<String>()

    // Clear any existing intervals
    editor.clear()

    intervals.forEachIndexed { index, interval ->
        // Extract and format the start and end times
        val startTime = interval["start"] as LocalTime
        val endTime = interval["end"] as LocalTime
        val formattedStartTime = startTime.format(formatter)
        val formattedEndTime = endTime.format(formatter)

        // Save the formatted times to SharedPreferences
        editor.putString("start_time_$index", formattedStartTime)
        editor.putString("end_time_$index", formattedEndTime)

        // Log the data and its type
        Log.d("PreferencesManager", "Saved Interval $index: Start - $formattedStartTime, End - $formattedEndTime")
        Log.d("PreferencesManager", "Type of data: StartTime - ${startTime::class.java}, EndTime - ${endTime::class.java}")

        // Add saved interval to the list for Toast display
        savedIntervals.add("Start: $formattedStartTime, End: $formattedEndTime")
    }

    // Apply the changes to SharedPreferences
    editor.apply()

    // Show Toast with saved intervals data
    Toast.makeText(
        context,
        "Saved Intervals: \n${savedIntervals.joinToString("\n")}",
        Toast.LENGTH_LONG
    ).show()
}

// Function to load intervals from SharedPreferences
@RequiresApi(Build.VERSION_CODES.O)
fun loadIntervalsFromSharedPreferences(context: Context): MutableList<Map<String, Any>> {
    val sharedPreferences = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    val intervals = mutableListOf<Map<String, Any>>()

    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    for (index in 0 until 3) {
        val startTimeString = sharedPreferences.getString("start_time_$index", null)
        val endTimeString = sharedPreferences.getString("end_time_$index", null)
        if (!startTimeString.isNullOrEmpty() && !endTimeString.isNullOrEmpty()) {
            val startTime = LocalTime.parse(startTimeString, formatter)
            val endTime = LocalTime.parse(endTimeString, formatter)
            intervals.add(mapOf("start" to startTime, "end" to endTime))
        }
    }
    return intervals
}


