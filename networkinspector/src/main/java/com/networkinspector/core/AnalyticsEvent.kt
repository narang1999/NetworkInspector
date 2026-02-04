package com.networkinspector.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a captured analytics event
 */
data class AnalyticsEvent(
    val id: String,
    val eventName: String,
    val params: Map<String, String>,
    val source: AnalyticsSource,
    val timestamp: Long = System.currentTimeMillis()
) {
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    val formattedTime: String
        get() = timeFormat.format(Date(timestamp))
    
    val paramsCount: Int
        get() = params.size
    
    fun matchesSearch(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return eventName.lowercase().contains(lowerQuery) ||
                params.any { (key, value) ->
                    key.lowercase().contains(lowerQuery) ||
                            value.lowercase().contains(lowerQuery)
                }
    }
}

/**
 * Analytics event source
 */
enum class AnalyticsSource {
    FIREBASE,
    CLEVERTAP,
    APPSFLYER,
    FACEBOOK,
    CUSTOM
}

