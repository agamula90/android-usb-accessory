package com.ismet.usbaccessory.model

data class HistoryRecord(val delay: Long, val request: String, val response: String, val isFailed: Boolean) {
    var responseByteArray: ByteArray = response.encodeToByteArray()
}
