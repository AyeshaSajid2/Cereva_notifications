package cereva.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.fremanrobots.cereva.R
import java.util.*

class KeepAliveService : Service() {

    companion object {
        const val CHANNEL_ID = "KeepAliveChannel"
        const val CHANNEL_NAME = "Keep Alive Service"
        const val NOTIFICATION_ID = 1
        const val ACTION_SHOW_NOTIFICATION = "cereva.services.SHOW_NOTIFICATION"
        const val ACTION_STOP_SERVICE = "cereva.services.STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, getServiceNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "This channel is used for Keep Alive Service notifications."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun getServiceNotification(): Notification {
        // Intent to stop the service when button is clicked
        val stopServiceIntent = Intent(this, KeepAliveService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopServicePendingIntent = PendingIntent.getService(
            this,
            0,
            stopServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_running_title))
            .setContentText(getString(R.string.service_running_message))
            .setSmallIcon(R.drawable.notification_icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(
                R.drawable.icon, // Icon for the button
                "Stop Service", // Button text
                stopServicePendingIntent // Action to stop the service
            )
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If the action is to stop the service
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf() // Stop the service
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID) // Remove the notification
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the scheduled notifications when the service is destroyed
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent) // Cancel any scheduled alarms
    }
}

// BroadcastReceiver to handle notifications
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == KeepAliveService.ACTION_SHOW_NOTIFICATION) {
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        val channelId = KeepAliveService.CHANNEL_ID
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val messages = context.resources.getStringArray(R.array.reminder_messages)
        val randomMessage = messages[Random().nextInt(messages.size)]

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Scheduled Reminder")
            .setContentText(randomMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(randomMessage)) // For longer messages
            .setSmallIcon(R.drawable.notification_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
