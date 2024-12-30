package cereva.utills

import android.content.Context
import android.util.Log
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PreferencesManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    // Get the selected days from shared preferences
    fun getSelectedDays(): List<String> {
        val selectedDays = sharedPreferences.getStringSet("selected_days", emptySet())?.toList() ?: emptyList()
        Log.d("PreferencesManager", "Fetched selected days: $selectedDays")
        return selectedDays
    }

    // Get the frequency from shared preferences
    fun getFrequency(): Int {
        val frequency = sharedPreferences.getInt("frequency", 1)  // Default to 1
        Log.d("PreferencesManager", "Fetched frequency: $frequency")
        return frequency
    }

    // Get intervals from shared preferences

    fun getIntervals(): List<Map<String, Any>> {
        val intervals = mutableListOf<Map<String, Any>>()

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val numberOfIntervals = sharedPreferences.all.filterKeys { it.startsWith("start_time_") }.size

        for (index in 0 until numberOfIntervals) {
            try {
                val startTimeString = sharedPreferences.getString("start_time_$index", null)
                val endTimeString = sharedPreferences.getString("end_time_$index", null)

                // Log the retrieved data and its type
                Log.d("PreferencesManager", "Fetched Interval $index: Start - $startTimeString, End - $endTimeString")
                Log.d("PreferencesManager", "Type of data: StartTime - ${startTimeString?.javaClass}, EndTime - ${endTimeString?.javaClass}")

                // Handle null or empty values gracefully
                if (startTimeString != null && endTimeString != null) {
                    val startTime = LocalTime.parse(startTimeString, formatter)
                    val endTime = LocalTime.parse(endTimeString, formatter)
                    intervals.add(mapOf("start" to startTime, "end" to endTime))
                } else {
                    Log.e("PreferencesManager", "Failed to fetch interval for index $index. Start time or end time is null.")
                    // Optionally add a default value or skip this interval
                    intervals.add(mapOf("start" to "Error", "end" to "Error"))
                }
            } catch (e: Exception) {
                Log.e("PreferencesManager", "Error parsing interval for index $index: ${e.message}")
                intervals.add(mapOf("start" to "Error", "end" to "Error"))
            }
        }

        // Return the list of intervals, where errors are indicated with "Error"
        return intervals
    }

}
