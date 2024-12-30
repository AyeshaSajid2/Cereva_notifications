package cereva.MainScreens


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import androidx.compose.ui.platform.LocalContext


@Composable
fun DaySelectionDialog(
    context: Context,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val sharedPreferences = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    // Retrieve the saved selected days from shared preferences
    val savedSelectedDays = sharedPreferences.getStringSet("selected_days", emptySet())?.toList() ?: emptyList()

    var selectedState by remember { mutableStateOf(savedSelectedDays.toSet()) }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Log selected days before saving
                Log.d("DaySelection", "Days selected: ${selectedState.toList()}")

                // Save days to preferences
                saveSelectedDaysToPreferences(context, selectedState.toList())

                // Notify the parent component (e.g., the screen) that the save operation is complete
                onSave(selectedState.toList()) // This can be used to update UI or other state

                // Close the dialog
                onDismiss()

                // Show a Toast message for the user
                Toast.makeText(context, "Days Saved!", Toast.LENGTH_SHORT).show()

                // Log the save action
                Log.d("DaySelection", "Days saved to preferences: ${selectedState.toList()}")
            }) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        title = { Text("Select Days", color = Color.White) },
        text = {
            Column {
                daysOfWeek.forEach { day ->
                    val isSelected = day in selectedState
                    DayButton(
                        day = day,
                        isSelected = isSelected,
                        onClick = {
                            selectedState = if (isSelected) {
                                selectedState - day
                            } else {
                                selectedState + day
                            }
                        }
                    )
                }
            }
        },
        containerColor = Color(0xFF1E1E1E)
    )
}

@Composable
fun DayButton(day: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF4CAF50) else Color(0xFF121212)
    val textColor = if (isSelected) Color.White else Color.Gray

    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(backgroundColor),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = day,
            style = TextStyle(color = textColor, fontSize = 18.sp)
        )
    }
}

fun saveSelectedDaysToPreferences(context: Context, selectedDays: List<String>) {
    val sharedPreferences = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putStringSet("selected_days", selectedDays.toSet())
    }

    // Log the saving action
    Log.d("Preferences", "Saved days: ${selectedDays.toSet()}")
}


@Composable
@Preview
fun DaySelectionDialogPreview() {
//    DaySelectionDialog(
////        context = LocalContext.current,
////        selectedDays = listOf("Monday", "Wednesday", "Friday"),
////        onSave = { saveSelectedDaysToPreferences(LocalContext, it) },
////        onDismiss = {}
//    )
}
