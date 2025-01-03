package cereva.bluetooth

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BluetoothUtils(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    fun enableBluetooth(activity: Activity) {
        // Check for BLUETOOTH_CONNECT permission (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1
            )
            return
        }

        // Enable Bluetooth
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, 1)
        } else {
            // Navigate to Bluetooth settings
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            context.startActivity(intent)
        }
    }

    fun showPermissionExplanation() {
        AlertDialog.Builder(context)
            .setTitle("Permission Required")
            .setMessage("This app requires Bluetooth and Location permissions to work properly.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        fun pairWithSmartwatch(context: Context, bluetoothAdapter: BluetoothAdapter): Boolean {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(context, "Bluetooth scan permission required", Toast.LENGTH_SHORT).show()
                    return false
                }
            }

            try {
                bluetoothAdapter.startDiscovery()
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        val action: String? = intent?.action
                        if (BluetoothDevice.ACTION_FOUND == action) {
                            val device: BluetoothDevice? =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            if (device?.name?.contains("Smartwatch", ignoreCase = true) == true) {
                                // Found a smartwatch, initiate pairing
                                try {
                                    device.createBond()
                                    context?.unregisterReceiver(this) // Unregister receiver
                                } catch (e: SecurityException) {
                                    Toast.makeText(
                                        context,
                                        "Pairing failed due to missing permissions",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                context.registerReceiver(receiver, filter)
            } catch (e: SecurityException) {
                Toast.makeText(context, "Failed to start discovery due to permissions", Toast.LENGTH_SHORT).show()
                return false
            }
            return false // Return actual pairing status if implemented
        }
    }
}
