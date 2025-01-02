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
import android.provider.Settings


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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (!alarmManager.canScheduleExactAlarms()) {
                // Show a message to the user
                Toast.makeText(context, "Permission required for exact alarms.", Toast.LENGTH_SHORT).show()

                // Direct the user to the settings page
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
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
