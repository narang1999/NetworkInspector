package com.networkinspector.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * No-op implementation of NetworkInspectorInterceptor for release builds.
 * Simply passes through requests without any tracking overhead.
 */
class NetworkInspectorInterceptor @JvmOverloads constructor(
    private val maxContentLength: Long = 250_000L,
    private val headersToRedact: Set<String> = emptySet()
) : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // No-op - just pass through
        return chain.proceed(chain.request())
    }
}



