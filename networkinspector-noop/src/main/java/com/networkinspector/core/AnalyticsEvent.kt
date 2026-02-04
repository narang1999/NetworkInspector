package com.networkinspector.core

/**
 * Represents a captured analytics event (no-op version)
 */
data class AnalyticsEvent(
    val id: String = "",
    val eventName: String = "",
    val params: Map<String, String> = emptyMap(),
    val source: AnalyticsSource = AnalyticsSource.FIREBASE,
    val timestamp: Long = 0
) {
    val formattedTime: String = ""
    val paramsCount: Int = 0
    
    fun matchesSearch(query: String): Boolean = false
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

