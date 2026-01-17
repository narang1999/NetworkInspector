package com.networkinspector.core

/**
 * No-op data class for NetworkRequest (minimal implementation)
 */
data class NetworkRequest(
    val id: String = "",
    val url: String = "",
    val method: String = "",
    val params: Map<String, String>? = null,
    val headers: Map<String, String>? = null,
    val requestBody: String? = null,
    val startTime: Long = 0,
    val endTime: Long? = null,
    val duration: Long? = null,
    val status: RequestStatus = RequestStatus.SUCCESS,
    val responseCode: Int? = null,
    val responseBody: String? = null,
    val responseHeaders: Map<String, String>? = null,
    val errorMessage: String? = null,
    val errorStackTrace: String? = null,
    val tag: String? = null
)

enum class RequestStatus {
    IN_PROGRESS,
    SUCCESS,
    FAILED,
    CANCELLED
}

data class RequestStats(
    val total: Int,
    val active: Int,
    val successful: Int,
    val failed: Int
)


