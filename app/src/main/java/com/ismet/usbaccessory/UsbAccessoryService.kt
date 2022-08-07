package com.ismet.usbaccessory

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import com.ismet.usb.UsbAccessory
import com.ismet.usb.UsbEmitter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UsbAccessoryService : Service() {
    @Inject
    lateinit var usbEmitter: UsbEmitter

    private var countClients: Int = 0

    private val binder = object: UsbAccessory.Stub() {
        override fun setToUsb(values: ByteArray?) {
            usbEmitter.readEvents.trySend(values!!)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        countClients++
        usbEmitter.connectionEvents.trySend(true)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (countClients > 0) {
            countClients--
        }
        if (countClients == 0) {
            usbEmitter.connectionEvents.trySend(false)
        }
        return super.onUnbind(intent)
    }
}