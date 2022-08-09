package com.ismet.usbaccessory

import android.os.RemoteException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismet.usb.UsbHost
import com.ismet.usbaccessory.model.HistoryRecord
import com.ismet.usbaccessory.model.ResponseDiversity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val coroutineDispatcher: CoroutineDispatcher
): ViewModel() {

    private val diversities = mapOf(
        "/5J1R" to ResponseDiversity(
            delayFrom = 120,
            delayTo = 420,
            responses = listOf("@5J001 ", "@5J101 ").map(String::encodeToByteArray)
        ),
        "/5J5R" to ResponseDiversity(
            delayFrom = 1000,
            delayTo = 3000,
            responses = listOf("@5J101 ").map(String::encodeToByteArray)
        ),
        "�D\u0000\b\u0002�%" to ResponseDiversity(
            delayFrom = 600,
            delayTo = 1300,
            responses = listOf(
                createCo2Response(0x33, 0xe4),
                createCo2Response(0xd3, 0x64),
                createCo2Response(0xc2, 0xde)
            )
        ),
        "/5H0000R" to ResponseDiversity(
            delayFrom = 600,
            delayTo = 1300,
            responses = listOf(
                50.toHeaterResponse(),
                208.toHeaterResponse(),
                180.toHeaterResponse()
            ).map(String::encodeToByteArray)
        ),
        "/5H750R"  to ResponseDiversity(
            delayFrom = 600,
            delayTo = 1300,
            responses = listOf(
                50.toHeaterResponse(),
                208.toHeaterResponse(),
                180.toHeaterResponse()
            ).map(String::encodeToByteArray)
        ),
        "/1ZR" to ResponseDiversity(
            delayFrom = 0,
            delayTo = 1000,
            responses = listOf("").map(String::encodeToByteArray)
        ),
    )
    private var readJob: Job? = null
    var usbHost: UsbHost? = null
    val history = MutableStateFlow<List<HistoryRecord>>(emptyList())

    fun onDataReceived(bytes: ByteArray) {
        readJob?.cancel()
        readJob = viewModelScope.launch(coroutineDispatcher) {
            val request = bytes.decodeToString()
            Log.e("Oops", request)
            val diversity = diversities[request]!!
            val historyRecord = diversity.randomHistoryRecord(request)
            history.value +=  historyRecord
            delay(historyRecord.delay)
            try {
                usbHost?.getFromUsb(historyRecord.response.encodeToByteArray())
            } catch (_: RemoteException) {
                //ignore
            }
            history.value = history.value.toMutableList().apply {
                this[lastIndex] = historyRecord.copy(isFailed = false)
            }
        }
    }

    private fun createCo2Response(value1: Int, value2: Int): ByteArray =
        listOf(0xFE, 0x44, 0x00, value1, value2, value1 + 10, value2 + 10)
            .map { it.toByte() }
            .toByteArray()

    private fun Int.toHeaterResponse() = "@5,0(0,0,0,0),${this},25,25,25,25"
}