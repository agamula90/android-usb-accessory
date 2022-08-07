package com.ismet.usbaccessory

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ismet.usb.UsbEmitter
import com.ismet.usb.UsbHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.EmptyCoroutineContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val coroutineScope = CoroutineScope(EmptyCoroutineContext)
    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var usbEmitter: UsbEmitter

    private var isSendToUsbServiceConnected = false

    private val sendToUsbConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isSendToUsbServiceConnected = true
            if (service != null) {
                viewModel.usbHost = UsbHost.Stub.asInterface(service)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isSendToUsbServiceConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val historyRecords by viewModel.history.collectAsState()

            LazyColumn(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                items(historyRecords) { historyRecord ->
                    Text(text = historyRecord.toString(), modifier = Modifier.padding(bottom = 10.dp))
                }
            }
        }
        coroutineScope.launch {
            for (event in usbEmitter.readEvents) {
                viewModel.onDataReceived(event)
            }
        }
        coroutineScope.launch {
            for (event in usbEmitter.connectionEvents) {
                if (event) {
                    connectToUsbHost()
                } else {
                    disconnectFromUsbHost()
                }
            }
        }
    }

    private fun connectToUsbHost() {
        if (!isSendToUsbServiceConnected) {
            val intent =
                Intent("com.ismet.usb.host").apply { `package` = "com.ismet.usbterminalnew" }
            bindService(intent, sendToUsbConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun disconnectFromUsbHost() {
        if (isSendToUsbServiceConnected) {
            unbindService(sendToUsbConnection)
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        disconnectFromUsbHost()
        super.onDestroy()
    }
}