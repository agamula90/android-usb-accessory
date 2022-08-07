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
        "/5J1R" to ResponseDiversity(delayFrom = 20, delayTo = 120, responses = listOf("5J001 ", "5J345").map(String::encodeToByteArray)),
        "/5J5R" to ResponseDiversity(delayFrom = 1000, delayTo = 3000, responses = listOf("5J101 ", "5J346").map(String::encodeToByteArray)),
        "(FE-44-00-08-02-9F-25)" to ResponseDiversity(delayFrom = 1000, delayTo = 3000, responses = listOf("380", "200", "417", "2300").map(String::encodeToByteArray)),
        "/5H0000R" to ResponseDiversity(delayFrom = 1000, delayTo = 3000, responses = listOf("@5,0(0,0,0,0),750,25,25,25,25", "@5,0(0,0,0,0),208,25,25,25,25", "@5,0(0,0,0,0),1380,25,25,25,25").map(String::encodeToByteArray)),
        "/5H750R"  to ResponseDiversity(delayFrom = 1000, delayTo = 3000, responses = listOf("@5,0(0,0,0,0),750,25,25,25,25", "@5,0(0,0,0,0),208,25,25,25,25", "@5,0(0,0,0,0),1380,25,25,25,25").map(String::encodeToByteArray)),
        "/1ZR" to ResponseDiversity(delayFrom = 0, delayTo = 1000, responses = listOf("").map(String::encodeToByteArray)),
    )
    private var readJob: Job? = null
    var usbHost: UsbHost? = null
    val history = MutableStateFlow<List<HistoryRecord>>(emptyList())

    fun onDataReceived(bytes: ByteArray) {
        readJob?.cancel()
        readJob = viewModelScope.launch(coroutineDispatcher) {
            val request = bytes.decodeToString()
            Log.e("Oops", "accessory request: $request")
            val diversity = diversities[request] ?: return@launch
            val historyRecord = diversity.randomHistoryRecord(request)
            history.value +=  historyRecord
            delay(historyRecord.delay)
            history.value = history.value.toMutableList().apply {
                removeLast()
                add(historyRecord.copy(isFailed = false))
            }

            try {
                usbHost?.getFromUsb(historyRecord.response.encodeToByteArray())
            } catch (_: RemoteException) {
                //ignore
            }
        }
    }
}