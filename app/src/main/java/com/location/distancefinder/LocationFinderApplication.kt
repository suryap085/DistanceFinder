package com.location.distancefinder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LocationFinderApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}