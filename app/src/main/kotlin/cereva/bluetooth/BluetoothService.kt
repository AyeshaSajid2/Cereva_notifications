package cereva.bluetooth

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.IOException
import java.util.*

class BluetoothService : Service() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    var isDeviceConnected = false
        private set

    companion object {
        private const val TAG = "BluetoothService"
        private val UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard UUID for SPP
        const val ACTION_CONNECTION_STATUS = "com.example.ACTION_CONNECTION_STATUS"
        const val EXTRA_IS_CONNECTED = "com.example.EXTRA_IS_CONNECTED"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val deviceAddress = intent?.getStringExtra("DEVICE_ADDRESS")
        if (deviceAddress != null) {
            connectToDevice(deviceAddress)
        } else {
            Log.e(TAG, "No device address provided in intent.")
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun isConnected(): Boolean = isDeviceConnected

    private fun connectToDevice(address: String) {
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(address)
        if (device == null) {
            Log.e(TAG, "Bluetooth device not found with address: $address")
            updateConnectionStatus(false)
            return
        }

        Thread {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SECURE)
                bluetoothAdapter?.cancelDiscovery() // Cancel discovery for faster connection
                bluetoothSocket?.connect()
                Log.d(TAG, "Connected to device: $address")
                updateConnectionStatus(true)
            } catch (e: IOException) {
                Log.e(TAG, "Error connecting to device", e)
                closeConnection()
                updateConnectionStatus(false)
            }
        }.start()
    }

    private fun updateConnectionStatus(connected: Boolean) {
        isDeviceConnected = connected

        // Broadcast connection status
        val intent = Intent(ACTION_CONNECTION_STATUS)
        intent.putExtra(EXTRA_IS_CONNECTED, connected)
        sendBroadcast(intent)

        if (!connected) {
            stopSelf() // Stop service if disconnected
        }
    }

    private fun closeConnection() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
        } catch (e: IOException) {
            Log.e(TAG, "Error closing Bluetooth socket", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeConnection()
    }
}
