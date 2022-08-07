package com.ismet.usb

import kotlinx.coroutines.channels.Channel

class UsbEmitter {
    val readEvents = Channel<ByteArray>(Channel.UNLIMITED)
    val connectionEvents = Channel<Boolean>(Channel.UNLIMITED)
}