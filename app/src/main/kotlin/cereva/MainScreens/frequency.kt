package cereva.MainScreens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import cereva.utills.PreferencesManager

@Composable
fun FrequencySelectionScreen(
    context: Context,
    preferencesManager: PreferencesManager = PreferencesManager(context),
    onDismiss: () -> Unit
) {
    val frequencies = listOf(1, 3, 5, 10, 15, 20, 30, 40, 45, 50, 60)
    var selectedFrequency by remember { mutableStateOf(preferencesManager.getFrequency()) }
    var selectedCategory by remember { mutableStateOf(preferencesManager.getCategory()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Frequency", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1E1E1E))
                    .padding(16.dp)
            ) {
                // Weekly/Daily Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CategoryButton("Weekly", selectedCategory) {
                        selectedCategory = "Weekly"
                        preferencesManager.saveCategory(selectedCategory)
                        if (selectedFrequency < 15) selectedFrequency = 15
                    }
                    CategoryButton("Daily", selectedCategory) {
                        selectedCategory = "Daily"
                        preferencesManager.saveCategory(selectedCategory)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    frequencies.forEach { frequency ->
                        val isEnabled = selectedCategory == "Daily" || frequency >= 15
                        FrequencyButton(
                            frequency = frequency,
                            selectedFrequency = selectedFrequency,
                            isEnabled = isEnabled
                        ) {
                            if (isEnabled) selectedFrequency = frequency
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        preferencesManager.saveFrequency(selectedFrequency)
                        Toast.makeText(context, "Frequency saved: $selectedFrequency", Toast.LENGTH_SHORT).show()
                        onDismiss()
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
fun FrequencyButton(
    frequency: Int,
    selectedFrequency: Int,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !isEnabled -> Color(0xFF3E3E3E)
        frequency == selectedFrequency -> Color(0xFF4CAF50)
        else -> Color(0xFF121212)
    }
    val textColor = if (!isEnabled) Color.Gray else if (frequency == selectedFrequency) Color.White else Color.Gray

    TextButton(
        onClick = { if (isEnabled) onClick() },
        modifier = Modifier
            .padding(8.dp)
            .background(backgroundColor),
        enabled = isEnabled,
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = "$frequency",
            style = TextStyle(color = textColor, fontSize = 18.sp)
        )
    }
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


