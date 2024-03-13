package com.ismet.usbaccessory

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.ismet.usb.UsbAccessory
import com.ismet.usb.UsbEmitter
import com.ismet.usb.UsbHost
import com.ismet.usbaccessory.model.ResponsesFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

@AndroidEntryPoint
class UsbAccessoryService : Service() {

    private val responsesFactory = ResponsesFactory()
    private var scope = CoroutineScope(EmptyCoroutineContext)
    private var readJob: Job? = null
    private var usbHost: UsbHost? = null
    private val sendToUsbConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service != null) {
                usbHost = UsbHost.Stub.asInterface(service)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            usbEmitter.historyRecords.clear()
            usbHost = null
        }
    }

    @Inject
    lateinit var usbEmitter: UsbEmitter

    private val binder = object: UsbAccessory.Stub() {
        override fun setToUsb(values: ByteArray?) {
            readJob?.cancel()
            readJob = scope.launch(Dispatchers.IO) {
                val response = responsesFactory.getResponse(values!!)
                usbEmitter.historyRecords += response
                delay(response.delay)
                try {
                    usbHost?.getFromUsb(response.response)
                } catch (_: RemoteException) {
                    //ignore
                }
                usbEmitter.historyRecords[usbEmitter.historyRecords.lastIndex] = response.toSuccess()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        val bindIntent =
            Intent("com.ismet.usb.host").apply { `package` = "com.ismet.usbterminalnew3" }
        bindService(bindIntent, sendToUsbConnection, Context.BIND_AUTO_CREATE)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (usbHost != null) {
            unbindService(sendToUsbConnection)
            usbEmitter.historyRecords.clear()
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}