package cereva.alarms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

fun showNotification(context: Context) {
    // Notification Channel is required for Android 8.0 (Oreo) and higher
    val channelId = "reminder_channel"
    val channelName = "Reminder Notifications"

    // Get NotificationManager instance
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create the NotificationChannel for Android 8.0+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    // Create the notification
    val notification: Notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)  // Icon for the notification
        .setContentTitle("Reminder")
        .setContentText("Hello")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    // Show the notification
    notificationManager.notify(1, notification)  // Unique ID for the notification
}
