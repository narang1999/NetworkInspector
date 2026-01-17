package com.networkinspector

import android.content.Context
import com.networkinspector.core.NetworkInspectorConfig
import com.networkinspector.core.NetworkRequest
import com.networkinspector.core.RequestStats

/**
 * No-op implementation of NetworkInspectorWrapper for release builds.
 * All methods are empty stubs that pass through without any overhead.
 */
object NetworkInspectorWrapper {
    
    @JvmStatic
    fun init(context: Context, config: NetworkInspectorConfig = NetworkInspectorConfig.DEBUG) {
        // No-op
    }
    
    @JvmStatic
    fun isEnabled(): Boolean = false
    
    @JvmStatic
    fun request(): RequestBuilder = RequestBuilder()
    
    @JvmStatic
    inline fun <T> track(
        url: String,
        method: String = "GET",
        headers: Map<String, String>? = null,
        body: Any? = null,
        block: () -> T
    ): T = block()
    
    @JvmStatic
    inline fun <T> trackWithCode(
        url: String,
        method: String = "GET",
        headers: Map<String, String>? = null,
        body: Any? = null,
        block: () -> Pair<Int, T>
    ): T = block().second
    
    suspend inline fun <T> trackSuspend(
        url: String,
        method: String = "GET",
        headers: Map<String, String>? = null,
        body: Any? = null,
        crossinline block: suspend () -> T
    ): T = block()
    
    suspend inline fun <T> trackSuspendWithCode(
        url: String,
        method: String = "GET",
        headers: Map<String, String>? = null,
        body: Any? = null,
        crossinline block: suspend () -> Pair<Int, T>
    ): T = block().second
    
    @JvmStatic
    fun getRequests(): List<NetworkRequest> = emptyList()
    
    @JvmStatic
    fun getStats(): RequestStats = RequestStats(0, 0, 0, 0)
    
    @JvmStatic
    fun search(query: String): List<NetworkRequest> = emptyList()
    
    @JvmStatic
    fun clear() {
        // No-op
    }
    
    @JvmStatic
    fun launch(context: Context) {
        // No-op
    }
    
    @JvmStatic
    fun addListener(listener: NetworkInspector.RequestListener) {
        // No-op
    }
    
    @JvmStatic
    fun removeListener(listener: NetworkInspector.RequestListener) {
        // No-op
    }
    
    class RequestBuilder {
        fun url(url: String) = this
        fun method(method: String) = this
        fun get() = this
        fun post() = this
        fun put() = this
        fun delete() = this
        fun patch() = this
        fun params(params: Map<String, String>) = this
        fun headers(headers: Map<String, String>) = this
        fun header(key: String, value: String) = this
        fun body(body: Any?) = this
        fun tag(tag: String) = this
        
        fun start(): RequestTracker = RequestTracker()
        
        inline fun <T> execute(block: () -> T): T = block()
        
        suspend inline fun <T> executeSuspend(crossinline block: suspend () -> T): T = block()
    }
    
    class RequestTracker {
        @JvmOverloads
        fun success(responseCode: Int = 200, response: Any? = null, headers: Map<String, String>? = null) {
            // No-op
        }
        
        @JvmOverloads
        fun failed(responseCode: Int = 0, error: Any? = null) {
            // No-op
        }
        
        fun failed(exception: Throwable) {
            // No-op
        }
        
        fun cancelled() {
            // No-op
        }
    }
}

