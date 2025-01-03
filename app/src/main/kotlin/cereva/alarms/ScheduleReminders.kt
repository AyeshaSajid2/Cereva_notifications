package  cereva.alarms


import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.work.*
import cereva.MainScreens.FullScreenActivity
import com.fremanrobots.cereva.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// Schedule reminders
fun scheduleReminders(
    context: Context,
    selectedDays: List<String>,
    intervals: List<Map<String, Any>>,
    frequency: Int
) {
    val workManager = WorkManager.getInstance(context)
    workManager.cancelAllWorkByTag("ReminderWork") // Clear existing reminders

    selectedDays.forEach { day ->
        intervals.forEach { interval ->
            val startTime = interval["start"]?.toString() ?: return@forEach
            val endTime = interval["end"]?.toString() ?: return@forEach

            try {
                val start = LocalTime.parse(startTime)
                val end = LocalTime.parse(endTime)

                val delay = calculateDelayForDay(day, start)
                val inputData = workDataOf(
                    "startTime" to start.toString(),
                    "endTime" to end.toString(),
                    "selectedDay" to day,
                    "frequency" to frequency
                )

                val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
                    frequency.toLong(), TimeUnit.MINUTES
                )
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("ReminderWork")
                    .build()

                workManager.enqueue(workRequest)
            } catch (e: Exception) {
                // Handle parsing issues
            }
        }
    }
}



// Calculate delay for the target day
private fun calculateDelayForDay(day: String, startTime: LocalTime): Long {
    val now = LocalDateTime.now()
    val targetDay = DayOfWeek.valueOf(day.uppercase())
    val targetDateTime = now.with(TemporalAdjusters.nextOrSame(targetDay))
        .withHour(startTime.hour)
        .withMinute(startTime.minute)

    return if (targetDateTime.isBefore(now)) {
        Duration.between(now, targetDateTime.plusWeeks(1)).toMillis()
    } else {
        Duration.between(now, targetDateTime).toMillis()
    }
}

// Notification Worker

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val startTime = LocalTime.parse(inputData.getString("startTime"))
        val endTime = LocalTime.parse(inputData.getString("endTime"))
        val selectedDay = inputData.getString("selectedDay")
        val frequency = inputData.getInt("frequency", 60)
        val now = LocalTime.now()

        // Cancel the work request if it is after the end time
        if (now.isAfter(endTime)) {
            // Schedule the next work request for the next selected day
            if (selectedDay != null) {
                scheduleNextReminder(applicationContext, selectedDay, startTime, endTime, frequency)
            }
            return@withContext Result.success()
        }

        // Skip notifications outside the time window
        if (now.isBefore(startTime)) {
            return@withContext Result.success()
        }

        // Select a random message
        val messages = applicationContext.resources.getStringArray(R.array.reminder_messages)
        val randomMessage = messages[Random.nextInt(messages.size)]

        // Show notification
        showNotification(applicationContext, "Reminder", randomMessage)
        Result.success()
    }
}
fun scheduleNextReminder(context: Context, selectedDay: String, startTime: LocalTime, endTime: LocalTime, frequency: Int) {
    val workManager = WorkManager.getInstance(context)
    val delay = calculateDelayForDay(selectedDay, startTime)
    val inputData = workDataOf(
        "startTime" to startTime.toString(),
        "endTime" to endTime.toString(),
        "selectedDay" to selectedDay,
        "frequency" to frequency
    )

    val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
        frequency.toLong(), TimeUnit.MINUTES
    )
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(inputData)
        .addTag("ReminderWork")
        .build()

    workManager.enqueue(workRequest)
}

