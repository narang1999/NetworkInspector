package com.networkinspector.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.networkinspector.NetworkInspector
import com.networkinspector.interceptor.CallbackInterceptor
import com.networkinspector.interceptor.NetworkInspectorInterceptor
import com.networkinspector.sample.databinding.ActivityMainBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    // Option 1: Using NetworkInspectorInterceptor (AUTOMATIC - Recommended for Retrofit)
    // All requests through this client are automatically tracked!
    private val clientWithInterceptor = OkHttpClient.Builder()
        .addInterceptor(NetworkInspectorInterceptor())
        .build()
    
    // Option 2: Plain client for manual tracking with CallbackInterceptor
    private val client = OkHttpClient()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupButtons()
    }
    
    private fun setupButtons() {
        binding.btnGetRequest.setOnClickListener {
            makeGetRequest()
        }
        
        binding.btnPostRequest.setOnClickListener {
            makePostRequest()
        }
        
        binding.btnFailedRequest.setOnClickListener {
            makeFailedRequest()
        }
        
        binding.btnMultipleRequests.setOnClickListener {
            repeat(5) { makeGetRequest() }
        }
        
        binding.btnOpenInspector.setOnClickListener {
            NetworkInspector.launch(this)
        }
        
        binding.btnClearAll.setOnClickListener {
            NetworkInspector.clearAll()
        }
    }
    
    private fun makeGetRequest() {
        val url = "https://jsonplaceholder.typicode.com/posts/1"
        
        // AUTOMATIC TRACKING with NetworkInspectorInterceptor
        // No manual tracking code needed - interceptor handles everything!
        val request = Request.Builder()
            .url(url)
            .build()
        
        clientWithInterceptor.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Error already tracked by interceptor
            }
            
            override fun onResponse(call: Call, response: Response) {
                // Success already tracked by interceptor
                response.body?.string() // Consume body
            }
        })
    }
    
    private fun makePostRequest() {
        val url = "https://jsonplaceholder.typicode.com/posts"
        val json = """{"title": "Test Post", "body": "This is a test", "userId": 1}"""
        
        // AUTOMATIC TRACKING - Interceptor captures headers, body, response automatically!
        val request = Request.Builder()
            .url(url)
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        
        clientWithInterceptor.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Error already tracked by interceptor
            }
            
            override fun onResponse(call: Call, response: Response) {
                // Success already tracked by interceptor
                response.body?.string()
            }
        })
    }
    
    private fun makeFailedRequest() {
        val url = "https://httpstat.us/500"
        
        // AUTOMATIC TRACKING - Even failures are captured with full details!
        val request = Request.Builder()
            .url(url)
            .build()
        
        clientWithInterceptor.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Error already tracked by interceptor
            }
            
            override fun onResponse(call: Call, response: Response) {
                // 500 error already tracked as failure by interceptor
                response.body?.string()
            }
        })
    }
    
    // ============================================================
    // ALTERNATIVE: Manual tracking with CallbackInterceptor
    // Use this when you can't use OkHttp interceptor (e.g., Volley, custom HTTP)
    // ============================================================
    
    private fun makeRequestWithManualTracking() {
        val url = "https://jsonplaceholder.typicode.com/users/1"
        
        // Manual tracking with CallbackInterceptor
        val interceptor = CallbackInterceptor.create<String>(
            url = url,
            method = "GET"
        )
        
        val request = Request.Builder()
            .url(url)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                interceptor.onFailure(0, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful) {
                    interceptor.onSuccess(response.code, body)
                } else {
                    interceptor.onFailure(response.code, body)
                }
            }
        })
    }
}




