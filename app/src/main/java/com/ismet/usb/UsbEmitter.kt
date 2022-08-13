package com.ismet.usb

import androidx.compose.runtime.mutableStateListOf
import com.ismet.usbaccessory.model.HistoryRecord

class UsbEmitter {
    val historyRecords = mutableStateListOf<HistoryRecord>()
}