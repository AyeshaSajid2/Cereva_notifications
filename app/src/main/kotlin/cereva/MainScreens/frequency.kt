package cereva.MainScreens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

@Composable
fun FrequencySelectionScreen(
    context: Context,
    onDismiss: () -> Unit
) {
    val frequencies = listOf(1,3,5, 10, 15, 20, 30, 40,45,50,60)
    var selectedFrequency by remember { mutableStateOf(1) }
    var selectedCategory by remember { mutableStateOf("Daily") }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Frequency", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1E1E1E))
                    .padding(16.dp)
            ) {
                // Frequency Category Selection (Daily, Weekly, Monthly)

                // Frequency Selection
                Text("Select Frequency:", color = Color.White, style = TextStyle(fontSize = 18.sp))

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Scrollable Row for Frequency Selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    frequencies.forEach { frequency ->
                        FrequencyButton(frequency, selectedFrequency) {
                            selectedFrequency = frequency
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        saveFrequencyToPreferences(context, selectedFrequency)
                        Toast.makeText(context, "Frequency saved: $selectedFrequency", Toast.LENGTH_SHORT).show()
                        onDismiss()  // Close the dialog after saving
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Save Frequency", color = Color.Black)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E1E1E)
    )
}

@Composable
fun CategoryButton(category: String, selectedCategory: String, onClick: () -> Unit) {
    val backgroundColor = if (category == selectedCategory) Color(0xFF4CAF50) else Color(0xFF121212)
    val textColor = if (category == selectedCategory) Color.White else Color.Gray

    TextButton(
        onClick = onClick,
        modifier = Modifier
            .padding(8.dp)
            .background(backgroundColor),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = category,
            style = TextStyle(color = textColor, fontSize = 16.sp)
        )
    }
}

@Composable
fun FrequencyButton(frequency: Int, selectedFrequency: Int, onClick: () -> Unit) {
    val backgroundColor = if (frequency == selectedFrequency) Color(0xFF4CAF50) else Color(0xFF121212)
    val textColor = if (frequency == selectedFrequency) Color.White else Color.Gray

    TextButton(
        onClick = onClick,
        modifier = Modifier
            .padding(8.dp)
            .background(backgroundColor),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = "$frequency",
            style = TextStyle(color = textColor, fontSize = 18.sp)
        )
    }
}

fun saveFrequencyToPreferences(context: Context, frequency: Int) {
    val sharedPreferences = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putInt("selected_frequency", frequency)
    }
}

@Composable
@Preview
fun FrequencySelectionScreenPreview() {
    FrequencySelectionScreen(
        context = LocalContext.current,
        onDismiss = {}
    )
}
