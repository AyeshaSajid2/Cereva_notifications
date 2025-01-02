package cereva.MainScreens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cereva.ui.theme.DarkGreen
import cereva.ui.theme.LightGreen
import cereva.utills.PreferencesManager

@Composable
fun DaySelectionDialog(
    context: Context,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val preferencesManager = PreferencesManager(context)

    // Retrieve the saved selected days from shared preferences
    val savedSelectedDays = preferencesManager.getSelectedDays()

    var selectedState by remember { mutableStateOf(savedSelectedDays.toSet()) }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                Log.d("DaySelection", "Days selected: ${selectedState.toList()}")

                // Save days using PreferencesManager
                preferencesManager.saveSelectedDays(selectedState.toList())

                // Notify parent about saved days
                onSave(selectedState.toList())

                // Close the dialog
                onDismiss()

                // Toast message for user feedback
                Toast.makeText(context, "Days Saved!", Toast.LENGTH_SHORT).show()
                Log.d("DaySelection", "Days saved to preferences: ${selectedState.toList()}")
            }) {
                Text("Save", color = LightGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        title = { Text("Select Days", color = DarkGreen) },
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
