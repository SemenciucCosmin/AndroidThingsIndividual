package com.example.myapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private val viewModel = BluetoothViewModel()

    companion object {
        const val TAG = "COMMUNICATION ERROR"
        const val DEVICE_NAME = "HC-06"
        const val UUID_NAME = "00001101-0000-1000-8000-00805F9B34FB"
        const val REQUEST_CODE = 0
        const val EXPECTED_DATA_LENGTH = 5
    }

    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private lateinit var connectionState: TextView
    private lateinit var readData: TextView
    private lateinit var retryConnectionButton: Button

    private var timestamp = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectionState = findViewById(R.id.connection_state)
        readData = findViewById(R.id.read_data)
        retryConnectionButton = findViewById(R.id.retry_connection_button)

        refreshUi()
        initializeBluetoothConnection()

        viewModel.uiState.observe(this) { uiState ->
            readData.text =
                getString(R.string.lbl_temperature_humidity, uiState.temperature, uiState.humidity)
            if (uiState.temperature.length != EXPECTED_DATA_LENGTH || uiState.humidity.length != EXPECTED_DATA_LENGTH) {
                Log.d(
                    TAG,
                    getString(
                        R.string.lbl_log_message,
                        System.currentTimeMillis() - timestamp,
                        uiState.temperature,
                        uiState.humidity
                    )
                )
                timestamp = System.currentTimeMillis()
            }
        }


        retryConnectionButton.setOnClickListener {
            initializeBluetoothConnection()
        }
    }

    private fun initializeBluetoothConnection() {
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter.isEnabled) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (bluetoothSocket?.isConnected == false || bluetoothSocket == null) {
                    val device: BluetoothDevice? = bluetoothAdapter.bondedDevices?.first { device ->
                        device.name == DEVICE_NAME
                    }

                    bluetoothSocket = device?.createRfcommSocketToServiceRecord(
                        UUID.fromString(UUID_NAME)
                    )

                    bluetoothSocket?.let { socket ->
                        try {
                            socket.connect()
                            Toast.makeText(
                                this,
                                getString(R.string.lbl_connection_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.receiveData(socket)
                        } catch (e: IOException) {
                            Toast.makeText(
                                this,
                                getString(R.string.lbl_connection_failure),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.lbl_already_connected),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_CODE)
        }
        refreshUi()
    }

    private fun refreshUi() {
        val isConnected = bluetoothSocket?.isConnected ?: false
        if (isConnected) {
            connectionState.text = getString(R.string.lbl_connected)
        } else {
            connectionState.text = getString(R.string.lbl_not_connected)
            readData.text = getString(R.string.lbl_no_data)
        }
    }
}