package com.networkinspector.interceptor

/**
 * No-op implementation of CallbackInterceptor for release builds.
 */
class CallbackInterceptor<T> private constructor() {
    
    @JvmOverloads
    fun onSuccess(responseCode: Int = 200, response: T? = null, headers: Map<String, String>? = null) {
        // No-op
    }
    
    @JvmOverloads
    fun onFailure(responseCode: Int = 0, error: Any? = null) {
        // No-op
    }
    
    fun onCancelled() {
        // No-op
    }
    
    fun getRequestId(): String = ""
    
    companion object {
        
        @JvmStatic
        @JvmOverloads
        fun <T> create(
            url: String,
            method: String = "GET",
            params: Map<String, String>? = null,
            headers: Map<String, String>? = null,
            body: Any? = null,
            tag: String? = null
        ): CallbackInterceptor<T> = CallbackInterceptor()
        
        @JvmStatic
        fun <T> builder(): Builder<T> = Builder()
    }
    
    class Builder<T> {
        fun url(url: String) = this
        fun baseUrl(baseUrl: String) = this
        fun method(method: String) = this
        fun get() = this
        fun post() = this
        fun put() = this
        fun delete() = this
        fun patch() = this
        fun params(params: Map<String, String>?) = this
        fun headers(headers: Map<String, String>?) = this
        fun addHeader(key: String, value: String) = this
        fun body(body: Any?) = this
        fun tag(tag: String) = this
        
        fun build(): CallbackInterceptor<T> = CallbackInterceptor()
    }
}

inline fun <T> trackRequest(
    url: String,
    method: String = "GET",
    params: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    body: Any? = null,
    crossinline onSuccess: (Int, T?) -> Unit,
    crossinline onFailure: (Int, Any?) -> Unit
): Pair<(Int, T?, Map<String, String>?) -> Unit, (Int, Any?) -> Unit> {
    val successCallback: (Int, T?, Map<String, String>?) -> Unit = { code, response, _ ->
        onSuccess(code, response)
    }
    val failureCallback: (Int, Any?) -> Unit = { code, error ->
        onFailure(code, error)
    }
    return Pair(successCallback, failureCallback)
}

