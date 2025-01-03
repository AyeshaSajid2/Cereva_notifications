package cereva.cancelation

import android.app.NotificationManager
import android.content.Context
import androidx.work.WorkManager

// Cancel all reminders by tag
fun cancelWeeklyReminders(context: Context) {
    val workManager = WorkManager.getInstance(context)

    // Cancel all work with the "ReminderWork" tag
    workManager.cancelAllWorkByTag("ReminderWork")
}

// Cancel specific notification by ID
fun cancelWeeklyNotification(context: Context, notificationId: Int) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(notificationId)
}
