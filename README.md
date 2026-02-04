# NetworkInspector 🔍

A lightweight, easy-to-integrate network request inspector for Android. Similar to [Chucker](https://github.com/ChuckerTeam/chucker) but simpler and **HTTP client agnostic** - works with any networking library (Retrofit, Volley, OkHttp, or custom implementations).

![Version](https://img.shields.io/badge/version-1.4.0-blue)
![Min SDK](https://img.shields.io/badge/minSdk-21-green)
![License](https://img.shields.io/badge/license-MIT-orange)

## Features ✨

- 📱 **Beautiful dark-themed UI** for viewing network requests
- 📊 **Analytics Inspector** for tracking Firebase, CleverTap, AppsFlyer events
- 🔔 **Notification** with live request statistics
- 🔍 **Search and filter** requests by status, URL, or method
- 📋 **Copy as cURL** for easy debugging
- 📤 **Share** request details
- 🛡️ **Crash-safe** - all methods wrapped in try-catch, never crashes your app
- 🚫 **No-op release artifact** for zero overhead in production
- 🔌 **HTTP client agnostic** - works with any networking library
- ☕ **Java & Kotlin** compatible

## Screenshots

| Request List | Request Detail | Analytics Events |
|:---:|:---:|:---:|
| ![List](screenshots/list.png) | ![Detail](screenshots/detail.png) | ![Analytics](screenshots/analytics.png) |

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
    debugImplementation 'com.github.narang1999:NetworkInspector:networkinspector:1.4.0'
    
    // No-op implementation for release (zero overhead)
    releaseImplementation 'com.github.narang1999:NetworkInspector:networkinspector-noop:1.4.0'
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

#### Option A: Using OkHttp Interceptor (Recommended)

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(NetworkInspectorInterceptor())
    .build()
```

#### Option B: Manual Tracking

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

#### Option C: Using CallbackInterceptor (for callback-based APIs)

```kotlin
val interceptor = CallbackInterceptor.create<MyResponse>(
    url = fullUrl,
    method = "POST",
    headers = headers,
    body = requestBody
)

apiClient.setOnFinishListener { client, code, response ->
    interceptor.onSuccess(code, response)
}
```

### 3. Track Analytics Events (New in 1.4.0)

```kotlin
// Log Firebase Analytics events
AnalyticsInspector.logEvent(
    eventName = "screen_view",
    params = bundleOf("screen_name" to "Home"),
    source = AnalyticsSource.FIREBASE
)

// Log CleverTap events
AnalyticsInspector.logEvent(
    eventName = "product_viewed",
    params = mapOf("product_id" to "123"),
    source = AnalyticsSource.CLEVERTAP
)

// Log AppsFlyer events
AnalyticsInspector.logEvent(
    eventName = "purchase",
    params = mapOf("revenue" to "99.99"),
    source = AnalyticsSource.APPSFLYER
)
```

### 4. Open the Inspector UI

```kotlin
// Launch Network Inspector
NetworkInspector.launch(context)

// Launch Analytics Inspector directly
AnalyticsInspector.launch(context)

// Or use the "Events" button in Network Inspector toolbar
```

### 5. Listen for Updates

```kotlin
// Network updates
NetworkInspector.addListener(object : NetworkInspector.RequestListener {
    override fun onRequestsUpdated(requests: List<NetworkRequest>, stats: RequestStats) {
        // Update your UI, log stats, etc.
    }
})

// Analytics updates
AnalyticsInspector.addListener(object : AnalyticsInspector.EventListener {
    override fun onEventsUpdated(events: List<AnalyticsEvent>) {
        // Handle analytics events
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
| `launchAnalytics(context)` | Open analytics inspector UI |
| `addListener(listener)` | Add update listener |
| `removeListener(listener)` | Remove update listener |

### AnalyticsInspector (New in 1.4.0)

| Method | Description |
|--------|-------------|
| `logEvent(name, params, source)` | Log an analytics event |
| `getEvents()` | Get all recorded events |
| `getEvents(source)` | Get events filtered by source |
| `searchEvents(query)` | Search events by name or params |
| `getEvent(id)` | Get specific event by ID |
| `getEventCount()` | Get total event count |
| `clearAll()` | Clear all recorded events |
| `setEnabled(enabled)` | Enable/disable logging |
| `setLogToLogcat(log)` | Enable/disable logcat output |
| `launch(context)` | Open analytics inspector UI |
| `addListener(listener)` | Add update listener |
| `removeListener(listener)` | Remove update listener |

### Analytics Sources

| Source | Description |
|--------|-------------|
| `FIREBASE` | Firebase Analytics |
| `CLEVERTAP` | CleverTap |
| `APPSFLYER` | AppsFlyer |
| `FACEBOOK` | Facebook Analytics |
| `CUSTOM` | Custom analytics |

## Crash Safety

All public methods in NetworkInspector and AnalyticsInspector are wrapped in try-catch blocks. This ensures:

- ✅ Your app will **never crash** due to inspector failures
- ✅ Heavy processing runs on background threads to prevent ANRs
- ✅ Safe to use in any environment

## What's New in 1.4.0

- 📊 **Analytics Inspector** - Track Firebase, CleverTap, AppsFlyer, Facebook analytics events
- 🔍 **Filter by source** - Filter analytics events by source (Firebase, CleverTap, etc.)
- 🛡️ **Crash-safe** - All methods wrapped in try-catch, background processing for heavy operations
- 🎯 **Events button** - Quick access to Analytics Inspector from Network Inspector toolbar

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
