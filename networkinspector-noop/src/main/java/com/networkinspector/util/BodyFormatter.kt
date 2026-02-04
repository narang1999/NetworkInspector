package com.networkinspector.util

/**
 * No-op implementation of BodyFormatter for release builds.
 */
object BodyFormatter {
    
    @JvmStatic
    @JvmOverloads
    fun format(body: Any?, maxSize: Int = 100_000): String? = null
    
    @JvmStatic
    fun isBinaryContent(content: String): Boolean = false
    
    @JvmStatic
    fun getContentTypeSummary(contentType: String?): String = "Unknown"
}




