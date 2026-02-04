package com.networkinspector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.networkinspector.core.AnalyticsEvent
import com.networkinspector.core.AnalyticsSource
import com.networkinspector.ui.AnalyticsListActivity
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * AnalyticsInspector - Logs and displays analytics events for debugging.
 * 
 * All methods are crash-safe and will never throw exceptions to the caller.
 */
object AnalyticsInspector {
    
    private const val TAG = "AnalyticsInspector"
    private const val MAX_EVENTS = 500
    
    private val events = CopyOnWriteArrayList<AnalyticsEvent>()
    private val eventIdGenerator = AtomicLong(0)
    private val listeners = CopyOnWriteArrayList<EventListener>()
    
    private var enabled = true
    private var logToLogcat = true
    
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "AnalyticsInspector-Worker").apply { isDaemon = true }
    }
    
    /**
     * Log an analytics event with Bundle params
     */
    @JvmStatic
    fun logEvent(
        eventName: String,
        params: Bundle?,
        source: AnalyticsSource = AnalyticsSource.FIREBASE
    ) {
        try {
            if (!enabled) return
            
            executor.execute {
                try {
                    val paramsMap = bundleToMap(params)
                    val event = AnalyticsEvent(
                        id = "evt_${eventIdGenerator.incrementAndGet()}_${System.currentTimeMillis()}",
                        eventName = eventName,
                        params = paramsMap,
                        source = source
                    )
                    
                    addEvent(event)
                    
                    if (logToLogcat) {
                        Log.d(TAG, "📊 [${source.name}] $eventName | ${paramsMap.size} params")
                    }
                    
                    notifyListeners()
                } catch (e: Throwable) {
                    Log.e(TAG, "Error logging event", e)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in logEvent", e)
        }
    }
    
    /**
     * Log an analytics event with Map params
     */
    @JvmStatic
    fun logEvent(
        eventName: String,
        params: Map<String, Any?>?,
        source: AnalyticsSource = AnalyticsSource.FIREBASE
    ) {
        try {
            if (!enabled) return
            
            executor.execute {
                try {
                    val paramsMap = try {
                        params?.mapValues { it.value?.toString() ?: "null" } ?: emptyMap()
                    } catch (e: Throwable) {
                        emptyMap()
                    }
                    
                    val event = AnalyticsEvent(
                        id = "evt_${eventIdGenerator.incrementAndGet()}_${System.currentTimeMillis()}",
                        eventName = eventName,
                        params = paramsMap,
                        source = source
                    )
                    
                    addEvent(event)
                    
                    if (logToLogcat) {
                        Log.d(TAG, "📊 [${source.name}] $eventName | ${paramsMap.size} params")
                    }
                    
                    notifyListeners()
                } catch (e: Throwable) {
                    Log.e(TAG, "Error logging event", e)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in logEvent", e)
        }
    }
    
    /**
     * Get all events (newest first)
     */
    @JvmStatic
    fun getEvents(): List<AnalyticsEvent> {
        return try { events.toList() } catch (e: Throwable) { emptyList() }
    }
    
    /**
     * Get events filtered by source
     */
    @JvmStatic
    fun getEvents(source: AnalyticsSource): List<AnalyticsEvent> {
        return try { events.filter { it.source == source } } catch (e: Throwable) { emptyList() }
    }
    
    /**
     * Search events by name or params
     */
    @JvmStatic
    fun searchEvents(query: String): List<AnalyticsEvent> {
        return try {
            if (query.isBlank()) return getEvents()
            events.filter { it.matchesSearch(query) }
        } catch (e: Throwable) { emptyList() }
    }
    
    /**
     * Get a specific event by ID
     */
    @JvmStatic
    fun getEvent(id: String): AnalyticsEvent? {
        return try { events.find { it.id == id } } catch (e: Throwable) { null }
    }
    
    /**
     * Get event count
     */
    @JvmStatic
    fun getEventCount(): Int {
        return try { events.size } catch (e: Throwable) { 0 }
    }
    
    /**
     * Clear all events
     */
    @JvmStatic
    fun clearAll() {
        try {
            events.clear()
            notifyListeners()
        } catch (e: Throwable) { }
    }
    
    /**
     * Enable/disable logging
     */
    @JvmStatic
    fun setEnabled(enabled: Boolean) {
        try { this.enabled = enabled } catch (e: Throwable) { }
    }
    
    /**
     * Check if enabled
     */
    @JvmStatic
    fun isEnabled(): Boolean = enabled
    
    /**
     * Enable/disable logcat output
     */
    @JvmStatic
    fun setLogToLogcat(log: Boolean) {
        try { this.logToLogcat = log } catch (e: Throwable) { }
    }
    
    /**
     * Launch Analytics Inspector UI
     */
    @JvmStatic
    fun launch(context: Context) {
        try {
            val intent = Intent(context, AnalyticsListActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            Log.e(TAG, "Error launching", e)
        }
    }
    
    /**
     * Get launch intent for Analytics Inspector
     */
    @JvmStatic
    fun getLaunchIntent(context: Context): Intent {
        return Intent(context, AnalyticsListActivity::class.java)
    }
    
    /**
     * Add a listener for event updates
     */
    @JvmStatic
    fun addListener(listener: EventListener) {
        try { listeners.add(listener) } catch (e: Throwable) { }
    }
    
    /**
     * Remove a listener
     */
    @JvmStatic
    fun removeListener(listener: EventListener) {
        try { listeners.remove(listener) } catch (e: Throwable) { }
    }
    
    private fun addEvent(event: AnalyticsEvent) {
        try {
            events.add(0, event)
            while (events.size > MAX_EVENTS) {
                events.removeAt(events.size - 1)
            }
        } catch (e: Throwable) { }
    }
    
    private fun bundleToMap(bundle: Bundle?): Map<String, String> {
        if (bundle == null) return emptyMap()
        return try {
            val map = mutableMapOf<String, String>()
            for (key in bundle.keySet()) {
                try { map[key] = bundle.get(key)?.toString() ?: "null" } catch (e: Throwable) { }
            }
            map
        } catch (e: Throwable) { emptyMap() }
    }
    
    private fun notifyListeners() {
        try {
            val currentEvents = getEvents()
            listeners.forEach {
                try { it.onEventsUpdated(currentEvents) } catch (e: Throwable) { }
            }
        } catch (e: Throwable) { }
    }
    
    interface EventListener {
        fun onEventsUpdated(events: List<AnalyticsEvent>)
    }
}

