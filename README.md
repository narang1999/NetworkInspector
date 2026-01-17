# NetworkInspector 🔍

A lightweight, easy-to-integrate network request inspector for Android. Similar to [Chucker](https://github.com/ChuckerTeam/chucker) but simpler and **HTTP client agnostic** - works with any networking library (Retrofit, Volley, OkHttp, or custom implementations).

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Min SDK](https://img.shields.io/badge/minSdk-21-green)
![License](https://img.shields.io/badge/license-MIT-orange)

## Features ✨

- 📱 **Beautiful dark-themed UI** for viewing network requests
- 🔔 **Notification** with live request statistics
- 🔍 **Search and filter** requests by status, URL, or method
- 📋 **Copy as cURL** for easy debugging
- 📤 **Share** request details
- 🚫 **No-op release artifact** for zero overhead in production
- 🔌 **HTTP client agnostic** - works with any networking library
- ☕ **Java & Kotlin** compatible

## Screenshots

| Request List | Request Detail | Notification |
|:---:|:---:|:---:|
| ![List](screenshots/list.png) | ![Detail](screenshots/detail.png) | ![Notification](screenshots/notification.png) |

## Installation

### Gradle (JitPack)

Add JitPack repository to your root `build.gradle`:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependencies:

```groovy
dependencies {
    // Debug implementation for development
    debugImplementation 'com.github.YourUsername:NetworkInspector:1.0.0'
    
    // No-op implementation for release (zero overhead)
    releaseImplementation 'com.github.YourUsername:NetworkInspector-noop:1.0.0'
}
```

### Local Module

Copy the `networkinspector` and `networkinspector-noop` folders to your project and add to `settings.gradle`:

```groovy
include ':networkinspector'
include ':networkinspector-noop'
```

Then in your app's `build.gradle`:

```groovy
dependencies {
    debugImplementation project(':networkinspector')
    releaseImplementation project(':networkinspector-noop')
}
```

## Usage

### 1. Initialize in Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Simple initialization with defaults
        NetworkInspector.init(this)
        
        // Or with custom configuration
        NetworkInspector.init(this, NetworkInspectorConfig(
            enabled = BuildConfig.DEBUG,
            showNotification = true,
            maxRequests = 500,
            logToLogcat = true,
            excludedHosts = listOf("analytics\\..*", "crashlytics\\..*")
        ))
    }
}
```

### 2. Track Network Requests

#### In Your Network Layer (Recommended)

```kotlin
// When request starts
val requestId = NetworkInspector.onRequestStart(
    url = "https://api.example.com/users",
    method = "POST",
    headers = mapOf("Authorization" to "Bearer xxx"),
    body = requestBody
)

// When request succeeds
NetworkInspector.onRequestSuccess(
    requestId = requestId,
    responseCode = 200,
    response = responseBody,
    headers = responseHeaders
)

// When request fails
NetworkInspector.onRequestFailed(
    requestId = requestId,
    responseCode = 500,
    error = exception
)
```

#### With OkHttp Interceptor

```kotlin
class NetworkInspectorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        val requestId = NetworkInspector.onRequestStart(
            url = request.url.toString(),
            method = request.method,
            headers = request.headers.toMap(),
            body = request.body?.let { bodyToString(it) }
        )
        
        return try {
            val response = chain.proceed(request)
            
            NetworkInspector.onRequestSuccess(
                requestId = requestId,
                responseCode = response.code,
                response = response.peekBody(1024 * 1024).string(),
                headers = response.headers.toMap()
            )
            
            response
        } catch (e: Exception) {
            NetworkInspector.onRequestFailed(requestId, 0, e)
            throw e
        }
    }
}
```

#### With Retrofit/Volley

```java
// Java example in your base network class
public void executeRequest(Request request, Callback callback) {
    String requestId = NetworkInspector.onRequestStart(
        request.getUrl(),
        request.getMethod(),
        null, // params
        request.getHeaders(),
        request.getBody(),
        null  // tag
    );
    
    client.execute(request, new Callback() {
        @Override
        public void onSuccess(Response response) {
            NetworkInspector.onRequestSuccess(
                requestId,
                response.getStatusCode(),
                response.getBody(),
                null
            );
            callback.onSuccess(response);
        }
        
        @Override
        public void onError(Exception error) {
            NetworkInspector.onRequestFailed(requestId, 0, error);
            callback.onError(error);
        }
    });
}
```

### 3. Open the Inspector UI

```kotlin
// Launch from anywhere
NetworkInspector.launch(context)

// Or get the intent for custom handling
val intent = NetworkInspector.getLaunchIntent(context)
startActivity(intent)
```

### 4. Listen for Updates

```kotlin
NetworkInspector.addListener(object : NetworkInspector.RequestListener {
    override fun onRequestsUpdated(requests: List<NetworkRequest>, stats: RequestStats) {
        // Update your UI, log stats, etc.
    }
})
```

## Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | Boolean | `true` | Enable/disable the inspector |
| `showNotification` | Boolean | `true` | Show persistent notification |
| `maxRequests` | Int | `500` | Maximum requests to store |
| `maxBodySize` | Int | `100000` | Max body size in characters |
| `logToLogcat` | Boolean | `true` | Log requests to Logcat |
| `notificationChannelName` | String | `"Network Inspector"` | Notification channel name |
| `excludedHosts` | List<String> | `[]` | Regex patterns for hosts to exclude |
| `excludedPaths` | List<String> | `[]` | Regex patterns for paths to exclude |

## API Reference

### NetworkInspector

| Method | Description |
|--------|-------------|
| `init(context)` | Initialize with default config |
| `init(context, config)` | Initialize with custom config |
| `isEnabled()` | Check if inspector is enabled |
| `onRequestStart(...)` | Record start of request, returns ID |
| `onRequestSuccess(...)` | Record successful response |
| `onRequestFailed(...)` | Record failed request |
| `onRequestCancelled(id)` | Record cancelled request |
| `getRequests()` | Get all recorded requests |
| `getRequest(id)` | Get specific request by ID |
| `searchRequests(query)` | Search requests |
| `getStats()` | Get current statistics |
| `clearAll()` | Clear all recorded requests |
| `launch(context)` | Open inspector UI |
| `addListener(listener)` | Add update listener |
| `removeListener(listener)` | Remove update listener |

## License

```
MIT License

Copyright (c) 2024

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Credits

Inspired by [Chucker](https://github.com/ChuckerTeam/chucker) - an HTTP inspector for OkHttp.


