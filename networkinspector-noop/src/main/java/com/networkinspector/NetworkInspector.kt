package com.networkinspector

import android.content.Context
import android.content.Intent
import com.networkinspector.core.NetworkInspectorConfig
import com.networkinspector.core.NetworkRequest
import com.networkinspector.core.RequestStats

/**
 * No-op implementation of NetworkInspector for release builds.
 * All methods are empty stubs that do nothing.
 */
object NetworkInspector {
    
    @JvmStatic
    fun init(context: Context) {
        // No-op
    }
    
    @JvmStatic
    fun init(context: Context, config: NetworkInspectorConfig) {
        // No-op
    }
    
    @JvmStatic
    fun isEnabled(): Boolean = false
    
    @JvmStatic
    @JvmOverloads
    fun onRequestStart(
        url: String,
        method: String = "GET",
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        body: Any? = null,
        tag: String? = null
    ): String = ""
    
    @JvmStatic
    @JvmOverloads
    fun onRequestSuccess(
        requestId: String,
        responseCode: Int = 200,
        response: Any? = null,
        headers: Map<String, String>? = null
    ) {
        // No-op
    }
    
    @JvmStatic
    @JvmOverloads
    fun onRequestFailed(
        requestId: String,
        responseCode: Int = 0,
        error: Any? = null
    ) {
        // No-op
    }
    
    @JvmStatic
    fun onRequestCancelled(requestId: String) {
        // No-op
    }
    
    @JvmStatic
    fun getRequests(): List<NetworkRequest> = emptyList()
    
    @JvmStatic
    fun getRequest(id: String): NetworkRequest? = null
    
    @JvmStatic
    fun searchRequests(query: String): List<NetworkRequest> = emptyList()
    
    @JvmStatic
    fun getStats(): RequestStats = RequestStats(0, 0, 0, 0)
    
    @JvmStatic
    fun clearAll() {
        // No-op
    }
    
    @JvmStatic
    fun launch(context: Context) {
        // No-op
    }
    
    @JvmStatic
    fun getLaunchIntent(context: Context): Intent = Intent()
    
    @JvmStatic
    fun addListener(listener: RequestListener) {
        // No-op
    }
    
    @JvmStatic
    fun removeListener(listener: RequestListener) {
        // No-op
    }
    
    interface RequestListener {
        fun onRequestsUpdated(requests: List<NetworkRequest>, stats: RequestStats)
    }
}

