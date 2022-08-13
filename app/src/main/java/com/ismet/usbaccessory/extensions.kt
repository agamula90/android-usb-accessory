package com.ismet.usbaccessory

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