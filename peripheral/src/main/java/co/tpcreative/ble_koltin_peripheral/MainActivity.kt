package co.tpcreative.ble_koltin_peripheral


import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import co.tpcreative.ble_koltin_peripheral.ui.theme.BlekoltinTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlekoltinTheme {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()) {

                    Text(text = "Peripheral running", fontSize = 24.sp)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startAdvertising()
    }

    private fun startAdvertising() {
        val bluetoothServer = BluetoothServer.getInstance(applicationContext)
        val peripheralManager = bluetoothServer.peripheralManager

        if (!peripheralManager.permissionsGranted()) {
            requestPermissions()
            return
        }

        if (!isBluetoothEnabled) {
            enableBleRequest.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        // Make sure we initialize the server only once
        if (!bluetoothServer.isInitialized) {
            bluetoothServer.initialize()
        }

        // All good now, we can start advertising
        handler.postDelayed({
            bluetoothServer.startAdvertising()
        }, 500)
   }

    private val enableBleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
           startAdvertising()
        }
    }

    private val isBluetoothEnabled: Boolean
        get() {
            val bluetoothManager: BluetoothManager = requireNotNull(applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager) { "cannot get BluetoothManager" }
            val bluetoothAdapter: BluetoothAdapter = requireNotNull(bluetoothManager.adapter) { "no bluetooth adapter found" }
            return bluetoothAdapter.isEnabled
        }

    private fun requestPermissions() {
        val missingPermissions = BluetoothServer.getInstance(applicationContext).peripheralManager.getMissingPermissions()
        if (missingPermissions.isNotEmpty() && !permissionRequestInProgress) {
            permissionRequestInProgress = true
            blePermissionRequest.launch(missingPermissions)
        }
    }

    private var permissionRequestInProgress = false
    private val blePermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionRequestInProgress = false
            permissions.entries.forEach {
                Timber.d("${it.key} = ${it.value}")
            }
        }
}