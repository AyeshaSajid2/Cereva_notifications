package cereva.alarms

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cereva.MainScreens.FullScreenActivity
import com.fremanrobots.cereva.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

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
            notificationId, // Ensure unique PendingIntent for each notification
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Custom sound URI from res/raw folder
        val soundUri: Uri = Uri.parse("android.resource://${context.packageName}/raw/res_notification")

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Enable removal when swiped
            .setContentIntent(pendingIntent)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission check for posting notifications
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
