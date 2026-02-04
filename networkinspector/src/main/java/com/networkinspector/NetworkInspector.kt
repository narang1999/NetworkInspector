package com.networkinspector

import android.content.Context
import android.content.Intent
import android.util.Log
import com.networkinspector.core.NetworkInspectorConfig
import com.networkinspector.core.NetworkRequest
import com.networkinspector.core.RequestStats
import com.networkinspector.core.RequestStatus
import com.networkinspector.notification.InspectorNotificationManager
import com.networkinspector.ui.AnalyticsListActivity
import com.networkinspector.ui.RequestListActivity
import com.networkinspector.util.BodyFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * NetworkInspector - A lightweight network request inspector for Android.
 * 
 * All methods are crash-safe and will never throw exceptions to the caller.
 * Heavy operations are processed on a background thread to prevent ANRs.
 */
object NetworkInspector {
    
    private const val TAG = "NetworkInspector"
    
    private var appContext: Context? = null
    private var config: NetworkInspectorConfig = NetworkInspectorConfig.RELEASE
    private var notificationManager: InspectorNotificationManager? = null
    
    // Request storage
    private val requests = CopyOnWriteArrayList<NetworkRequest>()
    private val activeRequests = ConcurrentHashMap<String, NetworkRequest>()
    private val requestIdGenerator = AtomicLong(0)
    
    // Stats
    @Volatile private var totalRequests = 0
    @Volatile private var successfulRequests = 0
    @Volatile private var failedRequests = 0
    @Volatile private var activeRequestCount = 0
    
    // Listeners
    private val listeners = CopyOnWriteArrayList<RequestListener>()
    
