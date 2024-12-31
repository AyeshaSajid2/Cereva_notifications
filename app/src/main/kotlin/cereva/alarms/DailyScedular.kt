package cereva.alarms

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import cereva.utills.PreferencesManager
import com.fremanrobots.cereva.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class NotificationScheduler(private val context: Context) {

    private val preferencesManager = PreferencesManager(context)
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun dailyscheduleNotifications() {
        val frequency = preferencesManager.getFrequency()
        val intervals = preferencesManager.getIntervals()

        Log.d("HomePage", "Intervals to use : $intervals")
        Log.d("HomePage", "Frequency to use : $frequency")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if exact alarms are permitted (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Permission Issue: Exact alarms not allowed.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        intervals.forEach { interval ->
            val startTime = interval["start"] as? LocalTime
            val endTime = interval["end"] as? LocalTime

            if (startTime != null && endTime != null) {
                var notificationTime = startTime
                while (notificationTime != null && !notificationTime.isAfter(endTime)) {
                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                        action = "SHOW_NOTIFICATION"
                        putExtra("RANDOM_MESSAGE", getRandomMessage())
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        notificationTime.toSecondOfDay(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val triggerTimeMillis = notificationTime.atDate(LocalDate.now())
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()

                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )

                    // Increment by frequency
                    notificationTime = notificationTime.plus(frequency.toLong(), ChronoUnit.MINUTES)
                }
            }
        }
    }

    private fun getRandomMessage(): String {
        val messages = context.resources.getStringArray(R.array.reminder_messages)
        return messages[Random.nextInt(messages.size)]
    }
}
