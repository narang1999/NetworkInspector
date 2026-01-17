package com.networkinspector.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.networkinspector.NetworkInspector
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
        
        val requestId = NetworkInspector.onRequestStart(
            url = url,
            method = "GET"
        )
        
        val request = Request.Builder()
            .url(url)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetworkInspector.onRequestFailed(requestId, 0, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                NetworkInspector.onRequestSuccess(
                    requestId = requestId,
                    responseCode = response.code,
                    response = body
                )
            }
        })
    }
    
    private fun makePostRequest() {
        val url = "https://jsonplaceholder.typicode.com/posts"
        val json = """{"title": "Test Post", "body": "This is a test", "userId": 1}"""
        
        val requestId = NetworkInspector.onRequestStart(
            url = url,
            method = "POST",
            headers = mapOf("Content-Type" to "application/json"),
            body = json
        )
        
        val request = Request.Builder()
            .url(url)
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetworkInspector.onRequestFailed(requestId, 0, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                NetworkInspector.onRequestSuccess(
                    requestId = requestId,
                    responseCode = response.code,
                    response = body
                )
            }
        })
    }
    
    private fun makeFailedRequest() {
        val url = "https://httpstat.us/500"
        
        val requestId = NetworkInspector.onRequestStart(
            url = url,
            method = "GET"
        )
        
        val request = Request.Builder()
            .url(url)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                NetworkInspector.onRequestFailed(requestId, 0, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    NetworkInspector.onRequestSuccess(requestId, response.code, response.body?.string())
                } else {
                    NetworkInspector.onRequestFailed(requestId, response.code, "HTTP ${response.code}")
                }
            }
        })
    }
}



