/***package cereva.alarms

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
//import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    when (permissionState.status) {
        is PermissionStatus.Granted -> {
            // Permission granted
            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
        is PermissionStatus.Denied -> {
            // Handle denied permission or show rationale
            if ((permissionState.status as PermissionStatus.Denied).shouldShowRationale) {
                Toast.makeText(context, "Please enable notification permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Request permission if not granted
    if (!permissionState.status.isGranted) {
        permissionState.launchPermissionRequest()
    }
}
*/