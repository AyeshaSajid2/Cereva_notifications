package cereva.bluetooth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.Notification
import android.app.PendingIntent
import android.app.NotificationChannel
import android.os.Build
import androidx.core.app.NotificationCompat
import cereva.MainScreens.FullScreenActivity
import com.fremanrobots.cereva.R
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class NotificationReceiverService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == "/notification") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val message = dataMap.getString("message")

                    // Handle the notification here
                    showNotificationOnSmartwatch(message)
                }
            }
        }
    }

    private fun showNotificationOnSmartwatch(message: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "smartwatch_channel"

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Smartwatch Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Smartwatch Reminder")
            .setContentText(message ?: "No message")
            .setSmallIcon(R.drawable.notification_icon)  // Use your own icon
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }
}