// Show notification with custom sound and screen wake functionality
fun showNotification(context: Context, title: String, message: String) {
    val channelId = "reminder_notifications"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create Notification Channel for Android O and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Reminder",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    // Intent to launch the FullScreenActivity
    val fullScreenIntent = Intent(context, FullScreenActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("NOTIFICATION_MESSAGE", message) // Pass the message to the activity
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        System.currentTimeMillis().toInt(), // Unique request code
        fullScreenIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val customSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification_icon_round)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSound(customSoundUri)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    // Check if the app is in the foreground
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcesses = activityManager.runningAppProcesses

    val isAppInForeground = appProcesses.any { processInfo ->
        processInfo.processName == context.packageName &&
                processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    if (isAppInForeground) {
        // App is in the foreground - start FullScreenActivity directly with sound
        context.startActivity(fullScreenIntent)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "app:NotificationWakeLock"
        )
        wakeLock.acquire(3000) // Wake the screen for 3 seconds
    } else {
        // App is in the background - show the notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}


/*import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

fun ScheduleReminders(context: Context, selectedDays: List<String>, intervals: List<Map<String, Any>>, frequency: Int) {
    val currentDay = LocalDateTime.now().dayOfWeek.name.toLowerCase()

    Log.d("ScheduleDebug", "Current Day: $currentDay")
    Log.d("ScheduleDebug", "Selected Days: $selectedDays")
    Log.d("ScheduleDebug", "Intervalssssss: $intervals")

    if (selectedDays.map { it.toLowerCase() }.contains(currentDay)) {
        val currentTime = LocalTime.now()
        Log.d("ScheduleDebug", "Current Time: $currentTime")

        intervals.forEach { interval ->
            // Ensure that 'start' and 'end' are not null and are valid strings
            val startTimeString = interval["start"] as? String
            val endTimeString = interval["end"] as? String

            Log.d("IntervalDebug", "Start: $startTimeString, End: $endTimeString")

            if (!startTimeString.isNullOrEmpty() && !endTimeString.isNullOrEmpty()) {
                try {
                    val startTime = LocalTime.parse(startTimeString)
                    val endTime = LocalTime.parse(endTimeString)

                    Log.d("IntervalDebug", "Parsed Start Time: $startTime, End Time: $endTime")

                    val currentTime = LocalTime.now()

                    if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                        scheduleNotifications(context, currentDay, startTime.toString(), endTime.toString(), frequency)
                    }
                } catch (e: DateTimeParseException) {
                    Log.e("IntervalDebug", "Invalid time format: Start=$startTimeString, End=$endTimeString", e)
                }
            } else {
                Log.e("IntervalDebug", "Start or end time is missing or invalid. Start=$startTimeString, End=$endTimeString")
            }
        }
    }
}


fun scheduleNotifications(context: Context, day: String, startTime: String, endTime: String, frequency: Int) {
    val workManager = WorkManager.getInstance(context)

    if (frequency < 15) {
        throw IllegalArgumentException("Frequency must be at least 15 minutes.")
    }

    val delay = calculateDelay(day, startTime)
    val intervalMillis = TimeUnit.MINUTES.toMillis(frequency.toLong())

    val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(intervalMillis, TimeUnit.MILLISECONDS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("day" to day, "startTime" to startTime, "endTime" to endTime))
        .build()

    workManager.enqueue(workRequest)

    Log.d("ReminderScheduler", "Notification scheduled for $day from $startTime to $endTime with frequency $frequency minutes.")
}

fun calculateDelay(day: String, startTime: String): Long {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val currentDateTime = LocalDateTime.now()
    val targetTime = LocalTime.parse(startTime, formatter)
    val targetDateTime = currentDateTime
        .with(DayOfWeek.valueOf(day.toUpperCase()))
        .withHour(targetTime.hour)
        .withMinute(targetTime.minute)
        .withSecond(0)
        .withNano(0)

    return if (targetDateTime.isBefore(currentDateTime)) {
        Duration.between(currentDateTime, targetDateTime.plusWeeks(1)).toMillis()
    } else {
        Duration.between(currentDateTime, targetDateTime).toMillis()
    }
}


class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val day = inputData.getString("day") ?: "saturday"
        val startTime = inputData.getString("startTime") ?: "12:00 pm"
        val endTime = inputData.getString("endTime") ?: "02:00 pm"

        Log.d("NotificationWorker", "Showing notification for $day from $startTime to $endTime")
        showNotification(applicationContext, "Reminder", "Reminder for $day from $startTime to $endTime")
        Result.success()
    }
}

fun showNotification(context: Context, title: String, message: String) {
    val channelId = "reminder_notifications"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
}

class ReminderForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "foreground_service"
            val channel = NotificationChannel(channelId, "Foreground Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Reminder Service")
                .setContentText("Your reminders are active.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()

            startForeground(1, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}


//package cereva.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.fremanrobots.cereva.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Random
import java.util.concurrent.TimeUnit

/*
fun scheduleReminders(
    context: Context,
    selectedDays: List<String>,
    intervals: List<Map<String, Any>>,
    frequency: Int
) {
    val workManager = WorkManager.getInstance(context)

    // Cancel all existing scheduled reminders
    workManager.cancelAllWorkByTag("ReminderWork")

    val currentDay = LocalDateTime.now().dayOfWeek.name.lowercase()
    if (selectedDays.map { it.lowercase() }.contains(currentDay)) {
        val currentTime = LocalTime.now()

        intervals.forEach { interval ->
            val startTime = interval["start"]?.toString() ?: return@forEach
            val endTime = interval["end"]?.toString() ?: return@forEach

            try {
                val start = LocalTime.parse(startTime)
                val end = LocalTime.parse(endTime)

                if (currentTime.isAfter(start) && currentTime.isBefore(end)) {
                    Log.d(
                        "ScheduleReminder",
                        "Scheduling notification for $currentDay at ${start.formatTimeWithDate()} to ${end.formatTimeWithDate()}"
                    )
                    scheduleNotifications(context, start, end, frequency)
                }
            } catch (e: Exception) {
                Log.e("ScheduleReminder", "Error parsing time: $startTime or $endTime", e)
            }
        }
    }
}**///

fun scheduleReminders(
    context: Context,
    selectedDays: List<String>,
    intervals: List<Map<String, Any>>,
    frequency: Int
) {
    val workManager = WorkManager.getInstance(context)
    workManager.cancelAllWorkByTag("ReminderWork") // Cancel existing reminders

    selectedDays.forEach { selectedDay ->
        intervals.forEach { interval ->
            val startTimeString = interval["start"]?.toString() ?: return@forEach
            val endTimeString = interval["end"]?.toString() ?: return@forEach

            try {
                val startTime = LocalTime.parse(startTimeString)
                val endTime = LocalTime.parse(endTimeString)

                Log.d(
                    "ScheduleReminder",
                    "Scheduling notification for $selectedDay at $startTimeString to $endTimeString"
                )
                scheduleNotificationsForDay(context, selectedDay, startTime, endTime, frequency)
            } catch (e: Exception) {
                Log.e("ScheduleReminder", "Error parsing time: $startTimeString or $endTimeString", e)
            }
        }
    }
}
private fun scheduleNotificationsForDay(
    context: Context,
    day: String,
    startTime: LocalTime,
    endTime: LocalTime,
    frequency: Int
) {
    val workManager = WorkManager.getInstance(context)
    val delay = calculateDelayForDay(day, startTime)
    val intervalMillis = TimeUnit.MINUTES.toMillis(frequency.toLong())

    val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(intervalMillis, TimeUnit.MILLISECONDS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(
            workDataOf(
                "day" to day,
                "startTime" to startTime.toString(),
                "endTime" to endTime.toString(),
                "frequency" to frequency
            )
        )
        .addTag("ReminderWork")
        .build()

    workManager.enqueue(workRequest)
    Log.d("ScheduleNotification", "Notification scheduled for $day at $startTime.")
}

private fun calculateDelayForDay(day: String, startTime: LocalTime): Long {
    val now = LocalDateTime.now()
    val targetDay = DayOfWeek.valueOf(day.uppercase())
    val targetDateTime = now.with(TemporalAdjusters.nextOrSame(targetDay))
        .withHour(startTime.hour)
        .withMinute(startTime.minute)
        .withSecond(0)
        .withNano(0)

    return Duration.between(now, targetDateTime).toMillis()
}

private fun scheduleNotifications(
    context: Context,
    startTime: LocalTime,
    endTime: LocalTime,
    frequency: Int
) {
    val workManager = WorkManager.getInstance(context)
    val delay = calculateInitialDelay(startTime)
    val intervalMillis = TimeUnit.MINUTES.toMillis(frequency.toLong())

    val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(intervalMillis, TimeUnit.MILLISECONDS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(
            workDataOf(
                "startTime" to startTime.formatTimeWithDate(),
                "endTime" to endTime.formatTimeWithDate(),
                "frequency" to frequency
            )
        )
        .addTag("ReminderWork") // Tag for identifying and canceling reminders
        .build()

    workManager.enqueueUniquePeriodicWork(
        "ReminderWork",
        ExistingPeriodicWorkPolicy.REPLACE,
        workRequest
    )

    Log.d("ScheduleNotification", "Notification scheduled at ${startTime.formatTimeWithDate()}.")
}


private fun calculateInitialDelay(startTime: LocalTime): Long {
    val now = LocalTime.now()
    val delayDuration = if (now.isBefore(startTime)) {
        Duration.between(now, startTime)
    } else {
        Duration.between(now, startTime.plusHours(24))
    }
    return delayDuration.toMillis()
}

private fun LocalTime.formatTimeWithDate(): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, hh:mm a")
    return this.atDate(LocalDate.now()).format(formatter)
}
*/
/*package cereva.alarms

import ReminderWorker
import android.content.Context
import androidx.work.*
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import android.util.Log

fun scheduleReminders(
    context: Context,
    selectedDays: List<String>,
    intervals: List<Map<String, Any>>,
    frequency: Int
) {
    val workManager = WorkManager.getInstance(context)
    workManager.cancelAllWorkByTag("ReminderWork") // Cancel existing reminders

    selectedDays.forEach { day ->
        intervals.forEach { interval ->
            val startTimeString = interval["start"]?.toString() ?: return@forEach
            val endTimeString = interval["end"]?.toString() ?: return@forEach

            try {
                val startTime = LocalTime.parse(startTimeString)
                val endTime = LocalTime.parse(endTimeString)

                Log.d("ScheduleReminder", "Scheduling for $day from $startTimeString to $endTimeString")
                scheduleNotificationsForDay(context, day, startTime, endTime, frequency)
            } catch (e: Exception) {
                Log.e("ScheduleReminder", "Error parsing times: $startTimeString or $endTimeString", e)
            }
        }
    }
}

private fun scheduleNotificationsForDay(
    context: Context,
    day: String,
    startTime: LocalTime,
    endTime: LocalTime,
    frequency: Int
) {
    val workManager = WorkManager.getInstance(context)

    // Calculate the first notification time based on the day and start time
    val delay = calculateDelayForDay(day, startTime)

    // Create a periodic work request with the frequency
    val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(frequency.toLong(), TimeUnit.MINUTES)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(
            workDataOf(
                "day" to day,
                "startTime" to startTime.toString(),
                "endTime" to endTime.toString(),
                "frequency" to frequency
            )
        )
        .addTag("ReminderWork")
        .build()

    workManager.enqueue(workRequest)
    Log.d("ScheduleNotification", "Scheduled notification for $day starting at $startTime with frequency $frequency minutes.")
}

private fun calculateDelayForDay(day: String, startTime: LocalTime): Long {
    val now = LocalDateTime.now()
    val targetDay = DayOfWeek.valueOf(day.uppercase())
    val targetDateTime = now.with(TemporalAdjusters.nextOrSame(targetDay))
        .withHour(startTime.hour)
        .withMinute(startTime.minute)
        .withSecond(0)
        .withNano(0)

    // If the target time is already past, schedule it for the next week
    return if (targetDateTime.isBefore(now)) {
        Duration.between(now, targetDateTime.plusWeeks(1)).toMillis()
    } else {
        Duration.between(now, targetDateTime).toMillis()
    }
}

*///