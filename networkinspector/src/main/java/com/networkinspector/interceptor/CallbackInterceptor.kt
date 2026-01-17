package com.networkinspector.interceptor

import com.networkinspector.NetworkInspector
import com.networkinspector.util.BodyFormatter

/**
 * A callback interceptor that wraps your existing callbacks to automatically
 * track network requests with NetworkInspector.
 * 
 * ## Usage with callback-based APIs:
 * 
 * ### Java:
 * ```java
 * // Create interceptor for your request
 * CallbackInterceptor<MyResponse> interceptor = CallbackInterceptor.create(
 *     fullUrl,
 *     httpMethod,
 *     params,
 *     headers,
 *     requestBody
 * );
 * 
 * // Use in your OnFinishListener
 * apiClient.setOnFinishListener(new OnFinishListener<MyResponse>() {
 *     @Override
 *     public void onSuccess(ApiClient<MyResponse> client, int code, MyResponse response) {
 *         interceptor.onSuccess(code, response, client.getHeaders());
 *         // Your existing success handling
 *     }
 *     
 *     @Override
 *     public void onFailure(ApiClient<MyResponse> client, int code, Object error) {
 *         interceptor.onFailure(code, error);
 *         // Your existing failure handling
 *     }
 * });
 * ```
 * 
 * ### Kotlin:
 * ```kotlin
 * val interceptor = CallbackInterceptor.create<MyResponse>(
 *     url = fullUrl,
 *     method = httpMethod,
 *     params = params,
 *     headers = headers,
 *     body = requestBody
 * )
 * 
 * apiClient.setOnFinishListener { client, code, response ->
 *     interceptor.onSuccess(code, response)
 *     // Your handling
 * }
 * ```
 */
class CallbackInterceptor<T> private constructor(
    private val requestId: String
) {
    
    /**
     * Call this when the request succeeds
     * 
     * @param responseCode HTTP response code
     * @param response The response object
     * @param headers Optional response headers
     */
    @JvmOverloads
    fun onSuccess(
        responseCode: Int = 200,
        response: T? = null,
        headers: Map<String, String>? = null
    ) {
        NetworkInspector.onRequestSuccess(requestId, responseCode, response, headers)
    }
    
    /**
     * Call this when the request fails
     * 
     * @param responseCode HTTP response code (0 if connection failed)
     * @param error The error object
     */
    @JvmOverloads
    fun onFailure(
        responseCode: Int = 0,
        error: Any? = null
    ) {
        NetworkInspector.onRequestFailed(requestId, responseCode, error)
    }
    
    /**
     * Call this when the request is cancelled
     */
    fun onCancelled() {
        NetworkInspector.onRequestCancelled(requestId)
    }
    
    /**
     * Get the request ID for manual tracking
     */
    fun getRequestId(): String = requestId
    
    companion object {
        
        /**
         * Create a new callback interceptor and start tracking.
         * 
         * @param url Full URL of the request
         * @param method HTTP method (GET, POST, etc.)
         * @param params Query parameters (optional)
         * @param headers Request headers (optional)
         * @param body Request body - will be auto-formatted (optional)
         * @param tag Custom tag for categorization (optional)
         * @return CallbackInterceptor instance to use in your callbacks
         */
        @JvmStatic
        @JvmOverloads
        fun <T> create(
            url: String,
            method: String = "GET",
            params: Map<String, String>? = null,
            headers: Map<String, String>? = null,
            body: Any? = null,
            tag: String? = null
        ): CallbackInterceptor<T> {
            // Format the body for better readability
            val formattedBody = BodyFormatter.format(body)
            
            val requestId = NetworkInspector.onRequestStart(
                url = url,
                method = method,
                params = params,
                headers = headers,
                body = formattedBody,
                tag = tag
            )
            
            return CallbackInterceptor(requestId)
        }
        
        /**
         * Create interceptor from a builder for more options
         */
        @JvmStatic
        fun <T> builder(): Builder<T> = Builder()
    }
    
    /**
     * Builder for creating CallbackInterceptor with fluent API
     */
    class Builder<T> {
        private var url: String = ""
        private var method: String = "GET"
        private var params: Map<String, String>? = null
        private var headers: Map<String, String>? = null
        private var body: Any? = null
        private var tag: String? = null
        private var baseUrl: String? = null
        
        fun url(url: String) = apply { this.url = url }
        fun baseUrl(baseUrl: String) = apply { this.baseUrl = baseUrl }
        fun method(method: String) = apply { this.method = method }
        fun get() = apply { this.method = "GET" }
        fun post() = apply { this.method = "POST" }
        fun put() = apply { this.method = "PUT" }
        fun delete() = apply { this.method = "DELETE" }
        fun patch() = apply { this.method = "PATCH" }
        fun params(params: Map<String, String>?) = apply { this.params = params }
        fun headers(headers: Map<String, String>?) = apply { this.headers = headers }
        fun addHeader(key: String, value: String) = apply {
            this.headers = (this.headers ?: mutableMapOf()) + (key to value)
        }
        fun body(body: Any?) = apply { this.body = body }
        fun tag(tag: String) = apply { this.tag = tag }
        
        /**
         * Build the interceptor and start tracking
         */
        fun build(): CallbackInterceptor<T> {
            val fullUrl = when {
                url.startsWith("http") -> url
                baseUrl != null -> baseUrl + url
                else -> url
            }
            
            return create(
                url = fullUrl,
                method = method,
                params = params,
                headers = headers,
                body = body,
                tag = tag
            )
        }
    }
}

/**
 * Extension function to wrap any callback with NetworkInspector tracking
 */
inline fun <T> trackRequest(
    url: String,
    method: String = "GET",
    params: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    body: Any? = null,
    onSuccess: (Int, T?) -> Unit,
    onFailure: (Int, Any?) -> Unit
): Pair<(Int, T?, Map<String, String>?) -> Unit, (Int, Any?) -> Unit> {
    val interceptor = CallbackInterceptor.create<T>(url, method, params, headers, body)
    
    val successCallback: (Int, T?, Map<String, String>?) -> Unit = { code, response, respHeaders ->
        interceptor.onSuccess(code, response, respHeaders)
        onSuccess(code, response)
    }
    
    val failureCallback: (Int, Any?) -> Unit = { code, error ->
        interceptor.onFailure(code, error)
        onFailure(code, error)
    }
    
    return Pair(successCallback, failureCallback)
}

