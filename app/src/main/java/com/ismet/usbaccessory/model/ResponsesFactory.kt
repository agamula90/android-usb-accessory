package com.ismet.usbaccessory.model

import com.ismet.usbaccessory.decodeToStringEnhanced

private const val RANDOM_REQUEST = "Random"

class ResponsesFactory {
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

    private fun createCo2Response(value1: Int, value2: Int): ByteArray =
        listOf(0xFE, 0x44, 0x00, value1, value2, value1 + 10, value2 + 10)
            .map { it.toByte() }
            .toByteArray()

    private fun Int.toHeaterResponse() = "@5,0(0,0,0,0),25,${this},25,25,25"

    fun getResponse(request: ByteArray): HistoryRecord {
        val decodedRequest = request.decodeToStringEnhanced()
        val diversity = diversities[decodedRequest] ?: diversities[RANDOM_REQUEST]!!
        return diversity.randomHistoryRecord(decodedRequest)
    }
}