package com.ivanpashaev.blue_finder

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {
    private val bluetoothPermissionManager = BluetoothPermissionsManager(this) {
        startBluetoothDiscovery()
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?,intent: Intent) {
            val action: String? = intent?.action

            if (BluetoothDevice.ACTION_FOUND == action) {

                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                if (ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_DENIED || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {

                    val deviceName = device?.name ?: "unknown device" //name
                    val deviceHardwareAddress = device?.address //mac address

                    Log.d("BluetoothScan","Find: $deviceName [$deviceHardwareAddress]")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver,filter)

    }
    fun onButtonStartClick(view: View) {
        bluetoothPermissionManager.checkAndRequest()
    }

    fun startBluetoothDiscovery() {
        Toast.makeText(this,"Start search",Toast.LENGTH_SHORT).show()


    }
}