package cereva.alarms

import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log
import cereva.MainScreens.FullScreenActivity
import com.fremanrobots.cereva.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Retrieve start and end time from input data
            val day = inputData.getString("day") ?: return@withContext Result.failure()
            val startTime = inputData.getString("startTime") ?: return@withContext Result.failure()
            val endTime = inputData.getString("endTime") ?: return@withContext Result.failure()

            Log.d("ReminderWorker", "Notification triggered: $day for $startTime to $endTime.")

            // Fetch a random motivational message
            val messages = applicationContext.resources.getStringArray(R.array.reminder_messages)
            val randomMessage = messages[Random().nextInt(messages.size)]

            // Display notification
            showNotification(
                applicationContext,
                title = "Cerebral Co-Pilot",
                message = randomMessage
            )

            Result.success()
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Error displaying notification: ${e.message}")
            Result.failure()
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "reminder_notifications"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ensure a unique notification ID
        val notificationId = System.currentTimeMillis().toInt()

        // Create a notification channel for Android 8.0+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled reminder notifications."
                // Set the custom sound
                setSound(
                    Uri.parse("android.resource://${applicationContext.packageName}/raw/res_notification"),
                    audioAttributes
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent to launch FullScreenActivity when the notification is clicked
        val intent = Intent(context, FullScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_MESSAGE", message)
        }

        // Create a PendingIntent to wrap the intent
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId, // Ensure unique PendingIntent for each notification
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_icon) // Ensure this icon exists in your resources
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // For longer messages
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent
            .build()

        // Use a dynamic notification ID to avoid duplicates
        notificationManager.notify(notificationId, notification)
    }
}
