@file:OptIn(ExperimentalMaterial3Api::class)

package cereva.MainScreens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import cereva.alarms.scheduleReminders
import cereva.alarms.showNotification

@Composable
fun HomePage(navController: NavController, context: Context) {
    var selectedDays by remember { mutableStateOf(listOf<String>()) }
    //var frequency by remember { mutableStateOf("") }
    val frequency by remember { mutableStateOf(1) }
    var details by remember { mutableStateOf("") }
    var isDialogOpen by remember { mutableStateOf(DialogType.None) }
    var intervals by remember { mutableStateOf(mutableListOf<Map<String, Any>>()) }
    var isMultipleIntervalsEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Set Reminders",
            style = TextStyle(color = Color.White, fontSize = 24.sp)
        )

        RoundedButton("Days") { isDialogOpen = DialogType.Days }
        RoundedButton("Interval") { isDialogOpen = DialogType.Interval }
        RoundedButton("Frequency") { isDialogOpen = DialogType.Frequency }

        Button(
            onClick = {
                navController.navigate("detail")
            }, // Navigation to Detail Screen
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Detail Screen")
        }

        RoundedButton("hello") {
            showNotification(context)
            isDialogOpen = DialogType.None
        }

            // You can update intervals here with some sample data for testing


        RoundedButton("Save Reminder") {
            Log.d("HomePage", "Selected Days: $selectedDays")
            Log.d("HomePage", "Intervals: $intervals")
            Log.d("HomePage", "Frequency: $frequency")

            if (selectedDays.isNotEmpty() && intervals.isNotEmpty()) {
                scheduleReminders(context, selectedDays, intervals, frequency)
                Toast.makeText(context, "Reminders Scheduled Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please select days, intervals, and frequency", Toast.LENGTH_SHORT).show()
            }
        }


        when (isDialogOpen) {
            DialogType.Days -> DaySelectionDialog(
                context = context,
//                selectedDays = selectedDays,
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
                onCancel = {
                    isDialogOpen = DialogType.None
                }
            )
            DialogType.Frequency -> FrequencySelectionScreen(
                context = context,
                onDismiss = { isDialogOpen = DialogType.None }
            )
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
            .width(200.dp),
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
