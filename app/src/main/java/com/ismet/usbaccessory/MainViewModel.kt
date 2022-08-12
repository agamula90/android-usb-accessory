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

private const val RANDOM_REQUEST = "Random"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val coroutineDispatcher: CoroutineDispatcher
): ViewModel() {

    private val diversities = mapOf(
        "/5J1R" to ResponseDiversity(
            delayFrom = 120,
            delayTo = 420,
            responses = listOf("@5J001", "@5J101").map(String::encodeToByteArray)
        ),
        "/5J5R" to ResponseDiversity(
            delayFrom = 1000,
            delayTo = 3000,
            responses = listOf("@5J101", "@5J001").map(String::encodeToByteArray)
        ),
        "FE440008029F25" to ResponseDiversity(
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
        RANDOM_REQUEST to ResponseDiversity(
            delayFrom = 2000,
            delayTo = 4500,
            responses = List(4) { "test$it" }.map(String::encodeToByteArray)
        )
    )
    private var readJob: Job? = null
    var usbHost: UsbHost? = null
    val history = MutableStateFlow<List<HistoryRecord>>(emptyList())

    fun onDataReceived(bytes: ByteArray) {
        readJob?.cancel()
        readJob = viewModelScope.launch(coroutineDispatcher) {
            val request = bytes.decodeToStringEnhanced()
            val diversity = diversities[request] ?: diversities[RANDOM_REQUEST]!!
            val historyRecord = diversity.randomHistoryRecord(request)
            Log.e("Oops", historyRecord.toString())
            history.value +=  historyRecord
            delay(historyRecord.delay)
            try {
                usbHost?.getFromUsb(historyRecord.response)
            } catch (_: RemoteException) {
                //ignore
            }
            history.value = history.value.toMutableList().apply {
                this[lastIndex] = historyRecord.toSuccess()
            }
        }
    }

    private fun createCo2Response(value1: Int, value2: Int): ByteArray =
        listOf(0xFE, 0x44, 0x00, value1, value2, value1 + 10, value2 + 10)
            .map { it.toByte() }
            .toByteArray()

    private fun Int.toHeaterResponse() = "@5,0(0,0,0,0),25,${this},25,25,25"
}

fun ByteArray.decodeToStringEnhanced() = when {
    this.size != 7 -> decodeToString()
    this[0] != 0xFE.toByte() || this[1] != 0x44.toByte() -> decodeToString()
    else -> joinToString(separator = "") {
        val value = (0xFF and it.toInt()).toString(radix = 16)
        if (value.length != 2) {
            "0" + value.uppercase()
        } else {
            value.uppercase()
        }
    }
}