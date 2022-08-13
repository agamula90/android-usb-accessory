package com.ismet.usbaccessory.model

import android.os.Parcelable
import com.ismet.usbaccessory.decodeToStringEnhanced
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

class ResponseDiversity(
    val delayFrom: Long,
    val delayTo: Long,
    val responses: List<ByteArray>
) {
    fun randomHistoryRecord(request: String): HistoryRecord {
        val delay = Random.nextLong(from = delayFrom, until = delayTo)
        val response = if (responses.size == 1) responses[0] else responses.random()
        return HistoryRecord(delay, request, response, true)
    }
}

@Parcelize
class HistoryRecord(val delay: Long, val request: String, val response: ByteArray, val isFailed: Boolean): Parcelable {
    override fun toString(): String {
        return "Delay: $delay, request: \"$request\", response: \"${response.decodeToStringEnhanced()}\", isFailed: $isFailed"
    }

    fun toSuccess() = HistoryRecord(delay, request, response, false)
}