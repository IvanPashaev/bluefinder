package com.ivanpashaev.blue_finder

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import java.security.KeyManagementException

class BluetoothPermissionsManager(private val activity: ComponentActivity, //current activity
                                  private val onBluetoothReady: () -> Unit //lambda function
) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy { //this code don't run while we don't use bluetooth adapter val
        val bluetoothManager = activity.getSystemService(BluetoothManager::class.java)
        bluetoothManager?.adapter //safe call by '?'
    }

    //launcher for permissions
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            checkAndEnableBluetooth()
        } else {
            Toast.makeText(activity,"Need permissions for bluetooth",Toast.LENGTH_SHORT).show()
        }
    }

    //launcher for enable bluetooth-tumbler
    private val enableBluetoothLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            Toast.makeText(activity,"Bluetooth successfully enabled", Toast.LENGTH_SHORT).show()
            onBluetoothReady()
        } else {
            Toast.makeText(activity,"Bluetooth must be enabled!", Toast.LENGTH_SHORT).show()
        }
    }

    //main method for start all checks
    fun checkAndRequest() {
        if (bluetoothAdapter == null) {
            Toast.makeText(activity,"This device don't support Bluetooth", Toast.LENGTH_SHORT).show()
            activity.finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }

    private fun checkAndEnableBluetooth() {
        bluetoothAdapter?.let { adapter ->
            if (!adapter.isEnabled) { //if adapter not enabled, enable it
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(intent)
            } else {
                Toast.makeText(activity,"Bluetooth now enabled", Toast.LENGTH_SHORT).show()
                onBluetoothReady() //call callback if all is enabled
            }
        }
    }
}