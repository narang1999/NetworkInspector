package com.networkinspector.sample

import android.app.Application
import com.networkinspector.NetworkInspector
import com.networkinspector.core.NetworkInspectorConfig

class SampleApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize NetworkInspector
        NetworkInspector.init(this, NetworkInspectorConfig(
            enabled = true,
            showNotification = true,
            logToLogcat = true,
            maxRequests = 100
        ))
    }
}



