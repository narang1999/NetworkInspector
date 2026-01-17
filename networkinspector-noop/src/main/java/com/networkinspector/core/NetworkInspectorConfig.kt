package com.networkinspector.core

/**
 * No-op configuration (minimal implementation)
 */
data class NetworkInspectorConfig(
    val enabled: Boolean = false,
    val showNotification: Boolean = false,
    val maxRequests: Int = 0,
    val maxBodySize: Int = 0,
    val logToLogcat: Boolean = false,
    val notificationChannelName: String = "",
    val excludedHosts: List<String> = emptyList(),
    val excludedPaths: List<String> = emptyList()
) {
    companion object {
        val DEBUG = NetworkInspectorConfig()
        val RELEASE = NetworkInspectorConfig()
    }
}


