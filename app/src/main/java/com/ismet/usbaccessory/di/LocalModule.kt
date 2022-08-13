package com.ismet.usbaccessory.di

import com.ismet.usb.UsbEmitter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideUsbEmitter(): UsbEmitter = UsbEmitter()
}