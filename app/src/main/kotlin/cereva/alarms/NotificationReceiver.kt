package  cereva.alarms



import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cereva.MainScreens.FullScreenActivity
import com.fremanrobots.cereva.R
import android.graphics.Color

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        // Wake the screen
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "cereva:NotificationWakeLock"
        )
        wakeLock.acquire(3000) // Wake the screen for 3 seconds

        // Custom sound URI from res/raw folder
        val soundUri: Uri =
            Uri.parse("android.resource://${context.packageName}/raw/notification_ring")

        // Create notification channel if needed
        createNotificationChannel(context, soundUri)

        try {
            // Notification details
            val notificationId = intent.getIntExtra("notification_id", System.currentTimeMillis().toInt())
            val title = intent.getStringExtra("title") ?: "Reminder"
            val message = intent.getStringExtra("RANDOM_MESSAGE") ?: "Default message"

            // Intent to launch the activity when the notification is clicked
            val fullScreenIntent = Intent(context, FullScreenActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("NOTIFICATION_MESSAGE", message)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build the notification
            val notificationBuilder = NotificationCompat.Builder(context, "reminder_channel")
                .setSmallIcon(R.drawable.ic_notification_icon_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set high priority
                .setSound(soundUri) // Attach the custom sound
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 100, 500)) // Vibration pattern

            // Show the notification
            val notificationManager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission check for posting notifications
                return
            }
            notificationManager.notify(notificationId, notificationBuilder.build())
        } finally {
            // Ensure wake lock is released to prevent battery drain
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    // Create notification channel for Android Oreo and above
    private fun createNotificationChannel(context: Context, soundUri: Uri) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "reminder_channel"
            val channelName = "Reminder Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for reminder notifications"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 100, 500)
                setSound(soundUri, null)  // Attach the custom sound to the channel
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
