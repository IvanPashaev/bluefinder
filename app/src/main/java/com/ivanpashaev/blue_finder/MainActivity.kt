package com.ivanpashaev.blue_finder

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
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
import android.os.Build
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private val foundDevices = mutableListOf<BluetoothDeviceItem>()
    private val displayDevices = mutableListOf<String>()

    private lateinit var listAdapter: ArrayAdapter<String>

    private val bluetoothPermissionManager = BluetoothPermissionsManager(this) {
        startBluetoothDiscovery()
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?,intent: Intent) {
            val action: String? = intent.action

            if (BluetoothDevice.ACTION_FOUND == action) {

                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val rssi: Int = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()

                val hasPermissions: Boolean = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.BLUETOOTH_SCAN) ==
                            PackageManager.PERMISSION_GRANTED
                } else {
                    ActivityCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED
                }
                if (hasPermissions) {

                    val deviceName = device?.name ?: "unknown device" //name
                    val deviceHardwareAddress = device?.address //mac address

                    val device = BluetoothDeviceItem(deviceName,
                        deviceHardwareAddress.toString(),rssi)

                    foundDevices.add(device)
                    displayDevices.add("${deviceName} [${deviceHardwareAddress}] (${rssi})")

                    listAdapter.notifyDataSetChanged()

                    findViewById<ListView>(R.id.list_view).adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1,foundDevices)
                    Log.d("BluetoothScan","Find: $deviceName [$deviceHardwareAddress] ($rssi)")
                    Toast.makeText(this@MainActivity,"$deviceName:($rssi)",Toast.LENGTH_SHORT).show()

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
        val listView = findViewById<ListView>(R.id.list_view)
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayDevices)
        listView.adapter = listAdapter

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver,filter)

    }
    fun onButtonStartClick(view: View) {
        bluetoothPermissionManager.checkAndRequest()
    }

    fun startBluetoothDiscovery() {

        val hasPermissions: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        }

        if (hasPermissions) {
            val bluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter = bluetoothManager?.adapter

            if (bluetoothAdapter?.startDiscovery() == true) {
                findViewById<TextView>(R.id.textView).text = "Start search..."
            } else {
                findViewById<TextView>(R.id.textView).text = "Bluetooth search ERROR"
            }
        } else {
            findViewById<TextView>(R.id.textView).text = "Hasn't permissions"
        }

    }
}