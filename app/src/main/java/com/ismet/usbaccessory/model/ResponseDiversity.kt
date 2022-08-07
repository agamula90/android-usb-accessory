package com.ismet.usbaccessory.model

import kotlin.random.Random

class ResponseDiversity(val delayFrom: Long, val delayTo: Long, val responses: List<ByteArray>) {

    fun randomHistoryRecord(request: String, mapper: (ByteArray) -> String): HistoryRecord {
        val delay = Random.nextLong(from = delayFrom, until = delayTo)
        val response = if (responses.size == 1) responses[0] else responses.random()
        return HistoryRecord(delay, request, mapper.invoke(response), true).apply { responseByteArray = response }
    }
}