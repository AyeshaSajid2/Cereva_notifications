import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.wearable.*
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task

fun sendNotificationToSmartwatch(context: Context, message: String) {
    val client = Wearable.getDataClient(context)
    val dataMap = DataMap()
    dataMap.putString("message", message)

    // Create PutDataRequest with the notification path
    val putDataRequest = PutDataRequest.create("/notification")
    putDataRequest.data = dataMap.toByteArray()

    // Send data to smartwatch
    val putDataTask: Task<DataItem> = client.putDataItem(putDataRequest)

    putDataTask.addOnSuccessListener {
        Log.d("Wearable", "Notification sent successfully to smartwatch")
        Toast.makeText(context, "Notification sent successfully to smartwatch", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener {
        Toast.makeText(context, "Failed to send notification to smartwatch", Toast.LENGTH_SHORT).show()

        Log.d("Wearable", "Failed to send notification to smartwatch")
    }
}