    // Background executor for heavy processing
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "NetworkInspector-Worker").apply { isDaemon = true }
    }
    
    // ==================== Initialization ====================
    
    @JvmStatic
    fun init(context: Context) {
        try {
            init(context, NetworkInspectorConfig.DEBUG)
        } catch (e: Throwable) {
            Log.e(TAG, "Error in init", e)
        }
    }
    
    @JvmStatic
    fun init(context: Context, config: NetworkInspectorConfig) {
        try {
            appContext = context.applicationContext
            this.config = config
            
            if (config.enabled && config.showNotification) {
                notificationManager = InspectorNotificationManager(context.applicationContext, config)
            }
            
            if (config.logToLogcat) {
                Log.d(TAG, "NetworkInspector initialized (enabled: ${config.enabled})")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in init", e)
        }
    }
    
    @JvmStatic
    fun isEnabled(): Boolean {
        return try { config.enabled } catch (e: Throwable) { false }
    }
    
    // ==================== Request Tracking ====================
    
    @JvmStatic
    @JvmOverloads
    fun onRequestStart(
        url: String,
        method: String = "GET",
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        body: Any? = null,
        tag: String? = null
    ): String {
        return try {
            if (!config.enabled) return ""
            if (!config.shouldTrack(url)) return ""
            
            val requestId = "req_${requestIdGenerator.incrementAndGet()}_${System.currentTimeMillis()}"
            
            executor.execute {
                try {
                    val bodyString = BodyFormatter.format(body, config.maxBodySize)
                    
                    val request = NetworkRequest(
                        id = requestId,
                        url = url,
                        method = method.uppercase(),
                        params = params,
                        headers = headers,
                        requestBody = bodyString,
                        startTime = System.currentTimeMillis(),
                        status = RequestStatus.IN_PROGRESS,
                        tag = tag
                    )
                    
                    activeRequests[requestId] = request
                    activeRequestCount++
                    totalRequests++
                    
                    if (config.logToLogcat) {
                        Log.d(TAG, "📤 [${request.method}] ${request.shortName}")
                    }
                    
                    notificationManager?.updateNotification(getStats())
                    notifyListeners()
                } catch (e: Throwable) {
                    Log.e(TAG, "Error in onRequestStart background", e)
                }
            }
            
            requestId
        } catch (e: Throwable) {
            Log.e(TAG, "Error in onRequestStart", e)
            ""
        }
    }
    
    @JvmStatic
    @JvmOverloads
    fun onRequestSuccess(
        requestId: String,
        responseCode: Int = 200,
        response: Any? = null,
        headers: Map<String, String>? = null
    ) {
        try {
            if (!config.enabled || requestId.isEmpty()) return
            
            val request = activeRequests.remove(requestId) ?: return
            activeRequestCount--
            successfulRequests++
            
            val endTime = System.currentTimeMillis()
            
            executor.execute {
                try {
                    val responseBody = BodyFormatter.format(response, config.maxBodySize)
                    
                    val completedRequest = request.copy(
                        status = RequestStatus.SUCCESS,
                        responseCode = responseCode,
                        responseBody = responseBody,
                        responseHeaders = headers,
                        endTime = endTime,
                        duration = endTime - request.startTime
                    )
                    
                    addRequest(completedRequest)
                    
                    if (config.logToLogcat) {
                        Log.d(TAG, "✅ [${completedRequest.method}] ${completedRequest.shortName} " +
                                "| $responseCode | ${completedRequest.formattedDuration}")
                    }
                    
                    notificationManager?.updateNotification(getStats())
                    notifyListeners()
                } catch (e: Throwable) {
                    Log.e(TAG, "Error in onRequestSuccess background", e)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in onRequestSuccess", e)
        }
    }
    
    @JvmStatic
    @JvmOverloads
    fun onRequestFailed(
        requestId: String,
        responseCode: Int = 0,
        error: Any? = null
    ) {
        try {
            if (!config.enabled || requestId.isEmpty()) return
            
            val request = activeRequests.remove(requestId) ?: return
            activeRequestCount--
            failedRequests++
            
            val endTime = System.currentTimeMillis()
            
            executor.execute {
                try {
                    val (errorMessage, stackTrace) = when (error) {
                        is Throwable -> Pair(
                            error.message ?: error::class.java.simpleName,
                            try { error.stackTraceToString() } catch (e: Throwable) { null }
                        )
                        else -> Pair(error?.toString() ?: "Unknown error", null)
                    }
                    
                    val completedRequest = request.copy(
                        status = RequestStatus.FAILED,
                        responseCode = if (responseCode != 0) responseCode else null,
                        errorMessage = errorMessage,
                        errorStackTrace = stackTrace,
                        endTime = endTime,
                        duration = endTime - request.startTime
                    )
                    
                    addRequest(completedRequest)
                    
                    if (config.logToLogcat) {
                        Log.e(TAG, "❌ [${completedRequest.method}] ${completedRequest.shortName} " +
                                "| ${completedRequest.formattedDuration} | $errorMessage")
                    }
                    
                    notificationManager?.updateNotification(getStats())
                    notifyListeners()
                } catch (e: Throwable) {
                    Log.e(TAG, "Error in onRequestFailed background", e)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in onRequestFailed", e)
        }
    }
    
    @JvmStatic
    fun onRequestCancelled(requestId: String) {
        try {
            if (!config.enabled || requestId.isEmpty()) return
            
            val request = activeRequests.remove(requestId) ?: return
            activeRequestCount--
            
            val endTime = System.currentTimeMillis()
            val completedRequest = request.copy(
                status = RequestStatus.CANCELLED,
                endTime = endTime,
                duration = endTime - request.startTime,
                errorMessage = "Request cancelled"
            )
            
            addRequest(completedRequest)
            
            if (config.logToLogcat) {
                Log.w(TAG, "⚠️ [${completedRequest.method}] ${completedRequest.shortName} | Cancelled")
            }
            
            notificationManager?.updateNotification(getStats())
            notifyListeners()
        } catch (e: Throwable) {
            Log.e(TAG, "Error in onRequestCancelled", e)
        }
    }
    
    // ==================== Query Methods ====================
    
    @JvmStatic
    fun getRequests(): List<NetworkRequest> {
        return try { requests.toList() } catch (e: Throwable) { emptyList() }
    }
    
    @JvmStatic
    fun getRequests(status: RequestStatus): List<NetworkRequest> {
        return try { requests.filter { it.status == status } } catch (e: Throwable) { emptyList() }
    }
    
    @JvmStatic
    fun getRequest(id: String): NetworkRequest? {
        return try { requests.find { it.id == id } ?: activeRequests[id] } catch (e: Throwable) { null }
    }
    
    @JvmStatic
    fun searchRequests(query: String): List<NetworkRequest> {
        return try {
            val lowerQuery = query.lowercase()
            requests.filter { request ->
                request.url.lowercase().contains(lowerQuery) ||
                request.method.lowercase().contains(lowerQuery) ||
                request.tag?.lowercase()?.contains(lowerQuery) == true
            }
        } catch (e: Throwable) { emptyList() }
    }
    
    @JvmStatic
    fun getStats(): RequestStats {
        return try {
            RequestStats(
                total = totalRequests,
                active = activeRequestCount,
                successful = successfulRequests,
                failed = failedRequests
            )
        } catch (e: Throwable) {
            RequestStats(0, 0, 0, 0)
        }
    }
    
    @JvmStatic
    fun clearAll() {
        try {
            requests.clear()
            totalRequests = 0
            successfulRequests = 0
            failedRequests = 0
            
            notificationManager?.dismiss()
            notifyListeners()
        } catch (e: Throwable) {
            Log.e(TAG, "Error in clearAll", e)
        }
    }
    
    // ==================== UI Methods ====================
    
    @JvmStatic
    fun launch(context: Context) {
        try {
            if (!config.enabled) return
            
            val intent = Intent(context, RequestListActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            Log.e(TAG, "Error launching", e)
        }
    }
    
    @JvmStatic
    fun getLaunchIntent(context: Context): Intent {
        return try {
            Intent(context, RequestListActivity::class.java)
        } catch (e: Throwable) {
            Intent()
        }
    }
    
    @JvmStatic
    fun launchAnalytics(context: Context) {
        try {
            val intent = Intent(context, AnalyticsListActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            Log.e(TAG, "Error launching analytics", e)
        }
    }
    
    // ==================== Listener Methods ====================
    
    @JvmStatic
    fun addListener(listener: RequestListener) {
        try { listeners.add(listener) } catch (e: Throwable) { }
    }
    
    @JvmStatic
    fun removeListener(listener: RequestListener) {
        try { listeners.remove(listener) } catch (e: Throwable) { }
    }
    
    // ==================== Internal Methods ====================
    
    private fun addRequest(request: NetworkRequest) {
        try {
            requests.add(0, request)
            
            while (requests.size > config.maxRequests) {
                requests.removeAt(requests.size - 1)
            }
        } catch (e: Throwable) { }
    }
    
    private fun notifyListeners() {
        try {
            val currentRequests = getRequests()
            val currentStats = getStats()
            listeners.forEach { 
                try {
                    it.onRequestsUpdated(currentRequests, currentStats)
                } catch (e: Throwable) { }
            }
        } catch (e: Throwable) { }
    }
    
    interface RequestListener {
        fun onRequestsUpdated(requests: List<NetworkRequest>, stats: RequestStats)
    }
}
