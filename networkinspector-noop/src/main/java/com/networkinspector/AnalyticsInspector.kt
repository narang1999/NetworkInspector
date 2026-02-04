package com.networkinspector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.networkinspector.core.AnalyticsEvent
import com.networkinspector.core.AnalyticsSource

/**
 * No-op implementation of AnalyticsInspector for release builds.
 * All methods are empty stubs that do nothing.
 */
object AnalyticsInspector {
    
    @JvmStatic
    fun logEvent(
        eventName: String,
        params: Bundle?,
        source: AnalyticsSource = AnalyticsSource.FIREBASE
    ) {
        // No-op
    }
    
    @JvmStatic
    fun logEvent(
        eventName: String,
        params: Map<String, Any?>?,
        source: AnalyticsSource = AnalyticsSource.FIREBASE
    ) {
        // No-op
    }
    
    @JvmStatic
    fun getEvents(): List<AnalyticsEvent> = emptyList()
    
    @JvmStatic
    fun getEvents(source: AnalyticsSource): List<AnalyticsEvent> = emptyList()
    
    @JvmStatic
    fun searchEvents(query: String): List<AnalyticsEvent> = emptyList()
    
    @JvmStatic
    fun getEvent(id: String): AnalyticsEvent? = null
    
    @JvmStatic
    fun getEventCount(): Int = 0
    
    @JvmStatic
    fun clearAll() {
        // No-op
    }
    
    @JvmStatic
    fun setEnabled(enabled: Boolean) {
        // No-op
    }
    
    @JvmStatic
    fun isEnabled(): Boolean = false
    
    @JvmStatic
    fun setLogToLogcat(log: Boolean) {
        // No-op
    }
    
    @JvmStatic
    fun launch(context: Context) {
        // No-op
    }
    
    @JvmStatic
    fun getLaunchIntent(context: Context): Intent = Intent()
    
    @JvmStatic
    fun addListener(listener: EventListener) {
        // No-op
    }
    
    @JvmStatic
    fun removeListener(listener: EventListener) {
        // No-op
    }
    
    interface EventListener {
        fun onEventsUpdated(events: List<AnalyticsEvent>)
    }
    
    @JvmStatic
    fun addSampleEvents() {
        // No-op
    }
}

