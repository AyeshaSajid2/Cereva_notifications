package cereva.utills

import android.content.Context
import androidx.core.content.edit
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PreferencesManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    companion object {
        const val KEY_SELECTED_DAYS = "selected_days"
        const val KEY_SELECTED_FREQUENCY = "selected_frequency"
        const val KEY_START_TIME_PREFIX = "start_time_"
        const val KEY_END_TIME_PREFIX = "end_time_"
    }

    // Get the selected days from shared preferences
    fun getSelectedDays(): List<String> {
        val selectedDaysSet = sharedPreferences.getStringSet(KEY_SELECTED_DAYS, emptySet())
        return selectedDaysSet?.toList() ?: emptyList()
    }

    // Save the selected days
    fun saveSelectedDays(days: List<String>) {
        editor.putStringSet(KEY_SELECTED_DAYS, days.toSet()).apply()
    }

    // Get the frequency from shared preferences
    /*fun getFrequency(): Int {
        return sharedPreferences.getInt(KEY_SELECTED_FREQUENCY, 0)
    }*/
    // Get the frequency from shared preferences
    fun getFrequency(): Int {
        return sharedPreferences.getInt(KEY_SELECTED_FREQUENCY, 1) // Default to 1
    }
    // Save the frequency
    fun saveFrequency(frequency: Int) {
        sharedPreferences.edit {
            putInt(KEY_SELECTED_FREQUENCY, frequency)
            apply()
        }
    }

    // Get intervals from shared preferences
    fun getIntervals(): List<Map<String, Any>> {
        val intervals = mutableListOf<Map<String, Any>>()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val numberOfIntervals = sharedPreferences.all.filterKeys { it.startsWith(KEY_START_TIME_PREFIX) }.size

        for (index in 0 until numberOfIntervals) {
            val startTimeString = sharedPreferences.getString("$KEY_START_TIME_PREFIX$index", null)
            val endTimeString = sharedPreferences.getString("$KEY_END_TIME_PREFIX$index", null)

            if (startTimeString != null && endTimeString != null) {
                val startTime = LocalTime.parse(startTimeString, formatter)
                val endTime = LocalTime.parse(endTimeString, formatter)
                intervals.add(mapOf("start" to startTime, "end" to endTime))
            } else {
                intervals.add(mapOf("start" to "Error", "end" to "Error"))
            }
        }

        return intervals
    }

    // Save an interval
    fun saveInterval(index: Int, startTime: LocalTime, endTime: LocalTime) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        editor.putString("$KEY_START_TIME_PREFIX$index", startTime.format(formatter))
        editor.putString("$KEY_END_TIME_PREFIX$index", endTime.format(formatter))
        editor.apply()
    }

    fun saveCategory(category: String) {
        sharedPreferences.edit {
            putString("selected_category", category)
        }
    }

    fun getCategory(): String {
        return sharedPreferences.getString("selected_category", "Daily") ?: "Daily"
    }
    // Function to get the start time of a specific interval by index
    fun getStartTime(index: Int): LocalTime? {
        val startTimeString = sharedPreferences.getString("$KEY_START_TIME_PREFIX$index", null)
        return startTimeString?.let {
            LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
        }
    }

}
