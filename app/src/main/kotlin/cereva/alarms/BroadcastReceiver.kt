package cereva.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cereva.services.KeepAliveService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, KeepAliveService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
