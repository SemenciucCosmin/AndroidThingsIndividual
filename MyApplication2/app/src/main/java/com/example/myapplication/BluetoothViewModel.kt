package com.example.myapplication

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

class BluetoothViewModel : ViewModel() {

    companion object {
        const val BUFFER_SIZE = 256
        const val DELAY_TIME = 300L
        const val NO_OFFSET = 0
        const val TEMPERATURE = 0
        const val HUMIDITY = 1
        const val MINIMUM_DATA_SIZE = 2
        const val DELIMITER = ";"
    }

    private val _uiState = MutableLiveData<UiState>().apply {
        value = UiState()
    }
    val uiState: LiveData<UiState>
        get() = _uiState

    fun receiveData(bluetoothSocket: BluetoothSocket) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val socketInputStream: InputStream = bluetoothSocket.inputStream
                val buffer = ByteArray(BUFFER_SIZE)
                var bytes: Int

                while (true) {
                    try {
                        if (socketInputStream.available() > 0) {
                            delay(DELAY_TIME)
                            bytes = socketInputStream.read(buffer)
                            val message = String(buffer, NO_OFFSET, bytes)
                            val data = message.split(DELIMITER)
                            if (data.size >= MINIMUM_DATA_SIZE) {
                                _uiState.postValue(
                                    _uiState.value?.copy(
                                        temperature = data[TEMPERATURE],
                                        humidity = data[HUMIDITY]
                                    )
                                )
                            }
                        }
                    } catch (e: IOException) {
                        break
                    }
                }
            }
        }
    }

    data class UiState(
        var temperature: String = "",
        var humidity: String = ""
    )
}
